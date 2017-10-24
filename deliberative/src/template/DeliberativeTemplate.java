package template;

/* import table */
import logist.simulation.Vehicle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import logist.agent.Agent;
import logist.behavior.DeliberativeBehavior;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;
import logist.plan.Action;
import logist.plan.Action.Delivery;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;

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
	TaskSet tasks;

	/* the planning class */
	Algorithm algorithm;

	City currentCity;
	Vehicle vehicle;
	Set<Task> tasksToDeliber; //Creo que sobra
	Set<Task> tasksToPick; //Creo que sobra
	HashMap<Task, Boolean> tasksToDo;	
	
	
	
	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		this.topology = topology;
		this.td = td;
		this.agent = agent;

		// initialize the planner
		int capacity = agent.vehicles().get(0).capacity();
		this.capacity = capacity;
		String algorithmName = agent.readProperty("algorithm", String.class, "ASTAR");

		// Throws IllegalArgumentException if algorithm is unknown
		algorithm = Algorithm.valueOf(algorithmName.toUpperCase());

		// ...
	}

	@Override
	public Plan plan(Vehicle vehicle, TaskSet tasks) {
		Plan plan;
		this.vehicle = vehicle;	
		currentCity = vehicle.getCurrentCity();
		tasksToDeliber = vehicle.getCurrentTasks();
		tasksToPick = tasks;
		tasksToDo = new HashMap<Task, Boolean>();	

		for (Task t : tasksToDeliber)
			tasksToDo.put(t, true);

		for (Task t : tasksToPick)
			tasksToDo.put(t, false);

		// Compute the plan with the selected algorithm.
		switch (algorithm) {
		case ASTAR:
			// ...
					plan = null; //Delete this!!
			HashMap<Task, Boolean> allTasksASTAR =  new HashMap<Task, Boolean>(tasksToDo);
			HashMap<Node, Double> q = new HashMap<Node, Double>	(); //Q at the notes given
			HashMap<Node, Double> c = new HashMap<Node, Double>	(); //C at the notes given
			Node n = null;
			int totalTasks = 2*tasks.size() + vehicle.getCurrentTasks().size();
			
			q.put(new Node(), 0.);
			
			while (true) {
				System.out.println("     Iter");
				//if Q.isEmpty -> failure
				n = getMinCostNode(q);
				q.remove(n);
				
				if (totalTasks == n.getNumerOfTasksDone())
					break; 
				
				//Check for C
				
				HashMap<Node, Double> s = succ(n, tasksToDo);
			
				q = merge(q, s);
				
				System.out.println("Printing q at the end of each while   !");
				for (Node nTest : q.keySet())
					System.out.println(nTest);
				
				//Problem!!! Q is empty, it should have the succ
			}
			
			
			//Test
			Node father = n;
			System.out.println(" Parents from best action, size " + father.getNumerOfTasksDone());

			while (father != null) {
				System.out.println(father.getTask() + "    Type: " + father.getType());
				father = father.getParent();
			}
			
			
			
			//Calculate path with n
			
					
			break;
		case BFS:	
			plan = new Plan(currentCity);
			this.tasks = tasks;
			ArrayList<Action> actions;

			for (Task t : bFS()) {
				if(tasksToDo.get(t)) {  //Delivery
					for(City c2 : currentCity.pathTo(t.deliveryCity))
						plan.append(new Move(c2));	
					currentCity = t.deliveryCity;
					plan.append(new Delivery(t));
					tasksToDo.remove(t);
				}else { //Pick
					for(City c1 : currentCity.pathTo(t.pickupCity))
						plan.append(new Move(c1));	
					currentCity = t.pickupCity;
					plan.append(new Pickup(t));
					tasksToDo.replace(t, true);
				}
			}
			break;
		default:
			throw new AssertionError("Should not happen.");
		}		
		return plan; //Changed for testing
	}

	
	private Node getMinCostNode(HashMap<Node, Double> nodes){
		Node node = nodes.keySet().iterator().next();

		for(Node n : nodes.keySet())
			if (node.getCost() > n.getCost())
				node = n;

		return node;
	}
	
	private HashMap<Node, Double> succ(Node node, HashMap<Task, Boolean> allTasks){ //All tasks must be setted at the very beggining and not changed
		HashMap<Node, Double> res = new HashMap<Node, Double>();
		HashMap<Task, Boolean> tasksDone = node.getTasksDone();
		for(Task t : allTasks.keySet()) {
			if (! tasksDone.containsKey(t)) { //Not delivered not picked
				if(! allTasks.get(t)) { //Type == pick
					if (node.getTask() == null) //For initial node
						res.put(new Node(t, false, node, 0.), currentCity.distanceTo(t.pickupCity));
					else
						res.put(new Node(t, false, node, node.getCityOfNode().distanceTo(t.pickupCity)), node.getCityOfNode().distanceTo(t.pickupCity));
				}else //Type == delivery
					res.put(new Node(t, true, node, node.getCityOfNode().distanceTo(t.deliveryCity)), node.getCityOfNode().distanceTo(t.deliveryCity));
			}else { //Task is already Picked or delivered
				if(! tasksDone.get(t)) //Picked -> have to add the delivery
					res.put(new Node(t, true, node, node.getCityOfNode().distanceTo(t.deliveryCity)), node.getCityOfNode().distanceTo(t.deliveryCity)); 
				//Else, Delivered -> no action for that task
			}
		}
		return res;	
	} 

	private HashMap<Node, Double> merge(HashMap<Node, Double> q, HashMap<Node, Double> s){
		HashMap<Node, Double> r = new HashMap<Node, Double> ();
		for (Node nS : s.keySet()) {
			if (q.size() == 0) //Case q is empty
				q.put(nS, s.get(nS));
			else {
				for(Node nQ : q.keySet()) {
					if (nQ.getTask().equals(nS.getTask()) && s.get(nS) == q.get(nQ)) { //We have found a new road for the same node so we compare it and if its shorter we change in q
						if(nQ.getCost() > nS.getCost())
							r.put(nS, nS.getCost());
						else
							r.put(nQ, nQ.getCost());
					}else  //New succ not present in q, add it.
						r.put(nS, s.get(nS));
				}
			}
		}
		return r;
	}

	
	private ArrayList<Task> bFS () { 
		ArrayList<Task> path = new ArrayList<Task>(); //Result
		ArrayList<Task> queue = new ArrayList<Task>(); //Initial node, no task
		ArrayList<Task> toRemove;
		HashMap<Task, Boolean> tasksToDo2 = new HashMap<Task, Boolean>(tasksToDo);	


		queue.add(null);

		//TESTEEEEEEEEE
		for(Task t: tasksToDo.keySet())
			System.out.println(t + " tipo: " + tasksToDo.get(t));

		while(!queue.isEmpty()) {
			Task task = queue.remove(0);			
			toRemove = new ArrayList<Task>();

			for(Task t : tasksToDo2.keySet()) {
				if(tasksToDo2.get(t) || capacity >= t.weight) { //I can pick it or deliber
					if(tasksToDo2.get(t)) {
						toRemove.add(t);
						capacity += t.weight;
//						System.out.println("Task " + t.id + "   -   type: deliber");
					}else { //To pick
						tasksToDo2.replace(t, true);
						capacity -= t.weight;
//						System.out.println("Task " + t.id + "   -   type: pick");
					}
					path.add(t);
					queue.add(t);
				} //No he podido cogerla y entonces se queda en toDo
			}
			tasksToDo2.keySet().removeAll(toRemove);
		}
		return path;
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
			// This cannot happen for this simple agent, but typically
			// you will need to consider the carriedTasks when the next
			// plan is computed.
		}
	}
}
