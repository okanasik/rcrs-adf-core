package data;


public class Node {
    public int id;
    public NodeType type;
    public int x;
    public int y;

    @Override
    public boolean equals(Object other) {
        return ((Node)other).id == this.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}

