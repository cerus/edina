package dev.cerus.edina.edinaj.compiler.step.command.stack;

import dev.cerus.edina.ast.ast.Command;
import dev.cerus.edina.edinaj.compiler.CompilerSettings;
import dev.cerus.edina.edinaj.compiler.StepData;
import dev.cerus.edina.edinaj.compiler.step.CompilerStep;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;

public class PopStep implements CompilerStep<StepData<Command.PopCommand>> {

    @Override
    public void compile(final ClassWriter cw, final CompilerSettings settings, final StepData<Command.PopCommand> obj) {
        final MethodVisitor methodVisitor = obj.methodVisitor();

        final Label label0 = new Label();
        methodVisitor.visitLabel(label0);
        methodVisitor.visitLineNumber(obj.data().getOrigin().getLine(), label0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, settings.getMainClassName(), "stack", "L" + settings.getStackName() + ";");
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, settings.getStackName(), "pop", "()V", false);
    }

}
