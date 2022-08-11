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

public class IfStep implements CompilerStep<StepData<Command.IfCommand>> {

    @Override
    public void compile(final ClassWriter cw, final CompilerSettings settings, final StepData<Command.IfCommand> obj) {
        final MethodVisitor mv = obj.methodVisitor();

        final Label labelIfStart = new Label();
        mv.visitLabel(labelIfStart);
        mv.visitLineNumber(obj.data().getOrigin().fromLineNum(), labelIfStart);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, settings.getMainClassName(), "stack", "L" + settings.getStackName() + ";");
        mv.visitMethodInsn(INVOKEVIRTUAL, settings.getStackName(), "peekLong", "()J", false);
        mv.visitInsn(LCONST_0);
        mv.visitInsn(LCMP);

        final Label labelElse = new Label();
        mv.visitJumpInsn(switch (obj.data().getType()) {
            case IFN -> IFEQ;
            case IFZ -> IFNE;
            case IFLT -> IFGT;
            case IFGT -> IFLT;
        }, labelElse);

        final Label labelIfBody = new Label();
        mv.visitLabel(labelIfBody);
        mv.visitLineNumber(obj.data().getIfBody().isEmpty() ? obj.data().getOrigin().fromLineNum()
                : obj.data().getIfBody().get(0).getOrigin().fromLineNum(), labelIfBody);
        for (final Command command : obj.data().getIfBody()) {
            obj.compiler().visit(command);
        }

        final Label labelIfEnd = new Label();
        mv.visitJumpInsn(GOTO, labelIfEnd);

        mv.visitLabel(labelElse);
        mv.visitLineNumber(obj.data().getElseBody().isEmpty() ? obj.data().getOrigin().fromLineNum()
                : obj.data().getElseBody().get(0).getOrigin().fromLineNum(), labelElse);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        for (final Command command : obj.data().getElseBody()) {
            obj.compiler().visit(command);
        }

        mv.visitLabel(labelIfEnd);
        mv.visitLineNumber(obj.data().getOrigin().fromLineNum(), labelIfEnd);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
    }

}
