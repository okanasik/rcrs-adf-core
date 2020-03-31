package data;

public class Human {
    public AgentType type;
    public int id;
    public int health;
    public int buriedness;
    public int damage;
    public int posId;
    public int x;
    public int y;

    @Override
    public boolean equals(Object other) {
        return ((Human)other).id == this.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
