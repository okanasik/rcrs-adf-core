package data;


import java.util.ArrayList;
import java.util.List;

public final class Episode {
    public String scenario;
    public String team;
    public int agentId;
    public AgentType agentType;
    public List<Frame> frames;

    public Episode() {
        frames = new ArrayList<>();
    }
}

