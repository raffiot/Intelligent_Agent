package template;

import java.util.List;

import logist.simulation.Vehicle;
import logist.task.Task;

/**
 * Class that represent an opponent with its task assigned, its possible solution, its vehicle
 * and the cost of its possible solution
 *
 *
 */
public class Opponent {
	private List<Task> tasks;
	private CentralizedClass solution;
	private List<Vehicle> vehicle;
	private double curCost;
	
	/**
	 * The constructor of the opponent
	 * @param tasks
	 * 	the list of task owned by opponent
	 * @param solution
	 * 	the possible solution proposed by the opponent
	 * @param vehicle
	 * 	the vehicles of the opponent
	 */
	public Opponent(List<Task> tasks, CentralizedClass solution,List<Vehicle> vehicle) {
		this.tasks = tasks;
		this.solution = solution;
		this.vehicle = vehicle;
		curCost = 0.;
	}
	
	/**
	 * Method to get the list of task owned by the opponent
	 * @return
	 * 	the list of task
	 */
	public List<Task> getTasks() {
		return tasks;
	}

	/**
	 * Method to obtain a possible solution of the opponent
	 * @return
	 * 	the possible solution
	 */
	public CentralizedClass getSolution() {
		return solution;
	}
	
	/**
	 * Method to set the possible solution of the opponent
	 * @param solution
	 * 	the new possible solution
	 */
	public void setSolution(CentralizedClass solution) {
		this.solution = solution;
	}

	/**
	 * Method to obtain the list of vehicle of the opponent
	 * (in our implementation there will be only one vehicle)
	 * @return
	 * 	the list of vehicle
	 */
	public List<Vehicle> getVehicle() {
		return vehicle;
	}

	/**
	 * Method to set the vehicles of the opponent
	 * @param vehicle
	 * 	the new list of vehicles
	 */
	public void setVehicle(List<Vehicle> vehicle) {
		this.vehicle = vehicle;
	}

	/**
	 * Method to get the possible cost of the solution of the opponent
	 * @return
	 * 	the possible cost
	 */
	public double getCurCost() {
		return curCost;
	}

	/**
	 * Method to get the possible cost of the solution of the opponent
	 * @param curCost
	 * 	the new possible cost
	 */
	public void setCurCost(double curCost) {
		this.curCost = curCost;
	}
	
	/**
	 * Method to add a task to the task list of the opponent
	 * @param t
	 * 	the task to add
	 */
	public void addTask(Task t){
		tasks.add(t);
	}

	@Override
	public String toString() {
		return "Opponent [tasks=" + tasks + ", vehicle=" + vehicle + ", curCost=" + curCost + "]";
	}
	
	
}
