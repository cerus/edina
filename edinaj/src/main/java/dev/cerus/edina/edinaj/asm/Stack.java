package dev.cerus.edina.edinaj.asm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.EmptyStackException;
import java.util.List;

/**
 * Stack template
 */
public class Stack {

    private final Deque<Object> stack = new ArrayDeque<>();

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
        this.roll(this.popInt(), this.popInt(), true);
    }

    public void rollLeft() {
        this.roll(this.popInt(), this.popInt(), false);
    }

    public long popLong() {
        final long l = this.peekLong();
        this.pop();
        return l;
    }

    public long peekLong() {
        this.checkSize(1);
        final Object pop = this.stack.peek();
        if (pop instanceof Long) {
            return (long) (Long) pop;
        } else if (pop instanceof Integer) {
            return (long) (int) (Integer) pop;
        } else if (pop instanceof Double) {
            return (long) (double) (Double) pop;
        } else if (pop instanceof Byte) {
            return (long) (byte) (Byte) pop;
        }
        throw new RuntimeException("Not a java.lang.Long: " + pop.getClass().getName(), new IllegalArgumentException());
    }

    public byte[] popByteArray() {
        this.checkSize(1);
        final int amt = this.popInt();
        this.checkSize(amt);
        final byte[] buf = new byte[amt];
        for (int i = 0; i < amt; i++) {
            buf[i] = (byte) this.popInt();
        }
        return buf;
    }

    public void roll(final int items, final int amount, final boolean dir) {
        this.checkSize(items);
        if (items == 0) {
            throw new IllegalArgumentException();
        }

        final List<Object> list = new ArrayList<>();
        for (int i = 0; i < items; i++) {
            list.add(this.stack.pop());
        }

        for (int i = 0; i < amount; i++) {
            if (dir) {
                list.add(0, list.remove(list.size() - 1));
            } else {
                list.add(list.remove(0));
            }
        }

        Collections.reverse(list);
        for (final Object o : list) {
            this.push(o);
        }
    }

    public int popInt() {
        final int i = this.peekInt();
        this.stack.pop();
        return i;
    }

    public int peekInt() {
        this.checkSize(1);
        final Object peek = this.stack.peek();
        if (peek instanceof Integer) {
            return (int) (Integer) peek;
        } else if (peek instanceof Long) {
            return (int) (long) (Long) peek;
        } else if (peek instanceof Double) {
            return (int) (double) (Double) peek;
        } else if (peek instanceof Byte) {
            return (int) (byte) (Byte) peek;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public void pushString(final String s) {
        final byte[] bytes = s.getBytes();
        for (final byte b : bytes) {
            this.push(b);
        }
        this.push(bytes.length);
    }

    public void push(final Object o) {
        this.stack.push(o);
    }

    public void checkSize(final int n) {
        if (this.stack.size() < n) {
            throw new RuntimeException("Not enough items in stack (" + n + " required)", new EmptyStackException());
        }
    }

    public void debugPrint() {
        System.out.println(Arrays.toString(this.stack.toArray()));
    }

    public int stackSize() {
        return this.stack.size();
    }

}
