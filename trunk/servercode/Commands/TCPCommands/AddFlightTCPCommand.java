package Commands.TCPCommands;

import java.net.*;
import java.io.*;

public class AddFlightTCPCommand extends AbstractTCPCommand {
  public int id;
  public int flightNum;
  public int flightSeats;
  public int flightPrice;
  
  public boolean success;
  
  public AddFlightTCPCommand(int pId, int pFlightNum, int pFlightSeats, int pFlightPrice) {
    super();
    id = pId;
    flightNum = pFlightNum;
    flightSeats = pFlightSeats;
    flightPrice = pFlightPrice;
    
    success = false;
  }
  
  
  
  public void doCommand() throws Exception {
    if(recv == null || send == null) { throw new Exception("One of the streams is null."); }
    
    //ObjectInputStream recv = new ObjectInputStream(toSeed.getInputStream());
    //ObjectOutputStream send = new ObjectOutputStream(toSeed.getOutputStream());
    
    System.out.println("OH MY GOD MY PANTS ARE ON FIRE.");
    
    // send myself to the server.
    send.writeObject(this); send.flush(); send.reset(); 
    // the server will spit out a mirrored image of me, take what i need from it.
    AddFlightTCPCommand mirror = (AddFlightTCPCommand) recv.readObject();
    
    // Store the returned object.
    this.success = mirror.success;
  }
}
