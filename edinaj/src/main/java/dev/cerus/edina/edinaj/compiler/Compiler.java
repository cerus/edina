package dev.cerus.edina.edinaj.compiler;

import dev.cerus.edina.ast.Parser;
import dev.cerus.edina.ast.ast.Command;
import dev.cerus.edina.ast.ast.Visitor;
import dev.cerus.edina.ast.token.Token;
import dev.cerus.edina.ast.token.TokenType;
import dev.cerus.edina.ast.token.Tokenizer;
import dev.cerus.edina.edinaj.compiler.exception.CompilerException;
import dev.cerus.edina.edinaj.compiler.step.CompilerStep;
import dev.cerus.edina.edinaj.compiler.step.command.branch.IfStep;
import dev.cerus.edina.edinaj.compiler.step.command.imports.ImportCallStep;
import dev.cerus.edina.edinaj.compiler.step.command.loop.LoopStep;
import dev.cerus.edina.edinaj.compiler.step.command.math.ArithmeticStep;
import dev.cerus.edina.edinaj.compiler.step.command.natives.NativeCallStep;
import dev.cerus.edina.edinaj.compiler.step.command.routine.RoutineCallStep;
import dev.cerus.edina.edinaj.compiler.step.command.routine.RoutineDeclareStep;
import dev.cerus.edina.edinaj.compiler.step.command.stack.DupStep;
import dev.cerus.edina.edinaj.compiler.step.command.stack.OverStep;
import dev.cerus.edina.edinaj.compiler.step.command.stack.PopStep;
import dev.cerus.edina.edinaj.compiler.step.command.stack.PushStep;
import dev.cerus.edina.edinaj.compiler.step.command.stack.RollLeftStep;
import dev.cerus.edina.edinaj.compiler.step.command.stack.RollRightStep;
import dev.cerus.edina.edinaj.compiler.step.command.stack.StackSizeStep;
import dev.cerus.edina.edinaj.compiler.step.command.stack.SwapStep;
import dev.cerus.edina.edinaj.compiler.step.init.ConstructorStep;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;

public class Compiler implements Visitor<Void> {

    private final Collection<String> routineNames = new HashSet<>();
    private final Map<String, byte[]> referencedClasses = new LinkedHashMap<>();
    private final Map<String, String> referenceNames = new LinkedHashMap<>();
    private final Map<String, String> referencePaths = new LinkedHashMap<>();
    private final Map<String, Compiler> referenceCompilers = new LinkedHashMap<>();
    private final Map<String, String> referenceFiles = new LinkedHashMap<>();
    private final CompilerSettings compilerSettings;
    private ClassWriter appClassWriter;
    private ClassWriter mainClassWriter;
    private ClassWriter stackClassWriter;
    private ClassWriter nativesClassWriter;
    private MethodVisitor mainMethodVisitor;
    private MethodVisitor currentMethodVisitor;
    private Label firstMainLabel;
    private Compiler parent;

    public Compiler(final CompilerSettings compilerSettings) {
        this.compilerSettings = compilerSettings;
        if (compilerSettings.isMain()) {
            this.initMain(compilerSettings);
        } else {
            this.initSub(compilerSettings);
        }
    }

    private void initMain(final CompilerSettings settings) {
        this.appClassWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        this.nativesClassWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        this.mainClassWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        this.stackClassWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);

        CompilerStep.compileAll(this.appClassWriter, settings, CompilerStep.appSteps());
        CompilerStep.compileAll(this.stackClassWriter, settings, CompilerStep.stackSteps());
        CompilerStep.compileAll(this.mainClassWriter, settings, CompilerStep.launcherSteps());
        CompilerStep.compileAll(this.nativesClassWriter, settings, CompilerStep.nativesSteps());

        this.initCommon();
    }

    private void initSub(final CompilerSettings settings) {
        this.mainClassWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);

        this.mainClassWriter.visit(
                V1_8,
                ACC_PUBLIC | ACC_SUPER,
                settings.getMainClassName(),
                null,
                "java/lang/Object",
                null
        );
        FieldVisitor fieldVisitor;
        {
            fieldVisitor = this.mainClassWriter.visitField(ACC_PRIVATE | ACC_FINAL, "stack", "L" + settings.getStackName() + ";", null, null);
            fieldVisitor.visitEnd();
        }
        {
            fieldVisitor = this.mainClassWriter.visitField(ACC_PRIVATE | ACC_FINAL, "natives", "L" + settings.getNativesName() + ";", null, null);
            fieldVisitor.visitEnd();
        }

        final String[] split = settings.getMainClassName().split("/");
        final String name = split[split.length - 1] + ".java";
        this.mainClassWriter.visitSource(name, null);

        this.initCommon();
    }

    private void initCommon() {
        this.mainMethodVisitor = (this.appClassWriter == null ? this.mainClassWriter : this.appClassWriter).visitMethod(
                ACC_PUBLIC,
                "run",
                "()V",
                null,
                null
        );
        this.mainMethodVisitor.visitCode();

        this.firstMainLabel = new Label();
        this.mainMethodVisitor.visitLabel(this.firstMainLabel);
        this.mainMethodVisitor.visitLineNumber(1, this.firstMainLabel);
        this.mainMethodVisitor.visitLdcInsn("Generated by EdinaJ");
        this.mainMethodVisitor.visitVarInsn(ASTORE, 1);
        this.currentMethodVisitor = this.mainMethodVisitor;
    }

    public Map<String, byte[]> finish() {
        if (this.compilerSettings.isMain()) {
            return this.finishMain();
        } else {
            return this.finishSub();
        }
    }

    private Map<String, byte[]> finishMain() {
        if (this.compilerSettings.isDebug()) {
            final Label label0 = new Label();
            this.mainMethodVisitor.visitLabel(label0);
            this.mainMethodVisitor.visitLineNumber(90, label0);
            this.mainMethodVisitor.visitVarInsn(ALOAD, 0);
            this.mainMethodVisitor.visitFieldInsn(GETFIELD, this.compilerSettings.getMainClassName(), "stack", "L" + this.compilerSettings.getStackName() + ";");
            this.mainMethodVisitor.visitMethodInsn(INVOKEVIRTUAL, this.compilerSettings.getStackName(), "debugPrint", "()V", false);
        }

        this.finishCommon();

        final Map<String, byte[]> classMap = new HashMap<>();
        classMap.put(this.compilerSettings.getMainClassName(), this.appClassWriter.toByteArray());
        classMap.put(this.compilerSettings.getAppLauncherName(), this.mainClassWriter.toByteArray());
        classMap.put(this.compilerSettings.getStackName(), this.stackClassWriter.toByteArray());
        classMap.put(this.compilerSettings.getNativesName(), this.nativesClassWriter.toByteArray());
        classMap.putAll(this.referencedClasses);
        return classMap;
    }

    private Map<String, byte[]> finishSub() {
        this.finishCommon();

        this.mainClassWriter.visitEnd();
        final Map<String, byte[]> classMap = new HashMap<>();
        classMap.put(this.compilerSettings.getMainClassName(), this.mainClassWriter.toByteArray());
        classMap.putAll(this.referencedClasses);
        return classMap;
    }

    private void finishCommon() {
        this.compile(new ConstructorStep(), null);

        final Label label1 = new Label();
        this.mainMethodVisitor.visitLabel(label1);
        this.mainMethodVisitor.visitLineNumber(100, label1);
        this.mainMethodVisitor.visitInsn(RETURN);

        final Label label2 = new Label();
        this.mainMethodVisitor.visitLabel(label2);
        this.mainMethodVisitor.visitLocalVariable("$credit$", "Ljava/lang/String;", null, this.firstMainLabel, label2, 1);
        this.mainMethodVisitor.visitMaxs(0, 0);
        this.mainMethodVisitor.visitEnd();
        if (this.appClassWriter != null) {
            this.appClassWriter.visitEnd();
        } else {
            this.mainClassWriter.visitEnd();
        }
    }

    public void addRoutineNames(final String... names) {
        for (final String name : names) {
            if (this.routineNames.contains(name)) {
                throw new IllegalArgumentException("Routine can not be declared more than once");
            }
            this.routineNames.add(name);
        }
    }

    public <T> void compile(final CompilerStep<StepData<T>> step, final T thing) {
        this.compile(step, thing, this.currentMethodVisitor);
    }

    public <T> void compile(final CompilerStep<StepData<T>> step, final T thing, final MethodVisitor mv) {
        step.compile(this.appClassWriter == null ? this.mainClassWriter : this.appClassWriter,
                this.compilerSettings,
                new StepData<>(this.appClassWriter == null ? this.mainClassWriter : this.appClassWriter, mv, this, this.compilerSettings, thing));
    }

    @Override
    public Void visitPush(final Command.PushCommand pushCommand) {
        this.compile(new PushStep(), pushCommand);
        return null;
    }

    @Override
    public Void visitPop(final Command.PopCommand popCommand) {
        this.compile(new PopStep(), popCommand);
        return null;
    }

    @Override
    public Void visitDup(final Command.DupCommand dupCommand) {
        this.compile(new DupStep(), dupCommand);
        return null;
    }

    @Override
    public Void visitSwap(final Command.SwapCommand swapCommand) {
        this.compile(new SwapStep(), swapCommand);
        return null;
    }

    @Override
    public Void visitOver(final Command.OverCommand overCommand) {
        this.compile(new OverStep(), overCommand);
        return null;
    }

    @Override
    public Void visitRollLeft(final Command.RollLeftCommand rollLeftCommand) {
        this.compile(new RollLeftStep(), rollLeftCommand);
        return null;
    }

    @Override
    public Void visitRollRight(final Command.RollRightCommand rollRightCommand) {
        this.compile(new RollRightStep(), rollRightCommand);
        return null;
    }

    @Override
    public Void visitRoutineCall(final Command.RoutineCallCommand routineCallCommand) {
        if (!this.routineNames.contains(routineCallCommand.getRoutineName())) {
            throw new CompilerException(routineCallCommand, "Unknown routine");
        }
        this.compile(new RoutineCallStep(), routineCallCommand);
        return null;
    }

    @Override
    public Void visitRoutineDeclare(final Command.RoutineDeclareCommand routineDeclareCommand) {
        this.compile(new RoutineDeclareStep(), routineDeclareCommand);
        return null;
    }

    @Override
    public Void visitEnd(final Command.EndCommand endCommand) {
        return null;
    }

    @Override
    public Void visitStackSize(final Command.StackSizeCommand stackSizeCommand) {
        this.compile(new StackSizeStep(), stackSizeCommand);
        return null;
    }

    @Override
    public Void visitPlus(final Command.PlusCommand plusCommand) {
        this.compile(new ArithmeticStep(LADD), plusCommand);
        return null;
    }

    @Override
    public Void visitMinus(final Command.MinusCommand minusCommand) {
        this.compile(new ArithmeticStep(LSUB), minusCommand);
        return null;
    }

    @Override
    public Void visitDivide(final Command.DivideCommand divideCommand) {
        this.compile(new ArithmeticStep(LDIV), divideCommand);
        return null;
    }

    @Override
    public Void visitMultiply(final Command.MultiplyCommand multiplyCommand) {
        this.compile(new ArithmeticStep(LMUL), multiplyCommand);
        return null;
    }

    @Override
    public Void visitModulo(final Command.ModuloCommand moduloCommand) {
        this.compile(new ArithmeticStep(LREM), moduloCommand);
        return null;
    }

    @Override
    public Void visitAnd(final Command.AndCommand andCommand) {
        this.compile(new ArithmeticStep(LAND), andCommand);
        return null;
    }

    @Override
    public Void visitOr(final Command.OrCommand orCommand) {
        this.compile(new ArithmeticStep(LOR), orCommand);
        return null;
    }

    @Override
    public Void visitXor(final Command.XorCommand xorCommand) {
        this.compile(new ArithmeticStep(LXOR), xorCommand);
        return null;
    }

    @Override
    public Void visitFlip(final Command.FlipCommand flipCommand) {
        this.visitPush(new Command.PushCommand(new Token(
                flipCommand.getOrigin().getLine(),
                flipCommand.getOrigin().getFrom(),
                flipCommand.getOrigin().getTo(),
                "-1",
                TokenType.WORD
        ), -1L));
        this.visitXor(new Command.XorCommand(new Token(
                flipCommand.getOrigin().getLine(),
                flipCommand.getOrigin().getFrom(),
                flipCommand.getOrigin().getTo(),
                "^",
                TokenType.XOR
        )));
        return null;
    }

    @Override
    public Void visitNativeCall(final Command.NativeCallCommand nativeCallCommand) {
        this.compile(new NativeCallStep(), nativeCallCommand);
        return null;
    }

    @Override
    public Void visitIf(final Command.IfCommand ifCommand) {
        this.compile(new IfStep(), ifCommand);
        return null;
    }

    @Override
    public Void visitImport(final Command.ImportCommand importCommand) {
        try {
            File scriptFile = null;
            for (final File inclusionDir : this.compilerSettings.getInclusions()) {
                String path = importCommand.getImportPath();
                if (!path.endsWith(".edina")) {
                    path += ".edina";
                }

                scriptFile = new File(inclusionDir, path);
                if (scriptFile.exists()) {
                    break;
                }
            }
            if (scriptFile == null || !scriptFile.exists()) {
                throw new FileNotFoundException(importCommand.getImportPath());
            }

            this.println("Importing " + importCommand.getImportPath() + " as " + importCommand.getImportName() + " (" + scriptFile.getAbsolutePath() + ")");

            final String potentialPreviousImport = this.parent == null ? null : this.parent.getImportPathByParent(scriptFile);
            if (potentialPreviousImport != null) {
                this.println(scriptFile.getAbsolutePath() + " has already been imported by a parent script - reusing already imported script");
                this.referenceNames.put(potentialPreviousImport, importCommand.getImportName());
                this.referencePaths.put(importCommand.getImportName(), potentialPreviousImport);
                this.referenceCompilers.put(importCommand.getImportName(), this.parent.getImportCompilerByParent(scriptFile));
                this.referenceFiles.put(scriptFile.getAbsolutePath(), potentialPreviousImport);
                return null;
            }

            final String[] pathSplit = importCommand.getImportName().split("/");
            final String normName = pathSplit[pathSplit.length - 1].split("\\.")[0];
            final String javaPath = String.join("/", Arrays.copyOfRange(pathSplit, 0, pathSplit.length - 1)) + "/" + normName;
            if (this.referencedClasses.containsKey(javaPath)) {
                throw new IllegalStateException("Can not reference a script with the same path more than once");
            }

            final List<String> lines = Files.readAllLines(scriptFile.toPath());
            final List<Token> tokens = new Tokenizer(lines).tokenize();
            final List<Command> commands = new Parser(lines, tokens).parse();

            this.println("Starting compilation of import " + importCommand.getImportName());
            final Compiler subCompiler = new Compiler(new CompilerSettings(
                    this.compilerSettings.getPackageName() + "/pkg_" + importCommand.getImportName(),
                    false,
                    this.compilerSettings.isQuiet(),
                    this.compilerSettings.getInclusions(),
                    "c_" + importCommand.getImportName().split("\\.")[0],
                    this.compilerSettings.getOriginalPackage() != null ? this.compilerSettings.getOriginalPackage() : this.compilerSettings.getPackageName()
            ));
            subCompiler.parent = this;
            for (final Command command : commands) {
                if (command instanceof Command.RoutineDeclareCommand decl) {
                    subCompiler.addRoutineNames(decl.getRoutineName());
                }
            }
            for (final Command command : commands) {
                subCompiler.visit(command);
            }

            final Map<String, byte[]> subClasses = subCompiler.finish();
            for (final String subKey : subClasses.keySet()) {
                if (this.referencedClasses.containsKey(subKey)) {
                    throw new IllegalStateException("Can not reference a script with the same path more than once");
                }
                if (subKey.equals(this.compilerSettings.getPackageName() + "/pkg_" + importCommand.getImportName()
                        + "/c_" + importCommand.getImportName().split("\\.")[0])) {
                    if (this.referenceNames.containsValue(importCommand.getImportName())) {
                        throw new IllegalStateException("Can not use the same name for multiple scripts");
                    }
                    this.referenceNames.put(subKey, importCommand.getImportName());
                    this.referencePaths.put(importCommand.getImportName(), subKey);
                    this.referenceCompilers.put(importCommand.getImportName(), subCompiler);
                    this.referenceFiles.put(scriptFile.getAbsolutePath(), subKey);
                }
                this.referencedClasses.put(subKey, subClasses.get(subKey));
            }
            this.println("Import " + importCommand.getImportName() + " has been compiled successfully");
        } catch (final Throwable t) {
            throw new CompilerException(importCommand, t, "Failed to compile \"" + importCommand.getImportPath() + "\"");
        }
        return null;
    }

    @Override
    public Void visitImportCall(final Command.ImportCallCommand importCallCommand) {
        if (!this.referencePaths.containsKey(importCallCommand.getImportName())) {
            throw new CompilerException(importCallCommand, "Unknown import reference");
        }
        final String path = this.referencePaths.get(importCallCommand.getImportName());
        final Compiler subCompiler = this.referenceCompilers.get(importCallCommand.getImportName());
        if (!subCompiler.getRoutineNames().contains(importCallCommand.getRoutineName())) {
            throw new CompilerException(importCallCommand, "Referenced import " + importCallCommand.getImportName()
                    + " does not contain routine " + importCallCommand.getRoutineName());
        }
        if (importCallCommand.getRoutineName().startsWith("_")) {
            throw new CompilerException(importCallCommand, "Can not access routine " + importCallCommand.getRoutineName()
                    + " in " + importCallCommand.getImportName());
        }
        this.compile(new ImportCallStep(), new ImportCallStep.Data(
                importCallCommand,
                path
        ));
        return null;
    }

    @Override
    public Void visitLoop(final Command.LoopCommand loopCommand) {
        this.compile(new LoopStep(), loopCommand);
        return null;
    }

    @Override
    public Void visitRoutineAnnotation(final Command.RoutineAnnotationCommand routineAnnotationCommand) {
        // Routine annotations are currently lost during compilation
        // TODO: Think about including them in the jar
        return null;
    }

    public MethodVisitor getCurrentMethodVisitor() {
        return this.currentMethodVisitor;
    }

    public void setCurrentMethodVisitor(final MethodVisitor currentMethodVisitor) {
        this.currentMethodVisitor = currentMethodVisitor;
    }

    public Map<String, byte[]> getReferencedClasses() {
        return this.referencedClasses;
    }

    public Map<String, String> getReferenceNames() {
        return this.referenceNames;
    }

    public Collection<String> getRoutineNames() {
        return Set.copyOf(this.routineNames);
    }

    private String getImportPathByParent(final File file) {
        if (this.referenceFiles.containsKey(file.getAbsolutePath())) {
            return this.referenceFiles.get(file.getAbsolutePath());
        }
        return this.parent == null ? null : this.parent.getImportPathByParent(file);
    }

    private Compiler getImportCompilerByParent(final File file) {
        if (this.referenceFiles.containsKey(file.getAbsolutePath())) {
            return this.referenceCompilers.get(this.referenceNames.get(this.referenceFiles.get(file.getAbsolutePath())));
        }
        return this.parent == null ? null : this.parent.getImportCompilerByParent(file);
    }

    private void println(final Object o) {
        if (!this.compilerSettings.isQuiet()) {
            System.out.println(o);
        }
    }

}
