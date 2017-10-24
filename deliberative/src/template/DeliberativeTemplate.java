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
	Set<Task> tasksToDeliber;
	Set<Task> tasksToPick;


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


			//TEST
			ArrayList<Task> res = bFS();





			//			while(!tasksToDeliber.isEmpty() || !tasksToPick.isEmpty()) {
			//				actions = bFS();
			//				for (Action a : actions) {
			//					plan.append(a);
			//					System.out.println(a);
			//				}
			//			}

			break;
		default:
			throw new AssertionError("Should not happen.");
		}		
		return null; //Changed for testing
	}

	private ArrayList<Task> bFS () { 
		//		ArrayList<Action> actions = new ArrayList<Action>();
		//		ArrayList<City> queue = new ArrayList<City>();
		//		ArrayList<City> visited = new ArrayList<City>();
		//		HashMap<City, Task> goalsDelibery = new HashMap<City, Task>();
		//		HashMap<City, Task> goalsPick = new HashMap<City, Task>();
		//		
		//		for (Task t : tasksToPick) 
		//			goalsPick.put(t.pickupCity, t);
		//		for (Task t : tasksToDeliber) 
		//			goalsDelibery.put(t.deliveryCity, t);
		//		
		//		visited.add(currentCity);
		//		queue.add(currentCity);
		//		
		//		while(!queue.isEmpty()) {
		//			City city = queue.remove(0);
		//			
		//			for(City c : city.neighbors()) {
		//				if(!visited.contains(c)) {
		//					this.currentCity = c;
		//					visited.add(c);
		//					queue.add(c);
		//					actions.add(new Move(c));
		//
		//					if (goalsDelibery.containsKey(c)) {
		//						Task t = goalsDelibery.get(c);
		//						actions.add(new Delivery(t));
		//						this.capacity += t.weight;
		//						tasksToDeliber.remove(t);
		//						goalsDelibery.remove(c);
		//					}
		//					if (goalsPick.containsKey(c) && goalsPick.get(c).weight < this.capacity) {
		//						Task t = goalsPick.get(c);
		//						goalsDelibery.put(t.deliveryCity, t);
		//						actions.add(new Pickup(t));
		//						this.capacity -= t.weight;
		//						tasksToPick.remove(t);
		//						goalsPick.remove(c);
		//						tasksToDeliber.add(t);
		//						goalsDelibery.put(t.deliveryCity, t);
		//						//tasksTODO.remove(t); No la elimino, ahora se ha convertido en una tarea a entregar
		//						
		//					}
		//				}
		//			}
		//		}
		//		
		//		return actions;

		ArrayList<Task> path = new ArrayList<Task>(); //Result
		ArrayList<Task> queue = new ArrayList<Task>();
		ArrayList<Task> visited = new ArrayList<Task>();
		ArrayList<Task> toDo = new ArrayList<Task>(); //All tasks that must be done
		ArrayList<Task> toDoCopy = new ArrayList<Task>(); //All tasks that must be done
		ArrayList<Task> delibers = new ArrayList<Task>(); //Will always have the tasks to be delibered at that moment

		Boolean deliber = false;

		toDo.addAll(tasksToDeliber);
		toDo.addAll(tasksToPick);

		for(Task t : toDo)
			System.out.println(t.toString());

		delibers.addAll(tasksToDeliber);

		//		visited.add(currentCity); //REMEMBER TO ADD THE FIRST CITY TO PLAN IF NEADED
		queue.add(null); //Initial node, no task

		//TESTEEEEEEEEE
		capacity = 6;
		int i = 0;
		
		
		while(!queue.isEmpty()) {
			if (i++ > 8)
				return null;
			Task task = queue.remove(0);
			ArrayList<Task> toDeliber2 = new ArrayList<Task>();


			for(Task t : toDo) {
				if(delibers.contains(t)) //Task is at the truck
					deliber = true;
				if(deliber || capacity >= t.weight) { //I can pick it or deliber
					//visited.add(t);
					//queue.add(t); //Ya lo estoy haciendo abajo..
					if(deliber) {
						deliber = false;
						delibers.remove(t);
						toDoCopy.remove(t);
						//toDo.remove(t);
						capacity += t.weight;
						System.out.println("Task " + t.id + "   -   type: deliber");
					}else { //To pick
						toDeliber2.add(t);
						delibers.add(t);
						toDoCopy.add(t);
						capacity -= t.weight;
						System.out.println("Task " + t.id + "   -   type: pick");
					}
					path.add(t);
				}else  //No he podido cogerla y entonces se queda en toDo
					toDoCopy.add(t);
			}
			toDo = new ArrayList(toDoCopy);
			queue.addAll(toDeliber2); //ERROR
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
