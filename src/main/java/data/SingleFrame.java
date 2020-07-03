package data;

import adf.agent.info.AgentInfo;
import adf.agent.info.WorldInfo;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.EntityID;

import java.util.HashMap;
import java.util.Map;

public class SingleFrame {
    private Graph graph;
    private Map<Integer, Ambulance> ambulances;
    private Map<Integer, FireBrigade> firebrigades;
    private Map<Integer, Police> polices;
    private Map<Integer, Human> civilians;
    private Human agent;
    private int time;

    public SingleFrame() {
        graph = new Graph();
        firebrigades = new HashMap<>();
        ambulances = new HashMap<>();
        polices = new HashMap<>();
        civilians = new HashMap<>();
        agent = null;
        time = -1;
    }

    public void update(WorldInfo worldInfo, AgentInfo agentInfo) {
        time = agentInfo.getTime();
        updateGraph(worldInfo);
        updateAgents(worldInfo);

        if (ambulances.containsKey(agentInfo.getID().getValue())) {
            agent = ambulances.get(agentInfo.getID().getValue());
        } else if (firebrigades.containsKey(agentInfo.getID().getValue())) {
            agent = firebrigades.get(agentInfo.getID().getValue());
        } else if (polices.containsKey(agentInfo.getID().getValue())) {
            agent = polices.get(agentInfo.getID().getValue());
        }
    }

    private void updateGraph(WorldInfo worldInfo) {
        for (StandardEntity road : worldInfo.getEntitiesOfType(StandardEntityURN.ROAD)) {
            Road dataRoad = (Road)graph.nodes.get(road.getID().getValue());
            if (dataRoad == null) { dataRoad = new Road(); graph.nodes.put(road.getID().getValue(), dataRoad); }
            dataRoad.type = NodeType.ROAD;
            setRoad((rescuecore2.standard.entities.Road)road, dataRoad, worldInfo);
        }
        // add hydrants
        for (StandardEntity hydrant : worldInfo.getEntitiesOfType(StandardEntityURN.HYDRANT)) {
            Road dataHydrant = (Road)graph.nodes.get(hydrant.getID().getValue());
            if (dataHydrant == null) { dataHydrant = new Road(); graph.nodes.put(hydrant.getID().getValue(), dataHydrant); }
            dataHydrant.type = NodeType.HYDRANT;
            setRoad((rescuecore2.standard.entities.Road)hydrant, dataHydrant, worldInfo);
        }
        // add normal buildings
        for (StandardEntity building : worldInfo.getEntitiesOfType(StandardEntityURN.BUILDING)) {
            Building dataBuilding = (Building)graph.nodes.get(building.getID().getValue());
            if (dataBuilding == null) { dataBuilding = new Building(); graph.nodes.put(building.getID().getValue(), dataBuilding); }
            dataBuilding.type = NodeType.BUILDING;
            setBuilding((rescuecore2.standard.entities.Building)building, dataBuilding);
        }
        // add refuge
        for (StandardEntity building : worldInfo.getEntitiesOfType(StandardEntityURN.REFUGE)) {
            Building dataBuilding = (Building)graph.nodes.get(building.getID().getValue());
            if (dataBuilding == null) { dataBuilding = new Building(); graph.nodes.put(building.getID().getValue(), dataBuilding); }
            dataBuilding.type = NodeType.REFUGE;
            setBuilding((rescuecore2.standard.entities.Building)building, dataBuilding);
        }
        // add gas stations
        for (StandardEntity building : worldInfo.getEntitiesOfType(StandardEntityURN.GAS_STATION)) {
            Building dataBuilding = (Building)graph.nodes.get(building.getID().getValue());
            if (dataBuilding == null) { dataBuilding = new Building(); graph.nodes.put(building.getID().getValue(), dataBuilding); }
            dataBuilding.type = NodeType.GASSTATION;
            setBuilding((rescuecore2.standard.entities.Building)building, dataBuilding);
        }
        // add ambulance centre
        for (StandardEntity building : worldInfo.getEntitiesOfType(StandardEntityURN.AMBULANCE_CENTRE)) {
            Building dataBuilding = (Building)graph.nodes.get(building.getID().getValue());
            if (dataBuilding == null) { dataBuilding = new Building(); graph.nodes.put(building.getID().getValue(), dataBuilding); }
            dataBuilding.type = NodeType.AMBULANCECENTRE;
            setBuilding((rescuecore2.standard.entities.Building)building, dataBuilding);
        }
        // add police office
        for (StandardEntity building : worldInfo.getEntitiesOfType(StandardEntityURN.POLICE_OFFICE)) {
            Building dataBuilding = (Building)graph.nodes.get(building.getID().getValue());
            if (dataBuilding == null) { dataBuilding = new Building(); graph.nodes.put(building.getID().getValue(), dataBuilding); }
            dataBuilding.type = NodeType.POLICEOFFICE;
            setBuilding((rescuecore2.standard.entities.Building)building, dataBuilding);
        }
        // add fire station
        for (StandardEntity building : worldInfo.getEntitiesOfType(StandardEntityURN.FIRE_STATION)) {
            Building dataBuilding = (Building)graph.nodes.get(building.getID().getValue());
            if (dataBuilding == null) { dataBuilding = new Building(); graph.nodes.put(building.getID().getValue(), dataBuilding); }
            dataBuilding.type = NodeType.FIRESTATION;
            setBuilding((rescuecore2.standard.entities.Building)building, dataBuilding);
        }

        // if edges are not created yet create them
        if (graph.edges.size() == 0) {
            for (data.Node node : graph.nodes.values()) {
                Area area = (Area) worldInfo.getEntity(new EntityID(node.id));
                for (EntityID eId : area.getNeighbours()) {
                    Area neighArea = (Area) worldInfo.getEntity(eId);
                    data.Edge edge = new data.Edge();
                    edge.from = area.getID().getValue();
                    edge.to = eId.getValue();
                    edge.weight = (int) Math.hypot(area.getX() - neighArea.getX(), area.getY() - neighArea.getY());
                    graph.edges.add(edge);
                }
            }
        }
    }

    private void setRoad(rescuecore2.standard.entities.Road simRoad, Road dataRoad, WorldInfo worldInfo) {
        dataRoad.id = simRoad.getID().getValue();
        dataRoad.x = simRoad.isXDefined() ? simRoad.getX() : -1;
        dataRoad.y = simRoad.isYDefined() ? simRoad.getY() : -1;
        dataRoad.repairCost = 0;

        if (simRoad.isBlockadesDefined()) {
            for (EntityID bId : simRoad.getBlockades()) {
                Blockade b = (Blockade) worldInfo.getEntity(bId);
                if (b.isRepairCostDefined()) {
                    dataRoad.repairCost += b.getRepairCost();
                }
            }
        }
    }

    private void setBuilding(rescuecore2.standard.entities.Building simBuilding, Building dataBuilding) {
        dataBuilding.id = simBuilding.getID().getValue();
        dataBuilding.x = simBuilding.getX();
        dataBuilding.y = simBuilding.getY();
        dataBuilding.area = simBuilding.getGroundArea();
        dataBuilding.floors = simBuilding.getFloors();
        dataBuilding.fieryness = simBuilding.isFierynessDefined() ? simBuilding.getFieryness() : 0;
        dataBuilding.brokennes = simBuilding.isBrokennessDefined() ? simBuilding.getBrokenness() : 0;
    }

    private void updateAgents(WorldInfo worldInfo) {
        for (StandardEntity se : worldInfo.getEntitiesOfType(StandardEntityURN.AMBULANCE_TEAM)) {
            Ambulance dataAT = ambulances.get(se.getID().getValue());
            if (dataAT == null) { dataAT = new Ambulance(); ambulances.put(se.getID().getValue(), dataAT); }
            dataAT.type = AgentType.AMBULANCE;
            setHumanProps((Human)dataAT, (rescuecore2.standard.entities.Human)se);
            //todo: add loaded properties
        }
        for (StandardEntity se : worldInfo.getEntitiesOfType(StandardEntityURN.FIRE_BRIGADE)) {
            FireBrigade dataFB = firebrigades.get(se.getID().getValue());
            if (dataFB == null) { dataFB = new FireBrigade(); firebrigades.put(se.getID().getValue(), dataFB); }
            dataFB.type = AgentType.FIREBRIGADE;
            setHumanProps((Human)dataFB, (rescuecore2.standard.entities.Human)se);
            rescuecore2.standard.entities.FireBrigade simFB = (rescuecore2.standard.entities.FireBrigade)se;
            dataFB.water = simFB.isWaterDefined() ? simFB.getWater() : -1;
        }
        for (StandardEntity se : worldInfo.getEntitiesOfType(StandardEntityURN.POLICE_FORCE)) {
            Police dataP = polices.get(se.getID().getValue());
            if (dataP == null) { dataP = new Police(); polices.put(se.getID().getValue(), dataP); }
            dataP.type = AgentType.POLICE;
            setHumanProps((Human)dataP, (rescuecore2.standard.entities.Human)se);
        }
        for (StandardEntity se : worldInfo.getEntitiesOfType(StandardEntityURN.CIVILIAN)) {
            Human dataH = civilians.get(se.getID().getValue());
            if (dataH == null) { dataH = new Human(); civilians.put(se.getID().getValue(), dataH); }
            dataH.type = AgentType.CIVILIAN;
            setHumanProps((Human)dataH, (rescuecore2.standard.entities.Human)se);
        }

    }

    private void setHumanProps(Human human, rescuecore2.standard.entities.Human simHuman) {
        human.id = simHuman.getID().getValue();
        human.posId = simHuman.isPositionDefined() ? simHuman.getPosition().getValue() : -1;
        human.damage = simHuman.isDamageDefined() ? simHuman.getDamage() : -1;
        human.buriedness = simHuman.isBuriednessDefined() ? simHuman.getBuriedness() : -1;
        human.health = simHuman.isHPDefined() ? simHuman.getHP() : -1;
        human.x = simHuman.isXDefined() ? simHuman.getX() : -1;
        human.y = simHuman.isYDefined() ? simHuman.getY() : -1;
    }
}
