package alex.momotov.reality.objects;

import alex.momotov.reality.Direction;
import com.github.tomaslanger.chalk.Chalk;

public class SnakeHead extends Obj {

    private String character;

    public SnakeHead(String character) {
        this.character = character;
    }

    @Override
    String repr() {
        return Chalk.on(character).magenta().bold().toString();
    }

    public static SnakeHead left() {
        return new SnakeHead("ᐊ");
    }

    public static SnakeHead right() {
        return new SnakeHead("ᐅ");
    }

    public static SnakeHead up() {
        return new SnakeHead("ᐃ");
    }

    public static SnakeHead down() {
        return new SnakeHead("ᐁ");
    }

    public static SnakeHead fromDirection(Direction direction) {
        if (direction == Direction.DOWN)
            return down();
        else if (direction == Direction.UP)
            return up();
        else if (direction == Direction.LEFT)
            return left();
        else if (direction == Direction.RIGHT)
            return right();
        throw new RuntimeException("Not a valid direction");
    }

}
