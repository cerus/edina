package dev.cerus.edina.edinaj.compiler.step.command.math;

import dev.cerus.edina.ast.ast.Command;
import dev.cerus.edina.edinaj.compiler.CompilerSettings;
import dev.cerus.edina.edinaj.compiler.StepData;
import dev.cerus.edina.edinaj.compiler.step.CompilerStep;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;

public class ArithmeticStep implements CompilerStep<StepData<Command>> {

    private final int opcode;

    public ArithmeticStep(final int opcode) {
        this.opcode = opcode;
    }

    @Override
    public void compile(final ClassWriter cw, final CompilerSettings settings, final StepData<Command> obj) {
        final MethodVisitor methodVisitor = obj.methodVisitor();

        final Label label = new Label();
        methodVisitor.visitLabel(label);
        methodVisitor.visitLineNumber(obj.data().getOrigin().getLine(), label);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, settings.getMainClassName(), "stack", "L" + settings.getStackName() + ";");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, settings.getMainClassName(), "stack", "L" + settings.getStackName() + ";");
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, settings.getStackName(), "popLong", "()J", false);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, settings.getMainClassName(), "stack", "L" + settings.getStackName() + ";");
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, settings.getStackName(), "popLong", "()J", false);
        methodVisitor.visitInsn(this.opcode);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, settings.getStackName(), "push", "(Ljava/lang/Object;)V", false);
    }

}
