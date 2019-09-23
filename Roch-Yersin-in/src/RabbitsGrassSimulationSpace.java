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
	
//	public void spreadGrass(int numGrass) {
//		int totalSpaceSize = grassSpace.getSizeX()*grassSpace.getSizeY();
//		if(numGrass < totalSpaceSize) {
//			ArrayList<Integer> id_list = new ArrayList<Integer>();
//	        for(int i = 0; i < totalSpaceSize; i++) {
//	            id_list.add(new Integer(i));
//	        }
//	        Collections.shuffle(id_list);
//	        for(int i = 0; i < numGrass; i++) {
//	        	int id = id_list.get(i);
//	        	int id_x = id%grassSpace.getSizeX();
//	        	int id_y = id/grassSpace.getSizeX();
//	        	grassSpace.putObjectAt(id_x, id_y, new Integer(1));
//	        }
//		}
//		else {
//		    for(int i = 0; i < grassSpace.getSizeX(); i++){
//		    	for(int j = 0; j < grassSpace.getSizeY(); j++){
//		    		grassSpace.putObjectAt(i, j, new Integer(1));
//		    	}
//		    }
//		}
//	}

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
	    boolean retVal = false;
	    int count = 0;
	    int countLimit = 10*agentSpace.getSizeX()*agentSpace.getSizeY();

	    while((retVal == false) && (count < countLimit)) {
	        int x = (int)(Math.random()*agentSpace.getSizeX());
	        int y = (int)(Math.random()*agentSpace.getSizeY());
	        if(isCellOccupied(x, y) == false) {
	            agentSpace.putObjectAt(x, y, agent);
	            agent.setXY(x, y);
	            agent.setRabbitGrassSimulationSpace(this);
	            retVal = true;
	        }
	        count++;
	    }
	    
	    return retVal;
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
