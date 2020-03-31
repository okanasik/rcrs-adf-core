package data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Episode {
    public String scenario;
    public String team;
    public Agent agent;
    public Graph graph;
    public List<Frame> frames;
    public Map<Integer,Ambulance> ambulances;
    public Map<Integer,FireBrigade> firebrigades;
    public Map<Integer,Police> polices;

    public Episode() {
        graph = new Graph();
        frames = new ArrayList<>();
        ambulances = new HashMap<>();
        firebrigades = new HashMap<>();
        polices = new HashMap<>();
    }
}

