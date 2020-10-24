package template;

import logist.simulation.Vehicle;
import logist.task.Task;

public class DeliveryAction extends Action{

	public PickUpAction pickUpAction; 
	
	public DeliveryAction(Task task, int time, Vehicle vehicle, double costs, PickUpAction pickupAction) {
		super(task, time, vehicle, costs);
		this.pickUpAction = pickupAction; 
	}

	
	public String toString() {
		return ("Deliver Task: " + this.task.id + " at time " + this.time +  " by Vehicle " + this.vehicle.id() + " the associated pickupAction " + this.pickUpAction.toString());
	}
	
	
}
