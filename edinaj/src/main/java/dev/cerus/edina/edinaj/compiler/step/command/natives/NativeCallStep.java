package dev.cerus.edina.edinaj.compiler.step.command.natives;

import dev.cerus.edina.ast.ast.Command;
import dev.cerus.edina.edinaj.compiler.CompilerSettings;
import dev.cerus.edina.edinaj.compiler.StepData;
import dev.cerus.edina.edinaj.compiler.step.CompilerStep;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;

public class NativeCallStep implements CompilerStep<StepData<Command.NativeCallCommand>> {

    @Override
    public void compile(final ClassWriter cw, final CompilerSettings settings, final StepData<Command.NativeCallCommand> obj) {
        final MethodVisitor methodVisitor = obj.methodVisitor();

        final Label label0 = new Label();
        methodVisitor.visitLabel(label0);
        methodVisitor.visitLineNumber(obj.data().getOrigin().fromLineNum(), label0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, settings.getMainClassName(), "natives", "L" + settings.getNativesName() + ";");
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, settings.getNativesName(), obj.data().getType().name().toLowerCase(), "()V", false);
    }

}
