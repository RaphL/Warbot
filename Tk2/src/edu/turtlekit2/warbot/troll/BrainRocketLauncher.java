package edu.turtlekit2.warbot.troll;

import java.util.List;
import java.util.Vector;

import edu.turtlekit2.warbot.WarBrain;
import edu.turtlekit2.warbot.message.WarMessage;
import edu.turtlekit2.warbot.percepts.Percept;
import edu.turtlekit2.warbot.waritems.WarFood;

public class BrainRocketLauncher extends WarBrain{
	
	private String etat;
	private Vector<Integer> Rocketescouade;
	private Vector<Integer> Healescouade;
	
	public BrainRocketLauncher(){
		etat = "explore";
	}
	
	
	
	@Override
	public String action() {
		while(isBlocked()){
			setRandomHeading();
		}
		
		List<Percept> listeP = getPercepts();
		List<WarMessage> listeM = getMessage();
		
		if(!this.emptyBag()&&this.getEnergy()<8000-200){
			return "eat";
		}
		
		if(!isReloaded()){
			if(!isReloading()){
				return "reload";
			}
		}
		
		switch(etat){
			case "backDef":
				return backDef(listeP, listeM);
			case "explore":
				return explore(listeP, listeM);
			case "escouade":
				return escouad(listeP, listeM);
		}
		return "move";
	}
	
	private String escouad(List<Percept> listeP, List<WarMessage> listeM) {
		if (this.getEnergy()<8000){
			String infos[] = new String[1];
			infos[0]=Integer.toString(this.getEnergy());
			this.broadcastMessage("WarExplorer", "needHeal", infos);
		}
		for(WarMessage m:listeM){
			if(m.getMessage().equals("backDef")){
				if (m.getDistance()<300){
					etat = "backDef";
					//setHeading(m.getAngle());
					//return "move";
				}
			}
			if(m.getMessage().equals("ennemiHere")){
				//System.out.println("RocketLauncher:"+this.getID()+" reception cible exploreur:"+m.getSender());
				int angleEnnemi =Integer.parseInt(m.getContent()[0]);
				int distanceEnnemi =Integer.parseInt(m.getContent()[1]);
				int angleAllie=m.getAngle();
				int distanceAllie=m.getDistance();
				int n = getAngleTarget(angleAllie, distanceAllie, angleEnnemi, distanceEnnemi);
				this.setHeading(n);
				this.setAngleTurret(n);
				if (m.getDistance()<=20){//duree de vie d'une rocket
					return "fire";
				}
			}
		}
		for(Percept p:listeP){
			if(p.getType().equals("WarBase") && p.getTeam()!=getTeam()){
				this.setAngleTurret(p.getAngle());
				return "fire";
			}
			if(p.getType().equals("WarRocketLauncher") && p.getTeam()!=getTeam()){
				this.setAngleTurret(p.getAngle());
				return "fire";
			}
		}
		
		return "move";
	}



	public String backDef(List<Percept> listeP, List<WarMessage> listeM){
		for(Percept p:listeP){
			if(p.getType().equals("WarRocketLauncher") && p.getTeam()!=getTeam()){
				this.setAngleTurret(p.getAngle());
				return "fire";
			}
			if(p.getType().equals("WarBase") && p.getTeam()!=getTeam()){
				this.setAngleTurret(p.getAngle());
				return "fire";
			}
		}
		for(WarMessage m:listeM){
			if(m.getMessage().equals("backDef")){
				setHeading(m.getAngle());
				return "move";
			}
			if(m.getMessage().equals("base ennemie")){
				int angleEnnemi =Integer.parseInt(m.getContent()[0]);
				int distanceEnnemi =Integer.parseInt(m.getContent()[1]);
				int angleAllie=m.getAngle();
				int distanceAllie=m.getDistance();
				int n = getAngleTarget(angleAllie, distanceAllie, angleEnnemi, distanceEnnemi);
				this.setHeading(n);
				return "move";
			}
		}
		return "move";
	}
	
	public String explore(List<Percept> listeP, List<WarMessage> listeM){
		for(Percept p:listeP){
			if(p.getType().equals("WarBase") && p.getTeam()!=getTeam()){
				this.setAngleTurret(p.getAngle());
				return "fire";
			}
			if(p.getType().equals("WarRocketLauncher") && p.getTeam()!=getTeam()){
				this.setAngleTurret(p.getAngle());
				return "fire";
			}
			if(!this.fullBag() && p.getType().equals("WarFood")&&p.getDistance()<=WarFood.MAX_DISTANCE_TAKE){
				return "take";
			}
			if (!this.fullBag() && p.getType().equals("WarFood")){
				this.setHeading(p.getAngle());
			}
		}
		for(WarMessage m:listeM){
			if (m.getMessage().equals("etatEscouade")){
				//System.out.println("RocketLauncher:"+this.getID()+" confirmation escouade exploreur:"+m.getSender());
				Rocketescouade=new Vector<Integer>();
				Healescouade=new Vector<Integer>();
				/*
				String content[] = m.getContent();
				int nbRockets=Integer.parseInt(content[0]);
				for (int i=1; i<nbRockets+1;i++){
					Rocketescouade.add(Integer.parseInt(content[i]));
				}
				for (int i=nbRockets+1; i<content.length;i++){
					Healescouade.add(Integer.parseInt(content[i]));
				}
				*/
				etat ="escouade";
				setHeading(m.getAngle());
				return "move";
			}
			if (m.getMessage().equals("EstTuDispoEscouade")){
				//System.out.println("RocketLauncher:"+this.getID()+" reception appel exploreur:"+m.getSender());
				if (evalReq()){
					//System.out.println("je suis dispo");
					reply(m,"DispoEscouade",null);
				}else{
					//System.out.println("je suis pas dispo");
				}
			}
			if(m.getMessage().equals("backDef")){
				if (m.getDistance()<300){
					etat = "backDef";
					//setHeading(m.getAngle());
					//return "move";
				}
			}
			/*
			if(m.getMessage().equals("base ennemie")){
				int angleEnnemi =Integer.parseInt(m.getContent()[0]);
				int distanceEnnemi =Integer.parseInt(m.getContent()[1]);
				int angleAllie=m.getAngle();
				int distanceAllie=m.getDistance();
				int n = getAngleTarget(angleAllie, distanceAllie, angleEnnemi, distanceEnnemi);
				this.setHeading(n);
				return "move";
			}
			*/
			/*
			if (m.getMessage().equals("rocket ennemie")){
				int angleEnnemi =Integer.parseInt(m.getContent()[0]);
				int distanceEnnemi =Integer.parseInt(m.getContent()[1]);
				int angleAllie=m.getAngle();
				int distanceAllie=m.getDistance();
				int n = getAngleTarget(angleAllie, distanceAllie, angleEnnemi, distanceEnnemi);
				this.setHeading(n);
				this.setAngleTurret(n);
				if (m.getDistance()<=20){//duree de vie d'une rocket
					return "fire";
				}
			}
			*/
		}
		return "move";
	}
	
	private boolean evalReq() {
		if (etat=="escouade"){
			return false;
		}else return true;

	}



	private double[] polToCart(int angle, int distance){
		double[] coord = new double[2];
		double radian = Math.PI*angle/180;
		coord[0]=Math.cos(radian)*distance;
		coord[1]=Math.sin(radian)*distance;
		return coord;
	}
	
private int getAngleTarget(int angle1,int distance1,int angle2,int distance2){
	double[] coord1 = polToCart(angle1,distance1);
	double[] coord2 = polToCart(angle2,distance2);
	double radian = Math.atan2(coord1[1]+coord2[1], coord1[0]+coord2[0]);
	return (int) Math.toDegrees(radian);
	
}
	
}
