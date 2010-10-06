package Commands.TCPCommands;

import java.net.*;
import java.io.*;
import java.util.*;

public class ItineraryTCPCommand extends AbstractTCPCommand {
  
  public transient ObjectInputStream carRecv;
  public transient ObjectOutputStream carSend;

  public transient ObjectInputStream roomRecv;
  public transient ObjectOutputStream roomSend;

  public transient ObjectInputStream flightRecv;
  public transient ObjectOutputStream flightSend;
  
  public void setCarStreams(ObjectInputStream in, ObjectOutputStream out) {
  	carRecv = in;
  	carSend = out;
  }
  
  public void setRoomStreams(ObjectInputStream in, ObjectOutputStream out) {
  	roomRecv = in;
  	roomSend = out;
  }
  
  public void setFlightStreams(ObjectInputStream in, ObjectOutputStream out) {
  	flightRecv = in;
  	flightSend = out;
  }
  
  public int id;
  public int customer;
  public Vector flightNumbers;
  public String location;
  public boolean car;
  public boolean room;

  public boolean success;
  
  public ItineraryTCPCommand(
    int pId, 
		int pCustomer, 
		Vector pFlightNumbers, 
		String pLocation, 
		boolean pCar, 
		boolean pRoom) 
	{
    super();
    id = pId;
    customer = pCustomer;
    flightNumbers = pFlightNumbers;
    location = pLocation;
    car = pCar;
    room = pRoom;
    
    success = false;
  }
  
  public void doCommand() throws Exception {
    success = true;

    // reserve as many flights as we want.    
    for(int i = 0; i < flightNumbers.size(); i++) {
      int flightNum = ((Integer) flightNumbers.elementAt(i)).intValue();
      ReserveFlightTCPCommand r = new ReserveFlightTCPCommand(id, customer, flightNum);
      flightSend.writeObject(r); flightSend.flush(); flightSend.reset();
      r = (ReserveFlightTCPCommand) flightRecv.readObject();
      success = success && r.success;
    } 
    
    // reserve a car if needed.
    if(car && success) {
      ReserveCarTCPCommand r = new ReserveCarTCPCommand(id, customer, location);
      carSend.writeObject(r); carSend.flush(); carSend.reset();
      r = (ReserveCarTCPCommand) carRecv.readObject();
      success = success && r.success;
    }
    
    // reserve a room if needed.
    if(room && success) {
      ReserveRoomTCPCommand r = new ReserveRoomTCPCommand(id, customer, location);
      roomSend.writeObject(r); roomSend.flush(); roomSend.reset();
      r = (ReserveRoomTCPCommand) roomRecv.readObject();
      success = success && r.success;
    }    
  }
}
