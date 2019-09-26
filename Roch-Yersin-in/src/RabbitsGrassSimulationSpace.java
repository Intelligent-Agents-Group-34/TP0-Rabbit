import java.util.ArrayList;
import java.util.Collections;

import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * @author 
 */

public class RabbitsGrassSimulationSpace {
	private Object2DGrid grassSpace;
	private Object2DGrid agentSpace;
	
	public RabbitsGrassSimulationSpace(int xSize, int ySize){
		grassSpace = new Object2DGrid(xSize, ySize);
		agentSpace = new Object2DGrid(xSize, ySize);
		
	    for(int i = 0; i < xSize; i++){
	    	for(int j = 0; j < ySize; j++){
	    		grassSpace.putObjectAt(i, j, new Integer(0));
	    	}
	    }
	}
	
	public void spreadGrass(int numInitGrass) {
		for(int i = 0; i < numInitGrass; i++) {
			// Choose coordinates
			int x = (int)(Math.random()*(grassSpace.getSizeX()));
			int y = (int)(Math.random()*(grassSpace.getSizeY()));
			
			// Get the value of the object at those coordinates
			int currentValue = getGrassAt(x, y);
			// Replace the Integer object with another one with the new value
			grassSpace.putObjectAt(x, y, new Integer(currentValue + 1));
	    }
	}

	public int getGrassAt(int x, int y) {
		int i;
		if(grassSpace.getObjectAt(x, y) != null) {
			i = ((Integer)grassSpace.getObjectAt(x, y)).intValue();
		}
		else {
			i = 0;
		}
		return i;
	}
	
	public Object2DGrid getCurrentGrassSpace() {
		return grassSpace;
	}
	
	public Object2DGrid getCurrentAgentSpace(){
	    return agentSpace;
	}
	
	public boolean isCellOccupied(int x, int y) {
	    return agentSpace.getObjectAt(x, y) != null;
	}

	public boolean addAgent(RabbitsGrassSimulationAgent agent) {
	    ArrayList<Integer> free_spaces = new ArrayList<Integer>();
	    for(int i = 0; i < agentSpace.getSizeX(); i++) {
			for(int j = 0; j < agentSpace.getSizeY(); j++) {
				if(!isCellOccupied(i, j)) {
					free_spaces.add(new Integer(agentSpace.getSizeX()*j + i));
				}
			}
		}
	    if(free_spaces.isEmpty()) {
	    	return false;
	    }
	    else {
	    	Collections.shuffle(free_spaces);
	    	int x = free_spaces.get(0).intValue() % agentSpace.getSizeX();
	    	int y = free_spaces.get(0).intValue()/agentSpace.getSizeX();
	    	agentSpace.putObjectAt(x, y, agent);
            agent.setXY(x, y);
            agent.setRabbitGrassSimulationSpace(this);
            return true;
	    }
	}
	
	public void removeAgentAt(int x, int y) {
	    agentSpace.putObjectAt(x, y, null);
	}
	
	public int eatGrassAt(int x, int y) {
		int numGrass = getGrassAt(x, y);
		grassSpace.putObjectAt(x, y, new Integer(0));
		return numGrass*50;
	}
	
	public boolean moveAgentAt(int x, int y, int newX, int newY) {
	    if(!isCellOccupied(newX, newY)) {
	    	RabbitsGrassSimulationAgent agent = (RabbitsGrassSimulationAgent)agentSpace.getObjectAt(x, y);
	    	removeAgentAt(x,y);
	    	agent.setXY(newX, newY);
	    	agentSpace.putObjectAt(newX, newY, agent);
	    	return true;
	    }
	    return false;
	}
	
	public int getTotalGrass(){
		int totalGrass = 0;
	    for(int i = 0; i < agentSpace.getSizeX(); i++){
	    	for(int j = 0; j < agentSpace.getSizeY(); j++){
	    		totalGrass += getGrassAt(i,j);
	    	}
	    }
	    return totalGrass;
	}
}
