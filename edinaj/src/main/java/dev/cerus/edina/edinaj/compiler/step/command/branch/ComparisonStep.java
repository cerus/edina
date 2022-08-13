package dev.cerus.edina.edinaj.compiler.step.command.branch;

import dev.cerus.edina.ast.ast.Command;
import dev.cerus.edina.edinaj.compiler.CompilerSettings;
import dev.cerus.edina.edinaj.compiler.StepData;
import dev.cerus.edina.edinaj.compiler.step.CompilerStep;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import static org.objectweb.asm.Opcodes.*;

public class ComparisonStep implements CompilerStep<StepData<Command.ComparisonCommand>> {

    @Override
    public void compile(final ClassWriter cw, final CompilerSettings settings, final StepData<Command.ComparisonCommand> obj) {
        final MethodVisitor methodVisitor = obj.methodVisitor();

        final Label label1 = new Label();
        methodVisitor.visitLabel(label1);
        methodVisitor.visitLineNumber(20, label1);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, settings.getMainClassName(), "stack", "L" + settings.getStackName() + ";");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, settings.getMainClassName(), "stack", "L" + settings.getStackName() + ";");
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, settings.getStackName(), "peekLong", "()J", false);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, settings.getMainClassName(), "stack", "L" + settings.getStackName() + ";");
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, settings.getStackName(), "peekSecondLong", "()J", false);
        methodVisitor.visitInsn(LCMP);
        final Label label2 = new Label();
        methodVisitor.visitJumpInsn(switch (obj.data().getType()) {
            case EQUALS -> IFNE;
            case NOT_EQUALS -> IFEQ;
            case GREATER_THAN -> IFLE;
            case GREATER_THAN_EQUALS -> IFLT;
            case LESSER_THAN -> IFGE;
            case LESSER_THAN_EQUALS -> IFGT;
        }, label2);
        methodVisitor.visitInsn(ICONST_1);
        final Label label3 = new Label();
        methodVisitor.visitJumpInsn(GOTO, label3);
        methodVisitor.visitLabel(label2);
        methodVisitor.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {settings.getStackName()});
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitLabel(label3);
        methodVisitor.visitFrame(Opcodes.F_FULL, 1, new Object[] {settings.getMainClassName()}, 2, new Object[] {settings.getStackName(), Opcodes.INTEGER});
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, settings.getStackName(), "pushBool", "(Z)V", false);
    }

}
