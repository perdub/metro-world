package eu.metroworld.infrastructure.train;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
final class TrainPhysicsTest {
 @Test void doorsAndLowPressurePreventTraction(){var c=new TrainPhysics();c.position=50;c.power=true;c.brake=0;c.traction=5;for(int i=0;i<200;i++)c.tick(.05,20,1000,0);assertEquals(0,c.speed);c.pressure=8;c.leftDoors=true;for(int i=0;i<200;i++)c.tick(.05,20,1000,0);assertEquals(0,c.speed);}
 @Test void closingPanelDoesNotExistInPhysicsAndEmergencyStops(){var c=new TrainPhysics();c.position=50;c.power=true;c.pressure=8;c.brake=0;c.traction=3;for(int i=0;i<100;i++)c.tick(.05,20,1000,0);assertTrue(c.position>50);assertFalse(c.reverse());assertFalse(c.doors(true));c.emergency=true;for(int i=0;i<100;i++)c.tick(.05,20,1000,0);assertEquals(0,c.speed);assertFalse(c.resetEmergency());c.brake=7;assertTrue(c.resetEmergency());}
 @Test void rearwardTravelNeverCrossesRouteStart(){var c=new TrainPhysics();c.position=30;c.power=true;c.pressure=8;c.brake=0;c.traction=5;c.direction=-1;for(int i=0;i<1000;i++)c.tick(.05,20,1000,0);assertEquals(20,c.position);assertEquals(0,c.speed);}
}
