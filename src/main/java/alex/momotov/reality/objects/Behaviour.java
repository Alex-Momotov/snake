package alex.momotov.reality.objects;

/** How a creature decides where to step next. */
public enum Behaviour {

    HUNT,       // closes on the snake's head
    FLEE,       // keeps its distance
    DRIFT       // wanders
}
