package dev.cerus.edina.edinaj.compiler.step.command.stack;

import dev.cerus.edina.ast.ast.Command;
import dev.cerus.edina.edinaj.compiler.CompilerSettings;
import dev.cerus.edina.edinaj.compiler.StepData;
import dev.cerus.edina.edinaj.compiler.step.CompilerStep;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;

public class PushStep implements CompilerStep<StepData<Command.PushCommand>> {

    @Override
    public void compile(final ClassWriter cw, final CompilerSettings settings, final StepData<Command.PushCommand> obj) {
        final MethodVisitor methodVisitor = obj.methodVisitor();
        final Object parsedValue = obj.data().getParsedValue();

        final Label label0 = new Label();
        methodVisitor.visitLabel(label0);
        methodVisitor.visitLineNumber(obj.data().getOrigin().fromLineNum(), label0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, settings.getMainClassName(), "stack", "L" + settings.getStackName() + ";");
        if (parsedValue instanceof Integer i) {
            this.compilePushInt(methodVisitor, i);
        } else if (parsedValue instanceof Double d) {
            this.compilePushDouble(methodVisitor, d);
        } else if (parsedValue instanceof Long l) {
            this.compilePushLong(methodVisitor, l);
        } else if (parsedValue instanceof String s) {
            this.compilePushString(methodVisitor, settings, s);
            return;
        } else {
            throw new UnsupportedOperationException("Can not compile push(" + parsedValue.getClass().getName() + ")");
        }
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, settings.getStackName(), "push", "(Ljava/lang/Object;)V", false);
    }

    private void compilePushInt(final MethodVisitor mv, final int i) {
        mv.visitIntInsn(BIPUSH, i);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
    }

    private void compilePushLong(final MethodVisitor mv, final long l) {
        mv.visitLdcInsn(l);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
    }

    private void compilePushDouble(final MethodVisitor mv, final double d) {
        mv.visitLdcInsn(d);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
    }

    private void compilePushString(final MethodVisitor mv, final CompilerSettings settings, final String s) {
        mv.visitLdcInsn(s);
        mv.visitMethodInsn(INVOKEVIRTUAL, settings.getStackName(), "pushString", "(Ljava/lang/String;)V", false);
    }

}
