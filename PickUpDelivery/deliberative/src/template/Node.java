package template;
import java.lang.Comparable;

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
	

	
	public int getH_cost() {
		
		
		return 0; 
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
		//System.out.println("I was here"); 
		
		
		
			if(this.state.equals(((Node)o).state)){
				
					return true;
					
			}
		
		
		
		return false;
		
		
	}
	@Override
	public int hashCode(){
		return this.toString().hashCode();
	}
	
	@Override 
	public String toString() {
		return (this.state.getCurrent().toString() +  " " + this.state.getPickedUpTask().toString() + " " + this.state.getDeliveredTask().toString()); 
	}

	
	@Override
	public int compareTo(Node e) {

		
		
		
		return 0; 
	}

	
	

}
