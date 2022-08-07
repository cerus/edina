package dev.cerus.edina.cli.interpreter;

import dev.cerus.edina.ast.ast.Command;
import dev.cerus.edina.ast.ast.Visitor;
import java.util.List;

public class Interpreter implements Visitor<Void> {

    private final Environment environment;

    public Interpreter(final Environment environment) {
        this.environment = environment;
    }

    @Override
    public Void visitPush(final Command.PushCommand pushCommand) {
        this.environment.push(pushCommand.getParsedValue());
        return null;
    }

    @Override
    public Void visitPop(final Command.PopCommand popCommand) {
        this.environment.pop();
        return null;
    }

    @Override
    public Void visitDup(final Command.DupCommand dupCommand) {
        this.environment.dup();
        return null;
    }

    @Override
    public Void visitSwap(final Command.SwapCommand swapCommand) {
        this.environment.swap();
        return null;
    }

    @Override
    public Void visitOver(final Command.OverCommand overCommand) {
        this.environment.over();
        return null;
    }

    @Override
    public Void visitRollLeft(final Command.RollLeftCommand rollLeftCommand) {
        this.environment.rollLeft();
        return null;
    }

    @Override
    public Void visitRollRight(final Command.RollRightCommand rollRightCommand) {
        this.environment.rollRight();
        return null;
    }

    @Override
    public Void visitRoutineCall(final Command.RoutineCallCommand routineCallCommand) {
        final List<Command> routine = this.environment.getRoutine(routineCallCommand.getRoutineName());
        for (final Command command : routine) {
            this.visit(command);
        }
        return null;
    }

    @Override
    public Void visitRoutineDeclare(final Command.RoutineDeclareCommand routineDeclareCommand) {
        this.environment.declareRoutine(routineDeclareCommand);
        return null;
    }

    @Override
    public Void visitEnd(final Command.EndCommand endCommand) {
        return null;
    }

    @Override
    public Void visitStackSize(final Command.StackSizeCommand stackSizeCommand) {
        this.environment.push(this.environment.stackSize());
        return null;
    }

    @Override
    public Void visitPlus(final Command.PlusCommand plusCommand) {
        this.environment.push(this.environment.popNum() + this.environment.popNum());
        return null;
    }

    @Override
    public Void visitMinus(final Command.MinusCommand minusCommand) {
        this.environment.push(this.environment.popNum() - this.environment.popNum());
        return null;
    }

    @Override
    public Void visitDivide(final Command.DivideCommand divideCommand) {
        this.environment.push(this.environment.popNum() / this.environment.popNum());
        return null;
    }

    @Override
    public Void visitMultiply(final Command.MultiplyCommand multiplyCommand) {
        this.environment.push(this.environment.popNum() * this.environment.popNum());
        return null;
    }

    @Override
    public Void visitModulo(final Command.ModuloCommand moduloCommand) {
        this.environment.push(this.environment.popNum() % this.environment.popNum());
        return null;
    }

    @Override
    public Void visitAnd(final Command.AndCommand andCommand) {
        return null;
    }

    @Override
    public Void visitOr(final Command.OrCommand orCommand) {
        return null;
    }

    @Override
    public Void visitXor(final Command.XorCommand xorCommand) {
        return null;
    }

    @Override
    public Void visitFlip(final Command.FlipCommand flipCommand) {
        return null;
    }

    @Override
    public Void visitNativeCall(final Command.NativeCallCommand nativeCallCommand) {
        return null;
    }

    @Override
    public Void visitIf(final Command.IfCommand ifCommand) {
        return null;
    }

    @Override
    public Void visitImport(final Command.ImportCommand importCommand) {
        return null;
    }

    @Override
    public Void visitImportCall(final Command.ImportCallCommand importCallCommand) {
        return null;
    }

    @Override
    public Void visitLoop(final Command.LoopCommand loopCommand) {
        return null;
    }

    @Override
    public Void visitRoutineAnnotation(final Command.RoutineAnnotationCommand routineAnnotationCommand) {
        return null;
    }

}
