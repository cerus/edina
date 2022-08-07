package dev.cerus.edina.eddoc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Document {

    private static final String EMOJI_GLOBAL = "\uD83C\uDF10";
    private static final String EMOJI_INTERNAL = "\uD83D\uDD11";

    private final StringBuilder docBuilder = new StringBuilder();
    private final List<String> routines = new ArrayList<>();
    private int start;

    public void init(final String path) {
        this.docBuilder.append("# ").append(path).append("\n\n");
        this.start = this.docBuilder.length();
        this.docBuilder.append("\n\n<br>\n\n");
    }

    public void insertRoutine(final String name, final RoutineOptions opts) {
        final boolean internal = name.startsWith("_");
        this.docBuilder.append("### ").append(internal ? EMOJI_INTERNAL : EMOJI_GLOBAL).append(" `").append(name).append("`\n");
        this.routines.add(name);
        if (opts != null) {
            if (opts.desc != null) {
                this.docBuilder.append(">").append(opts.desc.replace("\n", "\n>")).append("\n");
            }
            if (!opts.input.isEmpty() || !opts.output.isEmpty()) {
                final List<Map.Entry<String, String>> takes = opts.input.entrySet().stream().toList();
                final List<Map.Entry<String, String>> gives = opts.output.entrySet().stream().toList();

                this.docBuilder.append("\n| Takes | Gives |\n| --- | --- |\n");
                for (int i = 0; i < Math.max(takes.size(), gives.size()); i++) {
                    this.docBuilder.append("| ");
                    if (i < takes.size()) {
                        this.docBuilder.append(takes.get(i).getKey()).append(" (").append(takes.get(i).getValue()).append(")");
                    }
                    this.docBuilder.append(" | ");
                    if (i < gives.size()) {
                        this.docBuilder.append(gives.get(i).getKey()).append(" (").append(gives.get(i).getValue()).append(")");
                    }
                    this.docBuilder.append(" |\n");
                }
            }
        }
        this.docBuilder.append("\n<br>\n\n");
    }

    public String finish() {
        final StringBuilder tableBuilder = new StringBuilder();
        if (!this.routines.isEmpty()) {
            tableBuilder.append("| Routine | Visibility |\n| --- | --- |\n");
            for (final String routine : this.routines) {
                tableBuilder.append("| [").append(routine).append("](#-rt-").append(routine).append(") | ")
                        .append(routine.startsWith("_") ? (EMOJI_INTERNAL + " Internal") : (EMOJI_GLOBAL + " Global")).append(" |\n");
            }
            tableBuilder.append("\n");
        }
        this.docBuilder.insert(this.start, tableBuilder);
        return this.docBuilder.toString();
    }

    public static class RoutineOptions {

        private final Map<String, String> input;
        private final Map<String, String> output;
        private final String desc;

        public RoutineOptions(final String desc) {
            this(new LinkedHashMap<>(), new LinkedHashMap<>(), desc);
        }

        public RoutineOptions(final Map<String, String> input, final Map<String, String> output, final String desc) {
            this.input = input;
            this.output = output;
            this.desc = desc;
        }

        public void addIn(final String name, final String type) {
            this.input.put(name, type);
        }

        public void addOut(final String name, final String type) {
            this.output.put(name, type);
        }

        public Map<String, String> getInput() {
            return this.input;
        }

        public Map<String, String> getOutput() {
            return this.output;
        }

        public String getDesc() {
            return this.desc;
        }

    }

}
