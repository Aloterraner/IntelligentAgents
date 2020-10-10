package template;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.TaskSet;
import logist.topology.Topology.City;



public class AstarPlan extends Plan{
	
	private Vehicle vehicle;
	private TaskSet tasks;
	
	public AstarPlan(Vehicle vehicle, City city, TaskSet tasks) {
		super(city);
		this.vehicle = vehicle;
		this.tasks = tasks;
	}

}
