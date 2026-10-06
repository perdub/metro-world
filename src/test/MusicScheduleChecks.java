import eu.metroworld.infrastructure.MusicSchedule;
public final class MusicScheduleChecks {
 private static void require(boolean b){if(!b)throw new AssertionError();}
 public static void main(String[] args){
  var state=MusicSchedule.entered(100);require(!MusicSchedule.advance(state,259,7).start());
  var start=MusicSchedule.advance(state,260,7);require(start.start()&&!start.stop());
  require(!MusicSchedule.advance(start.state(),261,7).start());
  var stop=MusicSchedule.advance(start.state(),260+MusicSchedule.PLAY_WINDOW,7);require(stop.stop()&&!stop.start());
  require(!MusicSchedule.advance(stop.state(),stop.state().nextStart()-1,7).start());
  require(MusicSchedule.advance(stop.state(),stop.state().nextStart(),7).start());
  require(stop.state().nextStart()-260-MusicSchedule.PLAY_WINDOW>=MusicSchedule.MIN_PAUSE);
  System.out.println("MUSIC_SCHEDULE_OK: entry delay, one start, stop before next track, quiet pause");
 }
}
