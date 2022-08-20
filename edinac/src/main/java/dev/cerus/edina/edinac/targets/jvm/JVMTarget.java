package dev.cerus.edina.edinac.targets.jvm;

import dev.cerus.edina.ast.ast.Command;
import dev.cerus.edina.edinac.targets.CompilationTarget;
import java.util.List;

public class JVMTarget extends CompilationTarget<JVMFlavor, JVMOptions> {

    private static final JVMOptions OPTIONS = new JVMOptions();

    @Override
    public void compile(final List<Command> ast) {
        System.out.println("JVM Compile " + OPTIONS.getFlavor());
    }

    @Override
    public JVMFlavor[] getFlavors() {
        return JVMFlavor.values();
    }

    @Override
    public JVMFlavor getDefaultFlavor() {
        return JVMFlavor.JAVA_8;
    }

    @Override
    public JVMOptions getOptions() {
        return OPTIONS;
    }

}
