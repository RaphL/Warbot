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
	private int nbHealer=0;
	private int lifeTarget=9000;
	private int noEsc;//ID espl
	boolean baseSpoted=false;
	private Vector<Integer> escouade;
	
	
	public BrainExplorer(){
		etat = "init";
		angleBase = -1;
		IDtarget = -1;
		noEsc = -1;
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
			case "heal":
				System.out.println("coucou?");
				return heal(liste,listeM);
		}
		return "move";
	}
	
	
	
	
	private String heal(List<Percept> liste, List<WarMessage> listeM) {
		System.out.println("je suis un healer");
		for (Percept p: liste){
			if(!this.emptyBag() && p.getType().equals("WarBase")&&p.getDistance()<=WarFood.MAX_DISTANCE_TAKE){
				setAgentToGive(p.getId());
				return "give";
			}
			
			if(p.getType().equals("WarFood")&&p.getDistance()<=WarFood.MAX_DISTANCE_TAKE){
				return "take";
			}
			if(!this.fullBag() && p.getType().equals("WarFood")){
				setHeading(p.getAngle());
			}
		}

		for (WarMessage m : listeM){
			if (!this.emptyBag() && m.getMessage().equals("needHeal")){
				for (int i=escouade.get(0);i<escouade.size();i++){//je repond seulement a l'escouade
					if (m.getSender()==escouade.get(i)){
						String[] life = m.getContent();
						if (Integer.parseInt(life[0])<lifeTarget){
							lifeTarget=Integer.parseInt(life[0]);
							IDtarget=m.getSender();
							this.setHeading(m.getAngle());
						}
					}
				}
				if (this.fullBag() && this.getEnergy()<200){
					return "eat";
				}
			}
		}
		refresh("heal");
		return "move";
	}

	

	public String escouade(List<Percept> _liste, List<WarMessage> _listeM){
		for (WarMessage m : _listeM){
			if (escouade.size()<5){
				if(nbHealer<1 && m.getMessage().equals("DispoHealer")){
					escouade.add(m.getSender());
					String infos[] = new String[1];
					infos[0]=Integer.toString(noEsc);
					reply(m, "etatHealer", infos);
					nbHealer++;
				}
				if(m.getMessage().equals("DispoEscouade")){
					escouade.add(m.getSender());
					String infos[] = new String[1];
					infos[0]=Integer.toString(noEsc);
					reply(m, "etatEscouade", infos);
				}
			}
			if (escouade.size()<6){
				if(nbHealer<1 && m.getMessage().equals("DispoHealer")){
					escouade.add(m.getSender());
					String infos[] = new String[1];
					infos[0]=Integer.toString(noEsc);
					reply(m, "etatHealer", infos);
					nbHealer++;
				}
			}
		}
		
		if (escouade.size()<6){
			if (nbHealer<1){
				broadcastMessage("WarExplorer", "needHealer", null);
			}
			broadcastMessage("WarRocketLauncher", "EstTuDispoEscouade", null);
		}
		
		for (Percept p: _liste){
			if (p.getId()==IDtarget){
				
				String infos[] = new String[2];
				infos[0]=Integer.toString(p.getAngle());
				infos[1]=Integer.toString(p.getDistance());
				 
				if (escouade.size()>0){
					for (int i=0;i<escouade.size();i++){
						//System.out.println("Explorer:"+this.getID()+" demande tir à RocketLauncher:"+escouade.get(i));
						sendMessage(escouade.get(i), "ennemiHere", infos);
						//System.out.println("Explorer:"+this.getID()+" message envoyé");
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
	

	
	public String scout(List<Percept> _liste, List<WarMessage> _listeM){
		for(Percept p:_liste){
			if(p.getType().equals("WarRocketLauncher") && p.getTeam()!=getTeam()){
				etat = "escouade";
				IDtarget=p.getId();
				escouade=new Vector<Integer>();
				noEsc=getID();
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
		}
		return "move";
	}
	
	public String explore(List<Percept> _liste, List<WarMessage> _listeM){
		for (WarMessage m : _listeM){
			if(m.getMessage().equals("retourRapide")){
				alert=1;
			}
			if(m.getMessage().equals("etatHeal")){
				String content[] = m.getContent();
				int esc=Integer.parseInt(content[0]);
				noEsc=esc;
				etat = "heal";
				this.setHeading(m.getAngle());
				return "move";
			}
			if(m.getMessage().equals("needHealer")){
				if (canHeal()){
					reply(m,"DispoHealer",null);
				}
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
			if(p.getType().equals("WarFood")){
				setHeading(p.getAngle());
				String infos[] = new String[2];
				infos[0]=Integer.toString(p.getAngle());
				infos[1]=Integer.toString(p.getDistance());
				broadcastMessage("WarBase", "food", infos);
			}
			if(p.getType().equals("WarBase") && p.getTeam()!=getTeam()){
				String infos[] = new String[2];
				infos[0]=Integer.toString(p.getAngle());
				infos[1]=Integer.toString(p.getDistance());
				this.broadcastMessage("WarRocketLauncher", "base ennemie", infos);
				return "scout";
			}
			if(p.getType().equals("WarFood")&&p.getDistance()<=WarFood.MAX_DISTANCE_TAKE){
				return "take";
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
	
	private void refresh(String etat) {
		switch (etat){
			case "heal" :
				String infos[] = new String[2];
				infos[0]=etat;
				infos[1]=Integer.toString(noEsc);
				this.broadcastMessage("WarBase", "refresh", infos);
				break;
			case "scout":
				this.broadcastMessage("WarBase", "refresh", null);
				break;
			case "explore" :
				this.broadcastMessage("WarBase", "refresh", null);
				break;
			case "escouade":
				this.broadcastMessage("WarBase", "refresh", null);
				break;
			case "backBase" :
				this.broadcastMessage("WarBase", "refresh", null);
				break;
		}		
	}
	
	private boolean canHeal() {
		return (alert == 0);
	}

	
	
	
}
