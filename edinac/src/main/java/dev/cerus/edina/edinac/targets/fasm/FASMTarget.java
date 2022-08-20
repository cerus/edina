package dev.cerus.edina.edinac.targets.fasm;

import dev.cerus.edina.ast.ast.Command;
import dev.cerus.edina.edinac.targets.CompilationTarget;
import java.util.List;

public class FASMTarget extends CompilationTarget<FASMFlavor, FASMOptions> {

    private static final FASMOptions OPTIONS = new FASMOptions();

    @Override
    public void compile(final List<Command> ast) {
        System.out.println("FASM Compile");
    }

    @Override
    public FASMFlavor[] getFlavors() {
        return FASMFlavor.values();
    }

    @Override
    public FASMFlavor getDefaultFlavor() {
        return FASMFlavor.X86_LINUX;
    }

    @Override
    public FASMOptions getOptions() {
        return OPTIONS;
    }

}
