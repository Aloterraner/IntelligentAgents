package template;

//the list of imports
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.*;
import java.util.stream.IntStream;
import java.util.Collections;

import java.io.File;

import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.agent.Agent;
import logist.agent.AgentImpl;
import logist.simulation.Vehicle;
import logist.plan.Action.Pickup;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;
import logist.LogistSettings;
import logist.config.Parsers;


/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 * 
 */
@SuppressWarnings("unused")
public class AuctionTemplate_SLS implements AuctionBehavior {

	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent; 
	private boolean first_it;
	private Set<Task> won_tasks;
	private long timeout_plan;
	private HashSet<Task> availableTasks;  
	private int round_counter; 
	private int sls_iteration; 
	private double probabilty; 
	private HashMap<Integer,ArrayList<Action>> plan;
	private HashMap<Integer,ArrayList<Action>> old_plan;
	
	
	
	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {

		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;
		this.plan = new HashMap<Integer,ArrayList<Action>>();
		this.first_it = true;
		this.won_tasks = new HashSet<Task>();
		this.availableTasks = new HashSet<Task>(); 
		this.round_counter = 0; 
		this.probabilty = 0.4; 
		this.sls_iteration = 1500; 
		// the plan method cannot execute more than timeout_plan milliseconds+
		
		Random rnd;
		rnd = new Random(); 
		
		LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config" + File.separator + "settings_auction.xml");
        }
        catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }
        timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);
        

		// Read the opponents Vehicles starting City, or pick a random one
		
		System.out.println("\n \n CALLED SETUP FOR AGENT: " + agent.id() + " \n \n");
	}

	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
		
		if (winner == agent.id()) {
			
			old_plan = plan;
			this.first_it = false; 
			won_tasks.add(previous); 
			System.out.println("Won auction on Task " + previous);
			System.out.println("Won Task for Agent: " + agent.id() + "  " + won_tasks);
		
		}else{
			
		
			System.out.println("Lost Bidding");
			plan = copyPlan(old_plan);
		}
		
		
		
		System.out.println("The bids: ");
		for (Long bid : bids) {
			System.out.println("Bid: " + bid); 
		}
		
		

	}
	
	@Override
	public Long askPrice(Task task) {
		
		double bid; 
		availableTasks.add(task); 

		round_counter++; 


		if (this.won_tasks.size() == 0) {
			
	    	plan = SelectInitialSolution(topology, agent, task); 
	    	old_plan = copyPlan(plan);
	    	
		} else {
			
			plan = SLS_algorithm(topology, agent, task, old_plan);
			
		}

		if(this.won_tasks.size() == 0) {
			
		bid =  CalculateCost(old_plan); 
		
		} else {

		bid = CalculateCost(plan) - CalculateCost(old_plan);
		
		}
		
		System.out.println("\n\nRound: " + round_counter + " BID FOR AGENT: " + agent.id());
		System.out.println("Bid for Agent: " + agent.id() + " this round is: " + bid);
		
		
		// If we have a Zero Margin just bid 100.0
		if (bid < 0.0 ) {
			bid = 100.0; 
		}
		return (long) Math.round(bid);
		
	}

	
	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
	  	System.out.println("\n\nPARSING PLAN FOR AGENT: " + agent.id()); 
	  	
		System.out.println("The Final Plan");
		print_plan(this.plan);
		return parsePlan(this.plan, tasks);
		
	}

	
	
    private HashMap<Integer,ArrayList<Action>> SLS_algorithm(Topology topology, Agent agent, Task new_task, HashMap<Integer,ArrayList<Action>> passed_plan) {
   	    
    	/*
    	 * Implementation of the SLS algorithm. Base call to compute the plan.
    	 * 
    	 * @param tasks:	task set to be processed
    	 * @param topology:	topology used in this run
    	 * @param agent:	agent used in this run
    	 * @param p:		rejection probability used in local_choice()
    	 * @param task:		new task which we bid for
    	 * @return:			plan object
    	 */
    	
    	HashMap<Integer, ArrayList<Action>> plan = new HashMap<Integer,ArrayList<Action>>(); 
    			
    	long time_start = System.currentTimeMillis();
    	long time_needed = 0;
    	long wrap_up_time = (long)(timeout_plan * 0.005); 
    	
    	plan = copyPlan(passed_plan); 
    	// get time of last action
    	
    	int last_time;
    	if(plan.get(agent.vehicles().get(0).id()).size() == 0) {
    		last_time = -1;
    	}else {
    		last_time = plan.get(agent.vehicles().get(0).id()).get(plan.get(agent.vehicles().get(0).id()).size() - 1).getTime(); 
    	}
    	 
    	plan.get(agent.vehicles().get(0).id()).add(new PickUpAction(new_task, last_time + 1, agent.vehicles().get(0))); // append new PickUp action
    	plan.get(agent.vehicles().get(0).id()).add(new DeliveryAction(new_task, last_time + 2, agent.vehicles().get(0))); // append new DeliveryAction
    	
    	double best_costs = CalculateCost(plan);
    	HashMap<Integer,ArrayList<Action>> best_plan = copyPlan(plan);   	
    	int counter = 0;
    	HashMap<Integer,ArrayList<Action>> plan_old;
    	HashSet<HashMap<Integer,ArrayList<Action>>> neighbors;
    	
    	
    	// iterate 
    	
    	while(counter < sls_iteration) {
    		neighbors = ChooseNeighbours(plan);
    		
    		plan = LocalChoice(neighbors, plan, probabilty);
    		
    		double cur_costs = CalculateCost(plan);
        	
        	if (cur_costs < best_costs) {
        		best_costs = cur_costs;
        		best_plan = copyPlan(plan);
        		counter = 0;
        	}
        	
        	else {
        		
        		counter += 1;
        		
        	}
        	
    		time_needed = System.currentTimeMillis() - time_start;
    		
    		if (time_needed > timeout_plan - wrap_up_time) {
    			System.out.println("Time out reached");
    			break; 
    		}
    	}
    	
    	return best_plan;
    }
    	

    private HashMap<Integer,ArrayList<Action>> SelectInitialSolution(Topology topology, Agent agent, Task new_task){
    	
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
    	HashMap<Integer, ArrayList<Action>> new_plan = new HashMap<Integer,ArrayList<Action>>(); 
    	int[] current_time = new int[agent.vehicles().size()];
    	double task_weights =  0; 
    	Vehicle veh = null; 
    	
    	for(Vehicle vehicle : agent.vehicles()) {
    		new_plan.put(vehicle.id(), new ArrayList<Action>()); 
    	}
    	
    	// Check for each Tasks what the closest vehicle is 
    	double min_distance = Double.MAX_VALUE; 
    	int smallest = 0; 
    		
    	// Check for each Vehicle
    	for(Vehicle vehicle : agent.vehicles()) {
    			
    				
    		// Compute how close the different vehicles are to the task
    		
	
    		if(new_task.pickupCity.distanceTo(vehicle.getCurrentCity()) < min_distance && vehicle.capacity() > new_task.weight) {
					
        		min_distance = new_task.pickupCity.distanceTo(vehicle.getCurrentCity()); 
        		smallest = vehicle.id(); 
        		veh = vehicle;
        					
        	}
    		
    		
    				
    	}
    	
    	new_plan.get(smallest).add(new PickUpAction(new_task,current_time[smallest], veh));
    	current_time[smallest]++;    		
    	new_plan.get(smallest).add(new DeliveryAction(new_task,current_time[smallest], veh)); 
    	current_time[smallest]++; 
    		
    	return new_plan; 
    }
   
     
    private List<Plan> parsePlan(HashMap<Integer,ArrayList<Action>> intermediate_plans, TaskSet tasks){
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
    				for(Task task : tasks) {
    					if(task.id == action.task.id) {
    						plan.appendPickup(task);
            				current = task.pickupCity; 
            				break; 
    					}
    					
    				}
    				
    			}	
    				
    			if(action instanceof DeliveryAction) {
    				for(City city :  current.pathTo(action.task.deliveryCity)) {

    					plan.appendMove(city);; 
    					
    				}
    				
    				for(Task task : tasks) {
    					if(task.id == action.task.id) {
    						plan.appendDelivery(task);
    	    				current = task.deliveryCity;
            				break; 
    					}
    					
    				}
    				 
    			}

    		}
    		
			result.add(plan); 
    	}
    	
    	System.out.println("Time needed to Parse the Plan: " + (System.currentTimeMillis()- time_start) + " milliseconds");
    	
    	// Catches a case were we would return a non-epmtpy plan if we don't win any Task
    	if(won_tasks.isEmpty()) {
    		result =  new ArrayList<Plan>();
    		
    		for(Vehicle veh : agent.vehicles()) {
    			result.add(new Plan(veh.getCurrentCity())); 
    		}
    		
    	}
    	
    	
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
    		
    		
    		City prev; 
    		
    		// If its the opponent start at the randomly selected location
    		
    		prev = vehicle.getCurrentCity(); 
    		
    		
    		// Calculate the cost of all moves made in the plan
    		
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
    
    
    private double marginal_offset(HashMap<Integer,ArrayList<Action>> plan, Topology topology, TaskDistribution td){
		
    	
    	double total_prob = 0; 
    	int num_non_nan = 0; 
    	
    	for(Vehicle veh : agent.vehicles()) {
    	
    		double prob = 0; 
    		City prev = veh.getCurrentCity();
    		    			
	    	ArrayList<City> following_cities = new ArrayList<City>(); 
	    	following_cities.add(prev); 
	    		
	    	for( Action action : plan.get(veh.id())){
	    			
    			if (action instanceof PickUpAction) {
    				
    				for(City step : prev.pathTo(action.task.pickupCity)) {
    					
    					following_cities.add(step);  
    					
    				}
    				prev = action.task.pickupCity; 
    			}
    			
    			if (action instanceof DeliveryAction) {
    				
    				for(City step : prev.pathTo(action.task.deliveryCity)) {
    					following_cities.add(step); 
    				}
    				
    				prev = action.task.deliveryCity; 
    				
    			}
    			
    		}
    		
    		HashSet<City> i_cities = new HashSet<City>();   
    		
    		for (int i =  0; i < following_cities.size() - 1; i++) {
    			
    			
    			HashSet<City> j_cities = new HashSet<City>(); 
    			
    			if(i_cities.contains(following_cities.get(i))){
    				continue; 
    				
    			}else {
    				i_cities.add(following_cities.get(i));
    				for( int j = i + 1 ; j < following_cities.size(); j ++) {
	    				
	    				// Check if the City was already considered in a previous i step
	    				if(!j_cities.contains(following_cities.get(j))){
	    					prob += td.probability(following_cities.get(i),  following_cities.get(j)); 
		    				j_cities.add(following_cities.get(j)); 
	    				}
	    			}
    			
    					
    			}
		
    		}
    		
    		System.out.println("Vehicle Zero Margin Prob: " + prob/i_cities.size());   
    		
    		// Just ignore if Value is Zero, for a Vehicle
    		if(i_cities.size() > 0) {
    			total_prob += prob/i_cities.size(); 
    			num_non_nan++; 
    		}
    		
    	
    	}
    	
    	return total_prob/num_non_nan;
    	
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
