package dev.cerus.edina.edinaj.compiler;

import dev.cerus.edina.ast.ast.Command;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CompilerEnv {

    private final Map<String, byte[]> classMap = new HashMap<>();
    private final Map<String, Import> importMap = new HashMap<>();
    private final Map<String, Import> fileImportMap = new HashMap<>();
    private final Map<String, Command.RoutineDeclareCommand> routineMap = new HashMap<>();
    private final Set<String> usedRoutines = new HashSet<>();

    public void addClass(final String name, final byte[] cls) {
        this.classMap.put(name, cls);
    }

    public void addImport(final String filePath, final Import imprt) {
        this.importMap.put(imprt.name, imprt);
        this.fileImportMap.put(filePath, imprt);
    }

    public void addRoutine(final Command.RoutineDeclareCommand cmd) {
        this.routineMap.put(cmd.getRoutineName(), cmd);
    }

    public void markUse(final String rtName) {
        if (!this.usedRoutines.contains(rtName)) {
            this.usedRoutines.add(rtName);
        }
    }

    public byte[] getClassByName(final String name) {
        return this.classMap.get(name);
    }

    public Import getImportByName(final String name) {
        return this.importMap.get(name);
    }

    public Import getImportByPath(final String path) {
        return this.fileImportMap.get(path);
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

    public Map<String, String> getJavaPathToImportNameMap() {
        return this.importMap.entrySet().stream()
                .map(e -> Map.entry(
                        e.getValue().path,
                        e.getKey()
                ))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, byte[]> getClassMap() {
        return Map.copyOf(this.classMap);
    }

    public record Import(String name, String path, Compiler compiler) {
    }

}
