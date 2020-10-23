package template;

//the list of imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import logist.LogistSettings;
import java.io.File; 

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
		DeliveryAction delievery = new DeliveryAction(task,1, vehicles.get(0), 0.0, pickup);
		System.out.println("Pickup Action: " + pickup.toString());
		System.out.println("Delivery Action: " + delievery.toString());
		System.out.println("Change the Time");
		pickup.time = 5; 
		System.out.println("Pickup Action: " + pickup.toString());
		System.out.println("Delivery Action: " + delievery.toString());
        
        
        
        result = SLS_algorithm(tasks, topology, agent); 
        for (Plan plan : result) {
        	System.out.println("\n Resulting plans: " + plan.toString()+ "\n"); 
        }
        
        for(Vehicle veh: vehicles) {
        	System.out.println("Start City of Vehicle " + veh.id() + " is " + veh.getCurrentCity()); 
        }
        
        
        
        
        long time_end = System.currentTimeMillis();
        long duration = time_end - time_start;
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
    private List<Plan> SLS_algorithm(TaskSet tasks, Topology topology, Agent agent) {
    	
    	System.out.println("Computing Initial Solution ... ");
    	HashMap<Integer,ArrayList<Action>> plan = SelectInitialSolution( tasks, topology, agent); 
    	System.out.println("Finished Computing Initital Solution! "); 
    	System.out.print("\n" + plan.toString() + "\n");
    	
    	System.out.println("Dev: Check if the Initial Plan is a fullfilling the constraints: " + verify_constraint(plan)); 
    	
    	
    	
    	System.out.println("Parsing Plan! "); 
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
    	Vehicle veh = null; 
    	
    	for(Vehicle vehicle : agent.vehicles()) {
    		plan.put(vehicle.id(), new ArrayList<Action>()); 
    		
    	}
    	
    	// Check for each Tasks what the closest vehicle is 
    	for (Task task : tasks) {
    		double min_distance = Double.MAX_VALUE; 
    		int smallest = 0; 
    		
    		// Check for each Vehicle
    		for(Vehicle vehicle : agent.vehicles()) {
    			
    			// Check if it has enough room
    			if(current_load[vehicle.id()] + task.weight < vehicle.capacity()) {
    				
    				// How close it is
    				if( task.pickupCity.distanceTo(vehicle.getCurrentCity()) < min_distance) {
    					
    					min_distance = task.pickupCity.distanceTo(vehicle.getCurrentCity()); 
    					smallest = vehicle.id(); 
    					veh = vehicle;
    					
    				}
    				
    			}
    			
    		}
    		
    		PickUpAction pickup = new PickUpAction(task,current_time[smallest], veh, 0.0); 
    		plan.get(smallest).add(pickup);
    		current_time[smallest]++;    		
    		plan.get(smallest).add(new DeliveryAction(task,current_time[smallest], veh, 0.0, pickup)); 
    		current_time[smallest]++; 
    		
    		
    		current_load[smallest] += task.weight;
    		
    		
    		
    	}
    				
    	return plan; 
    }
    
    // Parse the Plan to the 
    private List<Plan> parsePlan(HashMap<Integer,ArrayList<Action>> intermediate_plans){
    	
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
    	
    	
    	
    	return result; 
    }
    

    
    // ChooseNeighbours(Aold,X,D,C,f)
    private HashSet<HashMap<Vehicle,ArrayList<Action>>> ChooseNeighbours(HashMap<Vehicle,ArrayList<Action>> plan) {
    	
    	
    	
    	return null; 
    	
    }
    
    // Calculate the Cost Function
    private double CalculateCost(HashMap<Vehicle,ArrayList<Action>> plan) {
    	double cost = 0.0; 
    	
    	
    	
    	return cost; 
    }
    
    
    
    
    
    
    // Look at different Local Variation of the Current Plan
    // Switch Task Schedule or Vehicle Assignment
    
    // ChangingVehicle(A, v1, v2) 
    private HashMap<Vehicle,ArrayList<Action>> ChangingVehicle(HashMap<Vehicle,ArrayList<Action>> plan) {
    	
    	
    	
    	return null; 
    }
    
    // ChangingTaskOrder(A, vi, tIdx1, tIdx2) replace with a function changing the order of a PickUp or Delivery Action 
    private HashMap<Vehicle,ArrayList<Action>> ChangingTaskOrder(HashMap<Vehicle,ArrayList<Action>> plan) {
    	
    	
    	return null; 
    }
    
    // UpdateTime(A, vi)
    private void UpdateTime() {
    	
    	
    }
    
    // Select the optimal plan in accordance with the lowest cost, add a probability p to the choice to escape local optima
    private HashMap<Vehicle,ArrayList<Action>> LocalChoice(HashSet<HashMap<Vehicle,ArrayList<Action>>> set) {
    	
		return null;
    	
    }
    
    private void UpdateCost() {
    	
    	
    }
    
    
    // Debug Function to print out a plan in the console
    private void print_plan() {
    	
    	
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
    				
    				// Check if adding the package would lead to going over capacity
    				if(current_load > vehicle.capacity()) {
    					return false; 
    				}
    			}
    			
    			
    			// Pop the weight of the current load
    			if(action instanceof DeliveryAction){
    				current_load -= action.task.weight; 
    				
    				
    				// Check if the package is already loaded at the time of delivery
    				if(((DeliveryAction) action).pickUpAction.time > action.time)  {
    					
    					return false;
    					
    				}
    				
    				
    			}
    			
    		}
    		
    		
    		
    		
    		

			
    	}
    	
		return true;
    	
    	
    	
    	
    }
    
}
