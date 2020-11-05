package template;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import logist.LogistSettings;
import java.io.File; 


import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;

import logist.topology.Topology;
import logist.topology.Topology.City;


public abstract class Action {
	
	protected Task task; 
	protected int time; 
	protected Vehicle vehicle; 
	protected double costs; 
	
	
	
	
	public Action(Task task, int time, Vehicle vehicle, double costs){
		this.setCosts(costs); 
		this.setTask(task); 
		this.setVehicle(vehicle);
		this.setTime(time); 
		
		
	}


	public Task getTask() {
		return task;
	}


	public void setTask(Task task) {
		this.task = task;
	}


	public int getTime() {
		return time;
	}


	public void setTime(int time) {
		this.time = time;
	}


	public double getCosts() {
		return costs;
	}


	public void setCosts(double costs) {
		this.costs = costs;
	}


	public Vehicle getVehicle() {
		return vehicle;
	}


	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}
	

}
