package edu.turtlekit2.warbot.troll;

import java.util.List;
import java.util.Vector;

import edu.turtlekit2.warbot.WarBrain;
import edu.turtlekit2.warbot.message.WarMessage;
import edu.turtlekit2.warbot.percepts.Percept;
import edu.turtlekit2.warbot.waritems.WarFood;

public class BrainExplorer extends WarBrain{	
	private String etat;
	private int angleBase;
	boolean baseSpoted=false;
	private Vector<Integer> escouade;
	
	
	public BrainExplorer(){
		etat = "init";
		angleBase = -1;
	}
	
	@Override
	public String action() {
		List<Percept> liste = getPercepts();
		List<WarMessage> listeM = getMessage();
		while(isBlocked()){
			setRandomHeading();
		}
		switch (etat){
			case "init":
				return init(liste, listeM);
			case "explore" :
				return explore(liste, listeM);
			case "backBase" :
				return backBase(liste, listeM);
			case "scout":
				return scout(liste, listeM);
			case "escouade":
				return escouade(liste, listeM);
		}
		return "move";
	}
	
	
	public String escouade(List<Percept> _liste, List<WarMessage> _listeM){		
		for (Percept p: _liste){
			if (p.getTeam()!=getTeam() && p.getType().equals("WarRocketLauncher")){
				if (escouade.size()<3){
					String infos[] = new String[2];
					infos[0]=Integer.toString(p.getAngle());
					infos[1]=Integer.toString(p.getDistance());
					broadcastMessage("WarRocketLauncher", "EstTuDispoEscouade", infos);
				}
				if (p.getDistance()>=34){
					this.setHeading(p.getAngle());
					//return "move";
				}
				else if(p.getDistance()<=31){
					this.setHeading(p.getAngle()+180);
					//return "move";
				}
			}
		}
		for (WarMessage m : _listeM){
			if (escouade.size()<3){
				boolean dejaVu=false;
				if(m.getMessage().equals("DispoEscouade")){
					for(Integer i:escouade){
						if (i==m.getSender()){
							dejaVu=true;
						}
					}
					if (!dejaVu){
						escouade.add(m.getSender());
					}
					String infos[] = new String[2];
					infos[0]=Integer.toString(m.getAngle());
					infos[1]=Integer.toString(m.getDistance());
					reply(m, "etatEsouade", infos);
				}
			}
		}
		return "move";
	}
	
	public String init(List<Percept> _liste, List<WarMessage> _listeM){
		
		this.broadcastMessage("WarBase", "needrole", null);
		for (WarMessage m : _listeM){
			if(m.getMessage().equals("role")){
				int role=Integer.parseInt(m.getContent()[0]);
				switch(role){
				case 0:
					etat="explore";
					break;
				case 1:
					etat="scout";
					break;
				}
			}
		}
		return "move";
	}
	
	public String scout(List<Percept> _liste, List<WarMessage> _listeM){
		for(Percept p:_liste){
			if(p.getType().equals("WarRocketLauncher") && p.getTeam()!=getTeam()){
				etat = "escouade";
				escouade=new Vector<Integer>();
				String infos[] = new String[2];
				infos[0]=Integer.toString(p.getAngle());
				infos[1]=Integer.toString(p.getDistance());
				this.broadcastMessage("WarRocketLauncher", "rocket ennemie", infos);
				if (p.getDistance()>=34){
					this.setHeading(p.getAngle());
					return "move";
				}
				else if(p.getDistance()<=31){
					this.setHeading(p.getAngle()+180);
					return "move";
				}
			}
			else if(p.getType().equals("WarBase") && p.getTeam()!=getTeam()){
				String infos[] = new String[2];
				infos[0]=Integer.toString(p.getAngle());
				infos[1]=Integer.toString(p.getDistance());
				this.broadcastMessage("WarRocketLauncher", "base ennemie", infos);
				return "idle";
			}
		}
		return "move";
	}
	
	public String explore(List<Percept> _liste, List<WarMessage> _listeM){
		if (this.fullBag()){
			broadcastMessage("WarBase", "t'es ou", null);
			etat = "backBase";
			return "idle";
		}
		for(Percept p:_liste){
			if(p.getType().equals("WarBase") && p.getTeam()!=getTeam()){
				String infos[] = new String[2];
				infos[0]=Integer.toString(p.getAngle());
				infos[1]=Integer.toString(p.getDistance());
				this.broadcastMessage("WarRocketLauncher", "base ennemie", infos);
				return "scout";
			}
		}
		if (_liste.size()>0){
			for (Percept p : _liste){
				if(p.getType().equals("WarFood")&&p.getDistance()<=WarFood.MAX_DISTANCE_TAKE){
					System.out.println(this.getID());
					return "take";
				}
				if(p.getType().equals("WarFood")){
					setHeading(p.getAngle());
					String infos[] = new String[2];
					infos[0]=Integer.toString(p.getAngle());
					infos[1]=Integer.toString(p.getDistance());
					broadcastMessage("WarBase", "food", infos);
				}
			}
		}
		return "move";
	}
	public String backBase(List<Percept> _liste, List<WarMessage> _listeM){
		if (this.emptyBag()){
			angleBase= -1;
			etat ="explore";
			return "idle";

		}
		if (_liste.size()>0){
			for (Percept p : _liste){
				if(p.getType().equals("WarBase")&&p.getDistance()<=WarFood.MAX_DISTANCE_TAKE){
					setAgentToGive(p.getId());
					return "give";
				}
				if(p.getType().equals("WarBase")){
					setHeading(p.getAngle());
					return "move";
				}
			}
		}
		if (angleBase == -1){
			for (WarMessage m : _listeM){
				if(m.getType().equals("WarBase")){
					angleBase = m.getAngle();
				}
			}
		}
		setHeading(angleBase);
		return "move";
	}
	
	
}
