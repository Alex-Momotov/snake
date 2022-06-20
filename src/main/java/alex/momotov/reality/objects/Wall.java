package alex.momotov.reality.objects;

import com.github.tomaslanger.chalk.Chalk;

public class Wall extends Obj {

    @Override
    String repr() {
        return Chalk.on("$").gray().bold().toString();
    }
}
