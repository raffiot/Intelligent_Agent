package template;

import logist.task.Task;
import logist.topology.Topology.City;

public class DeliveryNode extends Node{

	public DeliveryNode(Task t) {
		super(t);
	}

	public DeliveryNode(Node p, Task t) {
		super(p,t);
	}
	
	@Override
	public int getWeight() {
		return -super.getWeight();
	}

	@Override
	public int getType() {
		return 1;
	}

	@Override
	public Node clone() {
		return new DeliveryNode(this.getParent(), this.getTask());
	}

	@Override
	public City getCityOfNode() {
		return this.getTo();
	}

}
