package template;

import logist.simulation.Vehicle;
import logist.topology.Topology.City;

import java.util.*;
public class CentralizedClass {
	
	HashMap<Vehicle,LinkedList<TaskClass>> nextTask;
	
	public CentralizedClass(){
		nextTask = new HashMap<Vehicle,LinkedList<TaskClass>>();
	}
	
	public void initializeVehicle(Vehicle v){
		LinkedList<TaskClass> ll = new LinkedList<TaskClass>();
		nextTask.put(v,ll);
	}
	
	public int getLoad(Vehicle v, int time){
		if(time < 1){
			System.out.println("error when calling method getLoad in CentralizedClass");
			return -1;
		}
		LinkedList<TaskClass> ll = nextTask.get(v);
		if(ll.isEmpty()){
			//There is no task assigned to this vehicle yet
			return 0;
		}
		int size = ll.size();
		if(size <= time){
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
		
		for(Vehicle v: nextTask.keySet()){
			int cost = 0;
			if(!nextTask.get(v).isEmpty()){
				City oldCity = v.getCurrentCity();
				for(TaskClass tc: nextTask.get(v)){
					City newCity = tc.getCity();
					cost += oldCity.distanceTo(newCity);
					oldCity = newCity;
				}
			}
			totalCost += cost*v.costPerKm();
		}
		return totalCost;
	}
	
}