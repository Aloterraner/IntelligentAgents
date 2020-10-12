package template;

/* import table */
import template.AstarPlan;
import template.BFSPlan;
import logist.simulation.Vehicle;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;
import java.util.PriorityQueue;
import java.util.Queue;

import logist.agent.Agent;
import logist.behavior.DeliberativeBehavior;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * An optimal planner for one vehicle.
 */
@SuppressWarnings("unused")
public class DeliberativeTemplate implements DeliberativeBehavior {

	enum Algorithm { BFS, ASTAR, NAIVE}
	
	/* Environment */
	Topology topology;
	TaskDistribution td;
	
	/* the properties of the agent */
	Agent agent;
	int capacity;

	/* the planning class */
	Algorithm algorithm;
	
	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		this.topology = topology;
		this.td = td;
		this.agent = agent;
		
		
		// initialize the planner
		int capacity = agent.vehicles().get(0).capacity();
		String algorithmName = agent.readProperty("algorithm", String.class, "ASTAR");
		
		// Throws IllegalArgumentException if algorithm is unknown
		algorithm = Algorithm.valueOf(algorithmName.toUpperCase());

	}
	
	@Override
	public Plan plan(Vehicle vehicle, TaskSet tasks) {
		Plan plan;

		// Compute the plan with the selected algorithm.
		switch (algorithm) {
		case ASTAR:
			plan = aStar_Plan(vehicle, tasks);
			break;
		case BFS:
			plan = BFS_Plan(vehicle, tasks);
			break;
		case NAIVE:
			plan = naivePlan(vehicle, tasks);
			break;
		default:
			throw new AssertionError("Should not happen.");
		}		
		return plan;
	}
	
	private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);

		for (Task task : tasks) {
			// move: current city => pickup location
			for (City city : current.pathTo(task.pickupCity))
				plan.appendMove(city);

			plan.appendPickup(task);

			// move: pickup location => delivery location
			for (City city : task.path())
				plan.appendMove(city);

			plan.appendDelivery(task);

			// set current city
			current = task.deliveryCity;
		}
		return plan;
		
	}

	@Override
	public void planCancelled(TaskSet carriedTasks) {
		
		if (!carriedTasks.isEmpty()) {
			// This cannot happen for this simple agent, but typically
			// you will need to consider the carriedTasks when the next
			// plan is computed.
		}
	}
	
	private Plan BFS_Plan(Vehicle vehicle, TaskSet tasks) {
		
		State.acceptedTask = tasks; 
		
		TaskSet deliveryTask = TaskSet.noneOf(tasks);
		TaskSet pickedTask = TaskSet.noneOf(tasks); 
		
		State initState = new State(vehicle.getCurrentCity(), pickedTask, deliveryTask);
		
		System.out.println("Checking Equality"); 
		Node initNode = new Node(initState, null, 0.0);
		initState = new State(vehicle.getCurrentCity(), TaskSet.noneOf(tasks), TaskSet.noneOf(tasks));
		
		Node checkNode = new Node(initState, null, 0.0); 
		
		
		Node finalNode = new Node(new State(vehicle.getCurrentCity(), deliveryTask, tasks), null ,0.0); 
		
		
		HashSet<Node> test = new HashSet<Node>(); 
		test.add(finalNode); 
		test.add(checkNode);
		
		/*System.out.println(" The initnode is already in the Set:  " +test.contains(initNode)); 
		System.out.println(" The checknode is already in the Set: " +test.contains(checkNode)); 
		
		
		System.out.println("Test if GoalState works: " + finalNode.getState().isGoalState()); 
		System.out.println("Check if both nodes are equal: " + initNode.equals(checkNode)); 
		for(Node node : successor(initNode, vehicle)) System.out.println(node.toString());
		*/
		
		System.out.println("Starting Search ...");
		HashSet<Node> GoalNodes = BFS_Search(initNode, vehicle); 
		System.out.println("Finished Search");
		// Find the optimal Node
		
		System.out.println("Goal Nodes: " + GoalNodes.toString() );
		// Select the GoalNode with the lowest overall Costs
		double min = Double.MAX_VALUE; 
		Node optimal_node = null; 
		
		
		for(Node node : GoalNodes) {
			
			if(node.getCost() < min) {
				min = node.getCost();
				optimal_node = node; 
			}			
			
		}
		
		System.out.println("Found Optimal Node with cost: " + optimal_node.getCost());
		
		Plan plan = parse_Plan(optimal_node);
		
		return plan; 
		
	}
	
	private HashSet<Node> BFS_Search(Node initNode, Vehicle vehicle) {
		Deque<Node> Q = new LinkedList<Node>(); 
		HashSet<Node> GoalNodes = new HashSet<Node>();
		HashSet<Node> C = new HashSet<Node>(); 
		int count = 0; 
		
		Node n; 
		Q.add(initNode);
		
		do {
			// n = first(Q), Q <- Rest(Q)
			n = Q.pop();
			/*if(count < 5000) {
				System.out.println(n.toString()); 
			}*/
			
			// n's state is a Goal State, add it to the goal states
			
			if (n.getState().isGoalState()) {
				GoalNodes.add(n); 
				System.out.println("Found a Goal State"); 
				
				
			// Check for Cycle, if not proceed with successor, prevent fall through from Goalstates as this would lead to
		    // non-termination if succ of Goal state would be considered, as all possible succ() would be equally Goal States
			}else if(!C.contains(n)){
			
				C.add(n); 
				
				// succ(n)
				ArrayList<Node> S = successor(n, vehicle);
				
				// Append(Q,S) as in Lecture Slide 18/56
				Q.addAll(S);		
				
		
			}
			
			if(count % 5000 == 0) {
				System.out.println("Iteration: " + count); 
			}
			
			count++; 
		} while(!Q.isEmpty());
		
		
		
		return GoalNodes; 
		
	}
	
	private Plan aStar_Plan(Vehicle vehicle, TaskSet tasks) {

		State.acceptedTask = tasks; 
		
		TaskSet deliveryTask = TaskSet.noneOf(tasks);
		TaskSet pickedTask = TaskSet.noneOf(tasks); 
		
		State initState = new State(vehicle.getCurrentCity(), pickedTask, deliveryTask);
		
		System.out.println("Checking Equality"); 
		Node initNode = new Node(initState, null, 0.0);
		initState = new State(vehicle.getCurrentCity(), TaskSet.noneOf(tasks), TaskSet.noneOf(tasks));
		
		Node checkNode = new Node(initState, null, 0.0); 
		
		
		Node finalNode = new Node(new State(vehicle.getCurrentCity(), deliveryTask, tasks), null ,0.0); 
		
		
		HashSet<Node> test = new HashSet<Node>(); 
		test.add(finalNode); 
		test.add(checkNode);
		
		/*System.out.println(" The initnode is already in the Set:  " +test.contains(initNode)); 
		System.out.println(" The checknode is already in the Set: " +test.contains(checkNode)); 
		
		
		System.out.println("Test if GoalState works: " + finalNode.getState().isGoalState()); 
		System.out.println("Check if both nodes are equal: " + initNode.equals(checkNode)); 
		for(Node node : successor(initNode, vehicle)) System.out.println(node.toString());*/
		
		
		System.out.println("Starting Search ...");
		Node optimal_node = ASTAR_Search(initNode, vehicle); 
		System.out.println("Finished Search");
		// Find the optimal Node
		
		System.out.println("Found Optimal Node with cost: " + optimal_node.getCost());
		
		System.out.println("Compute plan from optimal node"); 
		
		Plan plan = parse_Plan(optimal_node);
		
		return plan; 
	}
	
	private Node ASTAR_Search(Node initNode, Vehicle vehicle) {
		PriorityQueue<Node> Q = new PriorityQueue<Node>(); 
		HashSet<Node> C = new HashSet<Node>(); 
		int count = 0; 
		
		Node n; 
		Q.add(initNode);
		
		do {
			//n = first(Q), Q <- Rest(Q)
			/*PriorityQueue<Node> Q_test =  new PriorityQueue<Node>(Q);

			while(!Q_test.isEmpty()) {
				Node test = Q_test.poll();
				System.out.println(test.toString() + " " + test.getCost());
			}
			System.out.println("_____");*/
			
			n = Q.poll();

			
			/*if(count < 500 ) {
				System.out.println(n.toString()); 
			}*/
			
			// n's state is a Goal State, add it to the goal states
			
			if (n.getState().isGoalState()) {
				

				System.out.println("Found a Goal State"); 
				return n;
				
			// Check for Cycle, if not proceed with successor, prevent fall through from Goalstates as this would lead to
		    // non-termination if succ of Goal state would be considered, as all possible succ() would be equally Goal States
			}else if(!C.contains(n)){
			
				C.add(n); 
				
				// succ(n)
				ArrayList<Node> S = successor(n, vehicle);
				
				// Append(Q,S) as in Lecture Slide 18/56
				Q.addAll(S);		
				
			}
			
			if(count % 5000  == 0) {
				System.out.println("Iteration: " + count); 
			}
			
			count++; 
		}while(!Q.isEmpty());
		
		
		return null; 
	}
	
	private  ArrayList<Node> successor(Node n, Vehicle vehicle){
		
		ArrayList<Node> S = new ArrayList<Node>(); 
		State curr_state = n.getState(); 
		TaskSet tasks = State.acceptedTask; 
		double cost; 
		State succ_state; 
		
		
		TaskSet deliverable_tasks = TaskSet.noneOf(State.getAcceptedTask()); 
		TaskSet pickable_tasks = TaskSet.noneOf(State.getAcceptedTask());
		TaskSet availableTasks = TaskSet.intersectComplement(tasks, TaskSet.union(curr_state.getDeliveredTask(), curr_state.getPickedUpTask())); 
		
		//System.out.println("Available Tasks: " + availableTasks.toString()); 
		
		// Check, which not yet PickedUp Task might be picked up in this city (AviableTasks intersected with all not yet delievered Tasks)
		
		for(Task task : availableTasks){
			
			// Add a Task to be PickUpable in a City, if we are in the correct city and the overall Sum of weight, does not exceed our capacity
			if(task.pickupCity == curr_state.getCurrent() && vehicle.capacity()  > (curr_state.getPickedUpTask().weightSum() + task.weight)) {
				pickable_tasks.add(task);
			}
		}
		
		
		//System.out.println("Pickable Tasks: " + pickable_tasks.toString()); 
		
		
		// Check, which not yet Delivered Task might be delivered in this city 
		for(Task task : curr_state.getPickedUpTask()){
			if(task.deliveryCity == curr_state.getCurrent()){
				deliverable_tasks.add(task);
			}
		}
		
		
		TaskSet current_load = TaskSet.intersectComplement(curr_state.getPickedUpTask(), deliverable_tasks);
		
		// All possible Successor States, if we choose to PickUp a pickable Task
		
		// Might improve on this by selecting a set of all possibly pickUpable States 
		for(Task task : pickable_tasks) {
			
			
			// Create a new TaskSet for PickedUp Actions plus the PickedUp task

			TaskSet new_pickedup = current_load.clone(); 
			new_pickedup.add(task); 
			
			// Create the new corresponding state object
			succ_state = new State(curr_state.getCurrent(), new_pickedup, TaskSet.union(deliverable_tasks, curr_state.getDeliveredTask()));  
			
			// The cost is identical for this action, as no movement is involved
			cost = n.getCost(); 
			
			// Add the new node to the successors
			S.add(new Node(succ_state, n, cost)); 
		}
		
		// All possible Successor State, if we choose to Move to City X
		for(City next : curr_state.getCurrent().neighbors()) {
			
				// The costs equals the cost of the previous node, plus the cost of movement to the new state
				cost = n.getCost() + (curr_state.getCurrent().distanceTo(next) * vehicle.costPerKm());
				
				succ_state = new State(next, current_load, TaskSet.union(deliverable_tasks, n.getState().getDeliveredTask()));  
				
				// Add the new node to the successors
				S.add(new Node(succ_state, n, cost));
		}
		
		return S;
		
	}
	
	private Plan parse_Plan(Node optimal_node) {
		System.out.println("Compute plan from optimal node"); 
		
		Stack<Node> nodes = new Stack<Node>();
		
		Node pred = optimal_node; 
		
		// Push all nodes on a stack, so the first element is the start node
		do {
			nodes.push(pred); 
			pred = pred.getPredecssesor();
			
		} while(pred != null); 
		
		
		// Start processing
		System.out.println("Identifiying associated Actions");
		
		Node cur_node;
		Node next_node = nodes.pop();
		Plan plan = new Plan(next_node.getState().getCurrent()); // intialize plan with starting city
		
		while (nodes.size() > 0) {
			cur_node = next_node;
			next_node = nodes.pop();
			
			// check for delivery first
			if (!cur_node.getState().getDeliveredTask().equals(next_node.getState().getDeliveredTask())) {
				TaskSet delivered_tasks = TaskSet.intersectComplement(next_node.getState().getDeliveredTask(), cur_node.getState().getDeliveredTask());
				for (Task task : delivered_tasks) {
					plan.appendDelivery(task);
				}
			}
			
			// check for pickup 
			if (!cur_node.getState().getPickedUpTask().equals(next_node.getState().getPickedUpTask())) {
				TaskSet pickedup_tasks = TaskSet.intersectComplement(next_node.getState().getPickedUpTask(), cur_node.getState().getPickedUpTask());
				for (Task task: pickedup_tasks) {
					plan.appendPickup(task);
				}
			}
			
			// check for move
			if (cur_node.getState().getCurrent() != next_node.getState().getCurrent()) {
				plan.appendMove(next_node.getState().getCurrent());
			}
			
		}
		return plan;
	}
}
