package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import logist.simulation.Vehicle;
import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class ReactiveTemplate implements ReactiveBehavior {

	private Random random;
	private double pPickup;
	private int numActions;
	private Agent myAgent;
	private HashMap<City, HashMap<Boolean,Double>> rewards; //Boolean true=taken
	private HashMap<City, HashMap<City,Double>> transitionTable;
	private HashMap<City,Double> v;
	private double goodEnough;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		double costPerKm = agent.vehicles().get(0).costPerKm(); //Be carefull because it's the cost for 1st vehicle
		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class,
				0.95);
		rewards = new HashMap<City, HashMap<Boolean,Double>>();
		transitionTable = new HashMap<City,HashMap<City,Double>>();
		v = new HashMap<City,Double>();
		this.random = new Random();
		this.pPickup = discount;
		this.numActions = 0;
		this.myAgent = agent;
		
		//INITIALIZE TABLES
		for(City c: topology.cities()){
			//V(c)
			v.put(c, 0.);
			//TRASITION
			HashMap<City,Double> hmTransition = new HashMap<City,Double>();
			double prob = 1./c.neighbors().size();
			//REWARD
			HashMap<Boolean,Double> hmTake = new HashMap<Boolean,Double>();
			double fromcToi = 0;
			double fromiToN = 0;
			for(City i : c.neighbors()){
				//TRANSITION
				hmTransition.put(i, prob); //we don't initialize non neighbors
				
				//REWARD
				fromcToi += td.probability(c, i)*td.reward(c, i)-c.distanceTo(i)*costPerKm;
				
				for(City j: topology.cities()){
					fromiToN+= td.probability(i, j)*td.reward(i, j)-i.distanceTo(j)*costPerKm;
				}
				fromiToN-=c.distanceTo(i)*costPerKm;
				
			}
			transitionTable.put(c, hmTransition);
			
			hmTake.put(true, fromcToi);
			hmTake.put(false,fromiToN);
			rewards.put(c, hmTake);
		}
		
		

		
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;

		if (availableTask == null || random.nextDouble() > pPickup) {
			City currentCity = vehicle.getCurrentCity();
			action = new Move(currentCity.randomNeighbor(random));
		} else {
			action = new Pickup(availableTask);
		}
		
		if (numActions >= 1) {
			System.out.println("The total profit after "+numActions+" actions is "+myAgent.getTotalProfit()+" (average profit: "+(myAgent.getTotalProfit() / (double)numActions)+")");
		}
		numActions++;
		
		return action;
	}
}
