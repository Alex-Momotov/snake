package alex.momotov.reality;

import alex.momotov.Config;
import alex.momotov.reality.objects.Creature;
import alex.momotov.reality.objects.Door;
import alex.momotov.reality.objects.Edible;
import alex.momotov.reality.objects.SnakeBody;
import alex.momotov.reality.objects.SnakeHead;
import alex.momotov.reality.objects.Species;
import alex.momotov.reality.objects.Treat;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * All of the world's state, and the invariants that keep it consistent.
 * Nothing outside touches the deque, the mob list or the board's cells directly:
 * the snake on the board and the snake in the deque are always the same snake.
 */
public class State {

    /** Ticks of protection when a world opens, so nothing can kill you on arrival. */
    private static final int SETTLING_IN = 55;

    private final Config config;
    private final Random random;
    private final int foodEveryTicks;

    private Board board;
    private final Deque<Pos> snake = new ArrayDeque<>();
    private final List<Mob> mobs = new ArrayList<>();
    private final Map<Effect, Integer> effects = new EnumMap<>(Effect.class);
    private final Map<Pos, Edible> perishing = new LinkedHashMap<>();

    private Direction direction;
    private Direction pending;
    private boolean over;
    private boolean paused;
    private int tick;
    private int score;
    private int best;
    private int level;
    private int eatenHere;
    private int pendingGrowth;
    private int hue;
    private int ticksUntilFood;
    private Pos door;
    private String banner;
    private int bannerUntil;
    private boolean bannerStale;

    public State(Config config) {
        this.config = config;
        this.random = new Random(config.seed);
        this.foodEveryTicks = Math.max(1, config.foodIntervalMs / config.stepMs);
        enterLevel(config.startLevel);
    }

    // ── the world ────────────────────────────────────────────────────────────────

    public Board board() {
        return board;
    }

    public int level() {
        return level;
    }

    /** Builds a whole new world, carrying the snake's length and score across. */
    public void enterLevel(int newLevel) {
        int carriedLength = snake.isEmpty() ? 2 : snake.size();
        this.level = newLevel;
        this.board = new Board(config.rows, config.cols);
        this.snake.clear();
        this.mobs.clear();
        this.perishing.clear();
        this.door = null;
        this.eatenHere = 0;
        this.pendingGrowth = 0;
        this.direction = Direction.RIGHT;
        this.pending = Direction.RIGHT;
        this.ticksUntilFood = 0;
        if (config.walls)
            Level.build(board, level, random);
        placeSnake(carriedLength);
        populate();
        grant(Effect.SHIELD, SETTLING_IN);   // a moment to read the room before it bites
        board.touchAll();                    // a brand new world: repaint every square
    }

    private void placeSnake(int length) {
        int row = board.rows() / 2;
        int room = Math.max(2, Math.min(length, board.cols() / 2));
        int startCol = Math.max(1, board.cols() / 2 - room);
        for (int i = 0; i < room; i++) {
            Pos pos = new Pos(row, startCol + i);
            board.cell(pos).clear();                 // never spawn inside a wall
            snake.addLast(pos);
            board.add(pos, i == room - 1 ? new SnakeHead(direction) : new SnakeBody(hue++));
        }
        // clear a runway ahead, so arriving in a maze still leaves time to react
        int runway = Math.max(6, board.cols() / 3);
        for (int c = startCol + room; c < Math.min(board.cols() - 1, startCol + room + runway); c++)
            board.cell(new Pos(row, c)).clear();
    }

    private void populate() {
        for (int i = 0; i < Level.mobCount(level); i++)
            spawnMob(Level.inhabitant(level, random.nextInt(100)));
    }

    // ── the snake ────────────────────────────────────────────────────────────────

    public Pos head() {
        return snake.peekLast();
    }

    public Pos tail() {
        return snake.peekFirst();
    }

    public int length() {
        return snake.size();
    }

    public Direction direction() {
        return direction;
    }

    public boolean growing() {
        return pendingGrowth > 0;
    }

    /**
     * Records a requested turn. Judged against the direction last actually moved, not
     * against another pending turn, so two quick presses can never reverse the snake
     * into its own neck.
     */
    public void steer(Direction requested) {
        if (requested == null || direction.isOpposite(requested))
            return;
        pending = requested;
    }

    public void applyPendingDirection() {
        direction = pending;
    }

    /** Lengthens the snake, or sheds segments when handed a negative number. */
    public void grow(int segments) {
        if (segments >= 0) {
            pendingGrowth += segments;
            return;
        }
        for (int i = 0; i < -segments && snake.size() > 2; i++) {
            Pos gone = snake.removeFirst();
            board.remove(gone, SnakeBody.class);
        }
    }

    /** Moves the head one square, dragging the tail along unless the snake is growing. */
    public void advance(Pos newHead) {
        Pos oldHead = head();
        if (oldHead != null) {
            board.remove(oldHead, SnakeHead.class);
            board.add(oldHead, new SnakeBody(hue++));
        }
        snake.addLast(newHead);
        board.add(newHead, new SnakeHead(direction));
        if (pendingGrowth > 0) {
            pendingGrowth--;
            return;
        }
        Pos gone = snake.removeFirst();
        board.remove(gone, SnakeBody.class);
    }

    // ── creatures ────────────────────────────────────────────────────────────────

    public List<Mob> mobs() {
        return mobs;
    }

    public Mob mobAt(Pos pos) {
        for (Mob mob : mobs) {
            if (mob.pos().equals(pos))
                return mob;
        }
        return null;
    }

    public void spawnMob(Species species) {
        Pos spot = freeSquare();
        if (spot == null)
            return;
        Mob mob = new Mob(species, spot);
        mobs.add(mob);
        board.add(spot, mob.token);
    }

    public void moveMob(Mob mob, Pos target) {
        board.remove(mob.pos(), mob.token);
        mob.moveTo(target);
        board.add(target, mob.token);
    }

    public void removeMob(Mob mob) {
        board.remove(mob.pos(), mob.token);
        mobs.remove(mob);
    }

    // ── treats ───────────────────────────────────────────────────────────────────

    public boolean foodDue() {
        if (ticksUntilFood > 0) {
            ticksUntilFood--;
            return false;
        }
        ticksUntilFood = foodEveryTicks - 1;
        return true;
    }

    public void placeTreat(Treat treat) {
        Pos spot = freeSquare();
        if (spot == null)
            return;
        Edible edible = new Edible(treat);
        if (treat.perishable()) {
            edible.expiresAt(tick + treat.life);
            perishing.put(spot, edible);
        }
        board.add(spot, edible);
    }

    public void takeTreat(Pos pos, Edible edible) {
        board.remove(pos, edible);
        perishing.remove(pos);
        eatenHere++;
    }

    /** Clears out anything that has sat around too long. */
    public void rotAway() {
        perishing.entrySet().removeIf(entry -> {
            Edible edible = entry.getValue();
            if (!edible.expired(tick))
                return false;
            if (board.find(entry.getKey(), Edible.class) == edible)
                board.remove(entry.getKey(), edible);
            return true;
        });
    }

    public int eatenHere() {
        return eatenHere;
    }

    // ── the way out ──────────────────────────────────────────────────────────────

    public boolean doorOpen() {
        return door != null;
    }

    /** Knocks a hole in the wall, somewhere along the edge. */
    public void openDoor() {
        for (int attempt = 0; attempt < 200 && door == null; attempt++) {
            Pos spot = edgeSquare();
            if (spot == null || !board.contains(spot, alex.momotov.reality.objects.Wall.class))
                continue;
            board.cell(spot).clear();
            board.add(spot, new Door());
            door = spot;
        }
    }

    // ── powers ───────────────────────────────────────────────────────────────────

    public void grant(Effect effect, int ticks) {
        effects.merge(effect, ticks, Integer::sum);
    }

    public boolean has(Effect effect) {
        return effects.getOrDefault(effect, 0) > 0;
    }

    public int remaining(Effect effect) {
        return effects.getOrDefault(effect, 0);
    }

    public Map<Effect, Integer> activeEffects() {
        return effects;
    }

    public void fadeEffects() {
        effects.entrySet().removeIf(entry -> {
            entry.setValue(entry.getValue() - 1);
            return entry.getValue() <= 0;
        });
    }

    // ── run of play ──────────────────────────────────────────────────────────────

    public int tick() {
        return tick;
    }

    public void nextTick() {
        tick++;
    }

    public int score() {
        return score;
    }

    public void addScore(int points) {
        score = Math.max(0, score + points);
        best = Math.max(best, score);
    }

    public boolean isOver() {
        return over;
    }

    public void gameOver() {
        over = true;
    }

    public boolean paused() {
        return paused;
    }

    public void togglePause() {
        paused = !paused;
    }

    public void say(String text, int ticks) {
        banner = text;
        bannerUntil = tick + ticks;
    }

    public String banner() {
        if (banner != null && tick >= bannerUntil) {
            banner = null;
            bannerStale = true;
        }
        return banner;
    }

    /** True once, right after a banner expires, so the view knows to wipe it. */
    public boolean bannerStale() {
        boolean stale = bannerStale;
        bannerStale = false;
        return stale;
    }

    public int randomInt(int bound) {
        return random.nextInt(Math.max(1, bound));
    }

    // ── finding somewhere to put things ──────────────────────────────────────────

    /** A square with nothing on it at all — never a wall, a snake, a treat or a creature. */
    public Pos freeSquare() {
        List<Pos> free = new ArrayList<>();
        for (Pos pos : board.positions()) {
            if (board.cell(pos).empty() && farFromHead(pos))
                free.add(pos);
        }
        if (free.isEmpty())
            return null;
        return free.get(random.nextInt(free.size()));
    }

    /** Nothing materialises near the player, nor in the lane straight ahead of them. */
    private boolean farFromHead(Pos pos) {
        Pos head = head();
        if (head == null)
            return true;
        int reach = Math.max(6, (board.rows() + board.cols()) / 5);
        if (Math.abs(pos.row - head.row) + Math.abs(pos.col - head.col) <= reach)
            return false;
        boolean sameLane = direction.rowDelta == 0 ? pos.row == head.row : pos.col == head.col;
        return !sameLane;
    }

    private Pos edgeSquare() {
        boolean horizontal = random.nextBoolean();
        if (horizontal) {
            int row = random.nextBoolean() ? 0 : board.rows() - 1;
            return new Pos(row, 1 + random.nextInt(Math.max(1, board.cols() - 2)));
        }
        int col = random.nextBoolean() ? 0 : board.cols() - 1;
        return new Pos(1 + random.nextInt(Math.max(1, board.rows() - 2)), col);
    }
}
