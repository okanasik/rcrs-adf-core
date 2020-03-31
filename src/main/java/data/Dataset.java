package data;

import adf.agent.info.AgentInfo;
import adf.agent.info.ScenarioInfo;
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Dataset {
    private static final String AMBULANCEURN = StandardEntityURN.AMBULANCE_TEAM.toString();
    private static final String POLICEURN = StandardEntityURN.POLICE_FORCE.toString();
    private static final String FIREBRIGADEURN = StandardEntityURN.FIRE_BRIGADE.toString();

    private Episode episode;
    private ActionType actionType;
    private static Object lockObject = new Object();
    private boolean saved;

    public Dataset() {}

    public void start(WorldInfo worldInfo, AgentInfo agentInfo, String scenario, String team) {
        episode = new Episode();
        episode.scenario = scenario;
        episode.team = team;
        Agent agent = new Agent();
        agent.agentId = agentInfo.getID().getValue();
        agent.agentType = getAgentType(agentInfo.me().getURN());
        episode.agent = agent;
        saved = false;

        Graph graph = new Graph();
        episode.graph = graph;
        fillGraph(worldInfo, graph);

        // add firebrigades
        for (StandardEntity se : worldInfo.getEntitiesOfType(StandardEntityURN.FIRE_BRIGADE)) {
            episode.firebrigades.put(se.getID().getValue(), createFireBrigade(se));
        }

        // add ambulances
        for (StandardEntity se : worldInfo.getEntitiesOfType(StandardEntityURN.AMBULANCE_TEAM)) {
            episode.ambulances.put(se.getID().getValue(), createAmbulance(se));
        }

        // add polices
        for (StandardEntity se : worldInfo.getEntitiesOfType(StandardEntityURN.POLICE_FORCE)) {
            episode.polices.put(se.getID().getValue(), createPolice(se));
        }

        // set the default action type
        if (agent.agentType == AgentType.AMBULANCE) {
            actionType = ActionType.LOAD;
        } else if (agent.agentType == AgentType.FIREBRIGADE) {
            actionType = ActionType.EXTINGUISH;
        } else if (agent.agentType == AgentType.POLICE) {
            actionType = ActionType.CLEAR;
        }
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

    private data.Ambulance createAmbulance(StandardEntity se) {
        data.Ambulance datasetamb = new data.Ambulance();
        setHumanProperties(datasetamb, (Human) se);
        datasetamb.type = AgentType.AMBULANCE;
        //TODO: how to set loaded
        datasetamb.loaded = 0;
        return datasetamb;
    }

    private data.FireBrigade createFireBrigade(StandardEntity se) {
        data.FireBrigade datasetfb = new data.FireBrigade();
        setHumanProperties(datasetfb, (Human) se);
        datasetfb.type = AgentType.FIREBRIGADE;
        datasetfb.water = ((FireBrigade) se).getWater();
        return datasetfb;
    }

    private data.Police createPolice(StandardEntity se) {
        data.Police datasetPolice = new data.Police();
        setHumanProperties(datasetPolice, (Human) se);
        datasetPolice.type = AgentType.POLICE;
        return datasetPolice;
    }

    public void addFrame(WorldInfo worldInfo, AgentInfo agentInfo) {
        Frame frame = new Frame();
        episode.frames.add(frame);
        frame.time = agentInfo.getTime();

        // add itself as a changed entity
        if (agentInfo.me().getURN().equals(FIREBRIGADEURN)) {
            frame.change.firebrigades.put(agentInfo.getID().getValue(), createFireBrigade(agentInfo.me()));
        } else if (agentInfo.me().getURN().equals(AMBULANCEURN)) {
            frame.change.ambulances.put(agentInfo.getID().getValue(), createAmbulance(agentInfo.me()));
        } else if (agentInfo.me().getURN().equals(POLICEURN)) {
            frame.change.polices.put(agentInfo.getID().getValue(), createPolice(agentInfo.me()));
        } else {
            System.err.println("Unknown agent type!!" + agentInfo.me().getStandardURN());
            System.exit(1);
        }

        for (EntityID id : worldInfo.getChanged().getChangedEntities()) {
            StandardEntity se = worldInfo.getEntity(id);
            switch (se.getStandardURN()) {
                case AMBULANCE_CENTRE:
                    frame.change.nodes.put(se.getID().getValue(), createBuilding(se, NodeType.AMBULANCECENTRE));
                    break;
                case POLICE_OFFICE:
                    frame.change.nodes.put(se.getID().getValue(), createBuilding(se, NodeType.POLICEOFFICE));
                    break;
                case FIRE_STATION:
                    frame.change.nodes.put(se.getID().getValue(), createBuilding(se, NodeType.FIRESTATION));
                    break;
                case REFUGE:
                    frame.change.nodes.put(se.getID().getValue(), createBuilding(se, NodeType.REFUGE));
                    break;
                case GAS_STATION:
                    frame.change.nodes.put(se.getID().getValue(), createBuilding(se, NodeType.GASSTATION));
                    break;
                case BUILDING:
                    frame.change.nodes.put(se.getID().getValue(), createBuilding(se, NodeType.BUILDING));
                    break;
                case ROAD:
                    frame.change.nodes.put(se.getID().getValue(), createRoad(se, NodeType.ROAD, worldInfo));
                    break;
                case HYDRANT:
                    frame.change.nodes.put(se.getID().getValue(), createRoad(se, NodeType.HYDRANT, worldInfo));
                    break;
                case AMBULANCE_TEAM:
                    frame.change.ambulances.put(se.getID().getValue(), createAmbulance(se));
                    break;
                case POLICE_FORCE:
                    frame.change.polices.put(se.getID().getValue(), createPolice(se));
                    break;
                case FIRE_BRIGADE:
                    frame.change.firebrigades.put(se.getID().getValue(), createFireBrigade(se));
                    break;
                case CIVILIAN:
                    data.Human civ = new data.Human();
                    setHumanProperties(civ, (Human)se);
                    frame.change.civilians.put(se.getID().getValue(), civ);
                    break;
            }
        }
    }

    private void fillGraph(WorldInfo worldInfo, Graph graph) {
        // add roads
        for (StandardEntity road : worldInfo.getEntitiesOfType(StandardEntityURN.ROAD)) {
            graph.nodes.put(road.getID().getValue(), createRoad(road, NodeType.ROAD, worldInfo));
        }
        // add hydrants
        for (StandardEntity hydrant : worldInfo.getEntitiesOfType(StandardEntityURN.HYDRANT)) {
            graph.nodes.put(hydrant.getID().getValue(), createRoad(hydrant, NodeType.HYDRANT, worldInfo));
        }
        // add normal buildings
        for (StandardEntity building : worldInfo.getEntitiesOfType(StandardEntityURN.BUILDING)) {
            graph.nodes.put(building.getID().getValue(), createBuilding(building, NodeType.BUILDING));
        }
        // add refuge
        for (StandardEntity building : worldInfo.getEntitiesOfType(StandardEntityURN.REFUGE)) {
            graph.nodes.put(building.getID().getValue(), createBuilding(building, NodeType.REFUGE));
        }
        // add gas stations
        for (StandardEntity building : worldInfo.getEntitiesOfType(StandardEntityURN.GAS_STATION)) {
            graph.nodes.put(building.getID().getValue(), createBuilding(building, NodeType.GASSTATION));
        }
        // add ambulance centre
        for (StandardEntity building : worldInfo.getEntitiesOfType(StandardEntityURN.AMBULANCE_CENTRE)) {
            graph.nodes.put(building.getID().getValue(), createBuilding(building, NodeType.AMBULANCECENTRE));
        }
        // add police office
        for (StandardEntity building : worldInfo.getEntitiesOfType(StandardEntityURN.POLICE_OFFICE)) {
            graph.nodes.put(building.getID().getValue(), createBuilding(building, NodeType.POLICEOFFICE));
        }
        // add fire station
        for (StandardEntity building : worldInfo.getEntitiesOfType(StandardEntityURN.FIRE_STATION)) {
            graph.nodes.put(building.getID().getValue(), createBuilding(building, NodeType.FIRESTATION));
        }

        // add edges
        for (data.Node node : graph.nodes.values()) {
            Area area = (Area) worldInfo.getEntity(new EntityID(node.id));
            for (EntityID eId : area.getNeighbours()) {
                Area nearArea = (Area) worldInfo.getEntity(eId);
                data.Edge edge = new data.Edge();
                edge.from = area.getID().getValue();
                edge.to = eId.getValue();
                edge.weight = (int) Math.hypot(area.getX() - nearArea.getX(), area.getY() - nearArea.getY());
                graph.edges.add(edge);
            }
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

    private data.Road createRoad(StandardEntity se, NodeType type, WorldInfo worldInfo) {
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
        return datasetRoad;
    }

    private data.Building createBuilding(StandardEntity se, NodeType type) {
        data.Building datasetBuilding = new data.Building();
        datasetBuilding.type = type;
        datasetBuilding.id = se.getID().getValue();
        datasetBuilding.x = ((Building)se).getX();
        datasetBuilding.y = ((Building)se).getY();
        datasetBuilding.area = ((Building)se).getGroundArea();
        datasetBuilding.floors = ((Building)se).getFloors();
        datasetBuilding.fieryness = ((Building)se).isFierynessDefined() ? ((Building)se).getFieryness() : 0;
        datasetBuilding.brokennes = ((Building)se).isBrokennessDefined() ? ((Building)se).getBrokenness() : 0;
        return datasetBuilding;
    }

    public void addAction(WorldInfo worldInfo, AgentInfo agentInfo, ScenarioInfo scenarioInfo, EntityID targetId) {
        Action action = new Action();
        if (targetId == null) {
            action.targetId = 0;
            action.type = ActionType.NULL;
        } else {
            action.type = actionType;
            action.targetId = targetId.getValue();
        }
        episode.frames.get(episode.frames.size()-1).action = action;

        if (agentInfo.getTime() == scenarioInfo.getKernelTimesteps()) {
            saveAsJson(worldInfo, agentInfo, scenarioInfo);
        }
    }

    private void saveAsJson(WorldInfo worldInfo, AgentInfo agentInfo, ScenarioInfo scenarioInfo) {
        synchronized (lockObject) {
            String fileName = createFileName(scenarioInfo, agentInfo);
            System.out.println(agentInfo.getID() + ": saving the file:" + fileName);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try {
                String episodeJsonString = gson.toJson(episode);
                ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(fileName + ".zip"));
                zipOut.setLevel(9); // maximum compression
                ZipEntry entry = new ZipEntry(fileName.substring(fileName.lastIndexOf("/") + 1) + ".json");
                zipOut.putNextEntry(entry);
                zipOut.write(episodeJsonString.getBytes());
                zipOut.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            System.out.println(agentInfo.getID() + ": file is saved!");
            lockObject.notify();
            saved = true;
        }
    }

    public boolean isSaved() {
        return saved;
    }

    private static String createFileName(ScenarioInfo scenarioInfo, AgentInfo agentInfo) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String agentType = agentInfo.me().getURN().substring(agentInfo.me().getURN().lastIndexOf(":")+1);
        String datasetFileName = scenarioInfo.getScenarioName()+"_"+scenarioInfo.getTeam()+"_"+agentType+"_"+agentInfo.getID()+"_"+timeFormat.format(new Date());
        datasetFileName = "../dataset/" + datasetFileName;
        return datasetFileName;
    }
}
