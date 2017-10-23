package template;

import java.util.*;

import logist.task.Task;
import logist.topology.Topology.City;

public abstract class Node {

	private Node parent;
	private List<Node> children;
	private Task task;
	private double cost;
	
	public Node(Task t){
		parent = null;
		this.task = t;
		children = new ArrayList<Node>();
		cost = 0;
	}
	
	//Constructor for initial nodes
	public Node(){
		parent = null;
		children = new ArrayList<Node>();
		task = null;
		cost = 0;
	}
	
	public Node(Node p, Task t){
		parent = p;
		this.task = t;
		children = new ArrayList<Node>();
		cost = 0;
	}
	
	public void addChild(Node child){
		children.add(child);
	}
	
	public City getFrom() {
		return task.pickupCity;
	}

	public City getTo() {
		return task.deliveryCity;
	}

	public int getWeight(){
		return task.weight;
	}
	
	public abstract int getType();

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}
	
	public List<Node> getChildren() {
		return children;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}
	
	public Task getTask(){
		return task;
	}
	
	public abstract City getCityOfNode();
	
	@Override
	public abstract Node clone();
	
	
}
