package template;

import logist.simulation.Vehicle;
import logist.task.Task;

public class PickUpAction extends Action {
	
	public PickUpAction(Task task, int time, Vehicle vehicle, double costs) {
		super(task, time, vehicle, costs);
		
		
		// TODO Auto-generated constructor stub
	}
	
	
	
	
	
	public String toString() {
		
		
		
		
		
		return ("PickUp Task: " + this.task.id + " at time " + this.time +  " by Vehicle " + this.vehicle.id());
	}
	


}
