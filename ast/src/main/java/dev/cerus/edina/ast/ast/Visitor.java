package dev.cerus.edina.ast.ast;

/**
 * Base class for command visitors. Performs specific operations for each implementation and returns the result.
 *
 * @param <T> The visitor type
 */
public interface Visitor<T> {

    /**
     * Convenience method
     *
     * @param visitable The visitable to visit
     *
     * @return The result
     */
    default T visit(final Visitable visitable) {
        return visitable.accept(this);
    }

    T visitPush(Command.PushCommand pushCommand);

    T visitPop(Command.PopCommand popCommand);

    T visitDup(Command.DupCommand dupCommand);

    T visitSwap(Command.SwapCommand swapCommand);

    T visitOver(Command.OverCommand overCommand);

    T visitRollLeft(Command.RollLeftCommand rollLeftCommand);

    T visitRollRight(Command.RollRightCommand rollRightCommand);

    T visitRoutineCall(Command.RoutineCallCommand routineCallCommand);

    T visitRoutineDeclare(Command.RoutineDeclareCommand routineDeclareCommand);

    T visitEnd(Command.EndCommand endCommand);

    T visitStackSize(Command.StackSizeCommand stackSizeCommand);

    T visitPlus(Command.PlusCommand plusCommand);

    T visitMinus(Command.MinusCommand minusCommand);

    T visitDivide(Command.DivideCommand divideCommand);

    T visitMultiply(Command.MultiplyCommand multiplyCommand);

    T visitModulo(Command.ModuloCommand moduloCommand);

    T visitAnd(Command.AndCommand andCommand);

    T visitOr(Command.OrCommand orCommand);

    T visitXor(Command.XorCommand xorCommand);

    T visitFlip(Command.FlipCommand flipCommand);

    T visitNativeCall(Command.NativeCallCommand nativeCallCommand);

    T visitIf(Command.IfCommand ifCommand);

    T visitComparison(Command.ComparisonCommand comparisonCommand);

    T visitImport(Command.ImportCommand importCommand);

    T visitImportCall(Command.ImportCallCommand importCallCommand);

    T visitLoop(Command.LoopCommand loopCommand);

    T visitRoutineAnnotation(Command.RoutineAnnotationCommand routineAnnotationCommand);

}
