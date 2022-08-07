package dev.cerus.edina.edinaj.compiler.step.init;

import dev.cerus.edina.edinaj.compiler.CompilerSettings;
import dev.cerus.edina.edinaj.compiler.StepData;
import dev.cerus.edina.edinaj.compiler.step.CompilerStep;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;

public class ConstructorStep implements CompilerStep<StepData<Void>> {

    @Override
    public void compile(final ClassWriter cw, final CompilerSettings settings, final StepData<Void> obj) {
        final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(L" + settings.getStackName() + ";L" + settings.getNativesName() + ";)V", null, null);
        mv.visitCode();
        final Label label0 = new Label();
        mv.visitLabel(label0);
        mv.visitLineNumber(8, label0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        final Label label1 = new Label();
        mv.visitLabel(label1);
        mv.visitLineNumber(9, label1);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitFieldInsn(PUTFIELD, settings.getMainClassName(), "stack", "L" + settings.getStackName() + ";");
        final Label label2 = new Label();
        mv.visitLabel(label2);
        mv.visitLineNumber(10, label2);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitFieldInsn(PUTFIELD, settings.getMainClassName(), "natives", "L" + settings.getNativesName() + ";");

        List<String> refs = new ArrayList<>(obj.compiler().getReferencedClasses().keySet());
        refs.addAll(obj.compiler().getReferenceNames().keySet());
        refs = refs.stream().distinct().collect(Collectors.toList());
        for (final String ref : refs) {
            final String refName = obj.compiler().getReferenceNames().get(ref);
            if (refName == null) {
                continue;
            }

            final FieldVisitor fv = cw.visitField(
                    ACC_PRIVATE | ACC_FINAL,
                    "imp_" + refName,
                    "L" + ref + ";",
                    null,
                    null
            );
            fv.visitEnd();

            final Label label = new Label();
            mv.visitLabel(label);
            mv.visitLineNumber(999, label);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitTypeInsn(NEW, ref);
            mv.visitInsn(DUP);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKESPECIAL, ref, "<init>", "(L" + settings.getStackName() + ";L" + settings.getNativesName() + ";)V", false);
            mv.visitFieldInsn(PUTFIELD, settings.getMainClassName(), "imp_" + refName, "L" + ref + ";");
        }

        if (!settings.isMain()) {
            final Label labelCallRun = new Label();
            mv.visitLabel(labelCallRun);
            mv.visitLineNumber(777, labelCallRun);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, settings.getMainClassName(), "run", "()V", false);
        }

        final Label label3 = new Label();
        mv.visitLabel(label3);
        mv.visitLineNumber(11, label3);
        mv.visitInsn(RETURN);
        final Label label4 = new Label();
        mv.visitLabel(label4);
        mv.visitLocalVariable("this", "L" + settings.getMainClassName() + ";", null, label0, label4, 0);
        mv.visitLocalVariable("stack", "L" + settings.getStackName() + ";", null, label0, label4, 1);
        mv.visitLocalVariable("natives", "L" + settings.getNativesName() + ";", null, label0, label4, 2);

        mv.visitMaxs(2, 3);
        mv.visitEnd();
    }

}
