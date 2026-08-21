package alex.momotov;

import alex.momotov.reality.Reality;
import alex.momotov.ui.TerminalInput;
import alex.momotov.ui.TerminalView;

/** Entry point: parse the command line, wire everything up once, run the game. */
public class App {

    private static final int BAD_USAGE = 2;

    public static void main(String[] args) {
        if (wantsHelp(args)) {
            printHelp();
            return;
        }
        Config config;
        try {
            config = Config.parse(args);
        } catch (IllegalArgumentException e) {
            System.err.println("snake: " + e.getMessage());
            System.err.println("Try 'snake --help' for the list of options.");
            System.exit(BAD_USAGE);
            return;
        }
        setupDependencies(config).start();
    }

    /** The one place where the game and its adapters are constructed and wired. */
    private static Reality setupDependencies(Config config) {
        return new Reality(config, new TerminalView(), new TerminalInput());
    }

    private static boolean wantsHelp(String[] args) {
        for (String arg : args) {
            if ("--help".equals(arg) || "-h".equals(arg))
                return true;
        }
        return false;
    }

    private static void printHelp() {
        Config d = Config.defaults();
        System.out.println("Usage: snake [options]");
        System.out.println("Options:");
        System.out.println("  --rows=<int>             Number of rows (default: " + d.rows + ")");
        System.out.println("  --cols=<int>             Number of columns (default: " + d.cols + ")");
        System.out.println("  --step_ms=<int>          Step duration in milliseconds (default: " + d.stepMs + ")");
        System.out.println("  --food_interval_ms=<int> Food interval in milliseconds (default: " + d.foodIntervalMs + ")");
        System.out.println("  --walls=<boolean>        Enable walls (default: " + d.walls + ")");
        System.out.println("  --collision=<boolean>    Enable snake collision (default: " + d.collision + ")");
        System.out.println("  --seed=<long>            Seed the food generator, for repeatable games");
        System.out.println("  --max_ticks=<int>        Stop after this many steps (default: unlimited)");
        System.out.println("  --start_level=<int>      Begin at this level (default: " + d.startLevel + ")");
        System.out.println("  --help                   Show this help message");
    }
}
