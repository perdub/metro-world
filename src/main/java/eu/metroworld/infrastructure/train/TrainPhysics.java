package eu.metroworld.infrastructure.train;
/** SI units; controller state belongs to the train, never to its open screen. */
public final class TrainPhysics {
 public boolean power,compressor,leftDoors,rightDoors,emergency,lights=true;
 public int direction=1,traction=0,brake=7;
 public double speed,pressure=0,position;
 public void tick(double seconds,double min,double max,double grade){
  if(!Double.isFinite(seconds)||seconds<=0||seconds>.25)throw new IllegalArgumentException("tick interval");
  pressure=Math.max(0,Math.min(8,pressure+seconds*(power&&compressor?.8:-.01)));
  boolean ready=power&&pressure>=4&&!leftDoors&&!rightDoors&&!emergency&&direction!=0;
  double effort=ready&&brake==0?Math.max(0,Math.min(5,traction))*.32*direction:0;
  double stop=leftDoors||rightDoors?2.5:emergency?3.2:pressure<2?1.5:brake*.24;
  double resistance=Math.abs(speed)<.001?0:Math.copySign(.035+speed*speed*.0005,speed);
  double next=speed+(effort-resistance-grade*9.81)*seconds;
  if(stop>0){double delta=stop*seconds;next=Math.abs(next)<=delta?0:next-Math.copySign(delta,next);}
  speed=Math.max(-12,Math.min(12,next));position+=speed*seconds;
  if(position<=min){position=min;if(speed<0){speed=0;traction=0;brake=7;}}
  if(position>=max){position=max;if(speed>0){speed=0;traction=0;brake=7;}}
 }
 public boolean stopped(){return Math.abs(speed)<.05;}
 public boolean reverse(){if(!stopped()||brake==0)return false;direction=direction==1?0:direction==0?-1:1;return true;}
 public boolean doors(boolean left){if(!stopped())return false;if(left)leftDoors=!leftDoors;else rightDoors=!rightDoors;traction=0;brake=7;return true;}
 public boolean resetEmergency(){if(!stopped()||brake==0)return false;emergency=false;return true;}
 public void sanitize(){if(!Double.isFinite(speed))speed=0;if(!Double.isFinite(position))position=0;if(!Double.isFinite(pressure))pressure=0;speed=Math.max(-12,Math.min(12,speed));pressure=Math.max(0,Math.min(8,pressure));direction=Math.max(-1,Math.min(1,direction));traction=Math.max(0,Math.min(5,traction));brake=Math.max(0,Math.min(7,brake));}
}
