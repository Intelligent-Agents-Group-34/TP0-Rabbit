import java.awt.Color;
import java.util.ArrayList;

import uchicago.src.sim.analysis.DataSource;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.util.SimUtilities;

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @author 
 */


public class RabbitsGrassSimulationModel extends SimModelImpl {		
	// Default values
	private static final int GRIDSIZE = 20; // Grid will be GRIDSIZE*GRIDSIZE
	private static final int NUMINITRABBITS = 10; // Number of rabbits at the beginning of the simulation
	private static final int NUMINITGRASS = 1000; // Number of grass at the beginning of the simulation
	private static final int GRASSGROWTHRATE = 100; // How much grass is added each step
	private static final int BIRTHTHRESHOLD = 500; // Minimum amount of energy required for a rabbit to reproduce
	private static final int RABBITMININITENERGY = 50; // Minimum initial energy when creating a rabbit
	private static final int RABBITMAXINITENERGY = 100; // Maximum initial energy when creating a rabbit
	private static final int RABBITENERGYLOSSRATE = 3; // How many energy a rabbit loses at each step
	private static final int ENERGYPERGRASS = 1; // How many energy a rabbit get per grass unit
	
	// Simulation parameters
	private int gridSize = GRIDSIZE;
	private int numInitRabbits = NUMINITRABBITS;
	private int numInitGrass = NUMINITGRASS;
	private int grassGrowthRate = GRASSGROWTHRATE;
	private int birthThreshold = BIRTHTHRESHOLD;
	private int rabbitMinInitEnergy = RABBITMININITENERGY;
	private int rabbitMaxInitEnergy = RABBITMAXINITENERGY;
	private int rabbitEnergyLossRate = RABBITENERGYLOSSRATE;
	private int energyPerGrass = ENERGYPERGRASS;

	private Schedule schedule;
	
	private RabbitsGrassSimulationSpace rgsSpace; // Space in which the simulation occurs
	
	private ArrayList<RabbitsGrassSimulationAgent> agentList; // List of rabbits
	
	private DisplaySurface displaySurface;
	
	private OpenSequenceGraph entitiesInSpace;

	public static void main(String[] args) {
		
		System.out.println("Rabbit skeleton");

		SimInit init = new SimInit();
		RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
		// Do "not" modify the following lines of parsing arguments
		if (args.length == 0) // by default, you don't use parameter file nor batch mode 
			init.loadModel(model, "", false);
		else
			init.loadModel(model, args[0], Boolean.parseBoolean(args[1]));
		
	}
	
	// Small class required to plot the amount of grass over time
	class GrassInSpace implements DataSource, Sequence {
		
		public Object execute() {
			return new Double(getSValue());
		}

		public double getSValue() {
			return (double)rgsSpace.getTotalGrass();
		}
	}
	
	// Small class required to plot the amount of rabbits over time
	class RabbitsInSpace implements DataSource, Sequence {
		
		public Object execute() {
			return new Double(getSValue());
		}

		public double getSValue() {
			return (double)countLivingAgents();
		}
	}
	
	public String getName() {
		return "Rabbit Model";
	}
	
	public void setup() {
	    rgsSpace = null;
	    agentList = new ArrayList<RabbitsGrassSimulationAgent>();
	    schedule = new Schedule(1);
	    
	    // Tear down displays
	    if(displaySurface != null) {
	        displaySurface.dispose();
	    }
	    displaySurface = null;
	    
	    if(entitiesInSpace != null) {
	    	entitiesInSpace.dispose();
	    }
	    entitiesInSpace = null;

	    // Create displays
	    displaySurface = new DisplaySurface(this, "Rabbit Model Window");
	    entitiesInSpace = new OpenSequenceGraph("Amount of Grass and Rabbits in Space", this);

	    // Register displays
	    registerDisplaySurface("Rabbit Model Window", displaySurface);
	    registerMediaProducer("Plot", entitiesInSpace);
	}

	public void begin() {
	    buildModel();
	    buildSchedule();
	    buildDisplay();
	    
	    // Activate the displays
	    displaySurface.display();
	    entitiesInSpace.display();
	}

	// Initialise the space and add grass and rabbits
	public void buildModel(){
	    rgsSpace = new RabbitsGrassSimulationSpace(gridSize, gridSize);
	    rgsSpace.spreadGrass(numInitGrass);
	    
	    for(int i = 0; i < numInitRabbits; i++){
	        addNewAgent();
	    }
	    
	    for(int i = 0; i < agentList.size(); i++){
	        RabbitsGrassSimulationAgent agent = (RabbitsGrassSimulationAgent)agentList.get(i);
	        agent.report();
	    }
	}
	
	// Initialise the scheduled actions
	public void buildSchedule(){
	    
	    // Action that update the simulation
	    class RabbitStep extends BasicAction {
	    	public void execute() {
	    		// Update rabbits
	    		SimUtilities.shuffle(agentList);
	    		for(int i =0; i < agentList.size(); i++) {
	    			RabbitsGrassSimulationAgent agent = (RabbitsGrassSimulationAgent)agentList.get(i);
	    			agent.step();
	    		}
	    		
	    		// Remove dead rabbits
	    		reapDeadAgents();
	    		
	    		// Add new grass
	    		rgsSpace.spreadGrass(grassGrowthRate);
	    		
	    		// Update the display
	    		displaySurface.updateDisplay();
	        }
	    }

	    // Set it to be called at each step
	    schedule.scheduleActionBeginning(0, new RabbitStep());
	    
	    
	    // Action that update the graphs
	    class RabbitUpdateGrassInSpace extends BasicAction {
	    	public void execute() {
	    		entitiesInSpace.step();
	    	}
	    }
	    
	    // Set it to be called at each step
	    schedule.scheduleActionAtInterval(10, new RabbitUpdateGrassInSpace());
	}
	
	// Initialise the display
	public void buildDisplay(){
	    
	    // Colour map for the ground 
	    ColorMap map = new ColorMap();

	    for(int i = 1; i < 32; i++){
	      map.mapColor(i, new Color(0, 255 - i*4, 0)); // Darker green the more grass there is
	    }
	    map.mapColor(0, new Color(0xa52a2a)); // Brown if no grass

	    // Display for the ground
	    Value2DDisplay displayGrass =
	        new Value2DDisplay(rgsSpace.getCurrentGrassSpace(), map);
	    
	    // Display for the rabbits
	    Object2DDisplay displayAgents = new Object2DDisplay(rgsSpace.getCurrentAgentSpace());
	    displayAgents.setObjectList(agentList);

	    // Register the displays
	    displaySurface.addDisplayableProbeable(displayGrass, "Grass");
	    displaySurface.addDisplayableProbeable(displayAgents, "Agents");
	    
	    // Register the graphs
	    entitiesInSpace.addSequence("Grass in Space", new GrassInSpace());
	    entitiesInSpace.addSequence("Rabbits in Space", new RabbitsInSpace());
	}
	
	public void addNewAgent() {
	    RabbitsGrassSimulationAgent a = new RabbitsGrassSimulationAgent(this);
	    if(rgsSpace.addAgent(a)) { // Add the agent to the space
	    	agentList.add(a); // If successful add it to the list
	    }
	}
	
	// Remove all dead rabbits
	private int reapDeadAgents() {
		int count = 0;
		for(int i = (agentList.size() - 1); i >= 0 ; i--) { // For each rabbit in the list
			RabbitsGrassSimulationAgent agent = (RabbitsGrassSimulationAgent)agentList.get(i);
			if(agent.getEnergy() <= 0) { // If the energy is 0 or smaller
		        rgsSpace.removeAgentAt(agent.getX(), agent.getY()); // Remove the rabbit from the world
		        agentList.remove(i); // Also remove it from the list
		        count++;
	        }
	    }
		
	    return count;
	}
	
	private int countLivingAgents() {
	    int livingAgents = 0;
	    for(int i = 0; i < agentList.size(); i++) { // For each rabbit in the list
	        RabbitsGrassSimulationAgent agent = (RabbitsGrassSimulationAgent)agentList.get(i);
	        if(agent.getEnergy() > 0) livingAgents++; // Check if energy is bigger than 0
	    }

	    return livingAgents;
	}
	
	public Schedule getSchedule() {
		return schedule;
	}

	public String[] getInitParam() {
		// Parameters to be set by users via the Repast UI slider bar
		// Do "not" modify the parameters names provided in the skeleton code, you can add more if you want 
		String[] params = { "GridSize", "NumInitRabbits", "NumInitGrass", "GrassGrowthRate", "BirthThreshold",
							"RabbitMinInitEnergy", "RabbitMaxInitEnergy", "RabbitEnergyLossRate", "EnergyPerGrass"};
		return params;
	}

	public int getGridSize() {
		return gridSize;
	}

	public void setGridSize(int gridSize) {
		this.gridSize = gridSize;
	}

	public int getNumInitRabbits() {
		return numInitRabbits;
	}

	public void setNumInitRabbits(int numInitRabbits) {
		this.numInitRabbits = numInitRabbits;
	}

	public int getNumInitGrass() {
		return numInitGrass;
	}

	public void setNumInitGrass(int numInitGrass) {
		this.numInitGrass = numInitGrass;
	}

	public int getGrassGrowthRate() {
		return grassGrowthRate;
	}

	public void setGrassGrowthRate(int grassGrowthRate) {
		this.grassGrowthRate = grassGrowthRate;
	}

	public int getBirthThreshold() {
		return birthThreshold;
	}

	public void setBirthThreshold(int birthThreshold) {
		this.birthThreshold = birthThreshold;
	}

	public int getRabbitMinInitEnergy() {
		return rabbitMinInitEnergy;
	}

	public void setRabbitMinInitEnergy(int rabbitMinInitEnergy) {
		this.rabbitMinInitEnergy = rabbitMinInitEnergy;
	}

	public int getRabbitMaxInitEnergy() {
		return rabbitMaxInitEnergy;
	}

	public void setRabbitMaxInitEnergy(int rabbitMaxInitEnergy) {
		this.rabbitMaxInitEnergy = rabbitMaxInitEnergy;
	}

	public int getRabbitEnergyLossRate() {
		return rabbitEnergyLossRate;
	}

	public void setRabbitEnergyLossRate(int rabbitEnergyLossRate) {
		this.rabbitEnergyLossRate = rabbitEnergyLossRate;
	}

	public int getEnergyPerGrass() {
		return energyPerGrass;
	}

	public void setEnergyPerGrass(int energyPerGrass) {
		this.energyPerGrass = energyPerGrass;
	}
}
