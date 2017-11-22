package template;

import java.util.List;

import logist.simulation.Vehicle;
import logist.task.Task;

public class Opponent {
	private List<Task> tasks;
	private CentralizedClass solution;
	private List<Vehicle> vehicle;
	private double curCost;
	
	
	public Opponent(List<Task> tasks, CentralizedClass solution,List<Vehicle> vehicle) {
		this.tasks = tasks;
		this.solution = solution;
		this.vehicle = vehicle;
		curCost = 0.;
	}
	
	public List<Task> getTasks() {
		return tasks;
	}
	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}
	public CentralizedClass getSolution() {
		return solution;
	}
	public void setSolution(CentralizedClass solution) {
		this.solution = solution;
	}

	public List<Vehicle> getVehicle() {
		return vehicle;
	}

	public void setVehicle(List<Vehicle> vehicle) {
		this.vehicle = vehicle;
	}

	public double getCurCost() {
		return curCost;
	}

	public void setCurCost(double curCost) {
		this.curCost = curCost;
	}
	
	public void addTask(Task t){
		tasks.add(t);
	}

	@Override
	public String toString() {
		return "Opponent [tasks=" + tasks + ", vehicle=" + vehicle + ", curCost=" + curCost + "]";
	}
	
	
}
