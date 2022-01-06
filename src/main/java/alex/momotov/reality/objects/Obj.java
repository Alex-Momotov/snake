package alex.momotov.reality.objects;

public abstract class Obj {

    abstract String repr();

    @Override
    public String toString() {
        return repr();
    }

}
