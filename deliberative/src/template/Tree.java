package template;

import java.util.*;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

public class Tree {
	//The root (head) of the tree
	Node root;
	//A map that contain all leaf and there corresponding cost
	Map<Node,Double> nodeToCost;
	
	//The cost per km of the given vehicle
	int costPerKm;
	
	/**
	 * Method to construct a tree from a vehicle (that might contains task already)
	 * and tasks that are task still on the map
	 * 
	 * @param vehicle
	 * 		the vehicle that will carry tasks that have a capacity, a cost per km and might have already task in it.
	 * @param tasks
	 * 		the tasks that are distributed around the map and the vehicle have to carry
	 */
	public Tree(Vehicle vehicle, TaskSet tasks){
		
		nodeToCost = new HashMap<Node,Double>();
		costPerKm = vehicle.costPerKm();
		
		/**
		 * The root node is a StartNode because it only contain a city from
		 * a weight that is by default set to 0 and a task equal to null
		 */
		root = new StartNode(vehicle.getCurrentCity()); 
		
		
		Node parent = root;
		Set<Node> setNode = new HashSet<Node>(); //The set we will iterate on to construct the tree
		
		int capacityUsed = 0; // the capacity already used in the truck (usefull when we have to recompute a plan)
		for(Task task : vehicle.getCurrentTasks()){
			//for all tasks that are currently in the truck we have to increment the capacity use 
			//and make DeliveryNode with those because we have to deliver it.
			setNode.add(new DeliveryNode(task));
			capacityUsed += task.weight;
		}
		
		for(Task task : tasks){
			//for all remaining task we create a PickupNode node because we have to go pick them up
			setNode.add(new PickupNode(task));
		}
		
		for(Node n : setNode){
			System.out.println("node "+n.getTask().toString());
		}
		System.out.println(setNode.size());
		//recursive method that construct the tree
		constructTree(setNode, parent, vehicle.capacity()-capacityUsed);
	}

	/**
	 * Recursive method that construct the tree
	 * 
	 * @param set
	 * 		The set of remaining nodes (deliveryNode or pickupNode) the vehicle as to go through to acheive it's goal
	 * @param parent
	 * 		The parent nodes which is at upper level in the tree
	 * @param capacityLeft
	 * 		The capacity left in the vehicle so we cannot pickup task that would overweight it
	 */
	private void constructTree(Set<Node> set, Node parent, int capacityLeft) {
		//System.out.println(set.size());
		

		for(Node node : set){
			
//			if(parent.getType() != 0)
//				System.out.println("parent "+parent.getTask().toString());
//			System.out.println("node "+node.getTask().toString());
//			
			//if the weight left in vehicle is not sufficient to carry the task
			if(capacityLeft - node.getWeight() > 0){ 
				
				//We clone the node because one same node can be at different place in the tree so we want them different
				Node n = node.clone(); 
				parent.addChild(n);
				n.setParent(parent);
				
				Set<Node> newSet = new HashSet<Node>(set);
				//if the node is of type PickupNode we have to append it's DeliveryNode corresponding
				if(node.getType() == 2){
					Node delivery = new DeliveryNode(n.getTask());
					newSet.add(delivery);
				}
				//We remove the node we have included in the tree and recall the recursive function with an actualized weight
//				System.out.println("antes "+newSet.size());
				newSet.remove(node);
//				System.out.println("despues "+newSet.size());
				
				constructTree(newSet,n,capacityLeft-n.getWeight());
			}
		}	
	}
	
	/**
	 * Method that run breath for search algorithm to search for the min cost path
	 * 
	 * @return
	 * 		the plan corresponding to this min cost path
	 */
	public Plan bFS(){
		
		//We go throught all node via a recursive function and set their cost in fuction of their parent
		for(Node n : root.getChildren()){
			setCostRecursive(n);
		}
		
		//We find the leaf node that have minimum cost
		double minCost = Double.MAX_VALUE;
		Node minNode = null;
		for(Node key : nodeToCost.keySet()){
			double value = nodeToCost.get(key);
			if(value < minCost){
				minCost = value;
				minNode = key;
			}
		}
		
		//We build a linked list to synthesize the plan in term of path of nodes
		LinkedList<Node> ll = new LinkedList<Node>();
		Node currNode = minNode;
		do{
			ll.addFirst(currNode);
			currNode = currNode.getParent();
		}while(currNode.getParent() != null); //exclude the root !
		
		//we transform our linked list of node into a plan that is a list of actions
		Plan p = new Plan(root.getCityOfNode());
		Node oldNode = root;
		for(Node n : ll){
			
			for (City city : oldNode.getCityOfNode().pathTo(n.getCityOfNode()))
				p.appendMove(city);
			if(n.getType() == 1){
				p.appendDelivery(n.getTask());
			}
			else{
				p.appendPickup(n.getTask());
			}
			oldNode = n;
		}
		return p;
	}

	/**
	 * Recursive function to set the cost of all nodes
	 * 
	 * @param n
	 * 		the current node to which we want to assign a cost
	 */
	private void setCostRecursive(Node n) {
		//cost = cost of the parent + distance from parent to child
		n.setCost(n.getParent().getCost() + n.getParent().getCityOfNode().distanceTo(n.getCityOfNode())*costPerKm);
		//if the node we currently have is a leaf add it to the hashmap with it's cost
		if(n.getChildren().isEmpty()){
			nodeToCost.put(n, n.getCost());
		}
		else{
			for(Node child : n.getChildren()){
				setCostRecursive(child);
			}
		}
		
	}
	
}
