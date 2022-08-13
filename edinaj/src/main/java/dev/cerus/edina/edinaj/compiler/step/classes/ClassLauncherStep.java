package dev.cerus.edina.edinaj.compiler.step.classes;

import dev.cerus.edina.edinaj.compiler.CompilerSettings;
import dev.cerus.edina.edinaj.compiler.step.CompilerStep;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import static org.objectweb.asm.Opcodes.*;

/**
 * Creates the bootstrapper class. Do not modify, this class will be automatically regenerated by scripts.
 */
public class ClassLauncherStep implements CompilerStep<Void> {

    @Override
    public void compile(final ClassWriter classWriter, final CompilerSettings settings, final Void obj) {
        MethodVisitor methodVisitor;

classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER, settings.getAppLauncherName(), null, "java/lang/Object", null);

classWriter.visitSource("Launcher.java", null);

{
methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
methodVisitor.visitCode();
Label label0 = new Label();
methodVisitor.visitLabel(label0);
methodVisitor.visitLineNumber(10, label0);
methodVisitor.visitVarInsn(ALOAD, 0);
methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
methodVisitor.visitInsn(RETURN);
Label label1 = new Label();
methodVisitor.visitLabel(label1);
methodVisitor.visitLocalVariable("this", "L" + settings.getAppLauncherName() + ";", null, label0, label1, 0);
methodVisitor.visitMaxs(1, 1);
methodVisitor.visitEnd();
}
{
methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
methodVisitor.visitCode();
Label label0 = new Label();
Label label1 = new Label();
Label label2 = new Label();
methodVisitor.visitTryCatchBlock(label0, label1, label2, "java/io/IOException");
Label label3 = new Label();
methodVisitor.visitLabel(label3);
methodVisitor.visitLineNumber(13, label3);
methodVisitor.visitTypeInsn(NEW, settings.getStackName());
methodVisitor.visitInsn(DUP);
methodVisitor.visitMethodInsn(INVOKESPECIAL, settings.getStackName(), "<init>", "()V", false);
methodVisitor.visitVarInsn(ASTORE, 1);
Label label4 = new Label();
methodVisitor.visitLabel(label4);
methodVisitor.visitLineNumber(14, label4);
methodVisitor.visitTypeInsn(NEW, "" + settings.getNativesName() + "");
methodVisitor.visitInsn(DUP);
methodVisitor.visitVarInsn(ALOAD, 1);
methodVisitor.visitMethodInsn(INVOKESPECIAL, "" + settings.getNativesName() + "", "<init>", "(L" + settings.getStackName() + ";)V", false);
methodVisitor.visitVarInsn(ASTORE, 2);
Label label5 = new Label();
methodVisitor.visitLabel(label5);
methodVisitor.visitLineNumber(15, label5);
methodVisitor.visitVarInsn(ALOAD, 2);
methodVisitor.visitInsn(settings.isRestricted() ? ICONST_1 : ICONST_0);
methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "" + settings.getNativesName() + "", "setRestricted", "(Z)V", false);
Label label6 = new Label();
methodVisitor.visitLabel(label6);
methodVisitor.visitLineNumber(16, label6);
methodVisitor.visitTypeInsn(NEW, settings.getAppName());
methodVisitor.visitInsn(DUP);
methodVisitor.visitVarInsn(ALOAD, 1);
methodVisitor.visitVarInsn(ALOAD, 2);
methodVisitor.visitMethodInsn(INVOKESPECIAL, settings.getAppName(), "<init>", "(L" + settings.getStackName() + ";L" + settings.getNativesName() + ";)V", false);
methodVisitor.visitVarInsn(ASTORE, 3);
Label label7 = new Label();
methodVisitor.visitLabel(label7);
methodVisitor.visitLineNumber(17, label7);
methodVisitor.visitVarInsn(ALOAD, 3);
methodVisitor.visitMethodInsn(INVOKEVIRTUAL, settings.getAppName(), "run", "()V", false);
methodVisitor.visitLabel(label0);
methodVisitor.visitLineNumber(19, label0);
methodVisitor.visitVarInsn(ALOAD, 2);
methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "" + settings.getNativesName() + "", "closeAll", "()V", false);
methodVisitor.visitLabel(label1);
methodVisitor.visitLineNumber(22, label1);
Label label8 = new Label();
methodVisitor.visitJumpInsn(GOTO, label8);
methodVisitor.visitLabel(label2);
methodVisitor.visitLineNumber(20, label2);
methodVisitor.visitFrame(Opcodes.F_FULL, 4, new Object[] {"[Ljava/lang/String;", settings.getStackName(), "" + settings.getNativesName() + "", settings.getAppName()}, 1, new Object[] {"java/io/IOException"});
methodVisitor.visitVarInsn(ASTORE, 4);
Label label9 = new Label();
methodVisitor.visitLabel(label9);
methodVisitor.visitLineNumber(21, label9);
methodVisitor.visitTypeInsn(NEW, "java/lang/RuntimeException");
methodVisitor.visitInsn(DUP);
methodVisitor.visitVarInsn(ALOAD, 4);
methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/Throwable;)V", false);
methodVisitor.visitInsn(ATHROW);
methodVisitor.visitLabel(label8);
methodVisitor.visitLineNumber(23, label8);
methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
methodVisitor.visitInsn(RETURN);
Label label10 = new Label();
methodVisitor.visitLabel(label10);
methodVisitor.visitLocalVariable("e", "Ljava/io/IOException;", null, label9, label8, 4);
methodVisitor.visitLocalVariable("args", "[Ljava/lang/String;", null, label3, label10, 0);
methodVisitor.visitLocalVariable("stack", "L" + settings.getStackName() + ";", null, label4, label10, 1);
methodVisitor.visitLocalVariable("natives", "L" + settings.getNativesName() + ";", null, label5, label10, 2);
methodVisitor.visitLocalVariable("app", "L" + settings.getAppName() + ";", null, label7, label10, 3);
methodVisitor.visitMaxs(4, 5);
methodVisitor.visitEnd();
}
classWriter.visitEnd();
    }

}
