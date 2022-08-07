package dev.cerus.edina.ast.ast;

import java.util.stream.Collectors;

/**
 * Simple visitor implementation that converts commands back into strings.
 */
public class StringVisitor implements Visitor<String> {

    @Override
    public String visitPush(final Command.PushCommand pushCommand) {
        return pushCommand.getParsedValue() + "";
    }

    @Override
    public String visitPop(final Command.PopCommand popCommand) {
        return "pop";
    }

    @Override
    public String visitDup(final Command.DupCommand dupCommand) {
        return "dup";
    }

    @Override
    public String visitSwap(final Command.SwapCommand swapCommand) {
        return "swap";
    }

    @Override
    public String visitOver(final Command.OverCommand overCommand) {
        return "over";
    }

    @Override
    public String visitRollLeft(final Command.RollLeftCommand rollLeftCommand) {
        return "lroll";
    }

    @Override
    public String visitRollRight(final Command.RollRightCommand rollRightCommand) {
        return "rroll";
    }

    @Override
    public String visitRoutineCall(final Command.RoutineCallCommand routineCallCommand) {
        return "." + routineCallCommand.getRoutineName();
    }

    @Override
    public String visitRoutineDeclare(final Command.RoutineDeclareCommand routineDeclareCommand) {
        return "rt " + routineDeclareCommand.getRoutineName() + "\n  " + routineDeclareCommand.getRoutineBody().stream()
                .map(this::visit)
                .collect(Collectors.joining(" ")) + "\nend";
    }

    @Override
    public String visitEnd(final Command.EndCommand endCommand) {
        return "end";
    }

    @Override
    public String visitStackSize(final Command.StackSizeCommand stackSizeCommand) {
        return "ssize";
    }

    @Override
    public String visitPlus(final Command.PlusCommand plusCommand) {
        return "+";
    }

    @Override
    public String visitMinus(final Command.MinusCommand minusCommand) {
        return "-";
    }

    @Override
    public String visitDivide(final Command.DivideCommand divideCommand) {
        return "/";
    }

    @Override
    public String visitMultiply(final Command.MultiplyCommand multiplyCommand) {
        return "*";
    }

    @Override
    public String visitModulo(final Command.ModuloCommand moduloCommand) {
        return "%";
    }

    @Override
    public String visitAnd(final Command.AndCommand andCommand) {
        return "&";
    }

    @Override
    public String visitOr(final Command.OrCommand orCommand) {
        return "|";
    }

    @Override
    public String visitXor(final Command.XorCommand xorCommand) {
        return "^";
    }

    @Override
    public String visitFlip(final Command.FlipCommand flipCommand) {
        return "~";
    }

    @Override
    public String visitNativeCall(final Command.NativeCallCommand nativeCallCommand) {
        return "native_" + nativeCallCommand.getType().name().toLowerCase();
    }

    @Override
    public String visitIf(final Command.IfCommand ifCommand) {
        return ifCommand.getType().name().toLowerCase() + " " + ifCommand.getIfBody().stream()
                .map(this::visit)
                .collect(Collectors.joining(" ")) + (ifCommand.getElseBody().isEmpty()
                ? "" : (ifCommand.getElseBody().get(0) instanceof Command.IfCommand
                ? this.visit(ifCommand.getElseBody().get(0)) : ifCommand.getElseBody().stream()
                .map(this::visit)
                .collect(Collectors.joining(" ")) + " end"));
    }

    @Override
    public String visitImport(final Command.ImportCommand importCommand) {
        return "import \"" + importCommand.getImportPath() + "\" as " + importCommand.getImportName();
    }

    @Override
    public String visitImportCall(final Command.ImportCallCommand importCallCommand) {
        return ":" + importCallCommand.getImportName() + "." + importCallCommand.getRoutineName();
    }

    @Override
    public String visitLoop(final Command.LoopCommand loopCommand) {
        return switch (loopCommand.getType()) {
            case UNTIL -> "until";
            case WHILE -> "white";
        } + loopCommand.getBody().stream()
                .map(this::visit)
                .collect(Collectors.joining(" ")) + " end";
    }

    @Override
    public String visitRoutineAnnotation(final Command.RoutineAnnotationCommand routineAnnotationCommand) {
        return "[ ... ]"; // I can't be bothered to translate this into a string
    }

}
