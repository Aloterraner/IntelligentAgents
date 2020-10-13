package template;

import logist.topology.Topology.*;
import logist.task.Task;
import logist.task.TaskSet;

public class State {
	
	private City curCity;
	private TaskSet pickedUpTasks;
	private TaskSet deliveredTasks;
	public static TaskSet acceptedTasks; 
	
	
	// Define a State based on the current Vehicle Position,
	// the aviable Task (Not yet delivered, Not yet PickedUp)
	// the pickedUp Task  
	// the delivered Task
	public State(City curCity, TaskSet pickedUpTasks, TaskSet deliveredTasks) {
		this.curCity = curCity;
		this.pickedUpTasks = pickedUpTasks;
		this.deliveredTasks = deliveredTasks; 
	}
	
	// Returns true if the state is an endstate, a state with no aviableTask and no pickedUpTasks, i.e all Task have been successfully delivered
	public boolean isGoalState() {
		return (this.deliveredTasks.equals(State.acceptedTasks) && this.pickedUpTasks.isEmpty()); 	
	} 
	
	public City getCurCity() {
		return curCity;
	}

	public void setCurCity(City curCity) {
		this.curCity = curCity;
	}

	public TaskSet getPickedUpTasks() {
		return pickedUpTasks;
	}

	public void setPickedUpTasks(TaskSet pickedUpTasks) {
		this.pickedUpTasks = pickedUpTasks;
	}

	public TaskSet getDeliveredTasks() {
		return deliveredTasks;
	}

	public void setDeliveredTasks(TaskSet deliveredTasks) {
		this.deliveredTasks = deliveredTasks;
	}

	public static TaskSet getAcceptedTasks() {
		return acceptedTasks;
	}

	public static void setAcceptedTasks(TaskSet acceptedTasks) {
		State.acceptedTasks = acceptedTasks;
	}
	
	@Override
	public boolean equals (Object o) {
		
		if(this.curCity.equals(((State)o).curCity)) {
			
			if(this.deliveredTasks.equals(((State)o).deliveredTasks)){
				
				if(this.pickedUpTasks.equals(((State)o).pickedUpTasks)){
					
					return true;
				}
				
			}
			
		}
		
		return false; 
	}
}


