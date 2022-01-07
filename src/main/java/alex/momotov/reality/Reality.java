package alex.momotov.reality;

import alex.momotov.Utils;
import alex.momotov.reality.objects.Food;
import alex.momotov.reality.objects.Obj;
import alex.momotov.reality.objects.Space;
import alex.momotov.reality.objects.Snake;
import jline.ConsoleReader;
import static alex.momotov.reality.Direction.*;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;

public class Reality implements Runnable {


    private final Field<Obj> field;
    private Direction direction;
    private final Queue<XY> tail;
    private final XY head;
    private int tailLength = 1;

    private static final int STEP_MS = 121;
    private static final int FOOD_INTERVAL_MS = 2_500;

    public Reality(int rows, int cols) {
        field = new Field<>(rows, cols);
        field.fill(() -> new Space());
        field.print();

        direction = Direction.DOWN;
        head = new XY(0, 0);
        tail = new ArrayDeque<>(1000);

        movementLoop();
        foodLoop();
    }

    @Override
    public void run() {
        try {
            ConsoleReader reader = new ConsoleReader();
            while (true) {
                int keyCode = reader.readVirtualKey();
                updateDirection(keyCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateDirection(int keyCode) {
        Direction newDirection = Direction.fromCode(keyCode);
        if (direction.isOposite(newDirection))
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
            newRow = field.rows - 1;
        if (newCol < 0)
            newCol = field.cols - 1;
        if (newRow >= field.rows)
            newRow = 0;
        if (newCol >= field.cols)
            newCol = 0;

        tail.add(new XY(head.x, head.y));
        if (tail.size() > tailLength) {
            XY toRemove = tail.remove();
            field.set(toRemove.x, toRemove.y, new Space());
        }

        head.x = newRow;
        head.y = newCol;
        if (field.get(head.x, head.y) instanceof Food) {
            tailLength += 1;
        }
        field.set(head.x, head.y, new Snake());
    }

    private void foodLoop() {
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    XY food = generateFood();
                    field.set(food.x, food.y, new Food());
                    Utils.sleep(FOOD_INTERVAL_MS);
                }
            }
        }.start();
    }

    private XY generateFood() {
        Random rand = new Random();
        XY food = new XY(rand.nextInt(field.rows), rand.nextInt(field.cols));
        while (tail.contains(food) || head.equals(food)) {
            food = new XY(rand.nextInt(field.rows), rand.nextInt(field.cols));
        }
        return food;
    }
}
