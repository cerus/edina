package dev.cerus.edina.ast.ast;

import dev.cerus.edina.ast.token.Location;
import java.util.List;
import java.util.Map;

/**
 * Base class for Edina commands
 */
public abstract class Command implements Visitable {

    private final Location origin;

    public Command(final Location origin) {
        this.origin = origin;
    }

    public Location getOrigin() {
        return this.origin;
    }

    /**
     * Pushes a value onto the stack
     * <p>
     * [A, B, C, D] -> [X, A, B, C, D]
     *
     * <pre>
     *     # Any number or string literal will be parsed as a push command
     *     123
     *     -456
     *     "hello whats up"  # -> [Len, c, b, a] -> [14, p, u,  , s, t, a, h, w,  , o, l, l, e, h]
     * </pre>
     */
    public static class PushCommand extends Command {

        private final Object parsedValue;

        public PushCommand(final Location origin, final Object parsedValue) {
            super(origin);
            this.parsedValue = parsedValue;
        }

        @Override
        public <T> T accept(final Visitor<T> visitor) {
            return visitor.visitPush(this);
        }

        public Object getParsedValue() {
            return this.parsedValue;
        }

    }

    /**
     * Discards the top item of the stack
     * <p>
     * [A, B, C, D] -> [B, C, D]
     *
     * <pre>
     *     pop
     * </pre>
     */
    public static class PopCommand extends Command {

        public PopCommand(final Location origin) {
            super(origin);
        }

        @Override
        public <T> T accept(final Visitor<T> visitor) {
            return visitor.visitPop(this);
        }

    }

    /**
     * Duplicates the top item of the stack
     * <p>
     * [A, B, C, D] -> [A, A, B, C, D]
     *
     * <pre>
     *     dup
     * </pre>
     */
    public static class DupCommand extends Command {

        public DupCommand(final Location origin) {
            super(origin);
        }

        @Override
        public <T> T accept(final Visitor<T> visitor) {
            return visitor.visitDup(this);
        }

    }

    /**
     * Swaps the two topmost items
     * <p>
     * [A, B, C, D] -> [B, A, C, D]
     *
     * <pre>
     *     swap
     * </pre>
     */
    public static class SwapCommand extends Command {

        public SwapCommand(final Location origin) {
            super(origin);
        }

        @Override
        public <T> T accept(final Visitor<T> visitor) {
            return visitor.visitSwap(this);
        }

    }

    /**
     * Duplicates the 2nd top item of the stack onto the top of the stack
     * <p>
     * [A, B, C, D] -> [B, A, B, C, D]
     *
     * <pre>
     *     over
     * </pre>
     */
    public static class OverCommand extends Command {

        public OverCommand(final Location origin) {
            super(origin);
        }

        @Override
        public <T> T accept(final Visitor<T> visitor) {
            return visitor.visitOver(this);
        }

    }

    /**
     * Rolls x items n times to the left
     * <p>
     * Takes two arguments: amount of items to roll and amount of times to perform the operation
     * <p>
     * Stack: [X, N, A, B, C, D] / X = 3, N = 1
     * <p>
     * [X, N, A, B, C, D] -> [B, C, A, D]
     * <p>
     * X and N were consumed by the command
     *
     * <pre>
     *     1 5 lroll  # Roll 5 topmost items 1 time to the left
     * </pre>
     */
    public static class RollLeftCommand extends Command {

        public RollLeftCommand(final Location origin) {
            super(origin);
        }

        @Override
        public <T> T accept(final Visitor<T> visitor) {
            return visitor.visitRollLeft(this);
        }

    }

    /**
     * Rolls x items n times to the right
     * <p>
     * Takes two arguments: amount of items to roll and amount of times to perform the operation
     * <p>
     * Stack: [X, N, A, B, C, D] / X = 3, N = 1
     * <p>
     * [X, N, A, B, C, D] -> [C, A, B, D]
     * <p>
     * X and N were consumed by the command
     *
     * <pre>
     *     1 3 rroll  # Roll 3 topmost items 1 time to the right
     * </pre>
     */
    public static class RollRightCommand extends Command {

        public RollRightCommand(final Location origin) {
            super(origin);
        }

        @Override
        public <T> T accept(final Visitor<T> visitor) {
            return visitor.visitRollRight(this);
        }

    }

    /**
     * Calls a declared routine
     *
     * <pre>
     *     .my_routine
     * </pre>
     */
    public static class RoutineCallCommand extends Command {

        private final String routineName;

        public RoutineCallCommand(final Location origin, final String routineName) {
            super(origin);
            this.routineName = routineName;
        }

        @Override
        public <T> T accept(final Visitor<T> visitor) {
            return visitor.visitRoutineCall(this);
        }

        public String getRoutineName() {
            return this.routineName;
        }

    }

    /**
     * Declares a routine
     *
     * <pre>
     *     rt my_routine
     *       # CODE
     *     end
     *
     *     # Not accessible by scripts that import this script
     *     rt _my_internal_routine
     *       # CODE
     *     end
     * </pre>
     */
    public static class RoutineDeclareCommand extends Command {

        private final String routineName;
        private final List<Command> routineBody;

        public RoutineDeclareCommand(final Location origin, final String routineName, final List<Command> routineBody) {
            super(origin);
            this.routineName = routineName;
            this.routineBody = routineBody;
        }

        @Override
        public <T> T accept(final Visitor<T> visitor) {
            return visitor.visitRoutineDeclare(this);
        }

        public String getRoutineName() {
            return this.routineName;
        }

        public List<Command> getRoutineBody() {
            return this.routineBody;
        }

    }

    /**
     * Ends a code block
     *
     * <pre>
     *     end
     * </pre>
     */
    public static class EndCommand extends Command {

        public EndCommand(final Location origin) {
            super(origin);
        }

        @Override
        public <T> T accept(final Visitor<T> visitor) {
            return visitor.visitEnd(this);
        }

    }

    /**
     * Pushes the amount of items to the top of the stack
     * <p>
     * [A, B, C, D] -> [4, A, B, C, D] / 4 was the size of the stack at the time of calling the command
     *
     * <pre>
     *     ssize
     * </pre>
     */
    public static class StackSizeCommand extends Command {

        public StackSizeCommand(final Location origin) {
            super(origin);
        }

        @Override
        public <T> T accept(final Visitor<T> visitor) {
            return visitor.visitStackSize(this);
        }

    }

    /**
     * Performs an add operation on the two topmost items on the stack
     * <p>
     * [A, B, C, D] -> [A+B, C, D]
     *
     * <pre>
     *     15 8 +  # 8 + 15 = 23
     * </pre>
     */
    public static class PlusCommand extends Command {

        public PlusCommand(final Location origin) {
            super(origin);
        }

        @Override
        public <T> T accept(final Visitor<T> visitor) {
            return visitor.visitPlus(this);
        }

    }

    /**
     * Performs a subtract operation on the two topmost items on the stack
     * <p>
     * [A, B, C, D] -> [A-B, C, D]
     *
     * <pre>
     *     20 15 -  # 15 - 20 = -5
     * </pre>
     */
    public static class MinusCommand extends Command {

        public MinusCommand(final Location origin) {
            super(origin);
        }

        @Override
        public <T> T accept(final Visitor<T> visitor) {
            return visitor.visitMinus(this);
        }

    }

    /**
     * Performs a divide operation on the two topmost items on the stack
     * <p>
     * [A, B, C, D] -> [A/B, C, D]
     *
     * <pre>
     *     3 9 /  # 9 / 3 = 3
     * </pre>
     */
    public static class DivideCommand extends Command {

        public DivideCommand(final Location origin) {
            super(origin);
        }

        @Override
        public <T> T accept(final Visitor<T> visitor) {
            return visitor.visitDivide(this);
        }

    }

    /**
     * Performs a multiply operation on the two topmost items on the stack
     * <p>
     * [A, B, C, D] -> [A*B, C, D]
     *
     * <pre>
     *     2 5 *  # 5 * 2 = 10
     * </pre>
     */
    public static class MultiplyCommand extends Command {

        public MultiplyCommand(final Location origin) {
            super(origin);
        }

        @Override
        public <T> T accept(final Visitor<T> visitor) {
            return visitor.visitMultiply(this);
        }

    }

    /**
     * Performs a modulo operation on the two topmost items on the stack
     * <p>
     * [A, B, C, D] -> [A%B, C, D]
     *
     * <pre>
     *     2 5 %  # 5 % 2 = 1
     * </pre>
     */
    public static class ModuloCommand extends Command {

        public ModuloCommand(final Location origin) {
            super(origin);
        }

        @Override
        public <T> T accept(final Visitor<T> visitor) {
            return visitor.visitModulo(this);
        }

    }

    /**
     * Performs an and (&) operation on the two topmost items on the stack
     * <p>
     * [A, B, C, D] -> [A&B, C, D]
     *
     * <pre>
     *     1 3 &  # 3 & 1 = 1
     * </pre>
     */
    public static class AndCommand extends Command {

        public AndCommand(final Location origin) {
            super(origin);
        }

        @Override
        public <T> T accept(final Visitor<T> visitor) {
            return visitor.visitAnd(this);
        }

    }

    /**
     * Performs an or (|) operation on the two topmost items on the stack
     * <p>
     * [A, B, C, D] -> [A|B, C, D]
     *
     * <pre>
     *     2 5 |  # 5 | 2 = 7
     * </pre>
     */
    public static class OrCommand extends Command {

        public OrCommand(final Location origin) {
            super(origin);
        }

        @Override
        public <T> T accept(final Visitor<T> visitor) {
            return visitor.visitOr(this);
        }

    }

    /**
     * Performs a xor (^) operation on the two topmost items on the stack
     * <p>
     * [A, B, C, D] -> [A^B, C, D]
     *
     * <pre>
     *     2 3 ^  # 3 ^ 2 = 1
     * </pre>
     */
    public static class XorCommand extends Command {

        public XorCommand(final Location origin) {
            super(origin);
        }

        @Override
        public <T> T accept(final Visitor<T> visitor) {
            return visitor.visitXor(this);
        }

    }

    /**
     * Performs a flip (~) operation on the two topmost items on the stack
     * <p>
     * [A, B, C, D] -> [A~B, C, D]
     *
     * <pre>
     *     2 ~  # ~2 = -3
     * </pre>
     */
    public static class FlipCommand extends Command {

        public FlipCommand(final Location origin) {
            super(origin);
        }

        @Override
        public <T> T accept(final Visitor<T> visitor) {
            return visitor.visitFlip(this);
        }

    }

    /**
     * Calls a native function
     *
     * <pre>
     *     native_write
     *
     *     native_stack_debug
     * </pre>
     */
    public static class NativeCallCommand extends Command {

        private final Type type;

        public NativeCallCommand(final Location origin, final Type type) {
            super(origin);
            this.type = type;
        }

        @Override
        public <T> T accept(final Visitor<T> visitor) {
            return visitor.visitNativeCall(this);
        }

        public Type getType() {
            return this.type;
        }

        public enum Type {
            WRITE,
            READ,
            OPEN,
            CLOSE,
            STACK_DEBUG,
            TIME
        }

    }

    /**
     * Branches the code depending on the topmost item of the stack
     *
     * <pre>
     *     x = peek_topmost_stack_item()
     *     ifn: if (x != 0)
     *     ifz: if (x == 0)
     *     iflt: if (x <= 0)
     *     ifgt: if (x >= 0)
     * </pre>
     *
     * <pre>
     *     ifn
     *       # CODE
     *     end
     *
     *     ifz
     *       # CODE
     *     end
     *
     *     iflt
     *       # CODE
     *     end
     *
     *     ifgt
     *       # CODE
     *     end
     * </pre>
     */
    public static class IfCommand extends Command {

        private final List<Command> ifBody;
        private final List<Command> elseBody;
        private final Type type;

        public IfCommand(final Location origin, final List<Command> ifBody, final List<Command> elseBody, final Type type) {
            super(origin);
            this.ifBody = ifBody;
            this.elseBody = elseBody;
            this.type = type;
        }

        @Override
        public <T> T accept(final Visitor<T> visitor) {
            return visitor.visitIf(this);
        }

        public List<Command> getIfBody() {
            return this.ifBody;
        }

        public List<Command> getElseBody() {
            return this.elseBody;
        }

        public Type getType() {
            return this.type;
        }

        public enum Type {
            IFN,
            IFZ,
            IFGT,
            IFLT
        }

    }

    /**
     * Imports another script
     *
     * <pre>
     *     import "path/to/script"         # Imported as 'script'
     *     import "another/script" as sc   # Imported as 'sc'
     * </pre>
     */
    public static class ImportCommand extends Command {

        private final String importPath;
        private final String importName;

        public ImportCommand(final Location origin, final String importPath, final String importName) {
            super(origin);
            this.importPath = importPath;
            this.importName = importName;
        }

        @Override
        public <T> T accept(final Visitor<T> visitor) {
            return visitor.visitImport(this);
        }

        public String getImportPath() {
            return this.importPath;
        }

        public String getImportName() {
            return this.importName;
        }

    }

    /**
     * Calls a routine of an imported script
     *
     * <pre>
     *     import "path/to/script" as sc
     *     :sc.my_cool_routine
     * </pre>
     */
    public static class ImportCallCommand extends Command {

        private final String importName;
        private final String routineName;

        public ImportCallCommand(final Location origin, final String importName, final String routineName) {
            super(origin);
            this.importName = importName;
            this.routineName = routineName;
        }

        @Override
        public <T> T accept(final Visitor<T> visitor) {
            return visitor.visitImportCall(this);
        }

        public String getImportName() {
            return this.importName;
        }

        public String getRoutineName() {
            return this.routineName;
        }

    }

    /**
     * Runs a set of commands while the topmost item of the stack matches the condition
     *
     * <pre>
     *     x = peek_topmost_stack_item()
     *     while: while (x != 0)
     *     until: while (x == 0)
     * </pre>
     *
     * <pre>
     *     while
     *       # CODE
     *     end
     * </pre>
     */
    public static class LoopCommand extends Command {

        private final Type type;
        private final List<Command> body;

        public LoopCommand(final Location origin, final Type type, final List<Command> body) {
            super(origin);
            this.type = type;
            this.body = body;
        }

        @Override
        public <T> T accept(final Visitor<T> visitor) {
            return visitor.visitLoop(this);
        }

        public Type getType() {
            return this.type;
        }

        public List<Command> getBody() {
            return this.body;
        }

        public enum Type {
            WHILE,
            UNTIL
        }

    }

    /**
     * Annotates a routine
     * <p>
     * Annotation have no real purpose for compilers or interpreters, they are mainly used by analysers or doc generators
     *
     * <pre>
     *     [ key1=value key2="value in quotes" key3={ a=1, b=2, c={ } } ]
     *     rt my_routine ....
     * </pre>
     */
    public static class RoutineAnnotationCommand extends Command {

        private final String routineName;
        private final Map<String, Object> elements;

        public RoutineAnnotationCommand(final Location origin, final String routineName, final Map<String, Object> elements) {
            super(origin);
            this.routineName = routineName;
            this.elements = elements;
        }

        @Override
        public <T> T accept(final Visitor<T> visitor) {
            return visitor.visitRoutineAnnotation(this);
        }

        public String getRoutineName() {
            return this.routineName;
        }

        public Map<String, Object> getElements() {
            return this.elements;
        }

    }

}
