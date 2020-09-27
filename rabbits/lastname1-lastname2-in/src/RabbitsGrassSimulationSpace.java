/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * @author 
 */
import uchicago.src.sim.space.Object2DGrid;


public class RabbitsGrassSimulationSpace {
	private Object2DGrid wonderlandSpace;
	private Object2DGrid agentSpace;
	
	public Object2DGrid getWonderlandSpace() {
		return wonderlandSpace;
	}

	public void setWonderlandSpace(Object2DGrid wonderlandSpace) {
		this.wonderlandSpace = wonderlandSpace;
	}

	public RabbitsGrassSimulationSpace(int gridSize) {
		
		wonderlandSpace = new Object2DGrid(gridSize,gridSize); 
		agentSpace = new Object2DGrid(gridSize, gridSize);
		// Init. the Grass of the Simulation World Space  
		for(int i = 0; i < gridSize; i++) {
			for(int j = 0; j < gridSize; j++) {
				wonderlandSpace.putObjectAt(i,j,Integer.valueOf(0));
			}
			
		}
				
	}
	
	public boolean isCellOccupied(int x, int y){
		   boolean retVal = false;
		   if(agentSpace.getObjectAt(x, y)!=null) retVal = true;
		   return retVal;
		   }
	
	
	public void growGrass(int grassGrowth){
		    // Randomly place money in moneySpace
		   for(int i = 0; i < grassGrowth; i++){

		    // Choose coordinates
		    int x = (int)(Math.random()*(wonderlandSpace.getSizeX()));
		    int y = (int)(Math.random()*(wonderlandSpace.getSizeY()));

		    // Get the value of the object at those coordinates
		    int I;
		    if(wonderlandSpace.getObjectAt(x,y)!= null){
		       I = ((Integer)wonderlandSpace.getObjectAt(x,y)).intValue();
		    }
		    else{
		       I = 0;
		    }
		    // Replace the Integer object with another one with the new value
		      wonderlandSpace.putObjectAt(x,y,Integer.valueOf(I + 1));
		    }
		  }
	
	public boolean addAgent(RabbitsGrassSimulationAgent agent){
	    boolean retVal = false;
	    int count = 0;
	    int countLimit = 10 * agentSpace.getSizeX() * agentSpace.getSizeY();

	    while((retVal==false) && (count < countLimit)){
	      int x = (int)(Math.random()*(agentSpace.getSizeX()));
	      int y = (int)(Math.random()*(agentSpace.getSizeY()));
	      if(isCellOccupied(x,y) == false){
	        agentSpace.putObjectAt(x,y,agent);
	        agent.setXY(x,y);
	        agent.setRabbitGrassSimulationSpace(this);
	        retVal = true;
	        
	      }
	      count++;
	    }

	    return retVal;
	  }
	
	public Object2DGrid getAgentSpace(){
	    return agentSpace;
	}
	
	public void removeAgentAt(int x, int y){
		   agentSpace.putObjectAt(x, y, null);
	}

	public int eatGrassAt(int x, int y) {
		int consumed;
	    if(wonderlandSpace.getObjectAt(x,y)!= null){
	      consumed = ((Integer)wonderlandSpace.getObjectAt(x,y)).intValue();
	    }
	    else{
	      consumed = 0;
	    }
	  
		wonderlandSpace.putObjectAt(x, y, Integer.valueOf(0));
		return consumed;
	}
	
	public boolean moveAgentAt(int x, int y, int newX, int newY){
		boolean retVal = false;
		  if(!isCellOccupied(newX, newY)){
		      RabbitsGrassSimulationAgent rabbit = (RabbitsGrassSimulationAgent)agentSpace.getObjectAt(x, y);
		      removeAgentAt(x,y);
		      rabbit.setXY(newX, newY);
		      agentSpace.putObjectAt(newX, newY, rabbit);
		      retVal = true;
		  }
		    return retVal;
		  }
	
	public int getTotalGrass(){
	    int totalGrass = 0;
	    for(int i = 0; i < agentSpace.getSizeX(); i++){
	      for(int j = 0; j < agentSpace.getSizeY(); j++){
	    	  int val; 
	    	  if(wonderlandSpace.getObjectAt(i,j)!= null){
	    	        val = ((Integer)wonderlandSpace.getObjectAt(i,j)).intValue();
	    	  }else{
	    	        val = 0;
	    	  }  
	    	 totalGrass += val;
	      }
	    }
	    return totalGrass;
	  }


}
