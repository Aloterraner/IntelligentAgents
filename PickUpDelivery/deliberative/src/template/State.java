package template;

import logist.topology.Topology.*;
import logist.task.Task;
import logist.task.TaskSet;

public class State {
	
	private City current;
	private TaskSet pickedUpTask;
	private TaskSet deliveredTask;
	public static TaskSet acceptedTask; 
	
	
	// Define a State based on the current Vehicle Position,
	// the aviable Task (Not yet delivered, Not yet PickedUp)
	// the pickedUp Task  
	// the delivered Task
	// 
	
	public State(City current, TaskSet pickedUpTask, TaskSet deliveredTask) {
		
		this.current = current;
		this.pickedUpTask = pickedUpTask;
		this.deliveredTask = deliveredTask; 

		
	}
	
	// Returns true if the state is an endstate, a state with no aviableTask and no pickedUpTask, i.e all Task have been successfully delivered
	public boolean isGoalState() {
		
		
		
		return (this.deliveredTask.equals(State.acceptedTask) && this.pickedUpTask.isEmpty()); 
		
	} 
		
	
	
	public City getCurrent() {
		return current;
	}

	public void setCurrent(City current) {
		this.current = current;
	}

	public TaskSet getPickedUpTask() {
		return pickedUpTask;
	}

	public void setPickedUpTask(TaskSet pickedUpTask) {
		this.pickedUpTask = pickedUpTask;
	}

	public TaskSet getDeliveredTask() {
		return deliveredTask;
	}

	public void setDeliveredTask(TaskSet deliveredTask) {
		this.deliveredTask = deliveredTask;
	}

	public static TaskSet getAcceptedTask() {
		return acceptedTask;
	}

	public static void setAcceptedTask(TaskSet acceptedTask) {
		State.acceptedTask = acceptedTask;
	}
	
	@Override
	public boolean equals (Object o) {
		
		
		if(this.current.equals(((State)o).current)) {
			
			if(this.deliveredTask.equals(((State)o).deliveredTask)){
				
				if(this.pickedUpTask.equals(((State)o).pickedUpTask)){
					
					return true;
				}
				
			}
			
		}
		
		return false; 
	}
}


