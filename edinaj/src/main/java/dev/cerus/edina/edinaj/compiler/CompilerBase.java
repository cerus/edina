package dev.cerus.edina.edinaj.compiler;

import dev.cerus.edina.ast.ast.Command;
import dev.cerus.edina.ast.ast.VisitorAdapter;
import dev.cerus.edina.edinaj.compiler.step.CompilerStep;
import dev.cerus.edina.edinaj.compiler.step.command.branch.IfStep;
import dev.cerus.edina.edinaj.compiler.step.command.loop.LoopStep;
import dev.cerus.edina.edinaj.compiler.step.command.math.ArithmeticStep;
import dev.cerus.edina.edinaj.compiler.step.command.natives.NativeCallStep;
import dev.cerus.edina.edinaj.compiler.step.command.stack.DupStep;
import dev.cerus.edina.edinaj.compiler.step.command.stack.OverStep;
import dev.cerus.edina.edinaj.compiler.step.command.stack.PopStep;
import dev.cerus.edina.edinaj.compiler.step.command.stack.PushStep;
import dev.cerus.edina.edinaj.compiler.step.command.stack.RollLeftStep;
import dev.cerus.edina.edinaj.compiler.step.command.stack.RollRightStep;
import dev.cerus.edina.edinaj.compiler.step.command.stack.StackSizeStep;
import dev.cerus.edina.edinaj.compiler.step.command.stack.SwapStep;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;

public abstract class CompilerBase extends VisitorAdapter<Void> {

    protected final CompilerEnv compilerEnv = new CompilerEnv();
    protected CompilerSettings compilerSettings;

    public CompilerBase(final CompilerSettings compilerSettings) {
        this.compilerSettings = compilerSettings;
    }

    public <T> void compile(final CompilerStep<StepData<T>> step, final T thing) {
        this.compile(step, thing, this.methodVisitor());
    }

    public <T> void compile(final CompilerStep<StepData<T>> step, final T thing, final MethodVisitor mv) {
        step.compile(
                this.classWriter(),
                this.compilerSettings,
                new StepData<>(this.classWriter(), mv, this, this.compilerSettings, thing)
        );
    }

    @Override
    public Void visitPush(final Command.PushCommand pushCommand) {
        this.compile(new PushStep(), pushCommand);
        if (pushCommand.getParsedValue() instanceof String s) {
            this.compilerEnv.trackStackTop(s.length());
        } else if (pushCommand.getParsedValue() instanceof Integer i) {
            this.compilerEnv.trackStackTop(i);
        } else if (pushCommand.getParsedValue() instanceof Long l) {
            this.compilerEnv.trackStackTop(l);
        } else {
            this.compilerEnv.trackStackTopUnknown();
        }
        return null;
    }

    @Override
    public Void visitPop(final Command.PopCommand popCommand) {
        this.compile(new PopStep(), popCommand);
        this.compilerEnv.trackStackTopUnknown();
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
        this.compilerEnv.trackStackTopUnknown();
        return null;
    }

    @Override
    public Void visitOver(final Command.OverCommand overCommand) {
        this.compile(new OverStep(), overCommand);
        this.compilerEnv.trackStackTopUnknown();
        return null;
    }

    @Override
    public Void visitRollLeft(final Command.RollLeftCommand rollLeftCommand) {
        this.compile(new RollLeftStep(), rollLeftCommand);
        this.compilerEnv.trackStackTopUnknown();
        return null;
    }

    @Override
    public Void visitRollRight(final Command.RollRightCommand rollRightCommand) {
        this.compile(new RollRightStep(), rollRightCommand);
        this.compilerEnv.trackStackTopUnknown();
        return null;
    }

    @Override
    public Void visitEnd(final Command.EndCommand endCommand) {
        return null;
    }

    @Override
    public Void visitStackSize(final Command.StackSizeCommand stackSizeCommand) {
        this.compile(new StackSizeStep(), stackSizeCommand);
        this.compilerEnv.trackStackTopUnknown();
        return null;
    }

    @Override
    public Void visitPlus(final Command.PlusCommand plusCommand) {
        this.compile(new ArithmeticStep(LADD), plusCommand);
        this.compilerEnv.trackStackTopUnknown();
        return null;
    }

    @Override
    public Void visitMinus(final Command.MinusCommand minusCommand) {
        this.compile(new ArithmeticStep(LSUB), minusCommand);
        this.compilerEnv.trackStackTopUnknown();
        return null;
    }

    @Override
    public Void visitDivide(final Command.DivideCommand divideCommand) {
        this.compile(new ArithmeticStep(LDIV), divideCommand);
        this.compilerEnv.trackStackTopUnknown();
        return null;
    }

    @Override
    public Void visitMultiply(final Command.MultiplyCommand multiplyCommand) {
        this.compile(new ArithmeticStep(LMUL), multiplyCommand);
        this.compilerEnv.trackStackTopUnknown();
        return null;
    }

    @Override
    public Void visitModulo(final Command.ModuloCommand moduloCommand) {
        this.compile(new ArithmeticStep(LREM), moduloCommand);
        this.compilerEnv.trackStackTopUnknown();
        return null;
    }

    @Override
    public Void visitAnd(final Command.AndCommand andCommand) {
        this.compile(new ArithmeticStep(LAND), andCommand);
        this.compilerEnv.trackStackTopUnknown();
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
        this.compilerEnv.trackStackTopUnknown();
        return null;
    }

    @Override
    public Void visitFlip(final Command.FlipCommand flipCommand) {
        this.visitPush(new Command.PushCommand(flipCommand.getOrigin(), -1L));
        this.visitXor(new Command.XorCommand(flipCommand.getOrigin()));
        if (this.compilerEnv.getTrackedStackTop() != null) {
            this.compilerEnv.trackStackTop(~this.compilerEnv.getTrackedStackTop());
        }
        return null;
    }

    @Override
    public Void visitNativeCall(final Command.NativeCallCommand nativeCallCommand) {
        this.compile(new NativeCallStep(), nativeCallCommand);
        this.compilerEnv.trackStackTopUnknown();
        return null;
    }

    @Override
    public Void visitIf(final Command.IfCommand ifCommand) {
        this.compile(new IfStep(), ifCommand);
        return null;
    }

    @Override
    public Void visitLoop(final Command.LoopCommand loopCommand) {
        this.compile(new LoopStep(), loopCommand);
        return null;
    }

    protected abstract MethodVisitor methodVisitor();

    protected abstract ClassWriter classWriter();

    public MethodVisitor getCurrentMethodVisitor() {
        return this.methodVisitor();
    }

    public abstract void setCurrentMethodVisitor(final MethodVisitor currentMethodVisitor);

    public CompilerEnv getCompilerEnv() {
        return this.compilerEnv;
    }

}
