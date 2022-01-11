package ept.ssj.projet;

import java.util.LinkedList;

import umontreal.ssj.charts.HistogramChart;
import umontreal.ssj.randvar.ExponentialGen;
import umontreal.ssj.randvar.RandomVariateGen;
import umontreal.ssj.rng.MRG32k3a;
import umontreal.ssj.simevents.Accumulate;
import umontreal.ssj.simevents.Event;
import umontreal.ssj.simevents.Sim;
import umontreal.ssj.stat.Tally;
import umontreal.ssj.stat.TallyStore;

public class Projet {
	
	//gï¿œnï¿œrateurs d'arrivï¿œe et de service
	RandomVariateGen genArr1;
	RandomVariateGen genServ11;
	RandomVariateGen genServ12;
	RandomVariateGen waitingTime1;
	
	RandomVariateGen genArr2;
	RandomVariateGen genServ21;
	RandomVariateGen genServ22;
	RandomVariateGen waitingTime2;
	//file d'attente des clients de type1
	LinkedList<Client> waitList1 = new LinkedList<Client> ();
    Tally custWaits1     = new Tally ("Waiting times1");
    Accumulate totWait1  = new Accumulate ("Size of queue1");
    
  
    //file d'attente des clients de type2
    LinkedList<Client> waitList2 = new LinkedList<Client> ();
    Tally custWaits2     = new Tally ("Waiting times 2");
    Accumulate totWait2  = new Accumulate ("Size of queue2");
	
	//listes des services de type1 et type2
    LinkedList<Client> servList1 = new LinkedList<Client> ();
    LinkedList<Client> servList2 = new LinkedList<Client> ();
    
    //Liste de tous les agents
    LinkedList<Agent> agentsEnService1 = new LinkedList<Agent>();
    LinkedList<Agent> agentsEnService2 = new LinkedList<Agent>();
    
    //agents libres de type1 et type2
    LinkedList<Agent> agentsLibres1 = new LinkedList<Agent>();
    LinkedList<Agent> agentsLibres2 = new LinkedList<Agent>();
    
    //liste des appels pris
    LinkedList<Call> appels = new LinkedList<Call>(); 
    static TallyStore tauxDoccupation1 = new TallyStore("Taux d'occupation moyen des agents de types 1");
    static TallyStore tauxDoccupation2 = new TallyStore("Taux d'occupation moyen des agents de types 2");
    int G120,G220;
    int L1,L2;
    Tally g1 = new Tally("Nombre espéré d'appels de type 1 répondus en moins de s secondes");
    Tally g2 = new Tally("Nombre espéré d'appels de type 2 répondus en moins de s secondes");
    Tally l1 = new Tally("nombre espéré d'abandons de type 1");
    Tally l2 = new Tally("nombre espéré d'abandons de type 2");
    
    
        
    public void verifCall(Call call) {
    	if (call.tempsDattente<20) {
    		if(call.client.type==1) {
    			G120++;
    			
    		
    		}
    		else {
    			G220++;
    		}
    	}
    }
    
    public void initVariables() {
    	System.out.println(agentsLibres2.size()+" - "+ agentsEnService2.size());
    	waitList1.clear();
    	waitList2.clear();
    	servList1.clear();
    	servList2.clear();
    	agentsLibres1.addAll(agentsEnService1);
    	agentsLibres2.addAll(agentsEnService2);
    	agentsEnService1.clear();
    	agentsEnService2.clear();
    	System.out.println(agentsLibres2.size()+" - "+ agentsEnService2.size());
    	appels.clear();
    	
    }
    
    public Projet(double lambda1,double lambda2, 
    		double mu11, double mu12, 
    		double mu21, double mu22,
    		double nu1,double nu2,
    		int a1, int a2) {
    	
    	for (int i = 0; i <  a1; i++) {
         	Agent agent = new Agent(1);
         	agentsLibres1.add(agent);
         	
         }
         for (int j = 0; j < a2 ; j++) {
         	Agent agent2 = new Agent(2);
         	agentsLibres2.add(agent2);
         }
    	
    	 genArr1 = new ExponentialGen (new MRG32k3a(), lambda1);
         genServ11 = new ExponentialGen (new MRG32k3a(), mu11);
         genServ12 = new ExponentialGen (new MRG32k3a(), mu12);
         genArr2 = new ExponentialGen (new MRG32k3a(), lambda2);
         genServ21 = new ExponentialGen (new MRG32k3a(), mu21);
         genServ22 = new ExponentialGen (new MRG32k3a(), mu22);
         
         waitingTime1 = new ExponentialGen (new MRG32k3a(), nu1);	
         waitingTime2 =  new ExponentialGen (new MRG32k3a(), nu2);
         
    }
    //public static int g1, g2;
    //public static int l1, l2;
    
    class Arrival extends Event{
		int type;
		public Arrival(int t){type=t;}
		  
		public void actions() {
	      if(type==1)
	      { 
	    	  new Arrival(1).schedule(genArr1.nextDouble()); // Next arrival.
	    	  Client cust = new Client();  // Cust just arrived.
	    	  cust.arrivTime = Sim.time();
	    	  cust.patienceTime = waitingTime1.nextDouble();
	    	  cust.type=1;
		      if (agentsLibres1.size()==0 && agentsLibres2.size()==0) { 
		         waitList1.addLast (cust);
		         totWait1.update (waitList1.size());
		         
		      } 
		      //else if(agentsLibres1.isEmpty() && !agentsLibres2.isEmpty()) {
		       else if(agentsLibres1.size()==0 && agentsLibres2.size()>0) {
		    	  Agent agent = agentsLibres2.removeFirst();
		    	  Call call= new Call(cust, agent);
		    	  call.tempsDattente = Sim.time()-call.client.arrivTime;		    	  
		    	  agentsEnService2.add(agent);
		    	  double st = genServ12.nextDouble();
		    	  cust.servTime = st;
		    	  verifCall(call);
		    	  custWaits2.add (0.0);
			      servList2.addLast(cust);
			      new Departure(2, call).schedule(cust.servTime);
		      }
		      
		      else if(agentsLibres1.size()>0) {    
		    	 Agent agent = agentsLibres1.removeFirst();
		    	 Call call= new Call(cust, agent);
		    	 call.tempsDattente = Sim.time()-call.client.arrivTime;
		    	 agentsEnService1.addLast(agent);
		    	 double st = genServ11.nextDouble();
		    	 cust.servTime = st;
		    	 verifCall(call);
		    	 custWaits1.add (0.0);
		         servList1.addLast(cust);
		         new Departure(1, call).schedule(cust.servTime);
		 
		      }
	      }
	   if(type==2)
	      { 
	    	  new Arrival(2).schedule (genArr2.nextDouble()); // Next arrival.
		      Client cust = new Client();  // Cust just arrived.
		      cust.arrivTime = Sim.time();
		      cust.patienceTime = waitingTime2.nextDouble();
		      cust.type=2;
		      if (agentsLibres1.isEmpty() && agentsLibres2.isEmpty()) {       // Must join the queue.
			         waitList2.addLast (cust);
			         totWait2.update (waitList2.size());
			   }
		       
		       else if(!agentsLibres1.isEmpty() && agentsLibres2.isEmpty()) {
		    	   Agent agent = agentsLibres1.removeFirst();
		    	   Call call= new Call(cust, agent);
		    	   call.tempsDattente = Sim.time()-call.client.arrivTime;
		    	   verifCall(call);
			       cust.servTime = genServ21.nextDouble();
			       servList1.addLast (cust);
			       agentsEnService1.addLast(agent);
			       custWaits1.add (0.0);
			       new Departure(1, call).schedule(cust.servTime);
		       }
		       else {                         // Starts service.
		    	   Agent agent = agentsLibres2.removeFirst();
		    	   Call call= new Call(cust, agent);
		    	   call.tempsDattente = Sim.time()-call.client.arrivTime;
				   cust.servTime = genServ22.nextDouble();
				   verifCall(call);
			       servList2.addLast (cust);
			       agentsEnService2.addLast(agent);
			       custWaits2.add (0.0);
			       new Departure(2, call).schedule(cust.servTime);
		        }
	      }  
		}
	 }
    
		
// Departure
class Departure extends Event {
	
	int type;
	Call call;
	public Departure(int t, Call call){
		this.call = call;
		type = t;
		call.agent.OccupationTimeJ += call.client.servTime; 
	}
	
	public void actions() {
		if( type == 1) {
			Client client = call.client;
			Agent agent = call.agent;
			servList1.remove(client);
			agentsEnService1.remove(agent);
			agentsLibres1.addLast(agent);
			
			Client cust = getClient(1);			
			if(cust!=null)
	        { 
	        	servList1.addLast(cust);
	        	Call call= new Call(cust, agent);
	        	call.tempsDattente = Sim.time()-call.client.arrivTime;
		    	verifCall(call);
		    	Agent a= agentsLibres1.removeFirst();
				agentsEnService1.addLast(a);
	            new Departure(1, call).schedule (cust.servTime);
	        }
			
			else {
				cust = getClient(2);
				if(cust != null) {
		        	servList1.addLast(cust);
		        	Call call= new Call(cust, agent);
		        	call.tempsDattente = Sim.time()-call.client.arrivTime;
			    	verifCall(call);
			    	Agent a= agentsLibres1.removeFirst();
					agentsEnService1.addLast(a);
		            new Departure(1, call).schedule (cust.servTime);
				 }
			}
		}
		
	  if(type==2) {
			Client client = call.client;
			Agent agent = call.agent;
			servList2.remove(client);
			agentsEnService2.remove(agent);
			agentsLibres2.addLast(agent);
			Client cust = getClient(2);
			if(cust!=null)
	        { 
	        	servList2.addLast (cust);
	        	Call call= new Call(cust, agent);
	        	call.tempsDattente = Sim.time()-call.client.arrivTime;
		    	verifCall(call);
		    	Agent a= agentsLibres2.removeFirst();
				agentsEnService2.addLast(a);
	            new Departure(2, call).schedule (cust.servTime);
	        }
			else {
				cust = getClient(1);
				if(cust != null) {
		        	servList2.addLast(cust);
		        	Call call= new Call(cust, agent);
		        	call.tempsDattente = Sim.time()-call.client.arrivTime;
			    	verifCall(call);
			    	Agent a = agentsLibres2.removeFirst();
					agentsEnService2.addLast(a);
		            new Departure(2, call).schedule (cust.servTime);
				}
			
			}
		}
 }
      
     /*cette methode retourne le client qui doit entrer en service
      ou null dans le cas ou tous les clients dans la file sont des client
      qui ont abandonnÃ©e*/
	public Client getClient(int type){
		Client cust=null;
			if(type ==1 ) {
				if (!waitList1.isEmpty()) {
					do{
							cust=waitList1.removeFirst();
							if(sim.time()-cust.arrivTime>cust.patienceTime)
								L1++;
								
					}
					while((sim.time()-cust.arrivTime>cust.patienceTime) && waitList1.size() > 0);
					//if(waitList1.size() > 0)
					if((sim.time()-cust.arrivTime)<=cust.patienceTime)
						return cust;
				}
			}//remove les clients qui ont abandonné
			
			if(type==2) {	
				if(!waitList2.isEmpty()) {
					do{
							cust=waitList2.removeFirst();							
							if(sim.time()-cust.arrivTime>cust.patienceTime)
								L2++;
					}
					while(sim.time()-cust.arrivTime>cust.patienceTime && waitList2.size() > 0);
				//	if(waitList2.size() > 0)
				    if((sim.time()-cust.arrivTime)<=cust.patienceTime)	
						return cust;
				}
					
			}
			return null;
	}
	}

int i = 0;

public class EndOfSim extends Event {
	public void actions() {
        Sim.stop();
        g1.add(G120);
		g2.add(G220);
		l1.add(L1);
		l2.add(L2);
		
		initVariables();
		
		System.out.println("end day "+ i++);
		for (Agent agent : agentsLibres1) {
			agent.OccupationTime.add(agent.OccupationTimeJ/28800.0);
		}
		for (Agent agent : agentsLibres2) {
			agent.OccupationTime.add(agent.OccupationTimeJ/28800.0);
		}
		
		G120=0;
		G220=0;
		L1=0;
		L2=0;
		
		for (Agent agent : agentsLibres1) {
			agent.OccupationTimeJ = 0.0;
		}
		
		
		
    	for (Agent agent : agentsLibres2) {
			agent.OccupationTimeJ = 0.0;
		}
    	
		
		
     }
}
	public void simulateOneDay (double timeHorizon) {
		 
	  Sim.init();
	  new EndOfSim().schedule (timeHorizon);
	  new Arrival(1).schedule (genArr1.nextDouble());
	  new Arrival(2).schedule (genArr2.nextDouble());
	  Sim.start();
	  
	
	}
	
	public void simulateNDay(int n,double timeHorizon) {
		for(int i=0; i<n;i++) {
			simulateOneDay(timeHorizon);
		}
	}
	
	
	public static void main (String[] args) {
	    	
			double mu11=0.2;
			double mu12 = 0.15;
			double lambda1= 6;
			double lambda2=0.6;
			double nu1 = 0.12;
			double nu2 = 0.24;
			double mu21= 0.14;
			double mu22 = 0.18;
			int agents1 = 31;
			int agents2 = 6;
			
			Projet queue = new Projet(lambda1, lambda2, 
						mu11, mu12 
						,mu21, mu22, 
						nu1, nu2,
						agents1, agents2);
			queue.simulateNDay (1000,28800.0);
			/*System.out.println (queue.custWaits1.report());
			System.out.println (queue.totWait1.report());
			System.out.println (queue.custWaits2.report());
			System.out.println (queue.totWait2.report());*/
			
			
			System.out.println (queue.g1.report());
			System.out.println (queue.g2.report());
			System.out.println (queue.l1.report());
			System.out.println (queue.l2.report());
			System.out.println("Agent type 1 n 1");
			System.out.println(queue.agentsLibres1.get(0).OccupationTime.report());
			System.out.println("Agent type 2 n 1");
			System.out.println(queue.agentsLibres2.get(0).OccupationTime.report());
			System.out.println("size "+queue.agentsLibres1.size());
			
			for (Agent agent : queue.agentsLibres1) {
				tauxDoccupation1.add(agent.OccupationTime.average());
			}
			for (Agent agent : queue.agentsLibres2) {
				tauxDoccupation2.add(agent.OccupationTime.average());
			}
			System.out.println(tauxDoccupation2.report());
			
			HistogramChart hist1 = new HistogramChart("Histogramme pour les agents de TYPE I pour 50 jours",
	                "AgentsI", "Taux d'occupation", tauxDoccupation1.getArray());
			hist1.view(600, 600);
			System.out.println("donnees 1 "+tauxDoccupation1.getArray());
			for (Double e : tauxDoccupation1.getArray()) {
				System.out.println(e*100);
			}
			HistogramChart hist2 = new HistogramChart("Histogramme pour les agents de TYPE II pour 50 jours",
	                "Agents II ", "Taux d'occupation", tauxDoccupation2.getArray());
			hist2.view(600, 600);
			System.out.println("donnees 2 "+tauxDoccupation2.getArray().length);
			
	}
}
