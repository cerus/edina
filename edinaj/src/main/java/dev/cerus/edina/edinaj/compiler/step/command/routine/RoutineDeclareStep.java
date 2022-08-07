package dev.cerus.edina.edinaj.compiler.step.command.routine;

import dev.cerus.edina.ast.ast.Command;
import dev.cerus.edina.edinaj.compiler.CompilerSettings;
import dev.cerus.edina.edinaj.compiler.StepData;
import dev.cerus.edina.edinaj.compiler.step.CompilerStep;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;

public class RoutineDeclareStep implements CompilerStep<StepData<Command.RoutineDeclareCommand>> {

    @Override
    public void compile(final ClassWriter cw, final CompilerSettings settings, final StepData<Command.RoutineDeclareCommand> obj) {
        final MethodVisitor methodVisitor = obj.classVisitor().visitMethod(
                obj.data().getRoutineName().startsWith("_") ? ACC_PRIVATE : ACC_PUBLIC,
                "routine_" + obj.data().getRoutineName(),
                "()V",
                null,
                null
        );
        methodVisitor.visitCode();

        final MethodVisitor cmv = obj.compiler().getCurrentMethodVisitor();
        obj.compiler().setCurrentMethodVisitor(methodVisitor);

        for (final Command cmd : obj.data().getRoutineBody()) {
            obj.compiler().visit(cmd);
        }
        obj.compiler().setCurrentMethodVisitor(cmv);

        final Label label = new Label();
        methodVisitor.visitLabel(label);
        methodVisitor.visitLineNumber(100, label);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }

}
