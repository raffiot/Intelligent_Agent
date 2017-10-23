package template;

import logist.topology.Topology.City;

public class StartNode extends Node{

	
	City from;
	
	public StartNode(City from) {
		super();
		this.from = from;
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getWeight() {
		return 0;
	}

	@Override
	public int getType() {
		return 0;
	}

	@Override
	public Node clone() {
		return new StartNode(from);
	}

	@Override
	public City getCityOfNode() {
		return from;
	}

}
