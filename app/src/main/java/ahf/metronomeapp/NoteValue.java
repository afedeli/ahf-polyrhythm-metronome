package ahf.metronomeapp;

/**
 * Created by Alex on 5/6/2015.
 */
public enum NoteValue {

    quarterNote("4"),
    eighthNote("8"),
    sixteenthNote("16"),
    quintuplet("5"),
    septuplet("7"),
    nantuplet("9"),
    hendecuplet("11"),
    tridecuplet("13"),
    thirtysecondNote("32"),
    eightNote_Triplet("3"),
    sixteenthNote_Triplet("6");


    private String noteValue;

    NoteValue(String noteValue) {
        this.noteValue = noteValue;
    }

    @Override public String toString() {
        return noteValue;
    }

    public short getNum() {
        return Short.parseShort(noteValue);
    }
}
