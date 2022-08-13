package dev.cerus.edina.edinaj.asm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Natives template
 * <p>
 * Implementation of native methods
 */
public class Natives {

    private static final int O_RDONLY = 0;
    private static final int O_WRONLY = 1;
    private static final int O_RDWR = 2;
    private static final int O_CREAT = 0x40;
    private static final int O_EXCL = 0x80;
    private static final int O_NOCTTY = 0x100;
    private static final int O_TRUNC = 0x200;
    private static final int O_APPEND = 0x400;
    private static final int O_NONBLOCK = 0x800;
    private static final int O_DIRECTORY = 0x10000;
    private final Map<Long, FileChannel> openedChannels = new HashMap<>();
    private final Stack stack;
    private boolean restricted = false;
    private long fdIndex = 1000;

    public Natives(final Stack stack) {
        this.stack = stack;
    }

    /**
     * native_time
     */
    public void time() {
        this.stack.push(System.currentTimeMillis() / 1000);
    }

    /**
     * native_stack_debug
     */
    public void stack_debug() {
        this.stack.debugPrint();
    }

    /**
     * native_open
     */
    public void open() {
        final String path = new StringBuilder(new String(this.stack.popByteArray())).reverse().toString();
        final long flags = this.stack.popLong();
        final Set<OpenOption> opts = new HashSet<>();

        if (this.restricted) {
            this.stack.push(-1);
            return;
        }

        if ((flags & O_RDONLY) != 0 || (flags & O_RDWR) != 0) {
            opts.add(StandardOpenOption.READ);
        }
        if ((flags & O_WRONLY) != 0 || (flags & O_RDWR) != 0) {
            opts.add(StandardOpenOption.WRITE);
        }

        if ((flags & O_CREAT) != 0) {
            opts.add(StandardOpenOption.CREATE);
        }
        if ((flags & O_TRUNC) != 0) {
            opts.add(StandardOpenOption.TRUNCATE_EXISTING);
        }
        if ((flags & O_APPEND) != 0) {
            opts.add(StandardOpenOption.APPEND);
        }
        if ((flags & O_NONBLOCK) != 0 || (flags & O_DIRECTORY) != 0) {
            throw new UnsupportedOperationException();
        }

        final FileChannel fc;
        try {
            fc = FileChannel.open(Path.of(path), opts);
        } catch (final IOException e) {
            this.stack.push(-1);
            return;
        }

        this.openedChannels.put(this.fdIndex++, fc);
        this.stack.push(this.fdIndex - 1);
    }

    /**
     * native_close
     */
    public void close() {
        final long fd = this.stack.popLong();
        if (!this.openedChannels.containsKey(fd)) {
            this.stack.push(-1);
            return;
        }

        try {
            this.openedChannels.remove(fd).close();
            this.stack.push(0);
        } catch (final IOException e) {
            this.stack.push(-1);
        }
    }

    /**
     * native_read
     */
    public void read() {
        final long fd = this.stack.popLong();
        final long amount = this.stack.popLong();

        Map.Entry<byte[], Integer> data = null;
        int response = 0;
        try {
            if (fd == 0) {
                final InputStream in = switch ((int) fd) {
                    case 0 -> System.in;
                    default -> null;
                };
                data = this.read0(in, (int) amount);
            } else {
                if (!this.openedChannels.containsKey(fd)) {
                    response = -1;
                } else {
                    data = this.read0(this.openedChannels.get(fd), (int) amount);
                }
            }
        } catch (final IOException e) {
            response = -1;
        }

        if (response == -1) {
            this.stack.push(response);
        } else {
            for (final byte b : data.getKey()) {
                this.stack.push(b);
            }
            this.stack.push(data.getKey().length);
            this.stack.push(data.getValue());
        }
    }

    /**
     * native_write
     */
    public void write() {
        final long fd = this.stack.popLong();
        final byte[] data = this.stack.popByteArray();
        final long amount = data.length;

        int response;
        try {
            if (fd >= 1 && fd <= 2) {
                final OutputStream out = switch ((int) fd) {
                    case 1 -> System.out;
                    case 2 -> System.err;
                    default -> null;
                };
                response = this.write0(out, data, (int) amount);
            } else {
                if (!this.openedChannels.containsKey(fd)) {
                    response = -1;
                } else {
                    response = this.write0(this.openedChannels.get(fd), data, (int) amount);
                }
            }
        } catch (final IOException e) {
            response = -1;
        }
        this.stack.push(response);
    }

    private int write0(final Object t, final byte[] buf, final int amt) throws IOException {
        if (t instanceof OutputStream out) {
            out.write(buf, 0, amt);
        } else if (t instanceof FileChannel fc) {
            return fc.write(ByteBuffer.wrap(buf, 0, amt));
        }
        return buf.length;
    }

    private Map.Entry<byte[], Integer> read0(final Object s, final int amt) throws IOException {
        final byte[] buf = new byte[amt];
        int read = 0;
        if (s instanceof InputStream in) {
            read = in.read(buf);
        } else if (s instanceof FileChannel fc) {
            final ByteBuffer buffer = ByteBuffer.wrap(buf);
            read = fc.read(buffer);
        }
        return Map.entry(buf, read);
    }

    public void closeAll() throws IOException {
        for (final FileChannel value : this.openedChannels.values()) {
            value.close();
        }
    }

    public void setRestricted(final boolean restricted) {
        this.restricted = restricted;
    }

}
