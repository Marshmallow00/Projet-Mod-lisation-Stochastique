package ept.ssj.projet;

public class Call {
	Client client;
	Agent agent;
	double tempsDattente;
	
	public Call(Client client, Agent agent) {
		this.client = client;
		this.agent = agent;
	}
};

//enlever classe client
//initialiser agent a null