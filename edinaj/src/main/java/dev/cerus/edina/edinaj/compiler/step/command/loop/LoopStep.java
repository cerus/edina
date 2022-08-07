package dev.cerus.edina.edinaj.compiler.step.command.loop;

import dev.cerus.edina.ast.ast.Command;
import dev.cerus.edina.edinaj.compiler.CompilerSettings;
import dev.cerus.edina.edinaj.compiler.StepData;
import dev.cerus.edina.edinaj.compiler.step.CompilerStep;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import static org.objectweb.asm.Opcodes.*;

public class LoopStep implements CompilerStep<StepData<Command.LoopCommand>> {

    @Override
    public void compile(final ClassWriter cw, final CompilerSettings settings, final StepData<Command.LoopCommand> obj) {
        final MethodVisitor mv = obj.methodVisitor();

        // Before loop
        final Label labelLoopStart = new Label();
        mv.visitLabel(labelLoopStart);
        mv.visitLineNumber(obj.data().getOrigin().getLine(), labelLoopStart);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, settings.getMainClassName(), "stack", "L" + settings.getStackName() + ";");
        mv.visitMethodInsn(INVOKEVIRTUAL, settings.getStackName(), "peekInt", "()I", false);

        // Jump to loop end if condition is met
        final Label labelLoopAfter = new Label();
        mv.visitJumpInsn(switch (obj.data().getType()) {
            case UNTIL -> IFNE;
            case WHILE -> IFEQ;
        }, labelLoopAfter);

        // Loop content start
        final Label labelLoopContent = new Label();
        mv.visitLabel(labelLoopContent);
        mv.visitLineNumber(obj.data().getBody().isEmpty() ? obj.data().getOrigin().getLine()
                : obj.data().getBody().get(0).getOrigin().getLine(), labelLoopContent);
        for (final Command command : obj.data().getBody()) {
            obj.compiler().visit(command);
        }
        mv.visitJumpInsn(GOTO, labelLoopStart);
        // Loop content end

        // After loop
        mv.visitLabel(labelLoopAfter);
        mv.visitLineNumber(obj.data().getBody().isEmpty() ? obj.data().getOrigin().getLine()
                : obj.data().getBody().get(obj.data().getBody().size() - 1).getOrigin().getLine(), labelLoopAfter);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
    }

}
