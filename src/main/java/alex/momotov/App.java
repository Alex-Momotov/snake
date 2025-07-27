package alex.momotov;

import alex.momotov.reality.Reality;
import jline.console.ConsoleReader;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class App {

    public static int ROWS = 30;
    public static int COLS = 23;
    public static int STEP_MS = 121;
    public static int FOOD_INTERVAL_MS = 2500;
    public static boolean WALLS = true;
    public static boolean COLLISION = true;

    public static void main(String[] args) throws IOException {
        if (containsHelpOption(args)) {
            printHelp();
            return;
        }

        Map<String, String> params = parseArgs(args);

        ROWS = Integer.parseInt(params.getOrDefault("rows", String.valueOf(ROWS)));
        COLS = Integer.parseInt(params.getOrDefault("cols", String.valueOf(COLS)));
        STEP_MS = Integer.parseInt(params.getOrDefault("step_ms", String.valueOf(STEP_MS)));
        FOOD_INTERVAL_MS = Integer.parseInt(params.getOrDefault("food_interval_ms", String.valueOf(FOOD_INTERVAL_MS)));
        WALLS = Boolean.parseBoolean(params.getOrDefault("walls", String.valueOf(WALLS)));
        COLLISION = Boolean.parseBoolean(params.getOrDefault("collision", String.valueOf(COLLISION)));

        Reality reality = new Reality();
    }

    private static boolean containsHelpOption(String[] args) {
        for (String arg : args) {
            if ("--help".equals(arg)) {
                return true;
            }
        }
        return false;
    }

    private static void printHelp() {
        System.out.println("Usage: snake [options]");
        System.out.println("Options:");
        System.out.println("  --rows=<int>             Number of rows (default: " + ROWS + ")");
        System.out.println("  --cols=<int>             Number of columns (default: " + COLS + ")");
        System.out.println("  --step_ms=<int>          Step duration in milliseconds (default: " + STEP_MS + ")");
        System.out.println("  --food_interval_ms=<int> Food interval in milliseconds (default: " + FOOD_INTERVAL_MS + ")");
        System.out.println("  --walls=<boolean>        Enable walls (default: " + WALLS + ")");
        System.out.println("  --collision=<boolean>    Enable snake collision (default: " + COLLISION + ")");
        System.out.println("  --help                   Show this help message");
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> params = new HashMap<>();
        for (String arg : args) {
            if (arg.startsWith("--")) {
                String[] keyValue = arg.substring(2).split("=", 2);
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return params;
    }

}
