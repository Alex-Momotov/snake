package alex.momotov.ui;

import alex.momotov.reality.Input;
import alex.momotov.reality.Key;
import jline.TerminalFactory;
import jline.console.ConsoleReader;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Reads the keyboard on its own thread and hands keys over a queue — the single
 * point of contact between that thread and the game.
 *
 * Arrows arrive as ESC [ A..D (or ESC O A..D in application cursor mode); the whole
 * sequence is parsed, so a bare capital A does not steer. WASD works too.
 */
public class TerminalInput implements Input {

    private static final int ESC = 27;
    private static final int QUEUE_DEPTH = 8;

    private final BlockingQueue<Key> pending = new ArrayBlockingQueue<>(QUEUE_DEPTH);
    private volatile boolean running;
    private Thread thread;

    @Override
    public void start() {
        running = true;
        thread = new Thread(this::readLoop, "keyboard");
        thread.setDaemon(true);                     // never keeps the JVM alive
        thread.start();
    }

    @Override
    public Key poll() {
        return pending.poll();
    }

    @Override
    public void stop() {
        running = false;
        if (thread != null)
            thread.interrupt();
        try {
            TerminalFactory.get().restore();        // undo raw mode, or the shell is left broken
        } catch (Exception ignored) {
            // shutting down anyway; nothing useful to do
        }
    }

    private void readLoop() {
        try {
            ConsoleReader reader = new ConsoleReader();
            while (running) {
                Key key = readKey(reader);
                if (key != null)
                    pending.offer(key);             // drop input rather than block the reader
            }
        } catch (IOException e) {
            // the terminal went away; steering simply stops
        }
    }

    private Key readKey(ConsoleReader reader) throws IOException {
        int c = reader.readCharacter();
        if (c == ESC)
            return readArrow(reader);
        switch (c) {
            case 'w': case 'W': return Key.UP;
            case 's': case 'S': return Key.DOWN;
            case 'a': case 'A': return Key.LEFT;
            case 'd': case 'D': return Key.RIGHT;
            case ' ':           return Key.PAUSE;
            case 'q': case 'Q': return Key.QUIT;
            default:            return null;
        }
    }

    private Key readArrow(ConsoleReader reader) throws IOException {
        int introducer = reader.readCharacter();
        if (introducer != '[' && introducer != 'O')
            return null;
        switch (reader.readCharacter()) {
            case 'A': return Key.UP;
            case 'B': return Key.DOWN;
            case 'C': return Key.RIGHT;
            case 'D': return Key.LEFT;
            default:  return null;
        }
    }
}
