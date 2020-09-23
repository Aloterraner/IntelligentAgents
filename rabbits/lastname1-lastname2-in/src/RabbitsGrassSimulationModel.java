
import java.awt.Color;
import java.util.ArrayList;

import uchicago.src.reflector.RangePropertyDescriptor;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.ModelManipulator;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.event.SliderListener;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.util.SimUtilities;
import uchicago.src.sim.analysis.DataSource;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;

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
	  	private static final int NUMINITGRASS = 100;
	  	private static final int GRIDSIZE = 20;
	  	private static final int BIRTHTHRESHOLD = 20;
	  	private static final int GRASSGROWTHRATE = 50;
	  	private static final int FERTILITY = 1; 
	  	private static final int STARTINGENERGY = 15; 
	  	private static final int NOURISHMENT = 100;
	  	
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
		private int Nourishment = NOURISHMENT; 
		
		private OpenSequenceGraph amountOfRabbitInSpace;

		
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
			
			initUI();
			
			//this.getModelManipulator().addSlider("Fertility", 0, 10, 1, new FertilityIncrementer());
			
			rgSpace = null;
			agentList = new ArrayList<RabbitsGrassSimulationAgent>();
			schedule = new Schedule(1);
			
			if (displaySurface != null){
			    displaySurface.dispose();
			}
			displaySurface = null;

			displaySurface = new DisplaySurface(this, "Alice 1"); 
			registerDisplaySurface("Alice 1", displaySurface);
			
		    if (amountOfRabbitInSpace != null){
		    	amountOfRabbitInSpace.dispose();
		      }
		    amountOfRabbitInSpace = null;
			
			amountOfRabbitInSpace = new OpenSequenceGraph("Amount Of Rabbits and Grass In Space",this);
		    this.registerMediaProducer("Plot", amountOfRabbitInSpace);
		  }
			

		class GrassInSpace implements DataSource, Sequence {

		    public Object execute() {
		      return new Double(getSValue());
		    }

		    public double getSValue() {
		      return (double)rgSpace.getTotalGrass();
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

		
		public void begin() {
			buildModel();
		    buildSchedule();
		    buildDisplay();

		    
		    
		    displaySurface.display();
		    amountOfRabbitInSpace.display();
			
		}
		
		public void buildModel() {
			 System.out.println("Building Model ...");
			 
			 rgSpace = new RabbitsGrassSimulationSpace(GridSize); 
			 rgSpace.growGrass(NumInitGrass);
			 
			 
			 for(int i = 0; i < NumInitRabbits; i++){
			     addNewAgent();
			 }
			 
			 for(int i = 0; i < agentList.size(); i++){
			     RabbitsGrassSimulationAgent rab = agentList.get(i);
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
			 
			 amountOfRabbitInSpace.addSequence("Rabbits In Space", new RabbitsInSpace());
			 amountOfRabbitInSpace.addSequence("Grass In Space", new GrassInSpace());
		}
		
		public void buildSchedule() {
			System.out.println("Running BuildSchedule");
			
			class RabbitNSWEStep extends BasicAction {
			      public void execute() {
			        SimUtilities.shuffle(agentList);
			        for(int i =0; i < agentList.size(); i++){
			          RabbitsGrassSimulationAgent rab = agentList.get(i);
			          rab.step();
			        }
			         
			        
			        reapDeadAgents(); 
			        birthNewAgents();
			        
			        rgSpace.growGrass(GrassGrowthRate);
			        displaySurface.updateDisplay();
			        
			        
			        
			        class CountLiving extends BasicAction {
			            public void execute(){
			              countLivingAgents();
			            }
			          }

			          schedule.scheduleActionAtInterval(10, new CountLiving());

			          class UpdateAmountOfRabbitInSpace extends BasicAction {
			            public void execute(){
			            	amountOfRabbitInSpace.step();
			            }
			          }

			          schedule.scheduleActionAtInterval(10, new UpdateAmountOfRabbitInSpace());
			        
			      }
			    }
			
			 schedule.scheduleActionBeginning(0, new RabbitNSWEStep());
			
		}

		public String[] getInitParam() {
			// TODO Auto-generated method stub
			// Parameters to be set by users via the Repast UI slider bar
			// Do "not" modify the parameters names provided in the skeleton code, you can add more if you want 
			String[] params = { "GridSize", "NumInitRabbits", "NumInitGrass", "GrassGrowthRate", "BirthThreshold","Fertility","Nourishment"};
			return params;
		}

		private void birthNewAgents() {
			if(agentList.size() < (GridSize*GridSize)) {
				for(int i = (agentList.size() - 1); i >= 0 ; i--){
				      RabbitsGrassSimulationAgent rab = agentList.get(i);
				      if(rab.getEnergy() > BirthThreshold){
				    	
				    	// Fertility effect 
				    	int numBabies = (int)Math.random()*Fertility + 1;   
				    	 
				    	for(int j = 0; j < numBabies; j++ ) {
					        RabbitsGrassSimulationAgent a = new RabbitsGrassSimulationAgent(startingEnergy);
					        a.setModel(this);
							agentList.add(a);
							rgSpace.addAgent(a); 
				    	}
				    	rab.setEnergy(rab.getEnergy()-BirthThreshold);

				      }
			}
			}
			
		}
		
		private void reapDeadAgents(){
		    for(int i = (agentList.size() - 1); i >= 0 ; i--){
		      RabbitsGrassSimulationAgent rab = agentList.get(i);
		      if(rab.getEnergy() < 1){
		        rgSpace.removeAgentAt(rab.getX(), rab.getY());
		        agentList.remove(i);
		      }
		    }
		  }
		
	    private int countLivingAgents(){
			    int livingAgents = 0;
			    for(int i = 0; i < agentList.size(); i++){
			      RabbitsGrassSimulationAgent rab = (RabbitsGrassSimulationAgent)agentList.get(i);
			      if(rab.getEnergy() > 0) livingAgents++;
			    }
			    System.out.println("Number of living agents is: " + livingAgents);

			    return livingAgents;
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
			a.setModel(this);
			agentList.add(a);
			rgSpace.addAgent(a); 
		}
		
		public int getNumInitGrass() {
			return NumInitGrass;
		}

		public void setNumInitGrass(int numInitGrass) {
			if (numInitGrass > (GridSize * GridSize)) {
				throw new IllegalArgumentException("Too much grass (" + numInitGrass + ") for given grid size (" + GridSize + ")");
			}
			else {
				NumInitGrass = numInitGrass;
			}
			
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
			if (numInitRabbits > (GridSize * GridSize)) {
				throw new IllegalArgumentException("Too many rabbits (" + numInitRabbits + ") for given grid size (" + GridSize + ")");
			}
			else {
				NumInitRabbits = numInitRabbits;
			}
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

		public int getNourishment() {
			return Nourishment;
		}

		public void setNourishment(int nourishment) {
			Nourishment = nourishment;
		}

		public int getFertility() {
			return Fertility;
		}

		public void setFertility(int fertility) {
			Fertility = fertility;
		}
		
		class GrassGrowthIncrementer extends SliderListener {
			  public void execute() {
				  if(isAdjusting) GrassGrowthRate = value;
			  }
			};
	     
	    class BirthThresholdIncrementer extends SliderListener {
	    	public void execute() { 
	    		if(isAdjusting) BirthThreshold = value;
			}
	    };
	     
	    class StartingEnergyIncrementer extends SliderListener {
	    	public void execute() { 
	    		if(isAdjusting) startingEnergy = value;
			}
	    };
	     
	    class NourishmentIncrementer extends SliderListener {
	    	public void execute() { 
	    		if(isAdjusting) Nourishment = value;
			}
	    };
	     
	    public void initUI() {
	    	
	    	RangePropertyDescriptor gr = new RangePropertyDescriptor("GrassGrowthRate", 0, 1000, 200);
			descriptors.put("GrassGrowthRate", gr);  
			RangePropertyDescriptor bt = new RangePropertyDescriptor("BirthThreshold", 10, 100, 10);
			descriptors.put("BirthThreshold", bt);
	    	RangePropertyDescriptor g = new RangePropertyDescriptor("GridSize", 10, 100, 20);
		    descriptors.put("GridSize", g); 
	    	RangePropertyDescriptor inr = new RangePropertyDescriptor("NumInitRabbits", 10, 150, 20);
		    descriptors.put("NumInitRabbits", inr);  
	        RangePropertyDescriptor ing = new RangePropertyDescriptor("NumInitGrass", 0, 4000, 1000);
	    	descriptors.put("NumInitGrass", ing); 
	    	RangePropertyDescriptor fer = new RangePropertyDescriptor("Fertility", 1, 8, 1);
	    	descriptors.put("Fertility", fer); 
	    	RangePropertyDescriptor nour = new RangePropertyDescriptor("Nourishment", 0, 200, 40);
	    	descriptors.put("Nourishment", nour); 
	    	
	    	ModelManipulator modelMani = this.getModelManipulator();
	    	modelMani.init();
	    	modelMani.addSlider("Grass Growth Rate", 0, 300, 25, new GrassGrowthIncrementer());
	    	modelMani.addSlider("Nourishment", 0, 200, 20, new NourishmentIncrementer());
	    	modelMani.addSlider("BirthThreshold", 10, 80, 5, new BirthThresholdIncrementer());
	    	modelMani.addSlider("startingEnergy", 0, 100, 10, new StartingEnergyIncrementer());
	    }
}
