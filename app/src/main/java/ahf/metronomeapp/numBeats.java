package ahf.metronomeapp;

/**
 * Created by Alex on 5/6/2015.
 */
public enum numBeats {

    one("1"),
    two("2"),
    three("3"),
    four("4"),
    five("5"),
    six("6"),
    seven("7"),
    eight("8");

    private String beat;

    numBeats(String beat) {
        this.beat = beat;
    }

    @Override public String toString() {
        return beat;
    }

    public short getNum() {
        return Short.parseShort(beat);
    }


}
