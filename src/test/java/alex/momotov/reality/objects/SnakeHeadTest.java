package alex.momotov.reality.objects;

import com.github.tomaslanger.chalk.Chalk;
import junit.framework.TestCase;
import org.junit.Test;

public class SnakeHeadTest {

    @Test
    public void color() {
        System.out.println("************************");
        System.out.println(Chalk.on("X").magenta().bold().toString());
        System.out.println(Chalk.on("X").cyan().bold().toString());
        System.out.println(Chalk.on("X").gray().bold().toString());
        System.out.println(Chalk.on("X").magenta().bold().toString());
        System.out.println(Chalk.on("X").magenta().bold().toString());
        System.out.println("************************");
    }

}