package template;

import java.awt.Window.Type;
import java.util.*;

import logist.task.Task;
import logist.topology.Topology.City;

public class Node {

	private Node parent;
	boolean type; //True -> delivery
	private Task task;
	private double cost;
	
	public Node(Task task, Boolean type, Node parent, Double cost){
		this.task = task;
		this.type = type;
		this.parent = parent;
		this.cost = cost;
	}
	
	//Constructor for initial nodes
	public Node(){
		this.task = null;
		this.type = false; 
		this.parent = null;
		this.cost = 0;
	}
		
	public City getFrom() {
		return this.task.pickupCity;
	}

	public City getTo() {
		return this.task.deliveryCity;
	}

	public int getWeight(){
		return this.task.weight;
	}
	
	public boolean getType() {
		return this.type;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
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
	
	public City getCityOfNode() {
		if (this.type)
			return this.task.deliveryCity;
		else if (this.task == null)
			return null;
		return this.task.pickupCity;
	}
	
	public HashMap<Task, Boolean> getTasksDone(){  // I only have to return the tasks delivered
		HashMap<Task, Boolean> tasksDone = new HashMap<Task, Boolean>(); 
		Node father = this.parent;
		while(father != null) {
			if(this.parent.getType()) 
				tasksDone.put(this.parent.task, true); //Add a delivery but task at this moment may not been delivered
			else if (! tasksDone.containsKey(task))
				tasksDone.put(this.parent.task, false); //Add a pick but task at this moment hasnt been delivered
			father = father.getParent();
		}
		return tasksDone;		
	}
	
	public int getNumerOfTasksDone(){  // I only have to return the tasks delivered
		int r = 0;
		Node father = this.parent;

		while(father != null) {
			r++;
			father = father.getParent();
		}
		return r;		
	}
	
//	public Node compare(Node n) {
//		
//		//Maybe theres no equals for task
//		if (this.task.equals(n.task)) {
//			if(this.cost < n.getCost())
//				return this;
//			else
//				return n;
//		}
//		return null; //Task are not the same
//	}
}
