package template;

import logist.task.Task;
import logist.topology.Topology.City;

public class PickupNode extends Node{

	public PickupNode(Node p, Task t) {
		super(p, t);
	}

	public PickupNode(Task t) {
		super(t);
	}
	
	@Override
	public int getType() {
		return 2;
	}

	@Override
	public Node clone() {
		return new PickupNode(this.getParent(),this.getTask());
	}

	@Override
	public City getCityOfNode() {
		return this.getFrom();
	}

}
