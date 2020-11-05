package template;

//the list of imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 * 
 */
@SuppressWarnings("unused")
public class AuctionTemplate implements AuctionBehavior {

	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	private Random random;
	private Vehicle vehicle;
	private City currentCity;
	private HashMap<Integer,ArrayList<Action>> plan;
	private boolean first_it;
	private Set<Task> won_tasks;
	
	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {

		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;
		this.vehicle = agent.vehicles().get(0);
		this.currentCity = vehicle.homeCity();
		this.plan = new HashMap<Integer,ArrayList<Action>>();
		this.first_it = true;
		this.won_tasks = new HashSet<Task>();
		
		long seed = -9019554669489983951L * currentCity.hashCode() * agent.id();
		this.random = new Random(seed);
	}

	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
		if (winner == agent.id()) {
			currentCity = previous.deliveryCity;
		}
	}
	
	@Override
	public Long askPrice(Task task) {
		
		HashMap<Integer,ArrayList<Action>> cur_plan = new HashMap<Integer,ArrayList<Action>>();
		
		won_tasks.add(task);

		if (this.first_it) {
	    	cur_plan = SelectInitialSolution(won_tasks, topology, agent); 
	    	this.first_it = false;
	    	
		}
		else {
			List<Plan> plans = SLS_algorithm(won_tasks, topology, agent, 0.4);
		}


		if (vehicle.capacity() < task.weight)
			return null;

		long distanceTask = task.pickupCity.distanceUnitsTo(task.deliveryCity);
		long distanceSum = distanceTask
				+ currentCity.distanceUnitsTo(task.pickupCity);
		double marginalCost = Measures.unitsToKM(distanceSum
				* vehicle.costPerKm());

		double ratio = 1.0 + (random.nextDouble() * 0.05 * task.id);
		double bid = ratio * marginalCost;

		return (long) Math.round(bid);
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		
		//		System.out.println("Agent " + agent.id() + " has tasks " + tasks);
		
		return parsePlan(this.plan);
		
	}

	private Plan naivePlan(Vehicle vehicle, Set<Task> tasks) {
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
	
	
    void SLS_algorithm(Set<Task> tasks, Topology topology, Agent agent, double p) {
    	/*
    	 * Implementation of the SLS algorithm. Base call to compute the plan.
    	 * 
    	 * @param tasks:	task set to be processed
    	 * @param topology:	topology used in this run
    	 * @param agent:	agent used in this run
    	 * @param p:		rejection probability used in local_choice()
    	 * @return:			parsed plan for logist platform
    	 */
    	
    	long time_start = System.currentTimeMillis();
    	long time_needed = 0;
    	long wrap_up_time = (long)(timeout_plan * 0.005); 
    	
    	
    	print_plan(plan); 
    	
    	System.out.println("Dev: Check if the Initial Plan is a fullfilling the constraints: " + verify_constraint(plan)); 
    	System.out.println("Dev: The costs for the Initial Plan are as follows: " + CalculateCost(plan));
    	
    	int counter = 0;
    	HashMap<Integer,ArrayList<Action>> plan_old;
    	HashSet<HashMap<Integer,ArrayList<Action>>> neighbors;
    	
    	// iterate
    	System.out.println("Start Iteration ... "); 
    	while(counter < num_iterations) {
    		neighbors = ChooseNeighbours(plan);
    		
    		plan = LocalChoice(neighbors, plan, p);
    		
    		counter += 1;
    		time_needed = System.currentTimeMillis() - time_start;
    		
    		if (time_needed > timeout_plan - wrap_up_time) {
    			System.out.println("TimeOut Reached, Parsing Plan!");
    			break; 
    		}
    	}
    	
    	System.out.println("Costs of the Plan: " + CalculateCost(plan)); 
    	
    }
    	

    private HashMap<Integer,ArrayList<Action>> SelectInitialSolution(Set<Task> tasks, Topology topology, Agent agent){
    	/*
    	 * Compute an initial plan based on distributing each task to the nearest vehicle that has some capacity left. 
    	 * HashMap Mapping Vehicle to Actions for the Vehicle
	     * 
	     * v_1 -> List of Actions for Vehicle 1  
	     *  .
	     *  . 
	     *  . 
	     * v_k -> List of Actions for Vehicle k
	     * 
	     * @param tasks:	task set which has to be processed
	     * @param topology:	topology used in this run
	     * @param agent:	agent used in this run
	     * @return:			initial (naive) plan as described above
    	 */
    	
    	// Generate a basic array in the length of the number of vehicles
    	HashMap<Integer, ArrayList<Action>> plan = new HashMap<Integer,ArrayList<Action>>(); 
    	double[] current_load = new double[agent.vehicles().size()];
    	int[] current_time = new int[agent.vehicles().size()];
    	double task_weights =  0; 
    	Vehicle veh = null; 
    	
    	for(Vehicle vehicle : agent.vehicles()) {
    		plan.put(vehicle.id(), new ArrayList<Action>()); 
    	}
    	
    	for (Task task : tasks) {
    		task_weights += task.weight;
    	}
    	
    	task_weights = (task_weights + 1) / agent.vehicles().size(); 
    	
    	// Check for each Tasks what the closest vehicle is 
    	for (Task task : tasks) {
    		double min_distance = Double.MAX_VALUE; 
    		int smallest = 0; 
    		
    		// Check for each Vehicle
    		for(Vehicle vehicle : agent.vehicles()) {
    			
    			// Check if it has enough room
    			if(current_load[vehicle.id()] + task.weight < task_weights) {
    				
    				// How close it is
    				if( task.pickupCity.distanceTo(vehicle.getCurrentCity()) < min_distance) {
    					
    					min_distance = task.pickupCity.distanceTo(vehicle.getCurrentCity()); 
    					smallest = vehicle.id(); 
    					veh = vehicle;
    					
    				}
    				
    			}
    			
    		}
    	
    		plan.get(smallest).add(new PickUpAction(task,current_time[smallest], veh, 0.0));
    		current_time[smallest]++;    		
    		plan.get(smallest).add(new DeliveryAction(task,current_time[smallest], veh, 0.0)); 
    		current_time[smallest]++; 
    		
    		current_load[smallest] += task.weight;
    		
    	}
    				
    	return plan; 
    }
   
     
    private List<Plan> parsePlan(HashMap<Integer,ArrayList<Action>> intermediate_plans){
    	/*
    	 * Parsing our plan representation to make usable by the logist framework.
    	 * 
    	 * @param intermediate_plans:	current plan
    	 * @return:						plan list
    	 */
    	
    	long time_start = System.currentTimeMillis();
    	long time_needed = 0;
    	
    	List<Plan> result = new ArrayList<Plan>();
    	
    	for(Vehicle vehicle : agent.vehicles()) {
    		City current = vehicle.getCurrentCity();
    		Plan plan = new Plan(current);  
    		
    		System.out.println("Parsing Plan for Vehicle:" + vehicle.id()); 
    		for(Action action : intermediate_plans.get(vehicle.id())) {
    			
    			if(action instanceof PickUpAction) {
    				for(City city :  current.pathTo(action.task.pickupCity)) {
    					
    					plan.appendMove(city);; 
    					
    				}
    				plan.appendPickup(action.task);
    				current = action.task.pickupCity; 
    			}	
    				
    			if(action instanceof DeliveryAction) {
    				for(City city :  current.pathTo(action.task.deliveryCity)) {

    					plan.appendMove(city);; 
    					
    				}
    				plan.appendDelivery(action.task);
    				current = action.task.deliveryCity; 
    			}

    		}
    		
			result.add(plan); 
    	}
    	
    	System.out.println("Time needed to Parse the Plan: " + (System.currentTimeMillis()- time_start));
    	return result; 
    }
    

    private HashSet<HashMap<Integer,ArrayList<Action>>> ChooseNeighbours(HashMap<Integer,ArrayList<Action>> plan) {
    	/*
    	 * Produces small variations of the current plan by executing the change vehicle operator
    	 * and the change task order operator. Adds those plan candidates to a candidate set if they
    	 * satisfy all CSP constraints,
    	 * 
    	 * @param plan:	current plan
    	 * @return:		hash set of plans containing new valid candidates
    	 */
    	
    	HashSet<HashMap<Integer,ArrayList<Action>>> neighbors = new HashSet<HashMap<Integer,ArrayList<Action>>>();
    	HashMap<Integer,ArrayList<Action>> new_plan;

    	
    	Random rand = new Random();
    	
    	int v_i = 0;
    	
    	do {
    		v_i = rand.nextInt(this.agent.vehicles().size());
    	}
    	while(plan.get(v_i).size() == 0);
    	
    	// Applying the changing vehicle order operator
    	for (int v_j = 0; v_j < this.agent.vehicles().size(); v_j++) {
    		if (v_j == v_i) {
    			continue;
    		}
    		
    		new_plan = ChangingVehicle(v_i, v_j, copyPlan(plan));
    		
    		if(verify_constraint(new_plan)){
    			neighbors.add(new_plan);
    		}

    	}
    	
    	// Applying the changing task order operator, not in extra function call
    	int vehicle = rand.nextInt(this.agent.vehicles().size());
    	Action temp;
    	
    	for (int idx1=0; idx1 < plan.get(vehicle).size() - 1; idx1 ++) {
    		for (int idx2=idx1+1; idx2 < plan.get(vehicle).size(); idx2 ++) {

    			new_plan = copyPlan(plan);
    			
    			temp = new_plan.get(vehicle).get(idx1);
    			new_plan.get(vehicle).set(idx1, new_plan.get(vehicle).get(idx2));
    			new_plan.get(vehicle).set(idx2, temp);
    	
    			UpdateTime(new_plan, vehicle, idx1);
    			if (verify_constraint(new_plan)) {
    				
    				neighbors.add(new_plan);
    			}
    		}
    	}
   	
    	return neighbors; 
    	
    }


    private double CalculateCost(HashMap<Integer,ArrayList<Action>> plan) {
    	/*
    	 * Computes the costs of the current plan.
    	 * 
    	 * @param plan:		current plant
    	 * @return:			costs of current plan as double
    	 */
    	double cost = 0.0; 
    	
    	for(Vehicle vehicle : this.agent.vehicles()) {
    		
    		// Calculate the cost of all moves made in the plan
    		City prev = vehicle.getCurrentCity(); 
    		
    		for(Action action : plan.get(vehicle.id())){
    			
    			
    			// Check if it is either a PickUp or a Delivery Action and then add the cost of the move from the previous city to the costs. 
    			if(action instanceof DeliveryAction) {
    				
    				cost += (prev.distanceTo(action.task.deliveryCity) * vehicle.costPerKm());
    				prev = action.task.deliveryCity;
    				
    			}else if(action instanceof PickUpAction){
    				
    				cost += (prev.distanceTo(action.task.pickupCity) * vehicle.costPerKm()); 
    				prev = action.task.pickupCity; 
    				
    			}
    			
    		}
    		
    	}
    	
    	return cost; 
    }

    
    private HashMap<Integer,ArrayList<Action>> ChangingVehicle(Integer v1, Integer v2, HashMap<Integer,ArrayList<Action>> plan) {
    	/*
    	 * Looks at different local variation of the current plan by switching 
    	 * tasks between vehicles.
    	 * 
    	 * @param v1:	vehicle id of first vehicle
    	 * @param v2: 	vehicle id of second vehicle
    	 * @param plan:	current plan available
    	 * @return:		updated plan after task has been swapped from vehicle 1 to vehicle 2
    	 */
   
    	// get a random task from vehicle 1 -> equal number of pickUps and deliveries, thus we have num_actions/2 different tasks
    	Random rand = new Random();
    	int t = rand.nextInt(plan.get(v1).size() / 2); 
    	int task_id = plan.get(v1).get(t).getTask().id;

    	// remove action associated with position t from list
    	Action action2swap = plan.get(v1).remove(t);
    	Action action2swap_pendant = null;
    	
    	// search for corresponding pickup or delivery action
    	for (Action a : plan.get(v1)) {
    		if (a.getTask().id == task_id) {
    			// and remove it from v1 if found
    			int a_idx = plan.get(v1).indexOf(a);
    			if (a_idx < t) {
    				t = a_idx;
    			}
    			action2swap_pendant = plan.get(v1).remove(a_idx);
    			// add actions to the end of v2's plan
    			// ensure that pickup action is added first
    			
    	    	if (action2swap instanceof PickUpAction) {
    	    		plan.get(v2).add(action2swap);
    	    		plan.get(v2).add(action2swap_pendant);
    	    	}
    	    	else if (action2swap instanceof DeliveryAction) {
    	    		plan.get(v2).add(action2swap_pendant);
    	    		plan.get(v2).add(action2swap);
    	    	}
    	    	else {
    	    		System.out.println("Action list for vehicle " + v1 + "contains an action which has invalid type.");
    	    		return null;
    	    	}
    	    	 
    			// stop searching
    			break;
    		}
    	}
    	
    	if (action2swap.time > action2swap_pendant.time) {
			UpdateTime(plan, v2, plan.get(v2).indexOf(action2swap_pendant));
		}
    	else {
    		UpdateTime(plan, v2, plan.get(v2).indexOf(action2swap));
    	}
 
 
    	// Update respective attributes according to new plan
    	UpdateTime(plan, v1, t);
    	
    	return plan;
    	
    }
    
    
    private void UpdateTime(HashMap<Integer,ArrayList<Action>> plan, int vehicle_id, int idx) {
    	/*
    	 *  Update the time attribute starting at a certain action in the plan.
    	 *  @param plan:		current plan
    	 *  @param vehicle_id:	vehicle for which we need to update the actions' time attributes
    	 *  @param idx:			position in plan from where we start updating
    	 */
    	
    	if (idx != 0) {
    		idx -= 1;
    	}
    	
    	for (int i = idx; i < plan.get(vehicle_id).size(); i++) {
    		plan.get(vehicle_id).get(i).time = i;
    	}
    }
    
    
    private HashMap<Integer,ArrayList<Action>> LocalChoice(HashSet<HashMap<Integer,ArrayList<Action>>> set, HashMap<Integer, ArrayList<Action>> current_plan, double p) {
    	/*
    	 * Selects the optimal plan in accordance with the lowest cost. Optimal plan is returned with probability p
    	 * to escape local optima.
    	 * 
    	 * @param set:			hash set of possible plans from which we want to select the optimal.
    	 * @param current_plan:	current plan which we return with probability 1 - p
    	 * @param p:			rejection probability
    	 * @return:				next plan
    	 */
    	
    	double min_costs = Double.POSITIVE_INFINITY;
    	HashMap<Integer,ArrayList<Action>>  best_plan = null;
    	for (HashMap<Integer,ArrayList<Action>> plan : set) {
    		double cur_costs = CalculateCost(plan);
    		if (cur_costs < min_costs) {
    			min_costs = cur_costs;
    			best_plan = plan;
    		}
    	}

    	Random rand = new Random();
    	
    	// Adds a random chance to either select the best or the old plan
    	if(rand.nextDouble() < p) {
    		return current_plan;
    	}else{
    		return best_plan;
    	}

    }
    
    
    private void print_plan(HashMap<Integer,ArrayList<Action>> plan) {
    	/*
    	 * Debug Function to print out a plan in the console.
    	 * 
    	 * @param plan:	plan which we want to print
    	 */
    	
    	for (int vehicle_id=0; vehicle_id < plan.size(); vehicle_id++) {
    		System.out.print("Plan for vehicle: " + vehicle_id + ": ");
    		System.out.print(plan.get(vehicle_id).toString() + "\n");
    	}
    	
    }
    
    
    private boolean verify_constraint(HashMap<Integer,ArrayList<Action>> plan) {
    	/*
    	 * Checks if the passed satisfies the constraints defined in the CSP.
    	 * Constraint (2) satisfied by Construction, as every NextAction of an Action remains in the same vehicle context 
    	 * Constraint (3) satisfied by Construction, Uniqueness of Actions guaranteed by change_functions and Init.    
		 * Constraint (5) satisfied by Construction, every chain of actions starts with a vehicle
    	 * 
    	 * @param plan:	plan for which we test the CSP constraints
    	 * @return:		false, as soon as one constraint is violated. true, otherwise.
    	 */
    	
    	for(Vehicle vehicle : agent.vehicles()) {

    		// Check constraint (1), Event Times of following events are subsequent integers. 
    		int time = -1; 
    		for (Action action : plan.get(vehicle.id())){
    			
    			if(action.time != time +1) {
    				return false;
    			}
    			
    			time++;
    		}

    		// Constraint (4) and (7), Vehicle remains always below its maximum capacity
    		int current_load = 0;
    		for(Action action : plan.get(vehicle.id())) {
    			
    			// Add the weight to the current load
    			if(action instanceof PickUpAction){
    				current_load += action.task.weight; 
    				
    				
    				// Violation of (4), Check if adding the package would lead to going over capacity
    				if(current_load > vehicle.capacity()) {
    					return false; 
    				}
    			}
    			
    			// Pop the weight of the current load
    			if(action instanceof DeliveryAction){
    				current_load -= action.task.weight; 
    				
    				// Violation of (6) Check if the package is already loaded at the time of delivery
    				for(Action pick_action : plan.get(vehicle.id())) {
    					if(pick_action instanceof PickUpAction) {
    						if(pick_action.task.id == action.task.id) {
    							if(pick_action.time > action.time) {
    								return false;
    							}
    							
    						}
    						
    					}
    					
    				}
    					
    			}
    			
    		}

    	}
    	
		return true;

    }
    
    
    private HashMap<Integer,ArrayList<Action>> copyPlan(HashMap<Integer,ArrayList<Action>> plan){
    	/*
    	 * Deep copy of a plan object, copying all attributes
    	 * 
    	 * @param plan:	plan to be copied
    	 * @return:		new plan object with attributes copied from input plan
    	 */
    	
    	HashMap<Integer,ArrayList<Action>> copy = new HashMap<Integer,ArrayList<Action>>(); 
    	
    	for(Vehicle vehicle : agent.vehicles()) {
    		
    		ArrayList<Action> list_copy = new ArrayList<Action>(); 
    		
    		for(Action action : plan.get(vehicle.id())){
    			
    			Action copy_action; 
    			
    			if(action instanceof DeliveryAction){
    				copy_action = new DeliveryAction((DeliveryAction) action); 
    				
    			}else{
    				copy_action = new PickUpAction((PickUpAction) action); 
    				
    			}
    			
    			list_copy.add(copy_action); 
    				
    		}
    		
    		copy.put(vehicle.id(), list_copy); 
    		
    		
    	}
    	
		return copy;
    }
    
}


