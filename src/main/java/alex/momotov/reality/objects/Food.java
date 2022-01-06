package alex.momotov.reality.objects;

import com.github.tomaslanger.chalk.Chalk;

public class Food extends Obj {

    @Override
    String repr() {
        return Chalk.on("@").red().bold().toString();
    }
}
