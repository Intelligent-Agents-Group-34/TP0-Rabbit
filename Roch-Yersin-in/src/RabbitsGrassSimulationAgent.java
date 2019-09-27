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
	private int energyPerGrass;
	
	private static int IDNumber = 0;
	private int ID;
	
	private RabbitsGrassSimulationModel model;
	
	private RabbitsGrassSimulationSpace rgsSpace;
	
	public RabbitsGrassSimulationAgent(RabbitsGrassSimulationModel model) {
		x = -1;
	    y = -1;
	    
	    int minEnergy = model.getRabbitMinInitEnergy();
	    int maxEnergy = model.getRabbitMaxInitEnergy();
	    // Random value between min and max
	    energy = (int)(minEnergy + (Math.random()*(maxEnergy - minEnergy)));
	    birthThreshold = model.getBirthThreshold();
	    energyLossRate = model.getRabbitEnergyLossRate();
	    energyPerGrass = model.getEnergyPerGrass();
	    
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
		G.drawOval(Color.WHITE); // Rabbits are white ovals
	}
	
	public void step() {
		int direction = (int)(Math.random()*4); // Choose a direction at random
		// Compute the new coords accordingly
		int newX, newY;
		if(direction/2 == 0) {
			newX = x + (direction % 2 == 0 ? -1 : 1);
			newY = y;
		}
		else {
			newX = x;
			newY = y + (direction % 2 == 0 ? -1 : 1);
		}
		
		// "Torus" propriety of the space
		Object2DGrid agentSpace = rgsSpace.getCurrentAgentSpace();
		newX = (newX + agentSpace.getSizeX()) % agentSpace.getSizeX();
	    newY = (newY + agentSpace.getSizeY()) % agentSpace.getSizeY();
	    
		tryMove(newX, newY);
		
		// Eat grass and gain energy
		energy += energyPerGrass*rgsSpace.eatGrassAt(x, y);
		
		// Add a new rabbit if energy is above birth threshold
		if(energy >= birthThreshold) {
			model.addNewAgent();
			energy = birthThreshold/2; // "Energy cost" of reproducing
		}
		
		// Passive energy loss
		energy -= energyLossRate;
	}
	
	private boolean tryMove(int newX, int newY) {
		return rgsSpace.moveAgentAt(x, y, newX, newY);
	}
}
