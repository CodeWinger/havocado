package Commands.TCPCommands;

import java.net.*;
import java.io.*;

public class QueryCarsTCPCommand extends AbstractTCPCommand {
  public int id;
  public String location;
  
  public int numCars;
  
  public QueryCarsTCPCommand( int pId, String pLocation) {
    super();
    id = pId;
    location = pLocation;
    
    numCars = -1;
  }
  
  
  
  public void doCommand() throws Exception {
    if(toSeed == null) { throw new Exception("seed socket is null."); }
    
    ObjectInputStream recv = new ObjectInputStream(toSeed.getInputStream());
    ObjectOutputStream send = new ObjectOutputStream(toSeed.getOutputStream());
    
    // send myself to the server.
    send.writeObject(this); send.flush(); send.reset(); 
    // the server will spit out a mirrored image of me, take what i need from it.
    QueryCarsTCPCommand mirror = (QueryCarsTCPCommand) recv.readObject();
    
    // Store the returned object.
    this.numCars = mirror.numCars;
  }
}
