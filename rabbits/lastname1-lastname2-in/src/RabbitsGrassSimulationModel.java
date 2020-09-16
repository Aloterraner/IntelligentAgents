
import java.awt.Color;
import java.util.ArrayList;

import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.gui.Object2DDisplay;
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
	
	
	
	 	private static final int NUMINITRABBIT = 10;
	  	private static final int NUMINITGRASS = 40;
	  	private static final int GRIDSIZE = 20;
	  	private static final int BIRTHTHRESHOLD = 50;
	  	private static final int GRASSGROWTHRATE = 50;
	  	private static final int FERTILITY = 1; 
	  	private static final int STARTINGENERGY = 15; 
	  	
		private Schedule schedule;
		private DisplaySurface displaySurface; 
		private RabbitsGrassSimulationSpace rgSpace;
		
		
		
		private ArrayList<RabbitsGrassSimulationAgent> agentList;

		private int startingEnergy = STARTINGENERGY;
		private int GridSize = GRIDSIZE;
		private int NumInitRabbits = NUMINITRABBIT;
		private int NumInitGrass = NUMINITGRASS;
		private int BirthThreshold = BIRTHTHRESHOLD;
		private int GrassGrowthRate = GRASSGROWTHRATE;
		private int Fertility = FERTILITY;
		
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
		
		public void setup() {
			System.out.println("Setting up ...");
			rgSpace = null; 
			agentList = new ArrayList<RabbitsGrassSimulationAgent>();
			schedule = new Schedule(1);
			
			
			if (displaySurface != null){
			    displaySurface.dispose();
			}
			displaySurface = null;

			displaySurface = new DisplaySurface(this, "Alice 1"); 
			registerDisplaySurface("Alice 1", displaySurface);
			
		}

		
		

		public void begin() {
			buildModel();
		    buildSchedule();
		    buildDisplay();

		    
		    
		    displaySurface.display();
			
		}

		
		public void buildModel() {
			 System.out.println("Building Model ...");
			 
			 rgSpace = new RabbitsGrassSimulationSpace(GridSize); 
			 rgSpace.growGrass(NumInitGrass);
			 
			 
			 for(int i = 0; i < NumInitRabbits; i++){
			     addNewAgent();
			 }
			 
			 for(int i = 0; i < agentList.size(); i++){
			     RabbitsGrassSimulationAgent rab = (RabbitsGrassSimulationAgent)agentList.get(i);
			     rab.report();
			 }
		}
		
		public void buildDisplay() {
			 System.out.println("Building Display ...");
			 
			 ColorMap map = new ColorMap();

			 for(int i = 1; i<16; i++){
			      map.mapColor(i, new Color(0, (int)(i * 8 + 127), 0));
			 }
			 Color color = new Color(100, 50, 0); 
			 map.mapColor(0, color);

			 Value2DDisplay displayGrass = new Value2DDisplay(rgSpace.getWonderlandSpace(), map);
			 Object2DDisplay displayAgents = new Object2DDisplay(rgSpace.getWonderlandSpace());
			 displayAgents.setObjectList(agentList);
			 displaySurface.addDisplayable(displayGrass, "Grass");
			 displaySurface.addDisplayable(displayAgents, "Agents"); 
		}
		
		public void buildSchedule() {
			System.out.println("Running BuildSchedule");
			
			class RabbitNSWEStep extends BasicAction {
			      public void execute() {
			        SimUtilities.shuffle(agentList);
			        for(int i =0; i < agentList.size(); i++){
			          RabbitsGrassSimulationAgent rab = (RabbitsGrassSimulationAgent)agentList.get(i);
			          rab.step();
			        }
			        
			        reapDeadAgents(); 
			        
			        rgSpace.growGrass(GrassGrowthRate);
			        
			        
			        
			        
			        
			        displaySurface.updateDisplay();
			      }
			    }
			
			 schedule.scheduleActionBeginning(0, new RabbitNSWEStep());
			
		}

		public String[] getInitParam() {
			// TODO Auto-generated method stub
			// Parameters to be set by users via the Repast UI slider bar
			// Do "not" modify the parameters names provided in the skeleton code, you can add more if you want 
			String[] params = { "GridSize", "NumInitRabbits", "NumInitGrass", "GrassGrowthRate", "BirthThreshold","Fertility"};
			return params;
		}

		
		private void reapDeadAgents(){
		    for(int i = (agentList.size() - 1); i >= 0 ; i--){
		      RabbitsGrassSimulationAgent rab = (RabbitsGrassSimulationAgent)agentList.get(i);
		      if(rab.getEnergy() < 1){
		        rgSpace.removeAgentAt(rab.getX(), rab.getY());
		        agentList.remove(i);
		      }
		    }
		  }
		
		
		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}

		public Schedule getSchedule() {
			// TODO Auto-generated method stub
			return schedule;
		}


		public int getBirthThreshold() {
			return BirthThreshold;
		}

		public void setBirthThreshold(int birthThreshold) {
			BirthThreshold = birthThreshold;
		}
		
		private void addNewAgent(){
			RabbitsGrassSimulationAgent a = new RabbitsGrassSimulationAgent(startingEnergy);
			agentList.add(a);
			rgSpace.addAgent(a); 
		}
		
		public int getNumInitGrass() {
			return NumInitGrass;
		}

		public void setNumInitGrass(int numInitGrass) {
			NumInitGrass = numInitGrass;
		}

		public int getGridSize() {
			return GridSize;
		}

		public void setGridSize(int gridSize) {
			GridSize = gridSize;
		}

		public int getNumInitRabbits() {
			return NumInitRabbits;
		}

		public void setNumInitRabbits(int numInitRabbits) {
			NumInitRabbits = numInitRabbits;
		}

		public int getGrassGrowthRate() {
			return GrassGrowthRate;
		}

		public void setGrassGrowthRate(int grassGrowthRate) {
			GrassGrowthRate = grassGrowthRate;
		}

		public int getStartingEnergy() {
			return startingEnergy;
		}

		public void setStartingEnergy(int startingEnergy) {
			this.startingEnergy = startingEnergy;
		}
		
		
		
		
}
