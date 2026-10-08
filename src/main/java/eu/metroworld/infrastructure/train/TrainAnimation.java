package eu.metroworld.infrastructure.train;
import com.google.gson.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Quaternionf;
/** TrainCarts relative frames: looped timelines and latched doors are separate modes. */
final class TrainAnimation {
 private TrainAnimation(){}
 record Frame(Vector3f translation,Vector3f rotation,boolean active){}
 static Frame sample(JsonObject animation,double seconds){var frames=animation.getAsJsonArray("frames");if(frames==null||frames.isEmpty())return new Frame(new Vector3f(),new Vector3f(),true);double total=0;for(var f:frames)total+=f.getAsJsonObject().get("duration").getAsDouble();double speed=animation.has("speed")?animation.get("speed").getAsDouble():1;double t=Math.max(0,(seconds-(animation.has("delay")?animation.get("delay").getAsDouble():0))*Math.abs(speed));if(animation.has("looped")&&animation.get("looped").getAsBoolean()&&total>0)t%=total;t=Math.min(total,t);if(speed<0)t=total-t;
  for(int i=0;i<frames.size();i++){var a=frames.get(i).getAsJsonObject();double duration=a.get("duration").getAsDouble();if(t<=duration||i==frames.size()-1){var b=frames.get(Math.min(i+1,frames.size()-1)).getAsJsonObject();return blend(a,b,duration==0?0:(float)(t/duration));}t-=duration;}throw new IllegalStateException();
 }
 static Frame door(JsonObject animation,float amount){var frames=animation.getAsJsonArray("frames");if(frames==null||frames.isEmpty())return new Frame(new Vector3f(),new Vector3f(),true);var closed=frames.get(0).getAsJsonObject();JsonObject open=closed;double max=-1;for(var raw:frames){var f=raw.getAsJsonObject();double score=TrainModels.vector(f,"translation",new Vector3f()).lengthSquared()+TrainModels.vector(f,"rotation",new Vector3f()).lengthSquared()*.001;if(score>max){open=f;max=score;}}return blend(closed,open,amount);}
 private static Frame blend(JsonObject a,JsonObject b,float progress){var translation=TrainModels.vector(a,"translation",new Vector3f()).lerp(TrainModels.vector(b,"translation",new Vector3f()),progress);var rotation=TrainModels.vector(a,"rotation",new Vector3f()).lerp(TrainModels.vector(b,"rotation",new Vector3f()),progress);return new Frame(translation,rotation,!a.has("active")||a.get("active").getAsBoolean());}
 static Matrix4f matrix(Frame f){var r=new Vector3f(f.rotation).mul((float)Math.PI/180);return new Matrix4f().translation(f.translation).rotateY(r.y).rotateX(r.x).rotateZ(r.z);}
}
