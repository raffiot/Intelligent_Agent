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

		// Compute the plan with the selected algorithm.
		switch (algorithm) {
		case ASTAR:
			// ...
			plan = naivePlan(vehicle, tasks);
			break;
		case BFS:	
			currentCity = vehicle.getCurrentCity();
			plan = new Plan(currentCity); //Check
			this.tasks = tasks;
			ArrayList<Action> actions;
			tasksToDeliber = vehicle.getCurrentTasks();
			tasksToPick = tasks;
			tasksToDo = new HashMap<Task, Boolean>();	


			for (Task t : tasksToDeliber)
				tasksToDo.put(t, true);

			for (Task t : tasksToPick)
				tasksToDo.put(t, false);

			ArrayList<Task> tasksBFS = bFS();

			plan = new Plan(currentCity);

			for (Task t : tasksBFS) {
				if(tasksToDo.get(t)) {  //Delivery
					for(City c : currentCity.pathTo(t.deliveryCity))
						plan.append(new Move(c));	
					currentCity = t.deliveryCity;
					plan.append(new Delivery(t));
					tasksToDo.remove(t);
				}else { //Pick
					for(City c : currentCity.pathTo(t.pickupCity))
						plan.append(new Move(c));	
					currentCity = t.pickupCity;
					plan.append(new Pickup(t));
					tasksToDo.replace(t, true);
				}
			}
			//			//TEST
//			System.out.println();
//			System.out.println();
//			System.out.println("Tareas   ");
//			for(Task t : tasksBFS)
//				System.out.println(t);				
//			System.out.println();
//			System.out.println();
//			System.out.println();
//			System.out.println();
//			System.out.println();
//			System.out.println();
//			for(Action a : plan)
//				System.out.println(a);
			break;
		default:
			throw new AssertionError("Should not happen.");
		}		
		return plan; //Changed for testing
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
