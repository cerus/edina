package dev.cerus.edina.ast.ast;

/**
 * Adapter for the Visitor interface
 *
 * @param <T> The visitor type
 */
public class VisitorAdapter<T> implements Visitor<T> {

    @Override
    public T visitPush(final Command.PushCommand pushCommand) {
        return null;
    }

    @Override
    public T visitPop(final Command.PopCommand popCommand) {
        return null;
    }

    @Override
    public T visitDup(final Command.DupCommand dupCommand) {
        return null;
    }

    @Override
    public T visitSwap(final Command.SwapCommand swapCommand) {
        return null;
    }

    @Override
    public T visitOver(final Command.OverCommand overCommand) {
        return null;
    }

    @Override
    public T visitRollLeft(final Command.RollLeftCommand rollLeftCommand) {
        return null;
    }

    @Override
    public T visitRollRight(final Command.RollRightCommand rollRightCommand) {
        return null;
    }

    @Override
    public T visitRoutineCall(final Command.RoutineCallCommand routineCallCommand) {
        return null;
    }

    @Override
    public T visitRoutineDeclare(final Command.RoutineDeclareCommand routineDeclareCommand) {
        return null;
    }

    @Override
    public T visitEnd(final Command.EndCommand endCommand) {
        return null;
    }

    @Override
    public T visitStackSize(final Command.StackSizeCommand stackSizeCommand) {
        return null;
    }

    @Override
    public T visitPlus(final Command.PlusCommand plusCommand) {
        return null;
    }

    @Override
    public T visitMinus(final Command.MinusCommand minusCommand) {
        return null;
    }

    @Override
    public T visitDivide(final Command.DivideCommand divideCommand) {
        return null;
    }

    @Override
    public T visitMultiply(final Command.MultiplyCommand multiplyCommand) {
        return null;
    }

    @Override
    public T visitModulo(final Command.ModuloCommand moduloCommand) {
        return null;
    }

    @Override
    public T visitAnd(final Command.AndCommand andCommand) {
        return null;
    }

    @Override
    public T visitOr(final Command.OrCommand orCommand) {
        return null;
    }

    @Override
    public T visitXor(final Command.XorCommand xorCommand) {
        return null;
    }

    @Override
    public T visitFlip(final Command.FlipCommand flipCommand) {
        return null;
    }

    @Override
    public T visitNativeCall(final Command.NativeCallCommand nativeCallCommand) {
        return null;
    }

    @Override
    public T visitIf(final Command.IfCommand ifCommand) {
        return null;
    }

    @Override
    public T visitComparison(final Command.ComparisonCommand comparisonCommand) {
        return null;
    }

    @Override
    public T visitImport(final Command.ImportCommand importCommand) {
        return null;
    }

    @Override
    public T visitImportCall(final Command.ImportCallCommand importCallCommand) {
        return null;
    }

    @Override
    public T visitLoop(final Command.LoopCommand loopCommand) {
        return null;
    }

    @Override
    public T visitRoutineAnnotation(final Command.RoutineAnnotationCommand routineAnnotationCommand) {
        return null;
    }

}
