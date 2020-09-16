import java.awt.Color;

import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;





/**
 * Class that implements the simulation agent for the rabbits grass simulation.

 * @author
 */

public class RabbitsGrassSimulationAgent implements Drawable {
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
		
		int newX, newY; 
		
		switch(rand) {
		case 0:
			// North
			newX = X;
			newY = Y+1;
			if(newY > rab.getWonderlandSpace().getSizeY()-1) newY = 0; 
			break; 
		case 1:
			// East
			newX = X+1;
			newY = Y;
			if(newX > rab.getWonderlandSpace().getSizeX()-1) newX = 0; 
			break;
		case 2: 
			// South
			newX = X;
			newY = Y-1;
			if(newY < 0) newY = rab.getWonderlandSpace().getSizeY()-1; 
			break;
		case 3: 
			// West
			newX = X-1; 
			newY = Y;
			if(newX < 0) newX = rab.getWonderlandSpace().getSizeX()-1; 
			break;
		default:
			newX = X;
			newY = Y;
			
			// Do nothing, remain in place (Should not happen)
			break;
		}

		
		
		if(rab.moveAgentAt(X, Y, newX, newY)) { 
		energy += rab.eatGrassAt(X, Y);
		}
		energy--; 
		
	}


	public RabbitsGrassSimulationSpace getRabbitGrassSimulationSpace() {
		return rab;
	}


	public void setRabbitGrassSimulationSpace(RabbitsGrassSimulationSpace rab) {
		this.rab = rab;
	}
	

	

}
	


