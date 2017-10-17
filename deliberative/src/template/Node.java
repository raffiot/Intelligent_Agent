package template;

public class Node {
	double weight; //Actual weight of the truck
	double cost; //Cost up to that task
	boolean type; //false = pick, true = delivery
	
	public Node(double weight, double cost, boolean type) {
		this.weight = weight;
		this.cost = cost;
		this.type = type;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public boolean isType() {
		return type;
	}

	public void setType(boolean type) {
		this.type = type;
	}


}
