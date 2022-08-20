package dev.cerus.edina.edinac.targets.jvm;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import dev.cerus.edina.edinac.targets.Options;

@Parameters(commandDescription = "jvm compilation target")
public class JVMOptions extends Options<JVMFlavor, JVMTarget> {

    @Parameter(
            order = 5,
            names = {"-D", "--debug"},
            description = "Enable debug output in the final jar"
    )
    public boolean debug;

    @Override
    public String getCommandName() {
        return "jvm";
    }

    @Override
    protected Class<JVMTarget> targetClass() {
        return JVMTarget.class;
    }

    @Override
    protected Class<JVMFlavor> flavorEnumClass() {
        return JVMFlavor.class;
    }

}
