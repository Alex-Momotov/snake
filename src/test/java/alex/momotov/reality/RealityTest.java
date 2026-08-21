package alex.momotov.reality;

import alex.momotov.Config;
import alex.momotov.reality.objects.Creature;
import alex.momotov.reality.objects.Edible;
import alex.momotov.reality.objects.SnakeBody;
import alex.momotov.reality.objects.Species;
import alex.momotov.reality.objects.Treat;
import alex.momotov.reality.objects.Wall;
import org.junit.Test;

import java.util.ArrayList;

import static alex.momotov.reality.Direction.*;
import static org.junit.Assert.*;

/** The game rules, exercised with no terminal in sight. */
public class RealityTest {

    private static Config config(int rows, int cols, boolean walls, boolean collision) {
        return new Config(rows, cols, 1, 1_000_000, walls, collision, 42L, 0, 1);
    }

    /** Creatures wander on their own; most rule tests want an empty stage. */
    private static State quietWorld(Config config) {
        State state = new State(config);
        for (Mob mob : new ArrayList<>(state.mobs()))
            state.removeMob(mob);
        while (state.has(Effect.SHIELD))
            state.fadeEffects();            // wear off the protection you arrive with
        return state;
    }

    private static void run(Config config, State state, int ticks) {
        for (int i = 0; i < ticks && !state.isOver(); i++)
            Reality.tick(config, state);
    }

    /** Coils the snake so the head has its own tail behind it and body to the right. */
    private static void coil(State state) {
        state.grow(2);
        state.advance(new Pos(6, 6));
        state.advance(new Pos(7, 6));
        state.advance(new Pos(7, 5));
    }

    @Test
    public void youArriveProtectedSoNothingCanKillYouOnTheDoorstep() {
        State state = new State(config(14, 14, true, true));
        assertTrue(state.has(Effect.SHIELD));
        assertTrue(state.remaining(Effect.SHIELD) > 20);
    }

    @Test
    public void nothingSpawnsOnTopOfYouOrInTheLaneAhead() {
        State state = new State(config(16, 22, true, true));
        for (Mob mob : state.mobs()) {
            assertTrue("spawned too close: " + mob.pos(),
                    Reality.distance(mob.pos(), state.head()) > 6);
            assertNotEquals("spawned straight ahead", state.head().row, mob.pos().row);
        }
    }

    @Test
    public void snakeIsOnTheBoardFromTheVeryFirstFrame() {
        State state = quietWorld(config(12, 12, true, true));
        assertEquals(2, state.length());
        assertEquals(new Pos(6, 5), state.head());
        assertEquals(new Pos(6, 4), state.tail());
        assertTrue(state.board().contains(state.tail(), SnakeBody.class));
    }

    @Test
    public void movingLeavesTheOldTailSquareEmpty() {
        Config config = config(12, 12, true, true);
        State state = quietWorld(config);
        Reality.tick(config, state);

        assertEquals(new Pos(6, 6), state.head());
        assertEquals(2, state.length());
        assertFalse("the tail square must be released",
                state.board().contains(new Pos(6, 4), SnakeBody.class));
    }

    @Test
    public void hittingAWallEndsTheGame() {
        Config config = config(12, 12, true, true);
        State state = quietWorld(config);
        run(config, state, 30);

        assertTrue(state.isOver());
        assertFalse("the snake must not end up inside the wall",
                state.board().contains(new Pos(6, 11), SnakeBody.class));
    }

    @Test
    public void followingItsOwnTailIsSafe() {
        Config config = config(12, 12, false, true);
        State state = quietWorld(config);
        coil(state);
        assertEquals(new Pos(7, 5), state.head());
        assertEquals(new Pos(6, 5), state.tail());

        state.steer(UP);
        Reality.tick(config, state);

        assertFalse("entering the square the tail is vacating must not kill", state.isOver());
        assertEquals(new Pos(6, 5), state.head());
        assertEquals(4, state.length());
    }

    @Test
    public void runningIntoItsOwnBodyStillKills() {
        Config config = config(12, 12, false, true);
        State state = quietWorld(config);
        coil(state);

        Reality.tick(config, state);        // (7,5) -> (7,6), a mid-body segment

        assertTrue(state.isOver());
    }

    @Test
    public void collisionCanBeTurnedOff() {
        Config config = config(12, 12, false, false);
        State state = quietWorld(config);
        coil(state);

        Reality.tick(config, state);

        assertFalse(state.isOver());
    }

    @Test
    public void aShieldCarriesYouThroughYourOwnBody() {
        Config config = config(12, 12, false, true);
        State state = quietWorld(config);
        coil(state);
        state.grant(Effect.SHIELD, 50);

        Reality.tick(config, state);

        assertFalse(state.isOver());
    }

    @Test
    public void phasingCarriesYouThroughWalls() {
        Config config = config(12, 12, true, true);
        State state = quietWorld(config);
        state.grant(Effect.PHASE, 300);
        run(config, state, 8);

        assertFalse("phasing should survive the wall", state.isOver());
    }

    @Test
    public void eatingGrowsTheSnakeAndClearsTheTreat() {
        Config config = config(12, 12, true, true);
        State state = quietWorld(config);
        Pos ahead = new Pos(6, 6);
        state.board().add(ahead, new Edible(Treat.APPLE));

        Reality.tick(config, state);

        assertEquals(3, state.length());
        assertEquals(Treat.APPLE.points, state.score());
        assertFalse(state.board().contains(ahead, Edible.class));
        assertTrue("the tail must stay put while growing",
                state.board().contains(new Pos(6, 4), SnakeBody.class));
    }

    @Test
    public void aBigTreatKeepsGrowingYouOverSeveralSteps() {
        Config config = config(14, 14, false, false);
        State state = quietWorld(config);
        state.board().add(new Pos(7, 7), new Edible(Treat.MELON));

        run(config, state, 4);

        assertEquals(2 + Treat.MELON.growth, state.length());
    }

    @Test
    public void rotShedsSegments() {
        Config config = config(14, 14, false, false);
        State state = quietWorld(config);
        state.grow(4);
        state.advance(new Pos(7, 7));
        state.advance(new Pos(7, 8));
        state.advance(new Pos(7, 9));
        state.advance(new Pos(7, 10));
        assertEquals(6, state.length());
        state.board().add(new Pos(7, 11), new Edible(Treat.ROT));

        Reality.tick(config, state);

        assertEquals(6 + Treat.ROT.growth, state.length());
    }

    @Test
    public void perishableTreatsRotAwayOnTheirOwn() {
        Config config = config(14, 14, false, false);
        State state = quietWorld(config);
        Pos spot = new Pos(2, 2);
        Edible melon = new Edible(Treat.MELON);
        melon.expiresAt(3);
        state.board().add(spot, melon);
        assertFalse(melon.expired(0));
        assertTrue(melon.expired(3));
    }

    @Test
    public void aTreatEffectIsGranted() {
        Config config = new Config(12, 12, 120, 1_000_000, true, true, 42L, 0, 1);
        State state = quietWorld(config);
        int normalPace = Reality.pace(config, state);
        state.board().add(new Pos(6, 6), new Edible(Treat.PEPPER));

        Reality.tick(config, state);

        assertTrue(state.has(Treat.PEPPER.effect));
        assertTrue("haste should quicken the clock", Reality.pace(config, state) < normalPace);

        State slowed = quietWorld(config);
        slowed.grant(Effect.DRAG, 50);
        assertTrue("drag should slow it", Reality.pace(config, slowed) > normalPace);
    }

    @Test
    public void hostileCreaturesAreFatalButShieldMakesYouThePredator() {
        Config config = config(12, 12, false, true);
        State deadly = quietWorld(config);
        deadly.spawnMob(Species.SPIDER);
        Mob spider = deadly.mobs().get(0);
        deadly.moveMob(spider, new Pos(6, 6));
        Reality.tick(config, deadly);
        assertTrue(deadly.isOver());

        State shielded = quietWorld(config);
        shielded.spawnMob(Species.SPIDER);
        shielded.moveMob(shielded.mobs().get(0), new Pos(6, 6));
        shielded.grant(Effect.SHIELD, 80);
        Reality.tick(config, shielded);
        assertFalse(shielded.isOver());
        assertTrue(shielded.mobs().isEmpty());
        assertTrue(shielded.score() > 0);
    }

    @Test
    public void friendlyCreaturesAreWorthCatching() {
        Config config = config(12, 12, false, true);
        State state = quietWorld(config);
        state.spawnMob(Species.MOUSE);
        state.moveMob(state.mobs().get(0), new Pos(6, 6));

        Reality.tick(config, state);

        assertFalse(state.isOver());
        assertTrue(state.mobs().isEmpty());
        assertEquals(Species.MOUSE.points, state.score());
    }

    @Test
    public void hunterscloseInAndPreyBacksAway() {
        Config config = config(14, 14, false, false);
        State state = quietWorld(config);
        state.spawnMob(Species.SPIDER);
        Mob spider = state.mobs().get(0);
        state.moveMob(spider, new Pos(1, 1));
        int before = Reality.distance(spider.pos(), state.head());
        Pos step = Reality.mobStep(state, spider);
        assertTrue("a hunter should close in",
                Reality.distance(step, state.head()) < before);

        state.spawnMob(Species.MOUSE);
        Mob mouse = state.mobs().get(1);
        state.moveMob(mouse, new Pos(6, 8));
        int mouseBefore = Reality.distance(mouse.pos(), state.head());
        assertTrue("prey should back away",
                Reality.distance(Reality.mobStep(state, mouse), state.head()) > mouseBefore);
    }

    @Test
    public void aDoorLeadsToTheNextWorld() {
        Config config = config(14, 14, true, true);
        State state = quietWorld(config);
        assertEquals(1, state.level());
        int scoreBefore = state.score();

        Reality.enterNextLevel(state);

        assertEquals(2, state.level());
        assertTrue(state.score() > scoreBefore);
        assertEquals("length carries across", 2, state.length());
        assertFalse(state.isOver());
    }

    @Test
    public void theDoorOnlyOpensOnceTheQuotaIsEaten() {
        Config config = config(14, 14, true, true);
        State state = quietWorld(config);
        assertFalse(state.doorOpen());
        state.openDoor();
        assertTrue(state.doorOpen());
    }

    @Test
    public void wrapsRoundTheEdgeWhenWallsAreOff() {
        Config config = config(12, 12, false, false);
        State state = quietWorld(config);
        run(config, state, 7);

        assertFalse(state.isOver());
        assertEquals(new Pos(6, 0), state.head());
    }

    @Test
    public void treatsOnlyEverLandOnAFreeSquare() {
        Config config = config(10, 10, true, true);
        State state = quietWorld(config);
        Board board = state.board();

        for (int i = 0; i < 200; i++)
            Reality.spawnTreat(state);

        for (Pos pos : board.positions()) {
            if (!board.contains(pos, Edible.class))
                continue;
            assertFalse("a treat landed in a wall at " + pos, board.contains(pos, Wall.class));
            assertFalse("a treat landed under the snake at " + pos,
                    board.contains(pos, SnakeBody.class));
            assertFalse("a treat landed on a creature at " + pos,
                    board.contains(pos, Creature.class));
        }
    }

    @Test
    public void pausingFreezesEverything() {
        Config config = config(12, 12, true, true);
        State state = quietWorld(config);
        Pos before = state.head();
        Reality.press(state, Key.PAUSE);
        run(config, state, 5);
        assertEquals(before, state.head());

        Reality.press(state, Key.PAUSE);
        Reality.tick(config, state);
        assertNotEquals(before, state.head());
    }

    @Test
    public void quittingEndsTheGame() {
        State state = quietWorld(config(12, 12, true, true));
        Reality.press(state, Key.QUIT);
        assertTrue(state.isOver());
    }

    @Test
    public void directlyReversingIsIgnored() {
        State state = quietWorld(config(12, 12, true, true));
        state.steer(LEFT);                  // the snake is heading RIGHT
        state.applyPendingDirection();
        assertEquals(RIGHT, state.direction());
    }

    @Test
    public void twoTurnsInsideOneTickCannotDoubleBack() {
        State state = quietWorld(config(12, 12, true, true));
        state.steer(UP);                    // legal while heading RIGHT
        state.steer(LEFT);                  // judged against RIGHT, not UP, so rejected
        state.applyPendingDirection();
        assertEquals(UP, state.direction());
    }

    @Test
    public void maxTicksStopsTheGame() {
        Config config = new Config(12, 12, 1, 1_000_000, false, false, 42L, 3, 1);
        State state = quietWorld(config);
        assertFalse(Reality.reachedTickLimit(config, state));
        run(config, state, 3);
        assertTrue(Reality.reachedTickLimit(config, state));
    }
}
