package Commands.TCPCommands;

import java.net.*;
import java.io.*;

public class QueryFlightTCPCommand extends AbstractTCPCommand {
  public int id;
  public int flightNumber;
  
  public int numSeats;
  
  public QueryFlightTCPCommand( int pId, int pFlightNumber) {
    super();
    id = pId;
    flightNumber = pFlightNumber;
    numSeats = -1;
  }
  
  
  
  public void doCommand() throws Exception {
    if(toSeed == null) { throw new Exception("seed socket is null."); }
    
    ObjectInputStream recv = new ObjectInputStream(toSeed.getInputStream());
    ObjectOutputStream send = new ObjectOutputStream(toSeed.getOutputStream());
    
    // send myself to the server.
    send.writeObject(this); 
    // the server will spit out a mirrored image of me, take what i need from it.
    QueryFlightTCPCommand mirror = (QueryFlightTCPCommand) recv.readObject();
    
    // Store the returned object.
    this.numSeats = mirror.numSeats;
  }
}
