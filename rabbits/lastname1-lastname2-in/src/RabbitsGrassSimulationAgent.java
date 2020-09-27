import java.awt.Color;

import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;





/**
 * Class that implements the simulation agent for the rabbits grass simulation.

 * @author
 */

public class RabbitsGrassSimulationAgent implements Drawable {
	  private RabbitsGrassSimulationModel model; 
	  private int X;
	  private int Y;
	  private int energy;
	  private static int IDNumber = 0;
	  private int ID;
	  private RabbitsGrassSimulationSpace rab; 
	  
	  
	  public RabbitsGrassSimulationAgent(int startingEnergy){
	    X = -1;
	    Y = -1;
	    setEnergy(startingEnergy);
	    ID = IDNumber + 1; 
	    IDNumber++; 
	  } 
	    
	  
	public void draw(SimGraphics g) {
		g.drawFastOval(Color.white);
	}

	public int getX() {
		// TODO Auto-generated method stub
		return X;
	}

	public int getY() {
		// TODO Auto-generated method stub
		return Y;
	}


	public int getEnergy() {
		return energy;
	}


	public void setEnergy(int energy) {
		this.energy = energy;
	}
	
	public void setXY(int newX, int newY){
	    X = newX;
	    Y = newY;
	  }

	public String getID(){
		return "Rabbit-" + ID;
	}
	
	public void report() {
		// TODO Auto-generated method stub
		System.out.println(getID() +
                " at " +
                X + ", " + Y +
                " has " +
                getEnergy() + " Power"
                );
	}


	public void step() {
		
		int rand = (int)(Math.random() * 4);
		Object2DGrid grid = rab.getAgentSpace();
		int newX, newY; 
		
		// Some simple control logic for the Rabbits Movement 
		switch(rand) {
		case 0:
			// North
			newX = X;
			newY = Y+1;
			
			newX = (newX + grid.getSizeX()) % grid.getSizeX();
		    newY = (newY + grid.getSizeY()) % grid.getSizeY();
		    
			break; 
		case 1:
			// East
			newX = X+1;
			newY = Y;
			newX = (newX + grid.getSizeX()) % grid.getSizeX();
		    newY = (newY + grid.getSizeY()) % grid.getSizeY();
		    break; 
		    
		case 2: 
			// South
			newX = X;
			newY = Y-1;
			newX = (newX + grid.getSizeX()) % grid.getSizeX();
		    newY = (newY + grid.getSizeY()) % grid.getSizeY();
			break;
		case 3: 
			// West
			newX = X-1; 
			newY = Y;
			newX = (newX + grid.getSizeX()) % grid.getSizeX();
		    newY = (newY + grid.getSizeY()) % grid.getSizeY();
			break;
		default:
			newX = X;
			newY = Y;
			
			// Do nothing, remain in place (Should not happen)
			break;
		}

		
		
		if(rab.moveAgentAt(X, Y, newX, newY)) { 
		energy += (int)(rab.eatGrassAt(X, Y) * (this.model.getNourishment()/100.0));
		}
		energy--; 
		
	}



	public RabbitsGrassSimulationSpace getRabbitGrassSimulationSpace() {
		return rab;
	}


	public void setRabbitGrassSimulationSpace(RabbitsGrassSimulationSpace rab) {
		this.rab = rab;
	}


	public RabbitsGrassSimulationModel getModel() {
		return model;
	}


	public void setModel(RabbitsGrassSimulationModel model) {
		this.model = model;
	}
	

	

}
	


