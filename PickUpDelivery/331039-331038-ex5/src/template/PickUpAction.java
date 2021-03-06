package template;

import logist.simulation.Vehicle;
import logist.task.Task;

public class PickUpAction extends Action {
	
	public PickUpAction(Task task, int time, Vehicle vehicle) {
		super(task, time, vehicle);	
	}
	
	
	public PickUpAction(PickUpAction pickup_action){
		super(pickup_action.task, pickup_action.time, pickup_action.vehicle); 
	}
	
	
	public String toString() {

		return ("PickUp Task: " + this.task.id + " at time " + this.time +  " by Vehicle " + this.vehicle.id() + " with Hashcode " + this.hashCode());
	}
	


}
