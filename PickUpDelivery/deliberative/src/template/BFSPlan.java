package template;
import logist.simulation.Vehicle;
import logist.task.TaskSet;
import logist.topology.Topology.City;
import logist.plan.Plan;

public class BFSPlan extends Plan{
	
	private Vehicle vehicle;
	private TaskSet tasks;
	
	public BFSPlan(Vehicle vehicle, City city, TaskSet tasks) {
		super(city);
		this.vehicle = vehicle;
		this.tasks = tasks;
	}


}
