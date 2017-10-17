package template;

/* import table */
import logist.simulation.Vehicle;

import java.util.HashSet;
import java.util.Set;

import logist.agent.Agent;
import logist.behavior.DeliberativeBehavior;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * An optimal planner for one vehicle.
 */
@SuppressWarnings("unused")
public class DeliberativeTemplate implements DeliberativeBehavior {

	enum Algorithm { BFS, ASTAR }

	/* Environment */
	Topology topology;
	TaskDistribution td;

	/* the properties of the agent */
	Agent agent;
	int capacity;

	/* the planning class */
	Algorithm algorithm;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		this.topology = topology;
		this.td = td;
		this.agent = agent;

		// initialize the planner
		int capacity = agent.vehicles().get(0).capacity();
		String algorithmName = agent.readProperty("algorithm", String.class, "ASTAR");

		// Throws IllegalArgumentException if algorithm is unknown
		algorithm = Algorithm.valueOf(algorithmName.toUpperCase());

		// ...
	}

	@Override
	public Plan plan(Vehicle vehicle, TaskSet tasks) {
		Plan plan;
		//Look carefully at vehicle.getCurrentTasks();


		Tree tree = createTree();

		// Compute the plan with the selected algorithm.
		switch (algorithm) {
		case ASTAR:
			// ...
			plan = naivePlan(vehicle, tasks);
			break;
		case BFS:
			// ...

			plan = naivePlan(vehicle, tasks);
			break;
		default:
			throw new AssertionError("Should not happen.");
		}		
		return plan;
	}

	private Node initializeTree(Vehicle vehicle, TaskSet tasks) {
		int capacityUsed = 0; //Capacity used already by the task taken before replanning
		Node head = new Node(0, vehicle.getCurrentCity(), null); //Capacity == total available

		Node parent = head;

		Set<Node> setNode = new HashSet<Node>();

		//We don't add the reward to the node as you are always suposed to take all tasks
		for (Task task : vehicle.getCurrentTasks()) {  //Add undelivered tasks to the tree as children of the current node
			setNode.add(new Node(- task.weight, null, task.deliveryCity)); //Undelivered tasks nodes
			capacityUsed += task.weight;
		}
		for (Task task : tasks)  //Add undelivered tasks to the tree as children of the current node
			setNode.add(new Node(task.weight, task.pickupCity, task.deliveryCity)); //Undelivered tasks nodes

		constructTree(setNode, parent, vehicle.capacity() - capacityUsed);
		
		return head;
	}

	private void constructTree(Set<Node> set, Node parent, int capacityLeft) {
		for(Node n : set) {
			if(capacityLeft + n.getCapacity() < capacity) {
				parent.addChild(n);
				Set<Node> newSet = new HashSet<Node> (set);
				if(n.isTypeTask()) { //Adding a tasks not taken yet
					Node child = new Node (-n.getCapacity(), null, n.getTo());
					newSet.add(child);
				}
				newSet.remove(n);
				constructTree(newSet, n, capacityLeft - n.getCapacity());
			}
		}
	}

	private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);

		for (Task task : tasks) {
			// move: current city => pickup location
			for (City city : current.pathTo(task.pickupCity))
				plan.appendMove(city);

			plan.appendPickup(task);

			// move: pickup location => delivery location
			for (City city : task.path())
				plan.appendMove(city);

			plan.appendDelivery(task);

			// set current city
			current = task.deliveryCity;
		}
		return plan;
	}

	@Override
	public void planCancelled(TaskSet carriedTasks) {

		if (!carriedTasks.isEmpty()) {
			//Plan plan = new Plan()
			// This cannot happen for this simple agent, but typically
			// you will need to consider the carriedTasks when the next
			// plan is computed.
		}
	}
}
