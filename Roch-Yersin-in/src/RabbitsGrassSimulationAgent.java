import java.awt.Color;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;


/**
 * Class that implements the simulation agent for the rabbits grass simulation.

 * @author
 */

public class RabbitsGrassSimulationAgent implements Drawable {
	private int x;
	private int y;
	
	private int energy;
	private int birthThreshold;
	private int energyLossRate;
	
	private static int IDNumber = 0;
	private int ID;
	
	private RabbitsGrassSimulationModel model;
	
	private RabbitsGrassSimulationSpace rgsSpace;
	
	public RabbitsGrassSimulationAgent(int minInitEnergy, int maxInitEnergy, int birthThreshold, int energyLossRate, RabbitsGrassSimulationModel model) {
		x = -1;
	    y = -1;
	    
	    energy = (int)(minInitEnergy + (Math.random()*(maxInitEnergy - minInitEnergy)));
	    this.birthThreshold = birthThreshold;
	    this.energyLossRate = energyLossRate;
	    
	    ID = IDNumber;
	    IDNumber++;
	    
	    this.model = model;
	}
	
	public void setXY(int newX, int newY){
	    x = newX;
	    y = newY;
	}

	public void setRabbitGrassSimulationSpace(RabbitsGrassSimulationSpace space){
	    rgsSpace = space;
	}
	
	public String getID() {
	    return "A-" + ID;
	}

	public int getEnergy(){
	    return energy;
	}

	public void report(){
	    System.out.println(getID() + " at " + x + ", " + y + " has " + getEnergy() + " energy");
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void draw(SimGraphics G) {
		if(energy > 10)
			G.drawFastRoundRect(Color.green);
		else
			G.drawFastRoundRect(Color.blue);
	}
	
	public void step() {
		int direction = (int)(Math.random()*4);
		int newX, newY;		
		if(direction/2 == 0) {
			newX = x + (direction % 2 == 0 ? -1 : 1);
			newY = y;
		}
		else {
			newX = x;
			newY = y + (direction % 2 == 0 ? -1 : 1);
		}
		
		Object2DGrid agentSpace = rgsSpace.getCurrentAgentSpace();
		newX = (newX + agentSpace.getSizeX()) % agentSpace.getSizeX();
	    newY = (newY + agentSpace.getSizeY()) % agentSpace.getSizeY();
	    
		tryMove(newX, newY);
		
		energy += rgsSpace.eatGrassAt(x, y);
		if(energy >= birthThreshold) {
			model.addNewAgent();
			energy = birthThreshold/2;
		}
		energy -= energyLossRate;
	}
	
	private boolean tryMove(int newX, int newY) {
		return rgsSpace.moveAgentAt(x, y, newX, newY);
	}
}
