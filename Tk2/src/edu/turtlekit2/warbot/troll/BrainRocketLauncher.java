package edu.turtlekit2.warbot.troll;

import java.util.List;

import edu.turtlekit2.warbot.WarBrain;
import edu.turtlekit2.warbot.message.WarMessage;
import edu.turtlekit2.warbot.percepts.Percept;

public class BrainRocketLauncher extends WarBrain{
	
	private String etat;
	
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
		}
		for(WarMessage m:listeM){
			if (m.getMessage().equals("etatEsouade")){
				etat ="escouad";
				setHeading(m.getAngle());
				return "move";
			}
			if (m.getMessage().equals("EstTuDispoEscouade")){
				if (evalReq()){
					reply(m,"DispoEscouade",null);
				}
			}
			if(m.getMessage().equals("backDef")){
				if (m.getDistance()<300){
					etat = "backDef";
					//setHeading(m.getAngle());
					//return "move";
				}
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
