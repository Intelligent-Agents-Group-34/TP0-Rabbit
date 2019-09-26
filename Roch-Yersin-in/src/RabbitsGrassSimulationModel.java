import java.awt.Color;
import java.util.ArrayList;

import uchicago.src.sim.analysis.BinDataSource;
import uchicago.src.sim.analysis.DataSource;
import uchicago.src.sim.analysis.OpenHistogram;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.util.SimUtilities;
import uchicago.src.sim.engine.SimInit;

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
	private static final int GRIDSIZE = 20;
	private static final int NUMINITRABBITS = 50;
	private static final int NUMINITGRASS = 1000;
	private static final int GRASSGROWTHRATE = 100;
	private static final int BIRTHTHRESHOLD = 10000;
	private static final int RABBITMININITENERGY = 500;
	private static final int RABBITMAXINITENERGY = 1000;
	private static final int RABBITENERGYLOSSRATE = 25;
	private static final int ENERGYPERGRASS = 1;
	
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
	
	private RabbitsGrassSimulationSpace rgsSpace;
	
	private ArrayList<RabbitsGrassSimulationAgent> agentList;
	
	private DisplaySurface displaySurface;
	
	private OpenSequenceGraph entitiesInSpace;
	private OpenHistogram rabbitEnergyDistribution;

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
	
	class GrassInSpace implements DataSource, Sequence {
		
		public Object execute() {
			return new Double(getSValue());
		}

		public double getSValue() {
			return (double)rgsSpace.getTotalGrass();
		}
	}
	
	class RabbitsInSpace implements DataSource, Sequence {
		
		public Object execute() {
			return new Double(getSValue());
		}

		public double getSValue() {
			return (double)countLivingAgents();
		}
	}
	
	class RabbitEnergy implements BinDataSource {
		public double getBinValue(Object o) {
			RabbitsGrassSimulationAgent agent = (RabbitsGrassSimulationAgent)o;
			return (double)agent.getEnergy();
		}
	}
	
	public String getName() {
		return "Rabbit Model";
	}
	
	public void setup() {
	    System.out.println("Running setup");
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
	    
	    if(rabbitEnergyDistribution != null) {
	    	rabbitEnergyDistribution.dispose();
	    }
	    rabbitEnergyDistribution = null;

	    // Create displays
	    displaySurface = new DisplaySurface(this, "Rabbit Model Window");
	    entitiesInSpace = new OpenSequenceGraph("Amount of Grass and Rabbits in Space", this);
	    rabbitEnergyDistribution = new OpenHistogram("Rabbit Energy", 8, 0);

	    // Register displays
	    registerDisplaySurface("Rabbit Model Window", displaySurface);
	    registerMediaProducer("Plot", entitiesInSpace);
	}

	public void begin() {
	    buildModel();
	    buildSchedule();
	    buildDisplay();
	    
	    displaySurface.display();
	    entitiesInSpace.display();
	    rabbitEnergyDistribution.display();
	}

	public void buildModel(){
	    System.out.println("Running BuildModel");
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
	
	public void buildSchedule(){
	    System.out.println("Running BuildSchedule");
	    
	    class RabbitStep extends BasicAction {
	    	public void execute() {
	    		SimUtilities.shuffle(agentList);
	    		for(int i =0; i < agentList.size(); i++) {
	    			RabbitsGrassSimulationAgent agent = (RabbitsGrassSimulationAgent)agentList.get(i);
	    			agent.step();
	    		}
	    		
	    		reapDeadAgents();
	    		
	    		rgsSpace.spreadGrass(grassGrowthRate);
	    		
	    		displaySurface.updateDisplay();
	        }
	    }

	    schedule.scheduleActionBeginning(0, new RabbitStep());
	    
	    class RabbitCountLiving extends BasicAction {
	        public void execute() {
	        	countLivingAgents();
	        }
	    }

	    schedule.scheduleActionAtInterval(10, new RabbitCountLiving());
	    
	    class RabbitUpdateGrassInSpace extends BasicAction {
	    	public void execute() {
	    		entitiesInSpace.step();
	    	}
	    }
	    
	    schedule.scheduleActionAtInterval(10, new RabbitUpdateGrassInSpace());
	    
	    class RabbitUpdateRabbitEnergy extends BasicAction {
	    	public void execute() {
	    		rabbitEnergyDistribution.step();
	    	}
	    }
	    
	    schedule.scheduleActionAtInterval(10, new RabbitUpdateRabbitEnergy());
	}
	
	public void buildDisplay(){
	    System.out.println("Running BuildDisplay");
	    
	    ColorMap map = new ColorMap();

	    for(int i = 1; i<16; i++){
	      map.mapColor(i, new Color(0, 255 - i*8, 0));
	    }
	    map.mapColor(0, Color.white);

	    Value2DDisplay displayGrass =
	        new Value2DDisplay(rgsSpace.getCurrentGrassSpace(), map);
	    
	    Object2DDisplay displayAgents = new Object2DDisplay(rgsSpace.getCurrentAgentSpace());
	    displayAgents.setObjectList(agentList);

	    displaySurface.addDisplayableProbeable(displayGrass, "Grass");
	    displaySurface.addDisplayableProbeable(displayAgents, "Agents");
	    
	    entitiesInSpace.addSequence("Grass in Space", new GrassInSpace());
	    entitiesInSpace.addSequence("Rabbits in Space", new RabbitsInSpace());
	    rabbitEnergyDistribution.createHistogramItem("Rabbit Energy", agentList, new RabbitEnergy());
	}
	
	public void addNewAgent() {
	    RabbitsGrassSimulationAgent a = new RabbitsGrassSimulationAgent(rabbitMinInitEnergy, rabbitMaxInitEnergy,
	    																birthThreshold, rabbitEnergyLossRate, this);
	    if(rgsSpace.addAgent(a)) {
	    	agentList.add(a);
	    }
	}
	
	private int reapDeadAgents() {
		int count = 0;
		for(int i = (agentList.size() - 1); i >= 0 ; i--) {
			RabbitsGrassSimulationAgent agent = (RabbitsGrassSimulationAgent)agentList.get(i);
			if(agent.getEnergy() < 1) {
		        rgsSpace.removeAgentAt(agent.getX(), agent.getY());
		        agentList.remove(i);
		        count++;
	        }
	    }
	    return count;
	}
	
	private int countLivingAgents() {
	    int livingAgents = 0;
	    for(int i = 0; i < agentList.size(); i++) {
	        RabbitsGrassSimulationAgent agent = (RabbitsGrassSimulationAgent)agentList.get(i);
	        if(agent.getEnergy() > 0) livingAgents++;
	    }
	    System.out.println("Number of living agents is: " + livingAgents);

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
