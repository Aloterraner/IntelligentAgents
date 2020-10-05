package template;
import logist.topology.Topology.City;

public class State {
	private City to;
	private City from;
	private int id; 
	
	public State(City from, City to, int id) {
		this.to = to;
		this.from = from;
		this.id = id; 
	}
	
	public City getFrom() {
		return from;
	}
	public void setFrom(City from) {
		this.from = from;
	}
	public City getTo() {
		return to;
	}
	public void setTo(City to) {
		this.to = to;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
