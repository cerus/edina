package dev.cerus.edina.edinac.targets.fasm;

import com.beust.jcommander.Parameters;
import dev.cerus.edina.edinac.targets.Options;

@Parameters(commandDescription = "fasm compilation target")
public class FASMOptions extends Options<FASMFlavor, FASMTarget> {

    @Override
    public String getCommandName() {
        return "fasm";
    }

    @Override
    protected Class<FASMTarget> targetClass() {
        return FASMTarget.class;
    }

    @Override
    protected Class<FASMFlavor> flavorEnumClass() {
        return FASMFlavor.class;
    }

}
