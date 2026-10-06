package eu.metroworld.infrastructure;
/** Tick-based music lifecycle; no per-tick restart and no overlapping tracks. */
public final class MusicSchedule {
 public static final long ENTRY_DELAY=160,PLAY_WINDOW=9600,MIN_PAUSE=800;
 public record State(long nextStart,long stopAt,boolean playing){}
 public record Decision(State state,boolean start,boolean stop){}
 public static State entered(long now){return new State(now+ENTRY_DELAY,0,false);}
 public static Decision advance(State state,long now,long salt){
  if(state.playing()&&now>=state.stopAt())return new Decision(new State(now+MIN_PAUSE+Math.floorMod(salt,800),0,false),false,true);
  if(!state.playing()&&now>=state.nextStart())return new Decision(new State(0,now+PLAY_WINDOW,true),true,false);
  return new Decision(state,false,false);
 }
 private MusicSchedule(){}
}
