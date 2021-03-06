package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	private double goodEnough;
	private List<Double> v;
	private List<City> bestAction;
	private List<State> states;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		
		double costPerKm = agent.vehicles().get(0).costPerKm(); //Be carefull because it's the cost for 1st vehicle
		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class,
				0.95);
		this.random = new Random();
		this.pPickup = discount;
		this.numActions = 0;
		this.myAgent = agent;
		this.goodEnough = 0.01;
		
		states = new ArrayList<State>();
		List<City> actions = topology.cities();
		
		for(City from: topology.cities()){
			for(City to: topology.cities()){
				if(!from.equals(to)){
					states.add( new State(from,to));
				}
			}
			states.add(new State(from,null));
		}
		
		//PRINT DEBBUG ---> It works
		/**
		for(State s: states){
			System.out.println(s.toString());
		}*/
		
		List<ArrayList<Double>> rewards = new ArrayList<ArrayList<Double>>();
		/**
		 * Si el estado es city1 to ? y que la action es city1 -> null
		 * Si el estado es city1 to null y que la action es city -> - coste(city1,city2)
		 * Si el estado es city1 to city2 y que la action es city2 -> AR(city1,city2) - coste(city1,city2)
		 * Si el estado es city1 to city2 y que la action es city3 -> null
		 */
		for(State s: states){
			ArrayList<Double> forState = new ArrayList<Double>();
			for(City act: actions){
				if(s.getFrom().equals(act)){
					forState.add(null);
				}
				else{
					if(!s.haveDestination()){
						forState.add(- s.getFrom().distanceTo(act) * costPerKm);				
					}
					else{
						if(s.getTo().equals(act)){
							forState.add(td.reward(s.getFrom(), act)- s.getFrom().distanceTo(act) * costPerKm);
						}
						else{
							forState.add(null);
						}
					}
				}
			}
			rewards.add(forState);
		}
		
		//PRINT DEBBUG ---> It works !!
		/**
		for(int i=0; i<rewards.size(); i++){
			System.out.println("For state "+states.get(i).toString());
			for(int j=0; j<rewards.get(i).size(); j++){
				System.out.println("	And action "+actions.get(j)+" We have "+rewards.get(i).get(j));
			}
		}
		*/
		List<ArrayList<ArrayList<Double>>> transitions = new ArrayList<ArrayList<ArrayList<Double>>>();
		/**
		 * Si el estado es city1 to ? y que la action es city1 -> null
		 * Si el estado es city1 to null y que la action es city2 -> - coste(city1,city2)
		 * Si el estado es city1 to city2 y que la action es city2 -> AR(city1,city2) - coste(city1,city2)
		 * Si el estado es city1 to city2 y que la action es city3 -> null
		 */
		for(State s: states){
			System.out.println("For s = "+s.toString());
			ArrayList<ArrayList<Double>> forS = new ArrayList<ArrayList<Double>>();
			for(City act: actions){
				System.out.println("	a = "+act.name);
				ArrayList<Double> forA = new ArrayList<Double>();
				
				//ACCUMULADOR
				double accumulador = 0;
				for(State sPrim : states){
					System.out.println("			s' = "+sPrim.toString());
					if (s.getFrom().equals(act)){
						forA.add(0.);
						System.out.println("				s.from == act -> 0");
					}
					else{
						if(!s.haveDestination()){
							if(!act.equals(sPrim.getFrom())){
								forA.add(0.);
								System.out.println("				act != sPrim.from -> 0");
							}
							else{
								if(sPrim.haveDestination()){
									double value = td.probability(sPrim.getFrom(), sPrim.getTo());
									forA.add(value);
									accumulador +=value;
									System.out.println("				probPacket(sPrim.from,sPrim.to)");
								}
								else{
									System.out.println("				1-sum(probPacket(sPrim.from,sPrim.allDest))");
									forA.add(-1.);
								}
							}
						}
						else{
							if(!act.equals(sPrim.getFrom())){
								forA.add(0.);
								System.out.println("				act != sPrim.from -> 0");
							}
							else{
								if(sPrim.haveDestination()){
									double value = td.probability(sPrim.getFrom(), sPrim.getTo());
									forA.add(value);
									System.out.println("				probPacket(sPrim.from,sPrim.to)");
								}
								else{
									forA.add(-1.);
									System.out.println("				1-sum(probPacket(sPrim.from,sPrim.allDest))");
								}
							}
						}
					}
						
				}
				//With this loop if there is no -1 we don't do an array out of bounds
				//Else it loop to set each -1 to the 1-accumulator
				//and change the instruction from add to set
				while(forA.indexOf(-1.) != -1.){
					forA.set(forA.indexOf(-1.),1-accumulador); 
				}		
				forS.add(forA);
			}
			transitions.add(forS);
		}
		
		//TABLE V(s)
		v = new ArrayList<Double>();
		List<Double> vPrim = new ArrayList<Double>();
		bestAction = new ArrayList<City>(); // best action for v
		//Initialize best action
		for(int i = 0; i< states.size(); i++){
			vPrim.add(0.);
			bestAction.add(null);
		}
		
		do{
			v = new ArrayList<Double>(vPrim);
			for(State s: states){
				double q = Double.NEGATIVE_INFINITY;
				for(City act: actions){
					double sum = 0;
					for(State sPrim: states){
						sum += transitions.get(states.indexOf(s)).get(actions.indexOf(act)).get(states.indexOf(sPrim))*v.get(states.indexOf(sPrim));
					}
					System.out.println("out the if statement q="+q);
					System.out.println("State : "+s.toString());
					System.out.println("Action : "+act.name);
					System.out.println("reward "+rewards.get(states.indexOf(s)).get(actions.indexOf(act)));
					//Put an other condition in the if to check if reward isn't null
					if(rewards.get(states.indexOf(s)).get(actions.indexOf(act)) != null && q < rewards.get(states.indexOf(s)).get(actions.indexOf(act))+discount*sum ){
						q = rewards.get(states.indexOf(s)).get(actions.indexOf(act))+discount*sum;
						
						//System.out.println("in the if statement q="+q);
						bestAction.set(states.indexOf(s), act);
					}
						
				}
				vPrim.set(states.indexOf(s),q);
			}
		}while(vectorDifference(v, vPrim)< goodEnough);
		
		v = new ArrayList<Double>(vPrim);
		
		for(int i =0; i< states.size(); i++){
			if(bestAction.get(i) ==null){
				System.out.println("null -> State : "+states.get(i)+" Rewards : "+v.get(i));
			}
			System.out.println("State : "+states.get(i)+" Rewards : "+v.get(i)+ " With actions : "+bestAction.get(i).name);
		}
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;

		// metodo calculo tablq
			//
		if (availableTask == null) {
			City toGo = bestAction.get(getIndexOf((new State(vehicle.getCurrentCity(),null))));
			action = new Move(toGo);
		} else {

			System.out.println("if we take the task :"+v.get(getIndexOf(new State(vehicle.getCurrentCity(),availableTask.deliveryCity))));
			System.out.println("if we don't take the task :"+v.get(getIndexOf(new State(vehicle.getCurrentCity(),null))));
			if(v.get(getIndexOf(new State(vehicle.getCurrentCity(),availableTask.deliveryCity))) > 
				v.get(getIndexOf(new State(vehicle.getCurrentCity(),null)))){
				action = new Pickup(availableTask);
			}
			else{
				
				City toGo = bestAction.get(getIndexOf((new State(vehicle.getCurrentCity(),null))));
				action = new Move(toGo);
			}
		}
		
		
		if (numActions >= 1) {
			System.out.println("The total profit after "+numActions+" actions is "+myAgent.getTotalProfit()+" (average profit: "+(myAgent.getTotalProfit() / (double)numActions)+")");
		}
		numActions++;
		
		return action;
	}
	
	public double vectorDifference(List<Double> v1, List<Double> v2){
		double diff = 0;
		for(int i =0; i< v1.size(); i++){
			diff += Math.abs(v1.get(i)-v2.get(i));
		}
		return diff;
	}
	
	private int getIndexOf(State s){
		for(int i =0; i< v.size(); i++){
			
			if(states.get(i).equals1(s.getFrom(), s.getTo())){
				return i;
			}
		}
		return -1;
	}
}
