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
		
		// Populate the grass space with no grass
	    for(int i = 0; i < xSize; i++){
	    	for(int j = 0; j < ySize; j++){
	    		grassSpace.putObjectAt(i, j, new Integer(0));
	    	}
	    }
	}
	
	// Spread randomly a given amount of grass across all cells
	public void spreadGrass(int numInitGrass) {
		for(int i = 0; i < numInitGrass; i++) {
			// Choose coordinates
			int x = (int)(Math.random()*(grassSpace.getSizeX()));
			int y = (int)(Math.random()*(grassSpace.getSizeY()));
			
			// Get the value of the object at those coordinates
			int currentValue = getGrassAt(x, y);
			// Replace the Integer object with another one with the new value
			if(currentValue < 31) {
				grassSpace.putObjectAt(x, y, new Integer(currentValue + 1));
			}
	    }
	}

	public int getGrassAt(int x, int y) {
		int i = 0;
		if(grassSpace.getObjectAt(x, y) != null) {
			i = ((Integer)grassSpace.getObjectAt(x, y)).intValue();
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
		// List with free space IDs. ID goes from left to right, top to bottom
		// For example for a 2x2 grid: 0 1
		//                             2 3
	    ArrayList<Integer> free_spaces = new ArrayList<Integer>();
	    for(int i = 0; i < agentSpace.getSizeX(); i++) {
			for(int j = 0; j < agentSpace.getSizeY(); j++) {
				if(!isCellOccupied(i, j)) {
					free_spaces.add(new Integer(agentSpace.getSizeX()*j + i));
				}
			}
		}
	    if(free_spaces.isEmpty()) { // Return false if no space available
	    	return false;
	    }
	    else {
	    	Collections.shuffle(free_spaces); // Choose a free space at random
	    	int x = free_spaces.get(0).intValue() % agentSpace.getSizeX();
	    	int y = free_spaces.get(0).intValue()/agentSpace.getSizeX();
	    	agentSpace.putObjectAt(x, y, agent); // Add the agent at that space
            agent.setXY(x, y);
            agent.setRabbitGrassSimulationSpace(this);
            return true;
	    }
	}
	
	public void removeAgentAt(int x, int y) {
	    agentSpace.putObjectAt(x, y, null);
	}
	
	public int eatGrassAt(int x, int y) {
		int numGrass = getGrassAt(x, y); // Get the amount of grass
		grassSpace.putObjectAt(x, y, new Integer(0)); // Set the amount of grass to zero in the space
		return numGrass;
	}
	
	public boolean moveAgentAt(int x, int y, int newX, int newY) {
	    if(!isCellOccupied(newX, newY)) { // If cell is free
	    	RabbitsGrassSimulationAgent agent = (RabbitsGrassSimulationAgent)agentSpace.getObjectAt(x, y);
	    	removeAgentAt(x,y); // Remove the agent at the old position
	    	agent.setXY(newX, newY); // Update the agent coordinates
	    	agentSpace.putObjectAt(newX, newY, agent); // Add the agent at the new position
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
