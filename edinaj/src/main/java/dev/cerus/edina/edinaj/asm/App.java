package dev.cerus.edina.edinaj.asm;

/**
 * App template
 * <p>
 * This is the class where all the script code will be placed in
 */
public class App {

    private final Stack stack;
    private final Natives natives;

    public App(final Stack stack, final Natives natives) {
        this.stack = stack;
        this.natives = natives;
    }

    public void run() {
        this.stack.push(123);
        this.stack.push(this.stack.popLong() & this.stack.popLong());
        this.stack.push(69);
        this.stack.push(this.stack.popLong() | this.stack.popLong());
        this.stack.push(6969);
        this.stack.push(this.stack.popLong() ^ this.stack.popLong());
        this.stack.push(696969);
        this.stack.push(~this.stack.popLong());
        this.stack.push(321);
    }

}
