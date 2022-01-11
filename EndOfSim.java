package ept.ssj.projet;

import umontreal.ssj.simevents.Event;
import umontreal.ssj.simevents.Sim;

public class EndOfSim extends Event {
	public void actions() {
        Sim.stop();
     }
}