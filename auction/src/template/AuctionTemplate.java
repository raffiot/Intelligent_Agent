package template;

//the list of imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 * 
 */
@SuppressWarnings("unused")
public class AuctionTemplate implements AuctionBehavior {

	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	private Random random;
	private Vehicle vehicle;
	private City currentCity;
    private double pValue;

    private int marginalCost;
    private int futureMaginalCost;
	private CentralizedClass ourSolution;
	private CentralizedClass ourFutureSolution;
	private boolean firstIteration;
	private HashMap<Integer, Opponent> opponents;
	
	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {

		marginalCost = 0;
		futureMaginalCost =0;
		firstIteration = true;
		pValue = 0.5;
		
		opponents = new HashMap<Integer, Opponent>();
		ourSolution = new CentralizedClass(agent.vehicles());
		
		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;
		this.vehicle = agent.vehicles().get(0);
		this.currentCity = vehicle.homeCity();

		long seed = -9019554669489983951L * currentCity.hashCode() * agent.id();
		this.random = new Random(seed);
		
	}

	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
		if (winner == agent.id()) {
			currentCity = previous.deliveryCity;
			ourSolution = ourFutureSolution;
			marginalCost = futureMaginalCost;
		}
		firstIteration = false; 
	}
	
	@Override
	public Long askPrice(Task task) {
		Long resultBid = Long.MAX_VALUE;
		Vehicle v = cantCarry(task);
		if(v == null){
			//No vehicle can carry the task, so bid = inf
			return resultBid;
		}
		try {
			resultBid = getFactorBid(task,v);

			if(firstIteration){
			}
			else{
				//Compute others bids
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultBid;
		/**
		if (vehicle.capacity() < task.weight)
			return null;

		long distanceTask = task.pickupCity.distanceUnitsTo(task.deliveryCity);
		long distanceSum = distanceTask
				+ currentCity.distanceUnitsTo(task.pickupCity);
		double marginalCost = Measures.unitsToKM(distanceSum
				* vehicle.costPerKm());

		double ratio = 1.0 + (random.nextDouble() * 0.05 * task.id);
		double bid = ratio * marginalCost;

		return (long) Math.round(bid);*/
	}

	private Long getFactorBid(Task task, Vehicle v) throws Exception {
		Long bid = new Long( getInitialBid(task,v));
		
		return bid;
	}

	private Vehicle cantCarry(Task task) {
		for(Vehicle v : agent.vehicles()){
			if(v.capacity() >= task.weight) return v;
		}
		return null;
	}

	private int getInitialBid(Task task, Vehicle v) throws Exception {
		ourFutureSolution = ourSolution.clone();
		ourFutureSolution.addFirst(new TaskClass(task,TaskClass.pickup), v); //will always return true
		Long tCondition = 0L; //Maybe 2/3 of time assigned
		ourFutureSolution = sLS(ourFutureSolution, agent.vehicles(), tCondition);
		futureMaginalCost = ourFutureSolution.computeCost();
		return futureMaginalCost-marginalCost;
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		
//		System.out.println("Agent " + agent.id() + " has tasks " + tasks);
		Plan planVehicle1 = null;
		//Plan planVehicle1 = naivePlan(vehicle, tasks);

		List<Plan> plans = new ArrayList<Plan>();
		plans.add(planVehicle1);
		while (plans.size() < vehicles.size())
			plans.add(Plan.EMPTY);

		return plans;
	}
	
	public CentralizedClass localChoice(CentralizedClass aOld, List<CentralizedClass> n){
		CentralizedClass abest = null;
		int minCost = Integer.MAX_VALUE;
		for(CentralizedClass cc : n){
			int actualCost = cc.computeCost();
			/**
			for(Plan p : cc.computePlan(agent.vehicles())){
				System.out.println(p);
			}
			System.out.println(actualCost);*/
			if(minCost > actualCost){
				minCost = actualCost;
				abest = cc;
			}
		}
						
		if(Math.random() > pValue){
			return aOld;
		}
		else{
			return abest;
		}
	}
	
	//If it wasn't possible to assign the task to vehicle v2 we return null, to be treated in chooseNeighbours;
	public CentralizedClass changingVehicle(CentralizedClass a, Vehicle v1, Vehicle v2){
		CentralizedClass newA = a.clone();
		TaskClass tc = newA.removeFirst(v1);
		if(!newA.addFirst(tc, v2)){
			return null;
		}
		
		return newA;
	}
	
	//If it wasn't possible to swap tasks we return null, to be treated in chooseNeighbours;
	public CentralizedClass changingTaskOrder(CentralizedClass a, Vehicle v, int indexOld, int indexNew){
		CentralizedClass newA = a.clone();
		if(!newA.swapTask(v, indexOld, indexNew)){
			return null;
		}
		return newA;
	}
	
	public List<CentralizedClass> ChooseNeighbours(CentralizedClass a, List<Vehicle> vehicles){
		List<CentralizedClass> n = new ArrayList<CentralizedClass>();
		boolean bool = true;
		Vehicle vi = null;
		int rand =  (int)Math.floor(Math.random()*(vehicles.size()));

		while(bool){
			vi = vehicles.get(rand);
			if(a.getNbTask(vi) > 0){
				bool = false;
			}
			else{
				rand = ++rand % vehicles.size();
			}
		}

		/**
		for(Plan p : a.computePlan(vehicles)){
			System.out.println(p);
		}*/
		for(Vehicle v : vehicles){
			if(!v.equals(vi)){
				CentralizedClass newA = changingVehicle(a,vi,v);
				if(newA != null){
					n.add(newA);
				}
			}
		}
		int length = a.getNbTask(vi);
		if(length >=4){
			for(int tIdx1 = 0; tIdx1 < length-1; tIdx1++){
				for(int tIdx2=tIdx1+1; tIdx2 < length; tIdx2++){
					CentralizedClass newA = changingTaskOrder(a,vi,tIdx1,tIdx2);
					if(newA != null){
						n.add(newA);
					}
				}
			}
		}
		return n;
	}
	
	public CentralizedClass sLS(CentralizedClass solution,List<Vehicle> vehicles, long tCondition) throws Exception{
		CentralizedClass a = solution;
		long time = 0; //To be modified with something like current time.
		while(time < tCondition){
			time++;
			CentralizedClass aOld = a;
			List<CentralizedClass> n = ChooseNeighbours(aOld, vehicles);
			
			a = localChoice(aOld, n);
			/**
			for(Plan p : a.computePlan(vehicles)){
				System.out.println(p);
			}*/
		}
		
		return a;
	}
}
