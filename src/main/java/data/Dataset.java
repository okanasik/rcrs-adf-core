package data;

import adf.agent.info.AgentInfo;
import adf.agent.info.WorldInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.EntityID;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Dataset {
    private static final String AMBULANCEURN = StandardEntityURN.AMBULANCE_TEAM.toString();
    private static final String POLICEURN = StandardEntityURN.POLICE_FORCE.toString();
    private static final String FIREBRIGADEURN = StandardEntityURN.FIRE_BRIGADE.toString();

    private Episode episode;
    public Dataset() {
        episode = new Episode();
    }

    public void start(String scenario, String team, int agentId, String agentURN) {
        episode.scenario = scenario;
        episode.team = team;
        episode.agentId = agentId;
        episode.agentType = getAgentType(agentURN);
    }

    private AgentType getAgentType(String urn) {
        AgentType type = null;
        if (urn.equalsIgnoreCase(AMBULANCEURN)) {
            type = AgentType.AMBULANCE;
        } else if (urn.equalsIgnoreCase(POLICEURN)) {
            type = AgentType.POLICE;
        } else if (urn.equalsIgnoreCase(FIREBRIGADEURN)) {
            type = AgentType.FIREBRIGADE;
        }
        return type;
    }

    public void addFrame(WorldInfo worldInfo, AgentInfo agentInfo) {
        Frame frame = new Frame();
        episode.frames.add(frame);
        frame.time = agentInfo.getTime();
        Graph graph = new Graph();

        // add roads
        for (StandardEntity road : worldInfo.getEntitiesOfType(StandardEntityURN.ROAD)) {
            addRoadToFrame(road, NodeType.ROAD, worldInfo, graph);
        }
        // add hydrants
        for (StandardEntity hydrant : worldInfo.getEntitiesOfType(StandardEntityURN.HYDRANT)) {
            addRoadToFrame(hydrant, NodeType.HYDRANT, worldInfo, graph);
        }

        // add normal buildings
        for (StandardEntity building : worldInfo.getEntitiesOfType(StandardEntityURN.BUILDING)) {
            addBuildingToFrame(building, NodeType.BUILDING, worldInfo, graph);
        }
        // add refuge
        for (StandardEntity building : worldInfo.getEntitiesOfType(StandardEntityURN.REFUGE)) {
            addBuildingToFrame(building, NodeType.REFUGE, worldInfo, graph);
        }
        // add gas stations
        for (StandardEntity building : worldInfo.getEntitiesOfType(StandardEntityURN.GAS_STATION)) {
            addBuildingToFrame(building, NodeType.GASSTATION, worldInfo, graph);
        }
        // add ambulance centre
        for (StandardEntity building : worldInfo.getEntitiesOfType(StandardEntityURN.AMBULANCE_CENTRE)) {
            addBuildingToFrame(building, NodeType.AMBULANCECENTRE, worldInfo, graph);
        }
        // add police office
        for (StandardEntity building : worldInfo.getEntitiesOfType(StandardEntityURN.POLICE_OFFICE)) {
            addBuildingToFrame(building, NodeType.POLICEOFFICE, worldInfo, graph);
        }
        // add fire station
        for (StandardEntity building : worldInfo.getEntitiesOfType(StandardEntityURN.FIRE_STATION)) {
            addBuildingToFrame(building, NodeType.FIRESTATION, worldInfo, graph);
        }

        // add edges
        //TODO: add two edges for each edge
        for (data.Node node : graph.nodes) {
            Area area = (Area) worldInfo.getEntity(new EntityID(node.id));
            for (EntityID eId : area.getNeighbours()) {
                Area nearArea = (Area)worldInfo.getEntity(eId);
                data.Edge edge = new data.Edge();
                edge.from = area.getID().getValue();
                edge.to = eId.getValue();
                edge.weight = (int)Math.hypot(area.getX()-nearArea.getX(), area.getY()-nearArea.getY());
                graph.edges.add(edge);
            }
        }

        // add civilians
        for (StandardEntity se : worldInfo.getEntitiesOfType(StandardEntityURN.CIVILIAN)) {
            data.Human datasetHuman = new data.Human();
            setHumanProperties(datasetHuman, (Human)se);
            frame.civilians.add(datasetHuman);
        }

        // add firebrigades
        for (StandardEntity se : worldInfo.getEntitiesOfType(StandardEntityURN.FIRE_BRIGADE)) {
            data.FireBrigade datasetfb = new data.FireBrigade();
            setHumanProperties(datasetfb, (Human)se);
            datasetfb.type = AgentType.FIREBRIGADE;
            datasetfb.water = ((FireBrigade)se).getWater();
            frame.firebrigades.add(datasetfb);
        }

        // add ambulances
        for (StandardEntity se : worldInfo.getEntitiesOfType(StandardEntityURN.AMBULANCE_TEAM)) {
            data.Ambulance datasetamb = new data.Ambulance();
            setHumanProperties(datasetamb, (Human)se);
            datasetamb.type = AgentType.AMBULANCE;
            //TODO: how to set loaded
            datasetamb.loaded = 0;
            frame.ambulances.add(datasetamb);
        }

        // add polices
        for (StandardEntity se : worldInfo.getEntitiesOfType(StandardEntityURN.POLICE_FORCE)) {
            data.Police datasetPolice = new data.Police();
            setHumanProperties(datasetPolice, (Human)se);
            datasetPolice.type = AgentType.POLICE;
            frame.polices.add(datasetPolice);
        }
    }

    private void setHumanProperties(data.Human datasetHuman, Human human) {
        datasetHuman.id = human.getID().getValue();
        datasetHuman.type = AgentType.CIVILIAN;
        datasetHuman.posId = human.getPosition().getValue();
        datasetHuman.x = human.isXDefined() ? human.getX() : 0;
        datasetHuman.y = human.isYDefined() ? human.getY() : 0;
        datasetHuman.health = human.isHPDefined() ? human.getHP() : 0;
        datasetHuman.buriedness = human.isBuriednessDefined() ? human.getBuriedness() : 0;
        datasetHuman.damage = human.isDamageDefined() ? human.getDamage() : 0;
    }

    private void addRoadToFrame(StandardEntity se, NodeType type, WorldInfo worldInfo, Graph graph) {
        data.Road datasetRoad = new data.Road();
        datasetRoad.type = type;
        datasetRoad.id = se.getID().getValue();
        datasetRoad.x = ((Road) se).getX();
        datasetRoad.y = ((Road) se).getY();
        datasetRoad.repairCost = 0;

        if (((Road) se).isBlockadesDefined()) {
            for (EntityID bId : ((Road) se).getBlockades()) {
                Blockade b = (Blockade) worldInfo.getEntity(bId);
                if (b.isRepairCostDefined()) {
                    datasetRoad.repairCost += b.getRepairCost();
                }
            }
        }
        graph.nodes.add(datasetRoad);
    }

    private void addBuildingToFrame(StandardEntity se, NodeType type, WorldInfo worldInfo, Graph graph) {
        data.Building datasetBuilding = new data.Building();
        datasetBuilding.type = type;
        datasetBuilding.id = se.getID().getValue();
        datasetBuilding.x = ((Building)se).getX();
        datasetBuilding.y = ((Building)se).getY();
        datasetBuilding.area = ((Building)se).getGroundArea();
        datasetBuilding.floors = ((Building)se).getFloors();
        datasetBuilding.fieryness = ((Building)se).isFierynessDefined() ? ((Building)se).getFieryness() : 0;
        datasetBuilding.brokennes = ((Building)se).isBrokennessDefined() ? ((Building)se).getBrokenness() : 0;
        graph.nodes.add(datasetBuilding);
    }

    public void addAction(Action action) {
        List<Frame> frames = episode.frames;
        frames.get(frames.size()-1).action = action;
    }

    public void saveAsJson(String fileName) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            String episodeJsonString = gson.toJson(episode);
            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(fileName + ".zip"));
            zipOut.setLevel(9); // maximum compression
            ZipEntry entry = new ZipEntry(fileName.substring(fileName.lastIndexOf("/")+1)+".json");
            zipOut.putNextEntry(entry);
            zipOut.write(episodeJsonString.getBytes());
            zipOut.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
