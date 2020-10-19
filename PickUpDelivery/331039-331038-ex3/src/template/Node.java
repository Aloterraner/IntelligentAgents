package template;
import java.lang.Comparable;

import logist.task.TaskSet;
import logist.task.Task;


// Use Comparable and Comparator to impose natural ordering and usage of priority queue 

public class Node implements Comparable<Node>{

	private static final String Node = null;

	// The State represented by the Node
	private State state; 
	
	// The predecessor node in the Search Tree
	private Node predecssesor; 
	
	// The cost of reaching the node
	private double cost;

	
	public Node(State state, Node pred, double cost) {
		
		this.setState(state); 
		this.setPredecssesor(pred); 
		this.setCost(cost);
	}
	
	
	public State getState() {
		return state;
	}


	public void setState(State state) {
		this.state = state;
	}


	public Node getPredecssesor() {
		return predecssesor;
	}


	public void setPredecssesor(Node predecssesor) {
		this.predecssesor = predecssesor;
	}


	public double getCost() {
		return cost;
	}


	public void setCost(double cost) {
		this.cost = cost;
	}
	
	
	@Override 
	public boolean equals(Object o) {
		if(this.state.equals(((Node)o).state)){
			if (this.cost >= ((Node)o).cost) {
				return true;
			}
					
		}
		
		return false;
		
	}
	
	@Override
	public int hashCode(){
		return this.toString().hashCode();
	}
	
	
	@Override 
	public String toString() {
		return (this.state.getCurCity().toString() +  " " + this.state.getPickedUpTasks().toString() + " " + this.state.getDeliveredTasks().toString()); 
	}

	
	@Override
	public int compareTo(Node e) {
		
		// return (int)((this.cost + heuristic(this)) - (e.cost + heuristic(e)));

		if (this.cost + heuristic(this) > e.cost + heuristic(e)) {
			return 1;
		}
		else if (this.cost + heuristic(this) < e.cost + heuristic(e)) {
			return -1;
		}
		else {
			return 0;
		}
	}


	public double heuristic(Node n) {
		TaskSet remaining_tasks = TaskSet.intersectComplement(State.acceptedTasks, this.getState().getDeliveredTasks());
		
		double min_cost = Double.POSITIVE_INFINITY;
		
		for (Task task : remaining_tasks) {
			if (task.pickupCity.distanceTo(task.deliveryCity) * 5 < min_cost) {
				min_cost = task.pickupCity.distanceTo(task.deliveryCity) * 5; // TODO: costs per km are hard-coded here
			}
		}
		
		return min_cost;
	}
}