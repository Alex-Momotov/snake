package alex.momotov.reality;

import alex.momotov.Config;
import alex.momotov.Utils;
import alex.momotov.reality.objects.Behaviour;
import alex.momotov.reality.objects.Creature;
import alex.momotov.reality.objects.Door;
import alex.momotov.reality.objects.Edible;
import alex.momotov.reality.objects.SnakeBody;
import alex.momotov.reality.objects.Species;
import alex.momotov.reality.objects.Treat;
import alex.momotov.reality.objects.Wall;

import java.util.ArrayList;
import java.util.List;

/**
 * The game. Reads top to bottom as the complete list of things it can do;
 * every action is one line delegating to the same-named function further down.
 *
 * One thread drives everything: the input adapter is the only other thread and it
 * hands keys over a queue, so no game state is ever touched concurrently.
 */
public class Reality {

    private static final int BANNER_TICKS = 22;
    private static final int MOB_EVERY_TICKS = 260;
    private static final int MOB_CEILING = 9;

    private final Config config;
    private final State state;
    private final View view;
    private final Input input;

    private volatile boolean stopped;

    public Reality(Config config, View view, Input input) {
        this.config = config;
        this.view = view;
        this.input = input;
        this.state = new State(config);
    }

    // ── lifecycle: the one place implemented inline ───────────────────────────────

    public void start() {
        java.lang.Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
        view.begin(state.board(), state.tick());
        input.start();
        state.say(Level.name(state.level()), 30);

        while (!isOver() && !reachedTickLimit()) {
            press(input.poll());
            tick();
            render();
            Utils.sleep(pace());
        }
        finale();
        stop();
    }

    /** Safe to call twice, and from the shutdown hook, so the terminal is always restored. */
    public synchronized void stop() {
        if (stopped)
            return;
        stopped = true;
        input.stop();
        view.finish(state.board());
    }

    // ── every action = one line delegating to the function of the same name ───────

    public void press(Key key)          { press(state, key); }
    public void tick()                  { tick(config, state); }
    public void render()                { render(view, state); }
    public int pace()                   { return pace(config, state); }
    public void spawnTreat()            { spawnTreat(state); }
    public void moveMobs()              { moveMobs(state); }
    public void enterNextLevel()        { enterNextLevel(state); }
    public boolean isOver()             { return state.isOver(); }
    public boolean reachedTickLimit()   { return reachedTickLimit(config, state); }
    public int score()                  { return state.score(); }
    public State state()                { return state; }

    // ── implementations: plain functions over state passed in explicitly ──────────

    static void press(State state, Key key) {
        if (key == null)
            return;
        if (key == Key.QUIT) {
            state.gameOver();
            return;
        }
        if (key == Key.PAUSE) {
            state.togglePause();
            state.say(state.paused() ? "PAUSED" : "", state.paused() ? 100000 : 0);
            return;
        }
        state.steer(key.direction);
    }

    static void tick(Config config, State state) {
        if (state.isOver() || state.paused())
            return;
        holdPhaseInsideWalls(state);
        state.fadeEffects();
        state.rotAway();
        state.applyPendingDirection();

        Pos next = state.board().wrap(state.head().step(state.direction()));

        if (state.board().contains(next, Door.class)) {
            enterNextLevel(state);
            return;
        }
        if (kills(config, state, next)) {
            state.gameOver();
            return;
        }

        eat(state, next);
        state.advance(next);
        meet(state, next);

        moveMobs(state);
        if (caught(state))
            return;

        if (state.foodDue())
            spawnTreat(state);
        if (state.eatenHere() >= Level.quota(state.level()) && !state.doorOpen())
            revealDoor(state);
        if (Math.floorMod(state.tick(), MOB_EVERY_TICKS) == MOB_EVERY_TICKS - 1)
            reinforce(state);
        state.nextTick();
    }

    /** Never strand the player mid-wall: phasing lasts until they are clear of it. */
    static void holdPhaseInsideWalls(State state) {
        if (state.has(Effect.PHASE)
                && state.remaining(Effect.PHASE) <= 1
                && state.board().contains(state.head(), Wall.class))
            state.grant(Effect.PHASE, 2);
    }

    /** Walls kill unless you are phasing. Your own body kills unless shielded, or it is
     *  the tail square you are about to vacate this very tick. */
    static boolean kills(Config config, State state, Pos next) {
        Board board = state.board();
        if (board.contains(next, Wall.class))
            return !state.has(Effect.PHASE);
        if (!config.collision || state.has(Effect.SHIELD) || state.has(Effect.PHASE))
            return false;
        if (!board.contains(next, SnakeBody.class))
            return false;
        Edible treat = board.find(next, Edible.class);
        boolean growing = state.growing() || (treat != null && treat.treat.growth > 0);
        return growing || !next.equals(state.tail());
    }

    /** Swallows whatever is on the square being moved onto. */
    static void eat(State state, Pos next) {
        Edible edible = state.board().find(next, Edible.class);
        if (edible == null)
            return;
        Treat treat = edible.treat;
        state.takeTreat(next, edible);
        state.addScore(treat.points);
        state.grow(treat.growth);
        if (treat.effect != null)
            state.grant(treat.effect, treat.life);
        state.say(treat.shout, BANNER_TICKS);
    }

    /** Bumping into a creature: fatal, lunch, or a favour, depending on who it is. */
    static void meet(State state, Pos next) {
        Mob mob = state.mobAt(next);
        if (mob == null)
            return;
        Species species = mob.species();
        if (species.hostile) {
            if (!state.has(Effect.SHIELD)) {
                state.gameOver();
                return;
            }
            state.removeMob(mob);                    // shielded: you are the predator now
            state.addScore(75);
            state.say("SMASHED", BANNER_TICKS);
            return;
        }
        state.removeMob(mob);
        state.addScore(species.points);
        state.grow(species.growth);
        if (species == Species.HERMIT)
            bestow(state);
        else
            state.say("CAUGHT IT", BANNER_TICKS);
    }

    /** The hermit hands out a random blessing. */
    static void bestow(State state) {
        Effect[] boons = Effect.values();
        Effect boon = boons[state.randomInt(boons.length)];
        state.grant(boon, 200);
        state.addScore(40);
        state.say("A GIFT: " + boon.label, BANNER_TICKS + 10);
    }

    static void moveMobs(State state) {
        for (Mob mob : new ArrayList<>(state.mobs())) {
            if (!mob.readyToMove(state.tick()))
                continue;
            Pos target = mobStep(state, mob);
            if (target != null)
                state.moveMob(mob, target);
        }
    }

    /** Hunters close in, prey backs away, everything else drifts. */
    static Pos mobStep(State state, Mob mob) {
        Board board = state.board();
        Pos head = state.head();
        List<Pos> options = new ArrayList<>();
        for (Direction d : Direction.values()) {
            Pos candidate = board.wrap(mob.pos().step(d));
            if (!walkable(state, mob, candidate))
                continue;
            options.add(candidate);
        }
        if (options.isEmpty())
            return null;
        Behaviour behaviour = mob.species().behaviour;
        if (behaviour == Behaviour.DRIFT)
            return options.get(state.randomInt(options.size()));

        Pos best = options.get(0);
        int bestDistance = distance(best, head);
        for (Pos option : options) {
            int d = distance(option, head);
            boolean better = behaviour == Behaviour.HUNT ? d < bestDistance : d > bestDistance;
            if (better) {
                best = option;
                bestDistance = d;
            }
        }
        return best;
    }

    static boolean walkable(State state, Mob mob, Pos pos) {
        Board board = state.board();
        if (board.contains(pos, Door.class))
            return false;
        if (board.contains(pos, Creature.class))
            return false;
        if (board.contains(pos, Wall.class))
            return mob.species().phases;
        return true;
    }

    static int distance(Pos a, Pos b) {
        return Math.abs(a.row - b.row) + Math.abs(a.col - b.col);
    }

    /** A creature that walked onto the snake gets the same treatment as walking into one. */
    static boolean caught(State state) {
        Mob mob = state.mobAt(state.head());
        if (mob == null)
            return false;
        meet(state, state.head());
        return state.isOver();
    }

    static void spawnTreat(State state) {
        state.placeTreat(Treat.roll(state.randomInt(Treat.totalWeight())));
    }

    static void reinforce(State state) {
        if (state.mobs().size() >= MOB_CEILING)
            return;
        state.spawnMob(Level.inhabitant(state.level(), state.randomInt(100)));
        state.say("SOMETHING STIRS", BANNER_TICKS);
    }

    static void revealDoor(State state) {
        state.openDoor();
        if (state.doorOpen())
            state.say("A DOOR OPENS", BANNER_TICKS + 12);
    }

    static void enterNextLevel(State state) {
        state.addScore(100 * state.level());
        state.enterLevel(state.level() + 1);
        state.say(Level.name(state.level()), 34);
    }

    /** Powers bend the clock. */
    static int pace(Config config, State state) {
        if (state.paused())
            return 40;
        if (state.has(Effect.HASTE))
            return Math.max(20, config.stepMs / 2);
        if (state.has(Effect.DRAG))
            return config.stepMs * 2;
        return config.stepMs;
    }

    static void render(View view, State state) {
        view.draw(state.board(), state.tick());
        view.hud(state.board(), hud(state));
        String banner = state.banner();
        if (banner != null && !banner.isEmpty())
            view.banner(state.board(), banner);
        if (state.bannerStale())
            view.clearBanner(state.board());
    }

    static String hud(State state) {
        StringBuilder line = new StringBuilder();
        line.append("SCORE ").append(state.score())
            .append("  LEN ").append(state.length())
            .append("  ").append(Level.name(state.level()));
        int toGo = Level.quota(state.level()) - state.eatenHere();
        line.append(state.doorOpen() ? "  DOOR OPEN" : "  DOOR IN " + Math.max(0, toGo));
        for (Effect effect : Effect.values()) {
            if (state.has(effect))
                line.append("  ").append(effect.label).append(' ').append(state.remaining(effect) / 10);
        }
        return line.toString();
    }

    static boolean reachedTickLimit(Config config, State state) {
        return config.maxTicks > 0 && state.tick() >= config.maxTicks;
    }

    static int score(State state) {
        return state.score();
    }

    private void finale() {
        if (!isOver())
            return;
        view.draw(state.board(), state.tick());
        view.hud(state.board(), hud(state));
        view.banner(state.board(), "GAME OVER    SCORE " + state.score());
    }
}
