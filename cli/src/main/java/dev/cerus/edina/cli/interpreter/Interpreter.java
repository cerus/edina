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
        if (pushCommand.getParsedValue() instanceof Number) {
            this.environment.getStack().push(pushCommand.getParsedValue());
        } else if (pushCommand.getParsedValue() instanceof String) {
            this.environment.getStack().pushString((String) pushCommand.getParsedValue());
        }
        return null;
    }

    @Override
    public Void visitPop(final Command.PopCommand popCommand) {
        this.environment.getStack().pop();
        return null;
    }

    @Override
    public Void visitDup(final Command.DupCommand dupCommand) {
        this.environment.getStack().dup();
        return null;
    }

    @Override
    public Void visitSwap(final Command.SwapCommand swapCommand) {
        this.environment.getStack().swap();
        return null;
    }

    @Override
    public Void visitOver(final Command.OverCommand overCommand) {
        this.environment.getStack().over();
        return null;
    }

    @Override
    public Void visitRollLeft(final Command.RollLeftCommand rollLeftCommand) {
        this.environment.getStack().rollLeft();
        return null;
    }

    @Override
    public Void visitRollRight(final Command.RollRightCommand rollRightCommand) {
        this.environment.getStack().rollRight();
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
        this.environment.getStack().push(this.environment.getStack().stackSize());
        return null;
    }

    @Override
    public Void visitPlus(final Command.PlusCommand plusCommand) {
        this.environment.getStack().push(this.environment.getStack().popLong() + this.environment.getStack().popLong());
        return null;
    }

    @Override
    public Void visitMinus(final Command.MinusCommand minusCommand) {
        this.environment.getStack().push(this.environment.getStack().popLong() - this.environment.getStack().popLong());
        return null;
    }

    @Override
    public Void visitDivide(final Command.DivideCommand divideCommand) {
        this.environment.getStack().push(this.environment.getStack().popLong() / this.environment.getStack().popLong());
        return null;
    }

    @Override
    public Void visitMultiply(final Command.MultiplyCommand multiplyCommand) {
        this.environment.getStack().push(this.environment.getStack().popLong() * this.environment.getStack().popLong());
        return null;
    }

    @Override
    public Void visitModulo(final Command.ModuloCommand moduloCommand) {
        this.environment.getStack().push(this.environment.getStack().popLong() % this.environment.getStack().popLong());
        return null;
    }

    @Override
    public Void visitAnd(final Command.AndCommand andCommand) {
        this.environment.getStack().push(this.environment.getStack().popLong() & this.environment.getStack().popLong());
        return null;
    }

    @Override
    public Void visitOr(final Command.OrCommand orCommand) {
        this.environment.getStack().push(this.environment.getStack().popLong() | this.environment.getStack().popLong());
        return null;
    }

    @Override
    public Void visitXor(final Command.XorCommand xorCommand) {
        this.environment.getStack().push(this.environment.getStack().popLong() ^ this.environment.getStack().popLong());
        return null;
    }

    @Override
    public Void visitFlip(final Command.FlipCommand flipCommand) {
        this.environment.getStack().push(~this.environment.getStack().popLong());
        return null;
    }

    @Override
    public Void visitNativeCall(final Command.NativeCallCommand nativeCallCommand) {
        return null;
    }

    @Override
    public Void visitIf(final Command.IfCommand ifCommand) {
        if (this.environment.getStack().popLong() >= 1) {
            for (final Command command : ifCommand.getIfBody()) {
                this.visit(command);
            }
        } else if (ifCommand.getElseBody() != null && !ifCommand.getElseBody().isEmpty()) {
            for (final Command command : ifCommand.getElseBody()) {
                this.visit(command);
            }
        }
        return null;
    }

    @Override
    public Void visitComparison(final Command.ComparisonCommand comparisonCommand) {
        return null;
    }

    @Override
    public Void visitImport(final Command.ImportCommand importCommand) {
        System.out.println("Unsupported operation");
        return null;
    }

    @Override
    public Void visitImportCall(final Command.ImportCallCommand importCallCommand) {
        System.out.println("Unsupported operation");
        return null;
    }

    @Override
    public Void visitLoop(final Command.LoopCommand loopCommand) {
        while (switch (loopCommand.getType()) {
            case UNTIL -> this.environment.getStack().peekLong() == 0;
            case WHILE -> this.environment.getStack().peekLong() != 0;
        }) {
            for (final Command command : loopCommand.getBody()) {
                this.visit(command);
            }
        }
        return null;
    }

    @Override
    public Void visitRoutineAnnotation(final Command.RoutineAnnotationCommand routineAnnotationCommand) {
        return null;
    }

}
