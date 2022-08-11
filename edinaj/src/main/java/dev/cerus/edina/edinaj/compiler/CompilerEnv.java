package dev.cerus.edina.edinaj.compiler;

import dev.cerus.edina.ast.ast.Command;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CompilerEnv {

    private final Map<String, Command.RoutineDeclareCommand> routineMap = new HashMap<>();
    private final Set<String> usedRoutines = new HashSet<>();

    public void addRoutine(final Command.RoutineDeclareCommand cmd) {
        this.routineMap.put(cmd.getRoutineName(), cmd);
    }

    public void markUse(final String rtName) {
        if (!this.usedRoutines.contains(rtName)) {
            this.usedRoutines.add(rtName);
        }
    }

    public boolean isUsed(final String rtName) {
        return this.usedRoutines.contains(rtName);
    }

    public boolean routineExists(final String name) {
        return this.getRoutine(name) != null;
    }

    public Command.RoutineDeclareCommand getRoutine(final String name) {
        return this.routineMap.get(name);
    }

    public Collection<String> getRoutineNames() {
        return this.routineMap.keySet();
    }

}
