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
        
//		System.out.println("Agent " + agent.id() + " has tasks " + tasks);
        Plan planVehicle1 = naivePlan(vehicles.get(0), tasks);

        List<Plan> plans = new ArrayList<Plan>();
        plans.add(planVehicle1);
        while (plans.size() < vehicles.size()) {
            plans.add(Plan.EMPTY);
        }
        
        long time_end = System.currentTimeMillis();
        long duration = time_end - time_start;
        System.out.println("The plan was generated in " + duration + " milliseconds.");
        
        return plans;
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
    private HashMap<Vehicle,List<Action>> SLS_algorithm() {
    	
    	
    	
    	return null; 
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
    private HashMap<Vehicle,List<Action>> SelectInitialSolution(TaskSet tasks, Topology topology, Agent agent){
    	
    	// Generate a basic array in the length of the number of vehicles
    	HashMap<Vehicle, List<Action>> plan = new HashMap<Vehicle,List<Action>>(); 
    
    	
    	
    	for(Vehicle vehicle : agent.vehicles()) {
    		
    		
    		
    		
    		
    		
    		
    	}
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	return plan; 
    }
    
    // Parse the Plan to the 
    private List<Plan> parsePlan(HashMap<Vehicle,List<Action>> result){
    	
    	
    	
    	
    	
    	return null; 
    }
    

    
    // ChooseNeighbours(Aold,X,D,C,f)
    private ChooseNeighbours(HashMap<Vehicle,List<Action>> plan, ) {
    	
    	
    	
    	return
    	
    }
    
    // Calculate the Cost Function
    private double CalculateCost(HashMap<Vehicle,List<Action>> plan) {
    	double cost = 0.0; 
    	
    	
    	
    	return cost; 
    }
    
    
    
    
    
    
    // Look at different Local Variation of the Current Plan
    // Switch Task Schedule or Vehicle Assignment
    
    // ChangingVehicle(A, v1, v2) 
    private HashMap<Vehicle,List<Action>> ChangingVehicle(HashMap<Vehicle,List<Action>> plan) {
    	
    	
    	
    	return null; 
    }
    
    // ChangingTaskOrder(A, vi, tIdx1, tIdx2) replace with a function changing the order of a PickUp or Delivery Action 
    private HashMap<Vehicle,List<Action>> ChangingTaskOrder(HashMap<Vehicle,List<Action>> plan) {
    	
    	
    	return null; 
    }
    
    // UpdateTime(A, vi)
    private void UpdateTime() {
    	
    	
    }
    
    // Select the optimal plan in accordance with the lowest cost, add a probability p to the choice to escape local optima
    private HashMap<Vehicle,List<Action>> LocalChoice(HashSet<List<Action>[]> set) {
    	
    	
    	
    }
    
    private void UpdateCost() {
    	
    	
    }
    
    
    // Debug Function to print out a plan in the console
    private void print_plan() {
    	
    	
    }
    
}
