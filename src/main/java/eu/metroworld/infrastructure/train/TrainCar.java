package eu.metroworld.infrastructure.train;
public enum TrainCar {
 METRO_BLUE(10,2.8,2.7,8),METRO_RED(10,2.8,2.7,8),LOCOMOTIVE(8,2.8,2.7,1),CONTAINER(10,2.8,2.5,0),PLATFORM(10,2.8,.5,0);
 public final double length,width,height;public final int seats;
 TrainCar(double length,double width,double height,int seats){this.length=length;this.width=width;this.height=height;this.seats=seats;}
 public boolean cargo(){return this==CONTAINER||this==PLATFORM;}
}
