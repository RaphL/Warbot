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
	private int IDtarget;
	private int alert=0;
	boolean baseSpoted=false;
	private Vector<Integer> escouade;
	
	
	public BrainExplorer(){
		etat = "init";
		angleBase = -1;
		IDtarget = -1;
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
		
		for (WarMessage m : _listeM){
			if (escouade.size()<6){
				if(m.getMessage().equals("DispoEscouade")){
					escouade.add(m.getSender());
					reply(m, "etatEscouade", null);
				}
			}
		}
		
		if (escouade.size()<6){
			broadcastMessage("WarRocketLauncher", "EstTuDispoEscouade", null);
		}
		
		for (Percept p: _liste){
			if (p.getId()==IDtarget){
				
				String infos[] = new String[2];
				infos[0]=Integer.toString(p.getAngle());
				infos[1]=Integer.toString(p.getDistance());
				 
				if (escouade.size()>0){
					for (int i=0;i<escouade.size();i++){
						System.out.println("Explorer:"+this.getID()+" demande tir à RocketLauncher:"+escouade.get(i));
						sendMessage(escouade.get(i), "ennemiHere", infos);
						System.out.println("Explorer:"+this.getID()+" message envoyé");
					}
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
				IDtarget=p.getId();
				escouade=new Vector<Integer>();
				/*
				String infos[] = new String[2];
				infos[0]=Integer.toString(p.getAngle());
				infos[1]=Integer.toString(p.getDistance());
				this.broadcastMessage("WarRocketLauncher", "rocket ennemie", infos);
				*/
				if (p.getDistance()>=34){
					this.setHeading(p.getAngle());
					return "move";
				}
				else if(p.getDistance()<=31){
					this.setHeading(p.getAngle()+180);
					return "move";
				}else{
					return "idle";
				}
			}
			/*
			else if(p.getType().equals("WarBase") && p.getTeam()!=getTeam()){
				String infos[] = new String[2];
				infos[0]=Integer.toString(p.getAngle());
				infos[1]=Integer.toString(p.getDistance());
				this.broadcastMessage("WarRocketLauncher", "base ennemie", infos);
				return "idle";
			}
			*/
		}
		return "move";
	}
	
	public String explore(List<Percept> _liste, List<WarMessage> _listeM){
		for (WarMessage m : _listeM){
			if(m.getMessage().equals("retourRapide")){
				alert=1;
			}
			if(m.getMessage().equals("retourEco")){
				alert=0;
			}
		}
		if ((alert==0&&this.fullBag())||(alert==1&&!this.emptyBag())){
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
