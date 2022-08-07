package dev.cerus.edina.edinaj.compiler;

import java.io.File;
import java.util.Collection;

/**
 * Handles the settings for the compiler
 */
public class CompilerSettings {

    private final String packageName;
    private final boolean debug;
    private final boolean quiet;
    private final Collection<File> inclusions;
    private final String className;
    private final String originalPackage;

    public CompilerSettings(final String packageName,
                            final boolean debug,
                            final boolean quiet,
                            final Collection<File> inclusions) {
        this(packageName, debug, quiet, inclusions, null, null);
    }

    public CompilerSettings(final String packageName,
                            final boolean debug,
                            final boolean quiet,
                            final Collection<File> inclusions,
                            final String className,
                            final String originalPackage) {
        this.packageName = packageName.replace(".", "/");
        this.debug = debug;
        this.quiet = quiet;
        this.inclusions = inclusions;
        this.className = className == null ? null : this.packageName + "/" + className;
        this.originalPackage = originalPackage;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public boolean isDebug() {
        return this.debug;
    }

    public boolean isQuiet() {
        return this.quiet;
    }

    public Collection<File> getInclusions() {
        return this.inclusions;
    }

    public String getMainClassName() {
        return this.className == null ? this.getAppName() : this.className;
    }

    public boolean isMain() {
        return this.className == null;
    }

    public String getAppName() {
        return this.getPackageName() + "/App";
    }

    public String getStackName() {
        return (this.isMain() ? this.getPackageName() : this.originalPackage) + "/Stack";
    }

    public String getAppLauncherName() {
        return this.getPackageName() + "/Launcher";
    }

    public String getNativesName() {
        return (this.isMain() ? this.getPackageName() : this.originalPackage) + "/Natives";
    }

    public String getOriginalPackage() {
        return this.originalPackage;
    }

}
