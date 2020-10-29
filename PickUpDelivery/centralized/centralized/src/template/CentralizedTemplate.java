package template;

//the list of imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import logist.LogistSettings;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.behavior.CentralizedBehavior;
import logist.agent.Agent;
import logist.config.Parsers;
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
public class CentralizedTemplate implements CentralizedBehavior {

    private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private long timeout_setup;
    private long timeout_plan;
    private HashMap<Integer,ArrayList<Action>> plan; 
    private double p; 
    private long num_iterations; 
    
    @Override
    public void setup(Topology topology, TaskDistribution distribution,
            Agent agent) {
        
        // this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config" + File.separator + "settings_default.xml");
        }
        catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }
        

        
        
        // the setup method cannot last more than timeout_setup milliseconds
        timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
        // the plan method cannot execute more than timeout_plan milliseconds
        timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);
        
        this.topology = topology;
        this.distribution = distribution;
        this.agent = agent;
        
        // Probaility to reject the new best plan and kep the current/old 
        this.p = 0.4; 
        this.num_iterations = 100000; 
        System.out.println("The Number of Iterations is: " + num_iterations);
        System.out.println("The probability to keep the current plan is set to : " + p); 
    }

    
    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        long time_start = System.currentTimeMillis();
        
        System.out.println("Number of Vehicles: " +  vehicles.size());
        System.out.println("Number of Vehicles assigned to agent: " + agent.vehicles().size()); 
        
        List<Plan> result;  
        
        //System.out.println("Agent " + agent.id() + " has tasks " + tasks);
        // Check if the Behaviour of the Reference to PickUpAction Object in the DeliveryAction Object works as expected
        Task task = (Task) tasks.toArray()[0]; 
 		PickUpAction pickup = new PickUpAction(task,0, vehicles.get(0), 0.0); 
		DeliveryAction delievery = new DeliveryAction(task,1, vehicles.get(0), 0.0);
		System.out.println("Pickup Action: " + pickup.toString());
		System.out.println("Delivery Action: " + delievery.toString()); 
        
		
		result = SLS_algorithm(tasks, topology, agent, p);
        
        /*for (Plan plan : result) {
        	System.out.println("\n Resulting plans: " + plan.toString()+ "\n"); 
        }
        
        for(Vehicle veh: vehicles) {
        	System.out.println("Start City of Vehicle " + veh.id() + " is " + veh.getCurrentCity()); 
        }
        
        */ 
		
        long time_end = System.currentTimeMillis();
        long duration = time_end - time_start;
        System.out.println("Costs of the Final Plan: " + CalculateCost(plan));
        System.out.println("The plan was generated in " + duration + " milliseconds."); 
        
		
		
		
        
        return result;
    }

    
    private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
        City current = vehicle.getCurrentCity();
        Plan plan = new Plan(current);

        for (Task task : tasks) {
            // move: current city => pickup location
            for (City city : current.pathTo(task.pickupCity)) {
                plan.appendMove(city);
            }

            plan.appendPickup(task);

            // move: pickup location => delivery location
            for (City city : task.path()) {
                plan.appendMove(city);
            }

            plan.appendDelivery(task);

            // set current city
            current = task.deliveryCity;
        }
        return plan;
    }
    

    // Base call to Compute the Plan
    private List<Plan> SLS_algorithm(TaskSet tasks, Topology topology, Agent agent, double p) {
    	
    	long time_start = System.currentTimeMillis();
    	long time_needed = 0;
    	long wrap_up_time = (long)(timeout_plan * 0.005); 
    	
    	System.out.println("Computing Initial Solution ... ");
    	plan = SelectInitialSolution( tasks, topology, agent); 
    	System.out.println("Finished Computing Initital Solution! "); 
    	System.out.print("\n" + plan.toString() + "\n");
    	
    	
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
    		
    		/*for (HashMap<Integer,ArrayList<Action>> p : neighbors) {
    			System.out.println(counter);
    			print_plan(p);
    		}*/
    		
    		plan = LocalChoice(neighbors, plan, p);
    		
    		counter += 1;
    		time_needed = System.currentTimeMillis() - time_start;
    		
    		if (time_needed > timeout_plan - wrap_up_time) {
    			System.out.println("TimeOut Reached, Parsing Plan!");
    			break; 
    		}
    	}
    	
    	System.out.println("Parsing Plan! "); 
    	System.out.println("Costs of the Plan: " + CalculateCost(plan)); 
    	
    	return parsePlan(plan); 
    }
    	

    /* HashMap Mapping Vehicle to Actions for the Vehicle
     * 
     * v_1 -> List of Actions for Vehicle 1  
     *  .
     *  . 
     *  .
     *  .
     *  . 
     * v_k -> List of Actions for Vehicle k
     * 
     */
    
    // Compute an inital plan based on distributing each Task to the nearest vehicle that has some Capacity left. 
    private HashMap<Integer,ArrayList<Action>> SelectInitialSolution(TaskSet tasks, Topology topology, Agent agent){
    	
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
   
    
    // Parse the Plan to the 
    private List<Plan> parsePlan(HashMap<Integer,ArrayList<Action>> intermediate_plans){
    	
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
    

    // ChooseNeighbours(Aold,X,D,C,f)
    private HashSet<HashMap<Integer,ArrayList<Action>>> ChooseNeighbours(HashMap<Integer,ArrayList<Action>> plan) {
    	
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
    	
    	// Applying the changing task order operator
    	int vehicle = rand.nextInt(this.agent.vehicles().size());
    	Action temp;
    	
    	for (int idx1=0; idx1 < plan.get(vehicle).size() - 1; idx1 ++) {
    		for (int idx2=idx1+1; idx2 < plan.get(vehicle).size(); idx2 ++) {

    			new_plan = copyPlan(plan);
    			
    			//System.out.println("Start. After: " + new_plan.get(vehicle).get(idx1) + ", " + new_plan.get(vehicle).get(idx2));
    			
    			temp = new_plan.get(vehicle).get(idx1);
    			new_plan.get(vehicle).set(idx1, new_plan.get(vehicle).get(idx2));
    			new_plan.get(vehicle).set(idx2, temp);
    			
    			//System.out.println("After: " + new_plan.get(vehicle).get(idx1) + ", " + new_plan.get(vehicle).get(idx2));
    			
    			UpdateTime(new_plan, vehicle, idx1);
    			if (verify_constraint(new_plan)) {
    				
    				neighbors.add(new_plan);
    			}
    		}
    	}
   	

    	return neighbors; 
    	
    }

    
    // Calculate the Cost Function, currently naivly
    private double CalculateCost(HashMap<Integer,ArrayList<Action>> plan) {
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

    
    // Look at different Local Variation of the Current Plan
    // Switch Task Schedule or Vehicle Assignment
    
    // ChangingVehicle(A, v1, v2) 
    private HashMap<Integer,ArrayList<Action>> ChangingVehicle(Integer v1, Integer v2, HashMap<Integer,ArrayList<Action>> plan) {
    	/*
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
 
 
    	UpdateTime(plan, v1, t);
  
    	UpdateCost(plan);
    	
    	return plan;
    	
    }
    
    
    // ChangingTaskOrder(A, vi, tIdx1, tIdx2) replace with a function changing the order of a PickUp or Delivery Action 
    // Currently implemented inside the Choose Neighbors Function
    private HashMap<Integer,ArrayList<Action>> ChangingTaskOrder(HashMap<Integer,ArrayList<Action>> plan) {
    
    	// TODO
    	return null; 
    }
    
    
    // UpdateTime(A, vi)
    private void UpdateTime(HashMap<Integer,ArrayList<Action>> plan, int vehicle_id, int idx) {
    	
    	if (idx != 0) {
    		idx -= 1;
    	}
    	
    	for (int i = idx; i < plan.get(vehicle_id).size(); i++) {
    		plan.get(vehicle_id).get(i).time = i;
    	}
    }
    
    
    // Select the optimal plan in accordance with the lowest cost, add a probability p to the choice to escape local optima
    private HashMap<Integer,ArrayList<Action>> LocalChoice(HashSet<HashMap<Integer,ArrayList<Action>>> set, HashMap<Integer, ArrayList<Action>> current_plan, double p) {
    	
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
    	
    	// Adds a Currently hardcoded random chance to either select the best or the old plan
    	if(rand.nextDouble() < p) {
    		return current_plan;
    	}else{
    		return best_plan;
    	}
		
    	
    }
    
    
    private void UpdateCost(HashMap<Integer,ArrayList<Action>> plan) {
    	
    }
    
    
    // Debug Function to print out a plan in the console
    private void print_plan(HashMap<Integer,ArrayList<Action>> plan) {
    	
    	for (int vehicle_id=0; vehicle_id < plan.size(); vehicle_id++) {
    		System.out.print("Plan for vehicle: " + vehicle_id + ": ");
    		System.out.print(plan.get(vehicle_id).toString() + "\n");
    	}
    	
    }
    
    
    
    // Verifies if a Plan fulfills all Pre-Specified Constraints
 
    // Constraint (2) satisfied by Construction, as every NextAction of an Action remains in the same vehicle context 
    // Constraint (3) satisfied by Construction, Uniqueness of Actions guranteed by change_functions and Init.    
    // Constraint (5) satisfied by Construction, every chain of actions starts with a vehicle
 
    private boolean verify_constraint(HashMap<Integer,ArrayList<Action>> plan) {
    	
    	
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
