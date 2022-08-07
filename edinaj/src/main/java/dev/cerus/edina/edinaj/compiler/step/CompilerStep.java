package dev.cerus.edina.edinaj.compiler.step;

import dev.cerus.edina.edinaj.compiler.CompilerSettings;
import dev.cerus.edina.edinaj.compiler.step.classes.ClassLauncherStep;
import dev.cerus.edina.edinaj.compiler.step.classes.ClassNativesStep;
import dev.cerus.edina.edinaj.compiler.step.classes.ClassStackStep;
import dev.cerus.edina.edinaj.compiler.step.init.ClassStep;
import org.objectweb.asm.ClassWriter;

/**
 * Represents a step in the compilation process
 *
 * @param <T> The data that is needed for this step
 */
public interface CompilerStep<T> {

    static CompilerStep<?>[] appSteps() {
        return new CompilerStep[] {
                new ClassStep()
        };
    }

    static CompilerStep<?>[] launcherSteps() {
        return new CompilerStep[] {
                new ClassLauncherStep()
        };
    }

    static CompilerStep<?>[] nativesSteps() {
        return new CompilerStep[] {
                new ClassNativesStep()
        };
    }

    static CompilerStep<?>[] stackSteps() {
        return new CompilerStep[] {
                new ClassStackStep()
        };
    }

    static void compileAll(final ClassWriter cw, final CompilerSettings settings, final CompilerStep<?>... steps) {
        for (final CompilerStep<?> step : steps) {
            step.compile(cw, settings, null);
        }
    }

    /**
     * Run this compilation step
     *
     * @param cw       The class writer
     * @param settings The compiler settings
     * @param obj      The data used by this step
     */
    void compile(ClassWriter cw, CompilerSettings settings, T obj);

}
