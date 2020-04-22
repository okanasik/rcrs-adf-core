package data;


import java.util.HashMap;
import java.util.Map;

public final class Frame {
    public int time;
    public Change change;
    public Action action;
    public Map<String, String> infoMap;

    public Frame() {
        change = new Change();
        infoMap = new HashMap<>();
    }
}

