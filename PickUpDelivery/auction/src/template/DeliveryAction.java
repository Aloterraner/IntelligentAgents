package template;

import logist.simulation.Vehicle;
import logist.task.Task;

public class DeliveryAction extends Action{

	public DeliveryAction(Task task, int time, Vehicle vehicle, double costs) {
		super(task, time, vehicle, costs);
	}

	
	public DeliveryAction(DeliveryAction delivery_action){
		super(delivery_action.task, delivery_action.time, delivery_action.vehicle, delivery_action.costs); 
	}
	
	
	
	public String toString() {
		return ("Deliver Task: " + this.task.id + " at time " + this.time +  " by Vehicle " + this.vehicle.id() + " with Hashcode " + this.hashCode());
	}
	
	
	
	
}
