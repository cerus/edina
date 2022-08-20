package dev.cerus.edina.edinac.targets;

import dev.cerus.edina.ast.ast.Command;
import java.util.List;

public abstract class CompilationTarget<F extends Enum<?>, O extends Options> {

    public abstract void compile(List<Command> ast);

    public abstract F[] getFlavors();

    public abstract F getDefaultFlavor();

    public abstract O getOptions();

}
