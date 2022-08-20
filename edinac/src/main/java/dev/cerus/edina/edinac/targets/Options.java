package dev.cerus.edina.edinac.targets;

import com.beust.jcommander.Parameter;
import java.io.File;
import java.util.List;

public abstract class Options<F extends Enum<F>, T extends CompilationTarget<F, ?>> {

    @Parameter(
            order = 2,
            names = {"-S", "--input", "--source"},
            description = "Input file name",
            required = true
    )
    public String inputFile;

    @Parameter(
            order = 3,
            names = {"-O", "--output"},
            description = "Output file name",
            required = true
    )
    public String outputFile;

    @Parameter(
            order = 4,
            names = {"--include", "-I"},
            description = "Directories that will be searched for imports"
    )
    public List<String> include = List.of();

    @Parameter(
            order = 1,
            names = {"-F", "--flavour"},
            description = "Compilation flavor",
            required = true
    )
    private String flavor;

    private List<File> includedFiles;

    public F getFlavor() {
        return Enum.valueOf(this.flavorEnumClass(), this.flavor);
    }

    public List<File> getIncludedFiles() {
        return this.includedFiles;
    }

    public void setIncludedFiles(final List<File> includedFiles) {
        this.includedFiles = includedFiles;
    }

    public abstract String getCommandName();

    protected abstract Class<T> targetClass();

    protected abstract Class<F> flavorEnumClass();

}
