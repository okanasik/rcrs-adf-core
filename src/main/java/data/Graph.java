package data;

import java.util.ArrayList;
import java.util.List;

public final class Graph {
    public List<Node> nodes;
    public List<Edge> edges;

    public Graph() {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
    }
}

