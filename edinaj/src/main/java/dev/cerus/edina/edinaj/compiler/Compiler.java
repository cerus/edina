package dev.cerus.edina.edinaj.compiler;

import dev.cerus.edina.ast.Parser;
import dev.cerus.edina.ast.ast.Command;
import dev.cerus.edina.ast.exception.ParserException;
import dev.cerus.edina.ast.token.Location;
import dev.cerus.edina.ast.token.Token;
import dev.cerus.edina.ast.token.Tokenizer;
import dev.cerus.edina.edinaj.Launcher;
import dev.cerus.edina.edinaj.compiler.exception.CompilerException;
import dev.cerus.edina.edinaj.compiler.step.CompilerStep;
import dev.cerus.edina.edinaj.compiler.step.command.imports.ImportCallStep;
import dev.cerus.edina.edinaj.compiler.step.command.routine.RoutineCallStep;
import dev.cerus.edina.edinaj.compiler.step.command.routine.RoutineDeclareStep;
import dev.cerus.edina.edinaj.compiler.step.init.ConstructorStep;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;

public class Compiler extends CompilerBase {

    private static final int ROUTINE_INLINE_SIZE = 5;

    private ClassWriter appClassWriter;
    private ClassWriter mainClassWriter;
    private ClassWriter stackClassWriter;
    private ClassWriter nativesClassWriter;
    private MethodVisitor mainMethodVisitor;
    private MethodVisitor currentMethodVisitor;
    private Compiler parent;

    public Compiler(final CompilerSettings compilerSettings) {
        super(compilerSettings);
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
        fieldVisitor = this.mainClassWriter.visitField(ACC_PRIVATE | ACC_FINAL, "stack", "L" + settings.getStackName() + ";", null, null);
        fieldVisitor.visitEnd();
        fieldVisitor = this.mainClassWriter.visitField(ACC_PRIVATE | ACC_FINAL, "natives", "L" + settings.getNativesName() + ";", null, null);
        fieldVisitor.visitEnd();

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
            this.visitNativeCall(new Command.NativeCallCommand(
                    Location.singleLine(this.compilerSettings.getSourceFileName(), "", 256, 0, 0),
                    Command.NativeCallCommand.Type.STACK_DEBUG)
            );
        }
        this.finishCommon();

        final Map<String, byte[]> classMap = new HashMap<>();
        classMap.put(this.compilerSettings.getMainClassName(), this.appClassWriter.toByteArray());
        classMap.put(this.compilerSettings.getAppLauncherName(), this.mainClassWriter.toByteArray());
        classMap.put(this.compilerSettings.getStackName(), this.stackClassWriter.toByteArray());
        classMap.put(this.compilerSettings.getNativesName(), this.nativesClassWriter.toByteArray());
        classMap.putAll(this.compilerEnv.getClassMap());
        return classMap;
    }

    private Map<String, byte[]> finishSub() {
        this.finishCommon();

        this.mainClassWriter.visitEnd();
        final Map<String, byte[]> classMap = new HashMap<>();
        classMap.put(this.compilerSettings.getMainClassName(), this.mainClassWriter.toByteArray());
        classMap.putAll(this.compilerEnv.getClassMap());
        return classMap;
    }

    private void finishCommon() {
        this.compile(new ConstructorStep(), null);

        for (final String routineName : this.compilerEnv.getRoutineNames()) {
            if (!this.compilerSettings.optimizationEnabled(Launcher.Options.Optimization.ROUTINE_INLINE)
                    || this.compilerEnv.isUsed(routineName)) {
                this.compile(new RoutineDeclareStep(), this.compilerEnv.getRoutine(routineName));
            }
        }

        final Label label1 = new Label();
        this.mainMethodVisitor.visitLabel(label1);
        this.mainMethodVisitor.visitLineNumber(100, label1);
        this.mainMethodVisitor.visitInsn(RETURN);

        this.mainMethodVisitor.visitMaxs(0, 0);
        this.mainMethodVisitor.visitEnd();
        if (this.appClassWriter != null) {
            this.appClassWriter.visitEnd();
        } else {
            this.mainClassWriter.visitEnd();
        }
    }

    public void addRoutines(final Command.RoutineDeclareCommand... declarations) {
        for (final Command.RoutineDeclareCommand rt : declarations) {
            if (this.compilerEnv.routineExists(rt.getRoutineName())) {
                throw new CompilerException("Routine can not be declared more than once", rt.getOrigin());
            }
            this.compilerEnv.addRoutine(rt);
        }
    }

    @Override
    public Void visitRoutineCall(final Command.RoutineCallCommand routineCallCommand) {
        final Command.RoutineDeclareCommand routine = this.compilerEnv.getRoutine(routineCallCommand.getRoutineName());
        if (routine == null) {
            throw new CompilerException(routineCallCommand, "Unknown routine");
        }
        if (this.compilerSettings.optimizationEnabled(Launcher.Options.Optimization.ROUTINE_INLINE)
                && routine.getRoutineBody().size() <= ROUTINE_INLINE_SIZE
                && routine.getRoutineBody().stream().allMatch(this::isSimpleExtended)) {
            for (final Command command : routine.getRoutineBody()) {
                this.visit(command);
            }
        } else {
            this.compilerEnv.markUse(routine.getRoutineName());
            this.compile(new RoutineCallStep(), routineCallCommand);
        }
        return null;
    }

    @Override
    public Void visitIf(final Command.IfCommand ifCommand) {
        if (this.compilerSettings.optimizationEnabled(Launcher.Options.Optimization.SMART_BRANCHES)
                && this.compilerEnv.getTrackedStackTop() != null) {
            final long topVal = this.compilerEnv.getTrackedStackTop();
            if ((ifCommand.getType() == Command.IfCommand.Type.IFN && topVal != 0)
                    || (ifCommand.getType() == Command.IfCommand.Type.IFZ && topVal == 0)
                    || (ifCommand.getType() == Command.IfCommand.Type.IFGT && topVal >= 0)
                    || (ifCommand.getType() == Command.IfCommand.Type.IFLT && topVal <= 0)) {
                for (final Command command : ifCommand.getIfBody()) {
                    this.visit(command);
                }
                return null;
            } else if ((ifCommand.getType() == Command.IfCommand.Type.IFN && topVal == 0)
                    || (ifCommand.getType() == Command.IfCommand.Type.IFZ && topVal != 0)
                    || (ifCommand.getType() == Command.IfCommand.Type.IFGT && topVal < 0)
                    || (ifCommand.getType() == Command.IfCommand.Type.IFLT && topVal > 0)) {
                if (ifCommand.getElseBody() != null) {
                    for (final Command command : ifCommand.getElseBody()) {
                        this.visit(command);
                    }
                }
                return null;
            }
        }
        return super.visitIf(ifCommand);
    }

    @Override
    public Void visitLoop(final Command.LoopCommand loopCommand) {
        if (this.compilerSettings.optimizationEnabled(Launcher.Options.Optimization.SMART_BRANCHES)
                && this.compilerEnv.getTrackedStackTop() != null) {
            final long topVal = this.compilerEnv.getTrackedStackTop();
            if ((loopCommand.getType() == Command.LoopCommand.Type.WHILE && topVal == 0)
                    || (loopCommand.getType() == Command.LoopCommand.Type.UNTIL && topVal != 0)) {
                return null;
            }
        }
        return super.visitLoop(loopCommand);
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
                this.compilerEnv.addImport(scriptFile.getAbsolutePath(), new CompilerEnv.Import(
                        importCommand.getImportName(),
                        potentialPreviousImport,
                        this.parent.getImportCompilerByParent(scriptFile)
                ));
                return null;
            }

            final String[] pathSplit = importCommand.getImportName().split("/");
            final String normName = pathSplit[pathSplit.length - 1].split("\\.")[0];
            final String javaPath = String.join("/", Arrays.copyOfRange(pathSplit, 0, pathSplit.length - 1)) + "/" + normName;
            if (this.compilerEnv.getClassByName(javaPath) != null) {
                throw new IllegalStateException("Can not reference a script with the same path more than once");
            }

            final List<String> lines = Files.readAllLines(scriptFile.toPath());
            final List<Token> tokens = new Tokenizer(scriptFile.getName(), lines).tokenize();
            final List<Command> commands = new Parser(scriptFile.getName(), lines, tokens).parse();

            this.println("Starting compilation of import " + importCommand.getImportName());
            final Compiler subCompiler = new Compiler(new CompilerSettings(
                    scriptFile.getName(),
                    this.compilerSettings.getPackageName() + "/pkg_" + importCommand.getImportName(),
                    false,
                    this.compilerSettings.isQuiet(),
                    this.compilerSettings.getInclusions(),
                    List.of(),
                    "c_" + importCommand.getImportName().split("\\.")[0],
                    this.compilerSettings.getOriginalPackage() != null ? this.compilerSettings.getOriginalPackage() : this.compilerSettings.getPackageName()
            ));
            subCompiler.parent = this;
            for (final Command command : commands) {
                if (command instanceof Command.RoutineDeclareCommand decl) {
                    subCompiler.addRoutines(decl);
                }
            }
            for (final Command command : commands) {
                subCompiler.visit(command);
            }

            final Map<String, byte[]> subClasses = subCompiler.finish();
            for (final String subKey : subClasses.keySet()) {
                if (this.compilerEnv.getClassByName(subKey) != null) {
                    throw new IllegalStateException("Can not reference a script with the same path more than once");
                }
                if (subKey.equals(this.compilerSettings.getPackageName() + "/pkg_" + importCommand.getImportName()
                        + "/c_" + importCommand.getImportName().split("\\.")[0])) {
                    if (this.compilerEnv.getImportByName(importCommand.getImportName()) != null) {
                        throw new IllegalStateException("Can not use the same name for multiple scripts");
                    }
                    this.compilerEnv.addImport(scriptFile.getAbsolutePath(), new CompilerEnv.Import(
                            importCommand.getImportName(),
                            subKey,
                            subCompiler
                    ));
                }
                this.compilerEnv.addClass(subKey, subClasses.get(subKey));
            }
            this.println("Import " + importCommand.getImportName() + " has been compiled successfully");
        } catch (final ParserException | CompilerException ex) {
            throw new CompilerException("Failed to compile imported script " + ex.getLocation().fileName(), ex, ex.getLocation());
        } catch (final Throwable t) {
            throw new CompilerException(importCommand, t, "Failed to compile \"" + importCommand.getImportPath() + "\"");
        }
        return null;
    }

    @Override
    public Void visitImportCall(final Command.ImportCallCommand importCallCommand) {
        if (this.compilerEnv.getImportByName(importCallCommand.getImportName()) == null) {
            throw new CompilerException(importCallCommand, "Unknown import reference");
        }

        final CompilerEnv.Import imprt = this.compilerEnv.getImportByName(importCallCommand.getImportName());
        final String path = imprt.path();
        final Compiler subCompiler = imprt.compiler();

        if (!subCompiler.compilerEnv.getRoutineNames().contains(importCallCommand.getRoutineName())) {
            throw new CompilerException(importCallCommand, "Referenced import " + importCallCommand.getImportName()
                    + " does not contain routine " + importCallCommand.getRoutineName());
        }
        if (importCallCommand.getRoutineName().startsWith("_")) {
            throw new CompilerException(importCallCommand, "Can not access routine " + importCallCommand.getRoutineName()
                    + " in " + importCallCommand.getImportName());
        }

        imprt.compiler().getCompilerEnv().markUse(importCallCommand.getRoutineName());
        this.compile(new ImportCallStep(), new ImportCallStep.Data(
                importCallCommand,
                path
        ));
        return null;
    }

    @Override
    public Void visitRoutineAnnotation(final Command.RoutineAnnotationCommand routineAnnotationCommand) {
        // Routine annotations are currently lost during compilation
        // TODO: Think about including them in the jar
        return null;
    }

    @Override
    public void setCurrentMethodVisitor(final MethodVisitor currentMethodVisitor) {
        this.currentMethodVisitor = currentMethodVisitor;
    }

    private String getImportPathByParent(final File file) {
        if (this.compilerEnv.getImportByPath(file.getAbsolutePath()) != null) {
            return this.compilerEnv.getImportByPath(file.getAbsolutePath()).path();
        }
        return this.parent == null ? null : this.parent.getImportPathByParent(file);
    }

    private Compiler getImportCompilerByParent(final File file) {
        if (this.compilerEnv.getImportByPath(file.getAbsolutePath()) != null) {
            return this.compilerEnv.getImportByPath(file.getAbsolutePath()).compiler();
        }
        return this.parent == null ? null : this.parent.getImportCompilerByParent(file);
    }

    private void println(final Object o) {
        if (!this.compilerSettings.isQuiet()) {
            System.out.println(o);
        }
    }

    private boolean isSimpleExtended(final Command command) {
        return this.isSimple(command) || command instanceof Command.RoutineCallCommand;
    }

    private boolean isSimple(final Command command) {
        return command instanceof Command.PlusCommand
                || command instanceof Command.MinusCommand
                || command instanceof Command.MultiplyCommand
                || command instanceof Command.DivideCommand
                || command instanceof Command.ModuloCommand
                || command instanceof Command.AndCommand
                || command instanceof Command.OrCommand
                || command instanceof Command.XorCommand
                || command instanceof Command.FlipCommand
                || command instanceof Command.PushCommand
                || command instanceof Command.PopCommand
                || command instanceof Command.OverCommand
                || command instanceof Command.SwapCommand
                || command instanceof Command.RollLeftCommand
                || command instanceof Command.RollRightCommand
                || command instanceof Command.DupCommand;
    }

    @Override
    protected MethodVisitor methodVisitor() {
        return this.currentMethodVisitor;
    }

    @Override
    protected ClassWriter classWriter() {
        return this.appClassWriter == null ? this.mainClassWriter : this.appClassWriter;
    }

}
