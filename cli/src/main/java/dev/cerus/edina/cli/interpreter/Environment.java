package dev.cerus.edina.cli.interpreter;

import dev.cerus.edina.ast.ast.Command;
import dev.cerus.edina.edinaj.asm.Stack;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Environment {

    private final Stack stack = new Stack();
    private final Map<String, Command.RoutineDeclareCommand> routineMap = new HashMap<>();

    public void declareRoutine(final Command.RoutineDeclareCommand cmd) {
        this.routineMap.put(cmd.getRoutineName(), cmd);
    }

    public List<Command> getRoutine(final String name) {
        return this.routineMap.get(name).getRoutineBody();
    }

    public Stack getStack() {
        return stack;
    }

}
