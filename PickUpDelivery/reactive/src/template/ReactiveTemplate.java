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

public class ReactiveTemplate implements ReactiveBehavior {

	private Random random;
	private double pPickup;
	private Double discount;
	private int numActions;
	private int numStates; 
	private int numCities; 
	private Agent myAgent;
	private int[] VB;
	private int[] policy; 
	private Topology topology; 
	private TaskDistribution TD; 
	
	
	// T(s,a,s'), returns the Probability to go from state s to state s' when taking action a. 
	private double[][][] TransitionTable; 
	// R(s,a) returns the Expected reward when taking Action a in state s
	private double[][] RewardTable; 

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		
		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class,
				0.95);
		
		
		
		
		
		this.random = new Random();
		this.pPickup = discount;
		this.numActions = 0;
		this.myAgent = agent;
		this.numCities = topology.cities().size();
		
		buildTransitionTable(topology, td, agent);
		buildRewardTable(topology, td, agent);
		ValueIteration(discount);
		
		
		
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;

		if (availableTask == null || random.nextDouble() > pPickup) {
			City currentCity = vehicle.getCurrentCity();
			action = new Move(currentCity.randomNeighbor(random));
		} else {
			action = new Pickup(availableTask);
		}
		
		if (numActions >= 1) {
			System.out.println("The total profit after "+numActions+" actions is "+myAgent.getTotalProfit()+" (average profit: "+(myAgent.getTotalProfit() / (double)numActions)+")");
		}
		numActions++;
		
		return action;
	}
	

	
	
	public void buildTransitionTable(Topology topology, TaskDistribution td, Agent agent) {
		TransitionTable = new double[numStates][numActions][numStates]; 
		
		
		
		
	}
		
	public void buildRewardTable(Topology topology, TaskDistribution td, Agent agent) {
		RewardTable = new double[numStates][numActions]; 
		
		
		
		
	}
	
	public void ValueIteration(double error) {
		
		int[] strategy = new int[numStates]; 
		double max_diff = 0; 
		
		
		// Init with random values, i.e 1 or something
		
		
		do {
			int[] old_strategy = strategy; 
			// Iterate over the State Space as s
			for(int s = 0; s < numStates ; s++) {
				
				double[] Q = new double[numActions];  
				// Iterate over the Actions
				for(int a = 0; a < numActions ; a++){
					
					double res = 0;  
					// Iterate over the State space as s'
					for(int s_dash = 0; s_dash < numStates; s_dash++) {
						res += TransitionTable[s][a][s_dash] * strategy[s_dash]; 
						
						
					}
				
					Q[a] =  RewardTable[s][a] + pPickup*res; 
					
				}
				
				// Choose argmax a of Q(a) 
				
				// Assign it to Strategy
				
				int index = 0; 
				double max = 0; 
				
				for(int i = 0; i < numActions; i++){
					if(Q[i] > max) {
						max = Q[i]; 
						index = i; 
					}
					
				}
				
				// Assign V(s) with the argmax a of Q(s,a)
				strategy[s] = index; 
				
				
				
				
			}
			// Calculate difference between previous and new strategy 
			
			
			max_diff = 0; 
			for(int i = 0 ; i < numStates ; i++) {
				
				for(int j = 0; j < numStates ; j++) {
					if(max_diff < Math.abs(strategy[i] - old_strategy[j])) {
						max_diff = Math.abs(strategy[i] - old_strategy[j]); 
					
					}
				}
			}
			
			
			
			
		}while(error < max_diff); 
			
		VB = strategy; 
	}
	
	// Gets the index of a State and returns the best possible action
	int policy_lookup(int state) {
			double[] res = new double[numActions]; 
			
			// Compute the results for each Action 
			for(int a = 0;a < numActions ; a++) {
				
				double sum = 0; 
				// Compute the sum over s' 
				for(int s_dash = 0 ; s_dash < numStates ; s_dash++){
					sum += TransitionTable[state][a][s_dash]*VB[state]; 
					
				}
				// Add reward and multiple with discount factor tau
				res[a] = RewardTable[state][a] + pPickup * sum ; 
				
			}
		
			// Find the maximum of the res Array, return its index (The Associated Action) 
			int index = 0; 
			double max = 0; 
			for(int i = 0; i < numActions; i++) {
				if(res[i] > max) {
					max = res[i]; 
					index = i; 
				}
				
			}
		
		return index; 
	}
			
			
			
			
	
		
	
	
	
	
}
