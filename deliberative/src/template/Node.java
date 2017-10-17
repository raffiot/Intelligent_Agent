package template;

import java.util.ArrayList;

import logist.topology.Topology.City;

public class Node {
	private int capacity; // Actual capacity of the truck
	private int cost; // Cost up to that task
	private City from, to; // If fromm == null -> Type == Delivery
	private ArrayList<Node> children;

	public Node(int capacity, int cost, City from, City to) {
		this.capacity = capacity;
		this.cost = cost;
		this.from = from;
		this.to = to;
		children = new ArrayList<Node>();
	}

	public Node(int capacity, City from, City to) {
		this.capacity = capacity;
		this.from = from;
		this.to = to;
		children = new ArrayList<Node>();
	}

	public void addChild(Node child) {
		children.add(child);
	}

	public boolean isTypeTask() {
		return from != null && to != null;
	}

	public ArrayList<Node> getChildren() {
		return children;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
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

}
