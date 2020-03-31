package data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Wrapper {
    public Wrapper() {

    }

    public void test() {
        Episode episode = new Episode();
        episode.scenario = "NY1";
        episode.team = "ait";

        Frame frame = new Frame();
        frame.time = 1;
        Graph graph = new Graph();
        Road node0 = new Road();
        node0.id = 7630;
        node0.type = NodeType.ROAD;
        node0.x = 197;
        node0.y = 54;
        node0.repairCost = 100;
//        graph.nodes.add(node0);
        Building node1 = new Building();
        node1.id = 7630;
        node1.area = 12123;
        node1.type = NodeType.BUILDING;
        node1.x = 197;
        node1.y = 54;
        node1.brokennes = 12;
        node1.fieryness = 59;
        node1.floors = 5;
//        graph.nodes.add(node1);
        Edge edge = new Edge();
        edge.from = node0.id;
        edge.to = node1.id;
        edge.weight = (int)Math.hypot(node0.x = node1.x, node0.y - node1.y);
//        graph.edges.add(edge);
//        frame.graph = graph;

        // ambulances
        Ambulance amb = new Ambulance();
        amb.type = AgentType.AMBULANCE;
        amb.health = 100;
        amb.id = 1993;
        amb.loaded = 34;
        amb.posId = 632;
        amb.x = 23;
        amb.y = 1;
//        frame.ambulances.add(amb);

        // firebrigades
        FireBrigade fb = new FireBrigade();
        fb.id = 233;
        fb.health = 84;
        fb.posId = 2134;
        fb.type = AgentType.FIREBRIGADE;
        fb.water = 89;
        fb.x = 98;
        fb.y = 39;
//        frame.firebrigades.add(fb);

        // polices
        Police police = new Police();
        police.id = 7542;
        police.health = 76;
        police.posId = 8165;
        police.type = AgentType.POLICE;
        police.x = 21;
        police.y = 76;
//        frame.polices.add(police);

        // civilians
        Human civ0 = new Human();
        civ0.id = 100;
        civ0.health = 45;
        civ0.posId = 33;
        civ0.type = AgentType.CIVILIAN;
        civ0.x = 66;
        civ0.y = 99;
//        frame.civilians.add(civ0);

        Human civ1 = new Human();
        civ1.id = 101;
        civ1.health = 46;
        civ1.posId = 34;
        civ1.type = AgentType.CIVILIAN;
        civ1.x = 67;
        civ1.y = 100;
//        frame.civilians.add(civ1);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(episode);
        System.out.println(jsonOutput);

        // deserialize
        Episode ep2 = gson.fromJson(jsonOutput, Episode.class);

    }

    public static void main(String[] args) {
        Wrapper wrapper = new Wrapper();
        wrapper.test();
    }
}
