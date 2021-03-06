package template;

import logist.task.Task;
import logist.topology.Topology.City;

public class TaskClass {
	public static final boolean pickup = true;
	public static final boolean delivery = false;
	
	private boolean type;
	private Task task;
	
	public TaskClass(Task t, boolean b){
		task = t;
		type = b;
	}
	
	public int getWeight(){
		if(type == pickup){
			return task.weight;
		}
		else{
			//It's a delivery
			return -task.weight;
		}
	}
	
	public boolean getType(){
		return type;
	}
	
	public City getCity(){
		if(type == pickup){
			return task.pickupCity;
		}
		else{
			//It's a delivery
			return task.deliveryCity;
		}
	}
	
	public Task getTask(){
		return task;
	}
	
	public boolean sameTask(TaskClass tc){
		return tc.getTask() == this.task;
	}
	
	@Override
	public String toString(){
		return "Task :"+this.getTask()+" type :"+this.getType();
	}
}