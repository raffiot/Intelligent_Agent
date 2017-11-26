package template;

import logist.Measures;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

import java.util.*;

/**
 * 
 * Class that represent a solution i.e an assignation of tasks to vehicle in order
 *
 */
public class CentralizedClass {
	
	HashMap<Vehicle,LinkedList<TaskClass>> nextTask;
	
	/**
	 * Empty contructor tu just initialize the solution without task and vehicle
	 */
	public CentralizedClass(){
		nextTask = new HashMap<Vehicle,LinkedList<TaskClass>>();
	}
	
	/**
	 * Constructor with list of vehicles that will do pickup and delivery tasks
	 * @param vehicles
	 * 	the list of vehicles 
	 */
	public CentralizedClass(List<Vehicle> vehicles){
		nextTask = new HashMap<Vehicle,LinkedList<TaskClass>>();
		for(Vehicle v: vehicles){
			this.initializeVehicle(v);
		}
	}
	
	/**
	 * Method to get the datastructure that contain the solution
	 * @return
	 * 	the datastructure
	 */
	public HashMap<Vehicle, LinkedList<TaskClass>> getNextTask() {
		return nextTask;
	}
	
	/**
	 * Method to set the datastructure that contain the solution
	 * @param hm
	 * 	the new solution
	 */
	public CentralizedClass(HashMap<Vehicle,LinkedList<TaskClass>> hm){
		nextTask = hm;
	}
	
	/**
	 * Method to add a vehicle to the solution
	 * @param v
	 * 	the vehicle to add
	 */
	public void initializeVehicle(Vehicle v){
		LinkedList<TaskClass> ll = new LinkedList<TaskClass>();
		nextTask.put(v,ll);
	}
	
	/**
	 * Method to obtain the load of a vehicle at a given time 
	 * @param v
	 * 	the vehicle of which we want to know the load
	 * @param time
	 * 	the time at which we want to know the load
	 * @return
	 * 	the load for given vehicle at given time
	 */
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
	
	/**
	 * Method that compute the travel cost of the vehicles for this solution
	 * 
	 * @return
	 * 	the sum of all cost of vehicles to pickup and deliver all tasks of the solution
	 */
	public int computeCost(){
		int totalCost = 0;
		
		/**
		 * For each vehicle which have task assigned we compute a cost as
		 * sum of all distances * costPerKm of the vehicle
		 */
		for(Vehicle v: nextTask.keySet()){
			int cost = 0;
			if(!nextTask.get(v).isEmpty()){
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
	
	/**
	 * Method to put a task t in vehicle v at time 0
	 * @param t
	 * 	the task
	 * @param v
	 * 	the vehicle
	 * @return
	 * 	true if the task have been assigned else false
	 */
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
	
	/**
	 * Method to remove the first task assigned to vehicle v
	 * So we first remove the taskclass corresponding to the pickup of the task
	 * And the remove its corresponding delivery.
	 * We assume in this method that vehicle v has at least one task assigned
	 * 
	 * @param v
	 * 	the vehicle
	 * @return
	 * 	the pickup taskclass that have been removed
	 */
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
	
	/**
	 * Method that add taskclass pickup and delivery at time 0 for vehicle v
	 * @param tc
	 * 	the taskclass to be added
	 * @param v
	 * 	the vehicle
	 * @return
	 * 	true if the operation was possible else false (depending on capacity and weight of the task)
	 */
	public boolean addFirst(TaskClass tc, Vehicle v){
		//tc is the pickup entity
		if(tc.getWeight() > v.capacity()){
			return false;
		}
		this.nextTask.get(v).addFirst(new TaskClass(tc.getTask(),TaskClass.delivery));
		this.nextTask.get(v).addFirst(tc);
		return true;
	}
	
	/**
	 * Method that swap the task order inside a vehicle, we take the task at index old and put it at index new
	 * we proced depending if the taskclass at index old is of type pick up or delivery
	 * @param v
	 * 	the vehicle on which the operation is done
	 * @param indexOld
	 * 	the index of the task we will move
	 * @param indexNew
	 * 	the index where the task will be placed
	 * @return
	 * 	true if the operation was possible else false
	 */
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
	
	/**
	 * Method that return the number of vehicle of our solution
	 * @return
	 * 	the number of vehicle
	 */
	public int getNbVehicle(){
		return nextTask.keySet().size();
	}
	
	/**
	 * Method that return number of task class for a given vehicle
	 * @param v
	 * 	the vehicle
	 * @return
	 * 	the number of task class assigned to that vehicle
	 */
	public int getNbTask(Vehicle v){
		return nextTask.get(v).size();
	}
	
	/**
	 * Method that return first taskclass assigned to vehicle v
	 * @param v
	 * 	the vehicle
	 * @return
	 * 	the first task class
	 */
	public TaskClass getFirstTask(Vehicle v){
		return nextTask.get(v).getFirst();
	}
	
	/**
	 * Method that return the plans for each vehicle corresponding to our solution
	 * @param vehicles
	 * 	the vehicles of the solution (this is important to get order lost inside hashmap
	 * @return
	 * 	the plans for each vehicle
	 */
	public List<Plan> computePlan(List<Vehicle> vehicles){
		List<Plan> plans = new ArrayList<Plan>();
		for(Vehicle v : vehicles){
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

	/**
	 * Method to remove a given task from the solution
	 * 
	 * @param previous
	 * 	the task to be removed
	 */
	public void removeTask(Task previous) {
		
		for(Vehicle v : nextTask.keySet()){
			LinkedList<TaskClass> ll = nextTask.get(v);
			for(int i = 0; i< ll.size(); i++){
				if(ll.get(i).sameTask(previous)){
					ll.remove(i);
					if(!ll.isEmpty())
						i = i-1;
					
				}
			}
		}
		
		
	}

	/**
	 * Method useful to convert our solution that contains task with wrong id to solution with same task but new id
	 * This method is used after the auction when we have a given taskset.
	 * @param tasks
	 * 	the task with new id
	 * @return
	 * 	the new solution
	 */
	public CentralizedClass clone(TaskSet tasks) {
		HashMap<Vehicle,LinkedList<TaskClass>> newNextTask = new HashMap<Vehicle,LinkedList<TaskClass>>();
		for(Vehicle v : nextTask.keySet()){
			LinkedList<TaskClass> ll = new LinkedList<TaskClass>();
			for(TaskClass tc : nextTask.get(v)){
				TaskClass t = getTaskFromTaskSet(tc,tasks);
				ll.add(t);
			}
			newNextTask.put(v, ll);
		}
		return new CentralizedClass(newNextTask);
	}

	/**
	 * Method that create new task class corresponding to old one but just we change the reference of the task
	 * this is used in clone just before when we just want to change reference to task of each taskclass
	 * @param tc
	 * 	the old taskclass
	 * @param tasks
	 * 	the new set of task
	 * @return
	 * 	the task class with reference to the new task of taskset
	 */
	private TaskClass getTaskFromTaskSet(TaskClass tc, TaskSet tasks) {
		for(Task t: tasks){
			if(t.id == tc.getTask().id)
				return new TaskClass(t,tc.getType());
		}
		return null;
	}

	public boolean isEmpty() {
		for(Vehicle v : nextTask.keySet()){
			if(!nextTask.get(v).isEmpty())
				return false;
		}
		return true;
	}
}