package dev.cerus.edina.edinaj.compiler.step.init;

import dev.cerus.edina.edinaj.compiler.CompilerSettings;
import dev.cerus.edina.edinaj.compiler.step.CompilerStep;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import static org.objectweb.asm.Opcodes.*;

public class ClassStep implements CompilerStep<Void> {

    @Override
    public void compile(final ClassWriter cw, final CompilerSettings settings, final Void obj) {
        cw.visit(
                V1_8,
                ACC_PUBLIC | ACC_SUPER,
                settings.getMainClassName(),
                null,
                "java/lang/Object",
                null
        );

        final String[] split = settings.getMainClassName().split("/");
        final String name = split[split.length - 1] + ".java";

        cw.visitSource(name, null);
        //cw.visitNestMember(settings.getLauncherName() + "$EdinaJ");
        //cw.visitInnerClass(settings.getLauncherName() + "$EdinaJ", settings.getLauncherName(), "EdinaJ", ACC_PRIVATE | ACC_STATIC | ACC_ANNOTATION | ACC_ABSTRACT | ACC_INTERFACE);
        //cw.visitInnerClass("java/lang/invoke/MethodHandles$Lookup", "java/lang/invoke/MethodHandles", "Lookup", ACC_PUBLIC | ACC_FINAL | ACC_STATIC);

        FieldVisitor fieldVisitor;
        {
            fieldVisitor = cw.visitField(ACC_PRIVATE | ACC_FINAL, "stack", "L" + settings.getStackName() + ";", null, null);
            fieldVisitor.visitEnd();
        }
        {
            fieldVisitor = cw.visitField(ACC_PRIVATE | ACC_FINAL, "natives", "L" + settings.getNativesName() + ";", null, null);
            fieldVisitor.visitEnd();
        }
    }
}
