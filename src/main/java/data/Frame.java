package data;


public final class Frame {
    public int time;
    public Change change;
    public Action action;

    public Frame() {
        change = new Change();
    }
}

