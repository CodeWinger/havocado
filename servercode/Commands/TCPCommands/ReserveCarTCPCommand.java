package Commands.TCPCommands;

import java.net.*;
import java.io.*;

public class ReserveCarTCPCommand extends AbstractTCPCommand {
  public int id;
  public int customer;
  public String location;
  
  public boolean success;
  
  public ReserveCarTCPCommand( int pId, int pCustomer, String pLocation) {
    super();
    id = pId;
    customer = pCustomer;
    location = pLocation;
    
    success = false;
  }
  
  
  
  public void doCommand() throws Exception {
    if(toSeed == null) { throw new Exception("seed socket is null."); }
    
    ObjectInputStream recv = new ObjectInputStream(toSeed.getInputStream());
    ObjectOutputStream send = new ObjectOutputStream(toSeed.getOutputStream());
    
    // send myself to the server.
    send.writeObject(this); send.flush(); send.reset(); 
    // the server will spit out a mirrored image of me, take what i need from it.
    ReserveCarTCPCommand mirror = (ReserveCarTCPCommand) recv.readObject();
    
    // Store the returned object.
    this.success = mirror.success;
  }
}