package template;

//the list of imports
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import logist.LogistSettings;
import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.config.Parsers;
import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.simulation.VehicleImpl;
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
	private int iterations;
	private HashMap<Integer, Opponent> opponents;
	private double meanCapacity;
	private double meanCostPerKm;
	private List<City> toDiscover;
	private CentralizedClass bestASLS;
	private int bestCostSLS;
	private int benefit;
	
    private long timeout_plan;
    private long timeout_bid;
	
	
	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {

		iterations = 0;
		marginalCost = 0;
		futureMaginalCost =0;
		firstIteration = true;
		pValue = 0.5;
		
		opponents = new HashMap<Integer, Opponent>(); //Integer determinate it's position in Long[] bid table
		ourSolution = new CentralizedClass(agent.vehicles());
		
		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;
		this.vehicle = agent.vehicles().get(0);
		this.currentCity = vehicle.homeCity();
		
		long seed = -9019554669489983951L * currentCity.hashCode() * agent.id();
		this.random = new Random(seed);
		
		meanCapacity = 0;
		meanCostPerKm = 0;
		for(Vehicle v : agent.vehicles()){
			meanCapacity += v.capacity();
			meanCostPerKm += v.costPerKm();
		}
		meanCapacity /= agent.vehicles().size();
		meanCostPerKm /= agent.vehicles().size();
		benefit = 0;
		
		toDiscover = new ArrayList<City>(topology.cities());
		
		LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config\\settings_auction.xml");
        }
        catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }
		
        timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);
        timeout_bid = ls.get(LogistSettings.TimeoutKey.BID);
	}

	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
		iterations++;
		System.out.println("auction result, winner =" +winner);
		for(int i = 0; i < bids.length; i++){
			System.out.println(" bids = "+bids[i]);
		}
		if (winner == agent.id()) {
			currentCity = previous.deliveryCity;
			ourSolution = ourFutureSolution;
			marginalCost = futureMaginalCost;
			toDiscover.removeAll(previous.pickupCity.pathTo(previous.deliveryCity));
			toDiscover.remove(previous.pickupCity);
			benefit += bids[winner];
		}
		if(firstIteration){
			HashMap<City,Double> distanceToPickup = new HashMap<City,Double>();
			for(City c : topology.cities()){
				distanceToPickup.put(c, Measures.unitsToKM((c.distanceUnitsTo(previous.pickupCity)+previous.pickupCity.distanceUnitsTo(previous.deliveryCity))*(long)meanCostPerKm));
			}
			for(int i = 0; i<bids.length; i++){
				if(i != agent.id()){
					Long bid = bids[i];
					double minDiff = Double.MAX_VALUE;
					City minCity = null;
					for(City c : topology.cities()){
						if(minDiff > Math.abs(bid -distanceToPickup.get(c))){
							minDiff = Math.abs(bid -distanceToPickup.get(c));
							minCity = c;
						}
					}
					//We create the ficticious Vehicle of the opponent 
					System.out.println("mincity :"+minCity.name);
					VehicleImpl vi = new VehicleImpl(0, "opponent", (int)meanCapacity, (int)meanCostPerKm, minCity, 0L, Color.black);
					//CURRENTCITY ISN'T AVAILABLE USE HOMECITY INSTEAD
					Vehicle v = vi.getInfo();
					ArrayList<Vehicle> ve = new ArrayList<Vehicle>();
					ve.add(v);
					CentralizedClass cc = new CentralizedClass(ve);
					Opponent op = new Opponent(new ArrayList<Task>(),cc,ve);
					opponents.put(i, op);
				}
			}
			firstIteration = false; 	
		}
		for(Integer oppId : opponents.keySet()){
			if(oppId != winner){
				Opponent op = opponents.get(oppId);
				op.getSolution().removeTask(previous);
				op.setCurCost(op.getSolution().computeCost());
			}
			else if(firstIteration && oppId == winner ){
				Opponent op = opponents.get(oppId);
				op.getSolution().putTask(previous, op.getVehicle().get(0));
				for(Plan p : op.getSolution().computePlan(op.getVehicle())){
					System.out.println(p);
				}
				op.addTask(previous);
				op.setCurCost(op.getSolution().computeCost());
			}
		}
		
	}
	
	@Override
	public Long askPrice(Task task) {
		long time_start = System.currentTimeMillis();
		
		System.out.println("askprice "+task+" it "+iterations);
		Long resultBid = Long.MAX_VALUE;
		Vehicle v = cantCarry(task);
		if(v == null){
			//No vehicle can carry the task, so bid = inf
			return resultBid;
		}
		try {
			resultBid = getFactorBid(task,v);
			double minOpponentCost = Double.MAX_VALUE;
			if(!firstIteration){
				//TODO
				//Si no es la primera iteration compute others bid and change resultbib
				//Only compute the top 3
				if(iterations < 10){
					for(Integer i : opponents.keySet()){
						time_start = System.currentTimeMillis();
						Opponent op = opponents.get(i);
						op.getSolution().putTask(task,op.getVehicle().get(0));
						System.out.println("compute opponent");
						CentralizedClass cc = sLS(op.getSolution(), op.getVehicle() , (long)(1/(6.*opponents.size())*timeout_bid+time_start));//TODO: tcondition in fuction of time!!!
						double cost = cc.computeCost() - op.getCurCost();
						if(cost < minOpponentCost){
							minOpponentCost = cost;
						}
					}
				}
				else{
					Collection c = opponents.values();
					List<Opponent> l = new ArrayList<Opponent>(c);
					Collections.sort(l, new Comparator<Opponent>(){

						@Override
						public int compare(Opponent arg0, Opponent arg1) {
							
							return arg0.getTasks().size()<arg1.getTasks().size() ? -1 : 1 ;
						}
						
					});
					int si = Math.min(3,l.size());
					for(int i = 0; i< si; i++){
						time_start = System.currentTimeMillis();
						Opponent op = l.get(i);
						op.getSolution().putTask(task,op.getVehicle().get(0));
						
						CentralizedClass cc = sLS(op.getSolution(), op.getVehicle() , (long)(1/(18.)*timeout_bid+time_start));//TODO: tcondition in fuction of time!!!
						double cost = cc.computeCost() - op.getCurCost();
						op.setSolution(cc);
						if(cost < minOpponentCost){
							minOpponentCost = cost;
						}
					}
				}
				long diffAbs = Math.abs((long)minOpponentCost - resultBid);
				if(diffAbs < resultBid*0.05){
					resultBid = (long) (resultBid*0.95);
				}
				else{
					resultBid = (long) (resultBid*1.05);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("our bid "+resultBid);
		return resultBid;
	}

	private Long getFactorBid(Task task, Vehicle v) throws Exception {
		Long bid = new Long( getInitialBid(task,v));
		System.out.println("first bid "+bid);
		double factor = 1.;
		factor += getFactorProbability(task);
		System.out.println("second bid "+bid*factor);
		factor += getFactorCapacity(task);
		System.out.println("third bid "+bid*factor);
		return (long)(bid*factor);
	}

	private double getFactorCapacity(Task task) {
		double factor = Math.exp(task.weight/meanCapacity);
		return AuctionTemplate.map(factor, 1, Math.exp(1), -0.05, 0.3);
	}

	//This Method compute the factor with which we multiply the bid.
	//The factor is in function of the cities where we never been.
	private double getFactorProbability(Task task) {
		double probability = 0;
		City pickup = task.pickupCity;
		for(City c : toDiscover){
			probability += distribution.probability(pickup, c);
		}
		
		for(City onPath: task.pickupCity.pathTo(task.deliveryCity)){
			for(City c : toDiscover){
				probability += distribution.probability(onPath, c);
			}	
		}
		probability/=topology.cities().size();
		
		return -0.15*(2*probability-1);
	}

	private Vehicle cantCarry(Task task) {
		boolean taskPut = false;
		int index = (int)(Math.random()*agent.vehicles().size());
		while(true){		
			if(agent.vehicles().get(index).capacity() >= task.weight)
				return agent.vehicles().get(index);	
			index = (index +1) % agent.vehicles().size();
		}
	}

	private int getInitialBid(Task task, Vehicle v) throws Exception {
		long time_start = System.currentTimeMillis();
		ourFutureSolution = ourSolution.clone();
		ourFutureSolution.addFirst(new TaskClass(task,TaskClass.pickup), v); //will always return true
		ourFutureSolution = sLS(ourFutureSolution, agent.vehicles(), (long)((4/6.)*timeout_bid+time_start));
		
		for(Plan p : ourFutureSolution.computePlan(agent.vehicles())){
			System.out.println(p);
		}
		futureMaginalCost = ourFutureSolution.computeCost();
		System.out.println(futureMaginalCost);
		return futureMaginalCost-marginalCost;
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		long time_start = System.currentTimeMillis();
        
//		System.out.println("Agent " + agent.id() + " has tasks " + tasks);
       
        CentralizedClass result = null;
		try {
			result = ourSolution.clone(tasks);
			result = sLS(result, vehicles, (long)((4/6.)*timeout_plan+time_start));
			
		} catch (Exception e) {
			System.exit(-1);
		}
		System.out.println("Benefit before minus plan us "+benefit);
		for(Plan p : result.computePlan(agent.vehicles())){
			System.out.println(p);
			benefit -= Measures.unitsToKM(p.totalDistanceUnits()*5);
		}
		System.out.println("Final Benefit us "+benefit);
        long time_end = System.currentTimeMillis();
        long duration = time_end - time_start;
        System.out.println("The plan was generated in "+duration+" milliseconds.");
        
        List<Plan> plans = result.computePlan(vehicles);
        
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
		if(minCost < bestCostSLS){
			bestASLS = abest;
			bestCostSLS = minCost;
		}
		if(Math.random() > pValue || abest == null){
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
		bestASLS = a;
		bestCostSLS = a.computeCost();
		
		while(System.currentTimeMillis() < tCondition){
			CentralizedClass aOld = a;
			List<CentralizedClass> n = ChooseNeighbours(aOld, vehicles);
			a = localChoice(aOld, n);
		}
		
		return bestASLS;
	}
	
	static double map(double x, double a, double b, double c, double d) {
        return (x - a) * (d - c) / (b - a) + c;
    }
}
