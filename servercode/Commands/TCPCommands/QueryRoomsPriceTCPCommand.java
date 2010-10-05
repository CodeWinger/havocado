package Commands.TCPCommands;

import java.net.*;
import java.io.*;

public class QueryRoomsPriceTCPCommand extends AbstractTCPCommand {
  public int id;
  public String location;
  
  public int price;

  
  public QueryRoomsPriceTCPCommand( int pId, String pLocation) {
    super();
    id = pId;
    location = pLocation;
    
    price = -1;
  }
  
  
  
  public void doCommand() throws Exception {
    if(toSeed == null) { throw new Exception("seed socket is null."); }
    
    ObjectInputStream recv = new ObjectInputStream(toSeed.getInputStream());
    ObjectOutputStream send = new ObjectOutputStream(toSeed.getOutputStream());
    
    // send myself to the server.
    send.writeObject(this); 
    // the server will spit out a mirrored image of me, take what i need from it.
    QueryRoomsPriceTCPCommand mirror = (QueryRoomsPriceTCPCommand) recv.readObject();
    
    // Store the returned object.
    this.price = mirror.price;
  }
}
