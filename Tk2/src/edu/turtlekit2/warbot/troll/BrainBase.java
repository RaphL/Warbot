package edu.turtlekit2.warbot.troll;

import java.util.List;

import edu.turtlekit2.warbot.WarBrain;
import edu.turtlekit2.warbot.message.WarMessage;
import edu.turtlekit2.warbot.percepts.Percept;

public class BrainBase extends WarBrain{
	private String etat;
	private int nextRole;

	public BrainBase(){
		etat="offensif";
		nextRole=0;
	}

	@Override
	public String action() {
		/*
		if(!emptyBag()){
			return "eat";
		}
		 */
		List<Percept> liste = getPercepts();
		List<WarMessage> listeM = getMessage();
		switch (etat){
			case "defensif":
				return defensif(liste,listeM);
			case "offensif" :
				return offensif(liste,listeM);
		}
		for(WarMessage m : listeM){
			
			reply(m, "ici", null);
		}
		/*
		if(getEnergy() > 12000){
			setNextAgentCreate("Explorer");
			return "create";
		}
		*/
		return "action";
	}

	private String offensif(List<Percept> liste, List<WarMessage> listeM) {
		
		for(WarMessage m : listeM){
			if (m.getType().equals("WarExplorer")){
				if (m.getMessage().equals("t'es ou")){
					reply(m, "ici", null);
				}
				if (m.getMessage().equals("food")){
					//food()
				}
				if (m.getMessage().equals("needrole")){
					int id= m.getSender();
					String infos[] = new String[1];
					infos[0]=Integer.toString(nextRole);
					this.sendMessage(id, "role", infos);
					nextRole=(nextRole+1)%2;
				}
			}
			if (m.getType().equals("WarRocketLauncher")){
				reply(m, "ici", null);
			}
		}
		for(Percept p : liste){
			if (p.getTeam()!= getTeam() && p.getType().equals("WarRocketLauncher")){
				etat = "defensif";
			}///////////LE POP de scout/expl
		}
		return "eat";
	}

	private String defensif(List<Percept> liste, List<WarMessage> listeM) {
		boolean ennemiSpoted=false;
		broadcastMessage("WarRocketLauncher", "backDef", null);
		for(WarMessage m : listeM){
			if (m.getType().equals("WarExplorer")){
				if (m.getMessage().equals("t'es ou")){
					reply(m, "ici", null);
				}
				if (m.getMessage().equals("food")){
					//food()
				}
				if (m.getMessage().equals("needrole")){
					int id= m.getSender();
					String infos[] = new String[1];
					infos[0]=Integer.toString(nextRole);
					this.sendMessage(id, "role", infos);
					nextRole=(nextRole+1)%2;
				}
			}
			if (m.getType().equals("WarRocketLauncher")){
				reply(m, "ici", null);
			}
		}
		for(Percept p : liste){
			if (p.getTeam()!= getTeam() && p.getType().equals("WarRocketLauncher")){
				etat = "defensif";
				ennemiSpoted=true;
			}
		}
		if (!ennemiSpoted){
			etat = "offensif";
		}
		return "eat";
	}
}
