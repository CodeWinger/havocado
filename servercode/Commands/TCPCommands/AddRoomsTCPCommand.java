package Commands.TCPCommands;

import java.net.*;
import java.io.*;

public class AddRoomsTCPCommand extends AbstractTCPCommand {
  public int id;
  public String location;
  public int numRooms;
  public int price;
  
  public boolean success;
  
  public AddRoomsTCPCommand(int pId, String pLocation, int pNumRooms, int pPrice) {
    super();
    id = pId;
    location = pLocation;
    numRooms = pNumRooms;
    price = pPrice;
    
    success = false;
  }
  
  
  
  public void doCommand() throws Exception {
    if(toSeed == null) { throw new Exception("seed socket is null."); }
    
    ObjectInputStream recv = new ObjectInputStream(toSeed.getInputStream());
    ObjectOutputStream send = new ObjectOutputStream(toSeed.getOutputStream());
    
    // send myself to the server.
    send.writeObject(this); send.flush(); send.reset(); 
    // the server will spit out a mirrored image of me, take what i need from it.
    AddRoomsTCPCommand mirror = (AddRoomsTCPCommand) recv.readObject();
    
    // Store the returned object.
    this.success = mirror.success;
  }
}
