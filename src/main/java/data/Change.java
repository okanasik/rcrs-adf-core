package data;


import java.util.HashMap;
import java.util.Map;

public final class Change {
    public Map<Integer,Node> nodes;
    public Map<Integer,FireBrigade> firebrigades;
    public Map<Integer,Ambulance> ambulances;
    public Map<Integer,Police> polices;
    public Map<Integer,Human> civilians;

    public Change() {
        nodes = new HashMap<>();
        firebrigades = new HashMap<>();
        ambulances = new HashMap<>();
        polices = new HashMap<>();
        civilians = new HashMap<>();
    }
}
