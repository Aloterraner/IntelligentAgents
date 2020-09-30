package template;

import java.util.Random;

import logist.simulation.Vehicle;
import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;
import java.util.Arrays;



public class ReactiveTemplate implements ReactiveBehavior {

	private Random random;
	private final double error_margin = 0.0000000000001; 
	private double pPickup;
	private Double discount;
	private int numActions;
	private int numStates; 
	private int numCities; 
	private Agent myAgent;
	private State[][] states;
	private double[] V;
	private Topology topology; 
	private TaskDistribution TD; 
	private int itcount; 
	private int[] strategy;

	
	// T(s,a,s'), returns the Probability to go from state s to state s' when taking action a. 
	private double[][][] TransitionTable; 
	// R(s,a) returns the Expected reward when taking Action a in state s
	private int[][] RewardTable; 

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		
		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class,
				0.95);
		
		
		this.itcount = 0; 
		this.random = new Random();
		this.discount = discount;
		this.myAgent = agent;
		this.numCities = topology.cities().size();

		this.topology = topology;
		
		for(City from: topology.cities()) {
			System.out.println("City: " + from.name +" has ID:  "+ from.id ) ; 
		}
		
		initStates();
		initActions();
		buildTransitionTable(topology, td, agent);
		buildRewardTable(topology, td, agent);
		
		// Calling Value Iteration with the predefined maximum error margin
		ValueIteration(error_margin);
		
		System.out.println("The Strategy is: ");
		System.out.println(Arrays.toString(strategy)); 
		System.out.println("\n Checking in Cities \n "); 
		
		for(City city : topology.cities()) {
			System.out.println("Name: " +city.name+ " ID: " + city.id);
		}
		

	}


	private void initActions() {
		System.out.println("Initalizing Actions ..."); 
		this.numActions = this.numCities + 1;
		System.out.println(numActions + " Actions created!");
	}

	private void initStates() {
		System.out.println("Initalizing State Space ..."); 
		this.numStates = this.numCities * this.numCities;
		this.states = new State[this.numCities][this.numCities];
		int id = 0; 
		for(City from : topology.cities()) {
			for(City to : topology.cities()) {
				this.states[from.id][to.id]= new State(from, to,id);
				id++;
				
			}
			
			
		}
		System.out.println(numStates + " States created!");
	}	
	
	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;
		State current_state; 
		// Determine the current state of the agent by looking at his current location and the possible task location
		// Going into the state (i,i) if no task is aviable; 
		if(availableTask == null) {
			current_state = states[vehicle.getCurrentCity().id][vehicle.getCurrentCity().id];
		// Going into the state (i,j) corresponding to where the task requieres delivery	
		}else {
			current_state = states[vehicle.getCurrentCity().id][availableTask.deliveryCity.id];
		}
		
		int action_index = strategy[current_state.getId()]; 
		
		if(action_index == numCities){
			System.out.println("I pickedUp");
			action = new Pickup(availableTask); 
		}else{
			System.out.println("I moved"); 
			action = new Move(topology.cities().get(action_index)); 
		}
		
		
		
		if (itcount >= 1) {
			System.out.println("The total profit after "+numActions+" actions is "+myAgent.getTotalProfit()+" (average profit: "+(myAgent.getTotalProfit() / (double)numActions)+")");
		}
		itcount++;
		
		return action;
	}
	

	
	
	public void buildTransitionTable(Topology topology, TaskDistribution td, Agent agent) {
		System.out.println("Building Transition Table ..."); 
		this.TransitionTable = new double[numStates][numActions][numStates];
		
		// Arrays.fill only works for 1D arrays
		for (int i=0; i < numStates; i++) {
			for (int j=0; j < numActions; j++) {
				Arrays.fill(TransitionTable[i][j], 0);
			}
		}
		
		
		// Init for the Move actions 
		// No Task (i,i) MoveX(0,...,n) (X,[0,...,n]) = td.prob(X, [0,...,n]) if (X neighbor i)  else 0.0  
		// A Task  (i,Y) MoveX(0,...,n) (X,[0,...,n]) = td.prob(X, [0,...,n]) if (X neighbor i ) else 0.0  
		for(City from: topology.cities()) {
			for(City task: topology.cities()) {
				for(City neighbor: from.neighbors()) {
					for(City neighborTask : topology.cities()) {
						if(neighbor.id != neighborTask.id) {
							this.TransitionTable[this.states[from.id][task.id].getId()][neighbor.id][this.states[neighbor.id][neighborTask.id].getId()] = td.probability(neighbor, neighborTask); 
						}else {
							this.TransitionTable[this.states[from.id][task.id].getId()][neighbor.id][this.states[neighbor.id][neighbor.id].getId()] = td.probability(neighbor, null); 
						}
						
					}
					
					
					
					
				}
				
				
				
				
			}
			
			
		}
		
		
		
		
		// Init for the Deliver Actions
		for(City from : topology.cities()) {
			for(City to : topology.cities()) {
				for (City next :  topology.cities()) {
					// Check if its the same city, if so, the probability needs to be get with the null call
					if(from.id != to.id) {
						if(next.id == to.id) {
							this.TransitionTable[this.states[from.id][to.id].getId()][this.numCities][this.states[to.id][next.id].getId()]= td.probability(to, null);
						}else {
							this.TransitionTable[this.states[from.id][to.id].getId()][this.numCities][this.states[to.id][next.id].getId()]= td.probability(to, next);
						}
					}
					
					
				}
				
			}
		}
		

		
		System.out.println("Finished Building Transition Table!"); 
	}
		
	public void buildRewardTable(Topology topology, TaskDistribution td, Agent agent) {
		System.out.println("Building Reward Table ..."); 
		RewardTable = new int[numStates][numActions]; 
		for(int i = 0; i< numStates; i++) {
			for(int j = 0; j < numActions; j++) {
				
					RewardTable[i][j] = 0; 
			}
		} 
		
		// Case 1: MoveAction 
		for(City from : topology.cities()) {
			for(City neighbor : from.neighbors()) {
				
				RewardTable[this.states[from.id][from.id].getId()][neighbor.id] = (int) (- (from.distanceTo(neighbor) * myAgent.vehicles().get(0).costPerKm())); 
				//System.out.println("The weight of the path between " +from.id +" and " +neighbor.id+ " is "+ (int) (- (from.distanceTo(neighbor) * myAgent.vehicles().get(0).costPerKm()))); 
			}
		}
		
		
		
		// Case 2: PickUpAndDeliver 

		for(City from : topology.cities()) {
			for(City to : topology.cities()) {
				if(from.id != to.id){
					RewardTable[this.states[from.id][to.id].getId()][numCities] = (td.reward(from, to)  + (int) (- (from.distanceTo(to) * myAgent.vehicles().get(0).costPerKm()))); 
					//System.out.println("Reward for Traveling between "+ from.id + " and " +to.id+ " is " + td.reward(from, to)); 
				}
			}
		}
		System.out.println("Finished Building Reward Table!"); 
	}
	
	public void ValueIteration(double error) {
		System.out.println("Starting Value Iteration ..."); 
		this.V = new double[numStates]; 
		this.strategy = new int[numStates]; 
		boolean bool = true;
		double diff = 0.0;
		double Q; 
		double max;
		
		
		// Init of Strategy and V
		for(int s = 0; s < numStates; s++) {
			V[s] = 1.0; 
			this.strategy[s] = 0;
			
		}
		
		int count = 0; 
		
		while(bool) {
			count++;
			bool = false;
			
			for(int s = 0; s < numStates; s++) {
				
				max = Double.MIN_VALUE;
				int index = 0;
				
				for(int a = 0; a < numActions ; a++) {
					Q = RewardTable[s][a];
					//System.out.println("Initial Reward is: " + Q + " for action " + a + " in State " + s);
					
					for(int s_dash = 0; s_dash < numStates; s_dash++) {
						Q += discount*this.TransitionTable[s][a][s_dash]*V[s_dash];
					}
					
					//System.out.println("Q is: " + Q + " for action " + a + " in State " + s);
					if(Q>max) {
						index = a;
						max = Q;
					}
				}
				diff = Math.abs(V[s]-max); 
				if(diff>error) {
					bool = true;
				}
				
				V[s] = max;
				strategy[s] = index;
			}
			System.out.println("Iteration: " + count);
			
			
		}

		
		
		
		System.out.println("Setting Optimal Strategy ...");  
		System.out.println("Finished Value Iteration");
		System.out.println("Ready to Roll"); 
	}
	

	
	
	}
			
			
	
		
	
	
	
	

