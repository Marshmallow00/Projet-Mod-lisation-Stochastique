package ept.ssj.projet;

import umontreal.ssj.stat.Tally;

public class Agent {
	//Call call;
	int type;
	double OccupationTimeJ;
	Tally OccupationTime = new Tally("Taux d'occupation pendant les 1000 jours");
	public Agent(int type) {
		//this.call = call;
		this.type = type;
	}
	
}
