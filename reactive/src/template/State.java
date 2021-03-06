package template;

import logist.topology.Topology.City;

public class State {
	private City from;
	private City to;
	
	public State(City f, City t){
		from = f;
		to = t;
	}

	public City getFrom() {
		return from;
	}

	public void setFrom(City from) {
		this.from = from;
	}

	public City getTo() {
		return to;
	}

	public void setTo(City to) {
		this.to = to;
	}
	
	public boolean haveDestination(){
		return to != null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
		return result;
	}

	
	
	public boolean equals1(City f, City t) {
		
		if (f == null){
			return false;
		}
		else if(f.id == from.id){
			if(t == null && to != null || t != null && to == null || (t!=null && to!=null && t.id != to.id)){
				return false;
			}
			else{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString(){
		String s = "from : "+from.name;
		if(this.haveDestination()){
			s+=" to : "+to.name;
		}
		else{
			s+=" to : no destination";
		}
		return s;
	}
	
	
}
