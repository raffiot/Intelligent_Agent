package template;

import logist.Measures;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology.City;

import java.util.*;
public class CentralizedClass {
	
	HashMap<Vehicle,LinkedList<TaskClass>> nextTask;
	
	public CentralizedClass(){
		nextTask = new HashMap<Vehicle,LinkedList<TaskClass>>();
	}
	
	public CentralizedClass(List<Vehicle> vehicles){
		nextTask = new HashMap<Vehicle,LinkedList<TaskClass>>();
		for(Vehicle v: vehicles){
			this.initializeVehicle(v);
		}
	}
	
	public HashMap<Vehicle, LinkedList<TaskClass>> getNextTask() {
		return nextTask;
	}
	
	public CentralizedClass(HashMap<Vehicle,LinkedList<TaskClass>> hm){
		nextTask = hm;
	}
	
	public void initializeVehicle(Vehicle v){
		LinkedList<TaskClass> ll = new LinkedList<TaskClass>();
		nextTask.put(v,ll);
	}
	
	public int getLoad(Vehicle v, int time){
		if(time < 0){
			return 0;
		}
		LinkedList<TaskClass> ll = nextTask.get(v);
		if(ll.isEmpty()){
			//There is no task assigned to this vehicle yet
			return 0;
		}
		
		int size = ll.size();
		if(size < time){
			//Vehicle v has already pickup and deliver all it's task at this time
			return 0;
		}
		int load = 0;
		for(int i = 0; i< time; i++){ // <= or just < ?
			load +=ll.get(i).getWeight();
		}
		return load;
	}
	
	public int computeCost(){
		int totalCost = 0;
		
		/**
		 * For each vehicle which have task assigned we compute a cost as
		 * sum of all distances * costPerKm of the vehicle
		 */
		//22-11-2017 Changed with distanceUnitsTo and unitsToKM as in spec.
		for(Vehicle v: nextTask.keySet()){
			int cost = 0;
			if(!nextTask.get(v).isEmpty()){
				//City oldCity = v.getCurrentCity();
				City oldCity = v.homeCity();
				for(TaskClass tc: nextTask.get(v)){
					City newCity = tc.getCity();
					cost += oldCity.distanceUnitsTo(newCity);
					oldCity = newCity;
				}
			}
			totalCost += Measures.unitsToKM(cost*v.costPerKm());
		}
		return totalCost;
	}
	
	public boolean putTask(Task t, Vehicle v){
		LinkedList<TaskClass> ll = nextTask.get(v);
		int load = this.getLoad(v, ll.size()-1);
		if(load + t.weight > v.capacity()){
			return false;
		}
		ll.addLast( new TaskClass(t,TaskClass.pickup));
		ll.addLast( new TaskClass(t,TaskClass.delivery));
		return true;
	}
	
	@Override
	public CentralizedClass clone(){
		HashMap<Vehicle,LinkedList<TaskClass>> newNextTask = new HashMap<Vehicle,LinkedList<TaskClass>>();
		for(Vehicle v : nextTask.keySet()){
			LinkedList<TaskClass> ll = new LinkedList<TaskClass>();
			for(TaskClass tc : nextTask.get(v)){
				ll.add(tc);
			}
			newNextTask.put(v, ll);
		}
		return new CentralizedClass(newNextTask);
	}
	
	public TaskClass removeFirst(Vehicle v){
		LinkedList<TaskClass> ll =  nextTask.get(v);
		TaskClass t = ll.poll();
		
		
		int index = 0;
		while(true){
			TaskClass tc = ll.get(index);
			if(tc.sameTask(t)){
				ll.remove(tc);
				break;
			}
			index++;
		}
		
		return t;
	}
	
	public boolean addFirst(TaskClass tc, Vehicle v){
		//tc is the pickup entity
		if(tc.getWeight() > v.capacity()){
			return false;
		}
		this.nextTask.get(v).addFirst(new TaskClass(tc.getTask(),TaskClass.delivery));
		this.nextTask.get(v).addFirst(tc);
		return true;
	}
	
	public boolean swapTask(Vehicle v, int indexOld, int indexNew){
		LinkedList<TaskClass> ll =  nextTask.get(v);
		TaskClass tc = ll.get(indexOld);
		if(tc.getType() == TaskClass.delivery){
			//If we cannot move the delivery at a certain position because it doesn't complete weight constraint
			for(int i = indexOld+1; i < indexNew; i++){
				if(this.getLoad(v, i)-tc.getWeight() > v.capacity()){
					return false;
				}
			}
			ll.remove(tc);
			ll.add(indexNew, tc);
			return true;
		}
		else{
			for(int i = indexOld+1; i <= indexNew; i++){		
				if(ll.get(i).sameTask(tc)){
					return false;
				}
			}
			ll.remove(tc);
			ll.add(indexNew, tc);
			return true;
		}
	}
	
	public int getNbVehicle(){
		return nextTask.keySet().size();
	}
	
	public int getNbTask(Vehicle v){
		return nextTask.get(v).size();
	}
	
	public TaskClass getFirstTask(Vehicle v){
		return nextTask.get(v).getFirst();
	}
	
	public List<Plan> computePlan(List<Vehicle> vehicles){
		List<Plan> plans = new ArrayList<Plan>();
		for(Vehicle v : vehicles){
			//City currentCity = v.getCurrentCity();
			City currentCity = v.homeCity();
			Plan p = new Plan(currentCity);
			LinkedList<TaskClass> ll = nextTask.get(v);
			for(TaskClass tc : ll){
				for(City city: currentCity.pathTo(tc.getCity())){
					
					p.appendMove(city);
				}
				if(tc.getType() == TaskClass.pickup){
					p.appendPickup(tc.getTask());
				}
				else{
					p.appendDelivery(tc.getTask());
				}
				currentCity = tc.getCity();
			}
			plans.add(p);
		}
		return plans;
	}

	public void removeTask(Task previous) {
		for(Vehicle v : nextTask.keySet()){
			LinkedList<TaskClass> ll = nextTask.get(v);
			for(int i = 0; i< ll.size(); i++){
				if(ll.get(i).sameTask(previous))
					ll.remove(i);
			}
		}
		
	}
}