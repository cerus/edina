package dev.cerus.edina.cli.interpreter;

import dev.cerus.edina.ast.ast.Command;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Environment {

    private final Deque<Object> stack = new ArrayDeque<>();
    private final Map<String, Command.RoutineDeclareCommand> routineMap = new HashMap<>();

    public void declareRoutine(final Command.RoutineDeclareCommand cmd) {
        this.routineMap.put(cmd.getRoutineName(), cmd);
    }

    public List<Command> getRoutine(final String name) {
        return this.routineMap.get(name).getRoutineBody();
    }

    public void push(final Object o) {
        this.stack.push(o);
    }

    public void pop() {
        this.checkSize(1);
        this.stack.pop();
    }

    public void swap() {
        this.checkSize(2);
        final Object a = this.stack.pop();
        final Object b = this.stack.pop();
        this.push(a);
        this.push(b);
    }

    public void over() {
        this.checkSize(2);
        final Object addLater = this.stack.pop();
        final Object duplicate = this.stack.peek();
        this.push(addLater);
        this.push(duplicate);
    }

    public void dup() {
        this.checkSize(1);
        final Object duplicate = this.stack.peek();
        this.push(duplicate);
    }

    public void rollRight() {
        this.roll(this.popInt(), true);
    }

    public void rollLeft() {
        this.roll(this.popInt(), false);
    }

    public Object popVal() {
        this.checkSize(1);
        return this.stack.pop();
    }

    public double popNum() {
        this.checkSize(1);
        return Double.parseDouble(String.valueOf(this.stack.pop()));
    }

    private int popInt() {
        this.checkSize(1);
        final Object pop = this.stack.pop();
        if (pop instanceof Integer) {
            return (int) (Integer) pop;
        } else if (pop instanceof Long) {
            return (int) (long) (Long) pop;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private void roll(final int num, final boolean dir) {
        this.checkSize(num);
        if (num == 0) {
            throw new IllegalArgumentException();
        }

        final List<Object> list = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            list.add(this.stack.pop());
        }
        Collections.reverse(list);

        if (dir) {
            list.add(0, list.remove(list.size() - 1));
        } else {
            list.add(list.remove(0));
        }

        list.forEach(this::push);
    }

    public int stackSize() {
        return this.stack.size();
    }

    public List<Object> stack() {
        return List.copyOf(this.stack);
    }

    private void checkSize(final int s) {
        if (this.stack.size() < s) {
            throw new EmptyStackException();
        }
    }

}
