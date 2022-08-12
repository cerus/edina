package dev.cerus.edina.edinaj.compiler;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * Generic data class for compiler steps
 *
 * @param classVisitor  Current class visitor
 * @param methodVisitor Current method visitor
 * @param compiler      Current compiler
 * @param settings      Compiler settings
 * @param data          The actual data
 * @param <T>           The type of the data
 */
public record StepData<T>(ClassVisitor classVisitor, MethodVisitor methodVisitor, CompilerBase compiler, CompilerSettings settings, T data) {

    public StepData(final ClassVisitor classVisitor, final MethodVisitor methodVisitor, final CompilerBase compiler, final CompilerSettings settings, final T data) {
        this.classVisitor = classVisitor;
        this.methodVisitor = methodVisitor;
        this.compiler = compiler;
        this.settings = settings;
        this.data = data;
    }

}
