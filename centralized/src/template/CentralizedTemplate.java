package template;

//the list of imports
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import logist.LogistSettings;
import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.behavior.CentralizedBehavior;
import logist.agent.Agent;
import logist.config.Parsers;
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
public class CentralizedTemplate implements CentralizedBehavior {

	public static final int terminateCondition = 10000;
	
    private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private long timeout_setup;
    private long timeout_plan;
    private double pValue;
    
    @Override
    public void setup(Topology topology, TaskDistribution distribution,
            Agent agent) {
        
    	double value = 0;
    	for(City c1 : topology.cities()){
			for(City c2: topology.cities()){
				if(!c1.equals(c2)){
					value += distribution.probability(c1, c2);
				}
			}
			System.out.println(value);
		}
    	
    	System.out.println(topology.cities().size());
    	
    	pValue = 0.5;
        // this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config\\settings_default.xml");
        }
        catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }
        
        // the setup method cannot last more than timeout_setup milliseconds
        timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
        // the plan method cannot execute more than timeout_plan milliseconds
        timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);
        
        this.topology = topology;
        this.distribution = distribution;
        this.agent = agent;
    }

    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        long time_start = System.currentTimeMillis();
        
//		System.out.println("Agent " + agent.id() + " has tasks " + tasks);
        
        CentralizedClass result = null;
		try {
			result = sLS(tasks, vehicles, terminateCondition);
		} catch (Exception e) {
			System.exit(-1);
		}
        
        long time_end = System.currentTimeMillis();
        long duration = time_end - time_start;
        System.out.println("The plan was generated in "+duration+" milliseconds.");
        
        List<Plan> plans = result.computePlan(vehicles);
        
        return plans;
    }

    private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
        City current = vehicle.getCurrentCity();
        Plan plan = new Plan(current);

        for (Task task : tasks) {
            // move: current city => pickup location
            for (City city : current.pathTo(task.pickupCity)) {
                plan.appendMove(city);
            }

            plan.appendPickup(task);

            // move: pickup location => delivery location
            for (City city : task.path()) {
                plan.appendMove(city);
            }

            plan.appendDelivery(task);

            // set current city
            current = task.deliveryCity;
        }
        return plan;
    }
    
	public CentralizedClass createInitialSolution(TaskSet tasks, List<Vehicle> vehicles) throws Exception{
		CentralizedClass result = new CentralizedClass(vehicles);
		/**
		 * For each task we try to put it in each vehicle until one is enough big to carry it.
		 * If no vehicle is enough big to carry task t we throw exception
		 */
		/**
		for(Task t : tasks){
			boolean taskPut = false;
			int index = -1;
			while(!taskPut){
				index++;
				if(index == vehicles.size()){
					System.out.println("Error the task is too big to be carry by any vehicle !");
					throw new Exception("Error the task is too big to be carry by any vehicle !");
				}
				taskPut = result.putTask(t, vehicles.get(index));				
			}
		}*/
		for(Task t : tasks){
			boolean taskPut = false;
			int index = (int)Math.floor(Math.random()*(vehicles.size()));
			while(!taskPut){		
				taskPut = result.putTask(t, vehicles.get(index));	
				if(!taskPut)
					index =(int)Math.floor(Math.random()*(vehicles.size()));
			}
		}
		return result;
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
	
	public CentralizedClass sLS(TaskSet tasks, List<Vehicle> vehicles, int tCondition) throws Exception{
		CentralizedClass a = createInitialSolution(tasks, vehicles);
		int iteration =0;
		while(iteration < tCondition){
			iteration++;
			CentralizedClass aOld = a;
			List<CentralizedClass> n = ChooseNeighbours(aOld, vehicles);
			//Infinite loop inside chooseNeighbours
			
			a = localChoice(aOld, n);
			/**
			for(Plan p : a.computePlan(vehicles)){
				System.out.println(p);
			}*/
		}
		
		return a;
	}
}