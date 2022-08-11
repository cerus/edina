package dev.cerus.edina.edinaj.compiler;

import dev.cerus.edina.edinaj.Launcher;
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
    private final Collection<Launcher.Options.Optimization> optimizations;
    private final String className;
    private final String originalPackage;

    public CompilerSettings(final String packageName,
                            final boolean debug,
                            final boolean quiet,
                            final Collection<File> inclusions,
                            final Collection<Launcher.Options.Optimization> optimizations) {
        this(packageName, debug, quiet, inclusions, optimizations, null, null);
    }

    public CompilerSettings(final String packageName,
                            final boolean debug,
                            final boolean quiet,
                            final Collection<File> inclusions,
                            final Collection<Launcher.Options.Optimization> optimizations,
                            final String className,
                            final String originalPackage) {
        this.packageName = packageName.replace(".", "/");
        this.debug = debug;
        this.quiet = quiet;
        this.inclusions = inclusions;
        this.optimizations = optimizations;
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

    public Collection<Launcher.Options.Optimization> getOptimizations() {
        return this.optimizations;
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

    public boolean optimizationEnabled(final Launcher.Options.Optimization opt) {
        return this.optimizations.contains(opt);
    }

}
