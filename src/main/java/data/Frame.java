package data;


import java.util.ArrayList;
import java.util.List;

public final class Frame {
    public int time;
    public Graph graph;
    public List<FireBrigade> firebrigades;
    public List<Ambulance> ambulances;
    public List<Police> polices;
    public List<Human> civilians;
    public Action action;

    public Frame() {
        firebrigades = new ArrayList<>();
        ambulances = new ArrayList<>();
        polices = new ArrayList<>();
        civilians = new ArrayList<>();
    }
}

