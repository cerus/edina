package dev.cerus.edina.edinac.targets;

import dev.cerus.edina.edinac.targets.fasm.FASMTarget;
import dev.cerus.edina.edinac.targets.jvm.JVMTarget;
import java.util.List;

public class CompilationTargets {

    private static final List<CompilationTarget<?, ?>> TARGETS = List.of(
            new JVMTarget(),
            new FASMTarget()
    );

    private CompilationTargets() {
        throw new UnsupportedOperationException();
    }

    public static List<CompilationTarget<?, ?>> getTargets() {
        return TARGETS;
    }

}
