package template;

import logist.task.Task;
import logist.topology.Topology.City;

/**
 * Class that encapsulate a task and if this task is pickup or delivered 
 * 
 *
 */
public class TaskClass {
	public static final boolean pickup = true;
	public static final boolean delivery = false;
	
	private boolean type;
	private Task task;
	
	/**
	 * Constructor of a taskclass
	 * @param t
	 * 	the task assigned
	 * @param b
	 * 	it's type, pickup or delivery
	 */
	public TaskClass(Task t, boolean b){
		task = t;
		type = b;
	}
	
	/**
	 * Method to get the weight of the task class, negative weight if deliver positive if pickup
	 * @return
	 */
	public int getWeight(){
		if(type == pickup){
			return task.weight;
		}
		else{
			//It's a delivery
			return -task.weight;
		}
	}
	
	/**
	 * Method to get the type of a taskclass
	 * @return
	 * 	pickup or deliver corresponding boolean
	 */
	public boolean getType(){
		return type;
	}
	
	/**
	 * Method to get the pickup city or delivery city depending on type of the taskclass
	 * @return
	 * 	the pickup city or delivery city depending on type of the taskclass
	 */
	public City getCity(){
		if(type == pickup){
			return task.pickupCity;
		}
		else{
			//It's a delivery
			return task.deliveryCity;
		}
	}
	
	/**
	 * Method to get the ecapsulated task inside taskclass
	 * @return
	 * 	the task
	 */
	public Task getTask(){
		return task;
	}
	
	/**
	 * Method to compare taskclass in function of their encapsulated task
	 * @param tc
	 * 	the task class we compare to our
	 * @return
	 * 	true if there are equal, else false
	 */
	public boolean sameTask(TaskClass tc){
		return tc.getTask() == this.task;
	}
	
	/**
	 * Method to compare a task to our taskclass in function of the id of the task
	 * @param tc
	 * 	the task to which we compare our
	 * @return
	 * 	true if they have equal, else false
	 */
	public boolean sameTask(Task tc){
		return tc.id == this.task.id;
	}
	
	@Override
	public String toString(){
		return "Task :"+this.getTask()+" type :"+this.getType();
	}
}