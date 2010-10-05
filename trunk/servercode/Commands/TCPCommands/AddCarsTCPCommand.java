package Commands.TCPCommands;

import java.net.*;
import java.io.*;

public class AddCarsTCPCommand extends AbstractTCPCommand {
  public int id;
  public String location;
  public int numCars;
  public int price;
  
  public boolean success;
  
  public AddCarsTCPCommand(int pId, String pLocation, int pNumCars, int pPrice) {
    super();
    id = pId;
    location = pLocation;
    numCars = pNumCars;
    price = pPrice;
    
    success = false;
  }
  
  
  
  public void doCommand() throws Exception {
    if(toSeed == null) { throw new Exception("seed socket is null."); }
    
    ObjectInputStream recv = new ObjectInputStream(toSeed.getInputStream());
    ObjectOutputStream send = new ObjectOutputStream(toSeed.getOutputStream());
    
    // send myself to the server.
    send.writeObject(this); 
    // the server will spit out a mirrored image of me, take what i need from it.
    AddCarsTCPCommand mirror = (AddCarsTCPCommand) recv.readObject();
    
    // Store the returned object.
    this.success = mirror.success;
  }
}
