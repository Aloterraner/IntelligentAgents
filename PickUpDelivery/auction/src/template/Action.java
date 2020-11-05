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

	
	
	public Action(Task task, int time, Vehicle vehicle){
		
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



	public Vehicle getVehicle() {
		return vehicle;
	}


	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}
	

}
