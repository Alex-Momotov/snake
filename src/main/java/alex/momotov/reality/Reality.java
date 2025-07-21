package alex.momotov.reality;

import alex.momotov.Utils;
import alex.momotov.reality.field.Field;
import alex.momotov.reality.objects.Food;
import alex.momotov.reality.objects.SnakeBody;
import alex.momotov.reality.objects.Wall;
import com.github.tomaslanger.chalk.Chalk;
import jline.console.ConsoleReader;

import static alex.momotov.App.*;
import static alex.momotov.reality.Direction.*;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;

public class Reality {

    private final Field field;
    private Direction direction;
    private final Queue<XY> tail;
    private final XY head;
    private int tailLength = 1;

    public Reality() {
        turnOffTerminalCursor();
        field = new Field(ROWS, COLS);
        field.print();
        if (WALLS)
            addWalls();

        direction = Direction.DOWN;
        head = new XY(1, 1);
        tail = new ArrayDeque<>(1000);

        movementLoop();
        foodLoop();
        keyboardLoop();
    }

    static void turnOffTerminalCursor() {
        System.out.println("\u001b[" + "?25l");
    }

    static void turnOnTerminalCursor() {
        System.out.println("\u001b[?25h");
    }

    private void addWalls() {
        for (int i = 0; i < field.cols(); i++) {
            field.add(0, i, new Wall());
        }
        for (int i = 0; i < field.rows(); i++) {
            field.add(i, 0, new Wall());
        }
        for (int i = 0; i < field.cols(); i++) {
            field.add(field.rows() - 1, i, new Wall());
        }
        for (int i = 0; i < field.rows(); i++) {
            field.add(i, field.cols() - 1, new Wall());
        }
    }

    public void keyboardLoop() {
        new Thread() {
            @Override
            public void run() {
                try {
                    ConsoleReader reader = new ConsoleReader();
                    while (true) {
                        int keyCode = reader.readCharacter();
                        updateDirection(keyCode);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void updateDirection(int keyCode) {
        Direction newDirection = Direction.fromCode(keyCode);
        if (newDirection == null || direction.isOposite(newDirection))
            return;
        direction = newDirection;
    }

    private void movementLoop() {
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    if (direction == UP)
                        updatePosition(head.x - 1, head.y);
                    else if (direction == DOWN)
                        updatePosition(head.x + 1, head.y);
                    else if (direction == LEFT)
                        updatePosition(head.x, head.y - 1);
                    else if (direction == RIGHT)
                        updatePosition(head.x, head.y + 1);
                    Utils.sleep(STEP_MS);
                }
            }
        }.start();
    }

    private void updatePosition(int newRow, int newCol) {
        if (newRow < 0)
            newRow = field.rows() - 1;
        if (newCol < 0)
            newCol = field.cols() - 1;
        if (newRow >= field.rows())
            newRow = 0;
        if (newCol >= field.cols())
            newCol = 0;

        if ((COLLISION && field.get(newRow, newCol).contains(SnakeBody.class)) || field.get(newRow, newCol).contains(Wall.class)) {
            gameOver();
        }

        tail.add(new XY(head.x, head.y));
        if (tail.size() > tailLength) {
            XY toRemove = tail.remove();
            field.remove(toRemove.x, toRemove.y, SnakeBody.class);
        }

        head.x = newRow;
        head.y = newCol;
        field.add(head.x, head.y, new SnakeBody());

        if (field.get(head.x, head.y).contains(Food.class)) {
            tailLength += 1;
            field.remove(head.x, head.y, Food.class);
        }
    }

    private void foodLoop() {
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    XY food = generateFood();
                    field.add(food.x, food.y, new Food());
                    Utils.sleep(FOOD_INTERVAL_MS);
                }
            }
        }.start();
    }

    private XY generateFood() {
        Random rand = new Random();
        XY food = new XY(rand.nextInt(field.rows() - 2) + 1, rand.nextInt(field.cols() - 2) + 1);
        while (tail.contains(food) || head.equals(food)) {
            food = new XY(rand.nextInt(field.rows()), rand.nextInt(field.cols()));
        }
        return food;
    }

    private void gameOver() {
        String message = "Game Over";
        field.message(Chalk.on(message).red().bold().toString(), message.length());

        field.seek(field.rows(), field.cols());
        field.sysPrintLn();
        turnOnTerminalCursor();
        System.exit(0);
    }
}
