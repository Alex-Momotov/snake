package alex.momotov.reality.objects;

import com.github.tomaslanger.chalk.Chalk;

public class SnakeBody extends Obj {

    @Override
    String repr() {
        return Chalk.on("X").green().bold().toString();
    }

}
