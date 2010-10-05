package Commands.TCPCommands;

import java.net.*;
import java.io.*;

public class ReserveFlightTCPCommand extends AbstractTCPCommand {
  public int id;
  public int customer;
  public int flightNumber;
  
  public boolean success;
  
  public ReserveFlightTCPCommand( int pId, int pCustomer, int pFlightNumber) {
    super();
    id = pId;
    customer = pCustomer;
    flightNumber = pFlightNumber;
    
    success = false;
  }
  
  
  
  public void doCommand() throws Exception {
    if(recv == null || send == null) { throw new Exception("One of the streams is null."); }
    
    //ObjectInputStream recv = new ObjectInputStream(toSeed.getInputStream());
    //ObjectOutputStream send = new ObjectOutputStream(toSeed.getOutputStream());
    
    // send myself to the server.
    send.writeObject(this); send.flush(); send.reset(); 
    // the server will spit out a mirrored image of me, take what i need from it.
    ReserveFlightTCPCommand mirror = (ReserveFlightTCPCommand) recv.readObject();
    
    // Store the returned object.
    this.success = mirror.success;
  }
}
