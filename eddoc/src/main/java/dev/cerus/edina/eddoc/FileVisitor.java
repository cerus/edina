package dev.cerus.edina.eddoc;

import dev.cerus.edina.ast.ast.Command;
import dev.cerus.edina.ast.ast.VisitorAdapter;
import java.util.Map;

public class FileVisitor extends VisitorAdapter<Void> {

    private final Document document;
    private Command.RoutineAnnotationCommand lastAnnotation;

    public FileVisitor(final Document document) {
        this.document = document;
    }

    @Override
    public Void visitRoutineAnnotation(final Command.RoutineAnnotationCommand routineAnnotationCommand) {
        this.lastAnnotation = routineAnnotationCommand;
        return null;
    }

    @Override
    public Void visitRoutineDeclare(final Command.RoutineDeclareCommand routineDeclareCommand) {
        final Document.RoutineOptions opts;
        if (this.lastAnnotation == null) {
            opts = null;
        } else {
            final Map<String, Object> elements = this.lastAnnotation.getElements();
            if (!elements.containsKey("eddoc") || !(elements.get("eddoc") instanceof Map<?, ?>)) {
                opts = null;
            } else {
                final Map<String, Object> eddocElems = (Map<String, Object>) elements.get("eddoc");
                opts = new Document.RoutineOptions(eddocElems.getOrDefault("desc", "").toString());
                if (eddocElems.containsKey("stack")) {
                    final Map<String, Object> stackElems = (Map<String, Object>) eddocElems.get("stack");
                    final String[] in = stackElems.getOrDefault("in", "").toString().split(",");
                    final String[] out = stackElems.getOrDefault("out", "").toString().split(",");
                    for (final String inElem : in) {
                        final String[] split = inElem.trim().split("\\$", 2);
                        if (split.length > 1) {
                            opts.addIn(split[0], split[1]);
                        }
                    }
                    for (final String outElem : out) {
                        final String[] split = outElem.trim().split("\\$", 2);
                        if (split.length > 1) {
                            opts.addOut(split[0], split[1]);
                        }
                    }
                }
            }
        }
        this.document.insertRoutine(routineDeclareCommand.getRoutineName(), opts);
        this.lastAnnotation = null;
        return null;
    }

}
