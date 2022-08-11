package dev.cerus.edina.edinaj.compiler.step.command.imports;

import dev.cerus.edina.ast.ast.Command;
import dev.cerus.edina.edinaj.compiler.CompilerSettings;
import dev.cerus.edina.edinaj.compiler.StepData;
import dev.cerus.edina.edinaj.compiler.step.CompilerStep;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;

public class ImportCallStep implements CompilerStep<StepData<ImportCallStep.Data>> {

    @Override
    public void compile(final ClassWriter cw, final CompilerSettings settings, final StepData<Data> obj) {
        final MethodVisitor methodVisitor = obj.methodVisitor();

        final Label label0 = new Label();
        methodVisitor.visitLabel(label0);
        methodVisitor.visitLineNumber(obj.data().cmd.getOrigin().fromLineNum(), label0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, settings.getMainClassName(), "imp_" + obj.data().cmd.getImportName(), "L" + obj.data().fullClassName + ";");
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, obj.data().fullClassName, "routine_" + obj.data().cmd.getRoutineName(), "()V", false);
    }

    public static class Data {

        private final Command.ImportCallCommand cmd;
        private final String fullClassName;

        public Data(final Command.ImportCallCommand cmd, final String fullClassName) {
            this.cmd = cmd;
            this.fullClassName = fullClassName;
        }
    }

}
