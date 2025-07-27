package alex.momotov.reality.objects;

import com.github.tomaslanger.chalk.Chalk;

public class SnakeBody2 extends Obj {

    @Override
    String repr() {
        return Chalk.on("X").magenta().bold().toString();
    }

}
