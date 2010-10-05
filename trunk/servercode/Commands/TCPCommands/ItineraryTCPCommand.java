package Commands.TCPCommands;

import java.net.*;
import java.io.*;

public class ItineraryTCPCommand extends AbstractTCPCommand {
  
  public transient Socket carSocket;
  public transient Socket flightSocket;
  public transient Socket roomSocket;

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
  
  public void setCarSocket(Socket s) { carSocket = s; }
  
  public void clearCarSocket() { carSocket = null; }
  
  public void setFlightSocket(Socket s) { flightSocket = s; }
  
  public void clearFlightSocket() { flightSocket = null; }
  
  public void setRoomSocket(Socket s) { roomSocket = s; }
  
  public void clearRoomSocket() { roomSocket = null; }
  
  public void doCommand() throws Exception {
    if(carSocket == null || flightSocket == null || roomSocket == null) { 
      throw new Exception("some sockets are null."); 
    }
/*    
    ObjectInputStream recv = new ObjectInputStream(toSeed.getInputStream());
    ObjectOutputStream send = new ObjectOutputStream(toSeed.getOutputStream());
    // send myself to the server.
    send.writeObject(this); 
    // the server will spit out a mirrored image of me, take what i need from it.
    DeleteFlightTCPCommand mirror = (DeleteFlightTCPCommand) recv.readObject();
    
    // Store the returned object.
    this.success = mirror.success;
*/
    success = true;
    
    for(int i = 0; i < flightNumbers.size(); i++) {
      int flightNum = ((Integer) flightNumbers.elementAt(i)).intValue();
      // TODO: RESERVE A FLIGHT.
    } 
    if(car && success) {
      // TODO: RESERVE A CAR.
    }
    if(room && success) {
      // TODO: RESERVE A ROOM.
    }
    
  }
}
