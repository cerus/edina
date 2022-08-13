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
        this.stack.pushBool(this.stack.peekLong() == this.stack.peekSecondLong());
        if (this.stack.popLong() > 0) {
            this.stack.push(111);
        } else {
            this.stack.push(222);
        }
        this.stack.push(321);
    }

}
