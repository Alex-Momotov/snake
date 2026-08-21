package alex.momotov;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Immutable game settings, parsed once from the command line and injected everywhere. */
public final class Config {

    private static final Set<String> KNOWN = new HashSet<>(Arrays.asList(
            "rows", "cols", "step_ms", "food_interval_ms", "walls", "collision", "seed", "max_ticks", "start_level"));

    public final int rows;
    public final int cols;
    public final int stepMs;
    public final int foodIntervalMs;
    public final boolean walls;
    public final boolean collision;
    public final long seed;
    public final int maxTicks;      // 0 = run until the game ends
    public final int startLevel;

    public Config(int rows, int cols, int stepMs, int foodIntervalMs,
                  boolean walls, boolean collision, long seed, int maxTicks, int startLevel) {
        this.rows = rows;
        this.cols = cols;
        this.stepMs = stepMs;
        this.foodIntervalMs = foodIntervalMs;
        this.walls = walls;
        this.collision = collision;
        this.seed = seed;
        this.maxTicks = maxTicks;
        this.startLevel = startLevel;
        validate();
    }

    /** Defaults, used both by {@link #parse} and by the --help text. */
    public static Config defaults() {
        return new Config(30, 23, 121, 2200, true, true, System.nanoTime(), 0, 1);
    }

    public static Config parse(String[] args) {
        Config d = defaults();
        Map<String, String> p = new HashMap<>();
        for (String arg : args) {
            if (!arg.startsWith("--"))
                throw new IllegalArgumentException("Unrecognised argument: " + arg);
            String[] kv = arg.substring(2).split("=", 2);
            if (kv.length != 2)
                throw new IllegalArgumentException("Expected --name=value but got: " + arg);
            if (!KNOWN.contains(kv[0]))
                throw new IllegalArgumentException("Unknown option: --" + kv[0]);
            p.put(kv[0], kv[1]);
        }
        return new Config(
                intOf(p, "rows", d.rows),
                intOf(p, "cols", d.cols),
                intOf(p, "step_ms", d.stepMs),
                intOf(p, "food_interval_ms", d.foodIntervalMs),
                boolOf(p, "walls", d.walls),
                boolOf(p, "collision", d.collision),
                longOf(p, "seed", d.seed),
                intOf(p, "max_ticks", d.maxTicks),
                intOf(p, "start_level", d.startLevel));
    }

    /** Every rule the rest of the code is then free to assume. */
    private void validate() {
        require(rows >= 8, "rows must be at least 8, got " + rows);
        require(cols >= 8, "cols must be at least 8, got " + cols);
        require(stepMs >= 1, "step_ms must be at least 1, got " + stepMs);
        require(foodIntervalMs >= 1, "food_interval_ms must be at least 1, got " + foodIntervalMs);
        require(maxTicks >= 0, "max_ticks must not be negative, got " + maxTicks);
        require(startLevel >= 1, "start_level must be at least 1, got " + startLevel);
    }

    private static void require(boolean ok, String message) {
        if (!ok)
            throw new IllegalArgumentException(message);
    }

    private static int intOf(Map<String, String> p, String key, int fallback) {
        String raw = p.get(key);
        if (raw == null)
            return fallback;
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("--" + key + " expects a whole number, got: " + raw);
        }
    }

    private static long longOf(Map<String, String> p, String key, long fallback) {
        String raw = p.get(key);
        if (raw == null)
            return fallback;
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("--" + key + " expects a whole number, got: " + raw);
        }
    }

    private static boolean boolOf(Map<String, String> p, String key, boolean fallback) {
        String raw = p.get(key);
        if (raw == null)
            return fallback;
        if ("true".equalsIgnoreCase(raw) || "false".equalsIgnoreCase(raw))
            return Boolean.parseBoolean(raw);
        throw new IllegalArgumentException("--" + key + " expects true or false, got: " + raw);
    }
}
