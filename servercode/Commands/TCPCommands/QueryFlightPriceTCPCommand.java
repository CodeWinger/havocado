package Commands.TCPCommands;

import java.net.*;
import java.io.*;

public class QueryFlightPriceTCPCommand extends AbstractTCPCommand {
  public int id;
  public int flightNumber;
  
  public int price;
  
  public QueryFlightPriceTCPCommand( int pId, int pFlightNumber) {
    super();
    id = pId;
    flightNumber = pFlightNumber;
    price = -1;
  }
  
  
  
  public void doCommand() throws Exception {
    if(toSeed == null) { throw new Exception("seed socket is null."); }
    
    ObjectInputStream recv = new ObjectInputStream(toSeed.getInputStream());
    ObjectOutputStream send = new ObjectOutputStream(toSeed.getOutputStream());
    
    // send myself to the server.
    send.writeObject(this); send.flush(); send.reset(); 
    // the server will spit out a mirrored image of me, take what i need from it.
    QueryFlightPriceTCPCommand mirror = (QueryFlightPriceTCPCommand) recv.readObject();
    
    // Store the returned object.
    this.price = mirror.price;
  }
}
