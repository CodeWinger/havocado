package Commands.TCPCommands;

import java.net.*;
import java.io.*;

public class NewCustomerTCPCommand extends AbstractTCPCommand {
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
  
  private void send(ObjectInputStream recv, ObjectOutputStream send) throws Exception {
  	if(recv == null || send == null) { throw new Exception("One of the streams is null."); }
  	send.writeObject(this); send.flush(); send.reset();
  	NewCustomerTCPCommand mirror = (NewCustomerTCPCommand) recv.readObject();
  	this.customer = mirror.customer;
  }
  
  public int id;
  
  public int customer;
  
  public NewCustomerTCPCommand( int pId) {
    super();
    id = pId;
    
    customer = -1;
  }
  
  
  
  public void doCommand() throws Exception {
    send(carRecv, carSend);
    send(roomRecv, roomSend);
    send(flightRecv, flightSend);
    
    /*
    //ObjectInputStream recv = new ObjectInputStream(toSeed.getInputStream());
    //ObjectOutputStream send = new ObjectOutputStream(toSeed.getOutputStream());
    
    // send myself to the server.
    send.writeObject(this); send.flush(); send.reset(); 
    // the server will spit out a mirrored image of me, take what i need from it.
    NewCustomerTCPCommand mirror = (NewCustomerTCPCommand) recv.readObject();
    
    // Store the returned object.
    this.customer = mirror.customer;*/
  }
}