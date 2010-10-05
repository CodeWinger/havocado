package Commands.TCPCommands;

import java.net.*;
import java.io.*;

public class DeleteFlightTCPCommand extends AbstractTCPCommand {
  public int id;
  public int flightNum;

  public boolean success;
  
  public DeleteFlightTCPCommand(int pId, int pFlightNum) {
    super();
    id = pId;
    flightNum = pFlightNum;
    
    success = false;
  }
  
  
  
  public void doCommand() throws Exception {
    if(recv == null || send == null) { throw new Exception("One of the streams is null."); }
    
    //ObjectInputStream recv = new ObjectInputStream(toSeed.getInputStream());
    //ObjectOutputStream send = new ObjectOutputStream(toSeed.getOutputStream());
    
    // send myself to the server.
    send.writeObject(this); send.flush(); send.reset(); 
    // the server will spit out a mirrored image of me, take what i need from it.
    DeleteFlightTCPCommand mirror = (DeleteFlightTCPCommand) recv.readObject();
    
    // Store the returned object.
    this.success = mirror.success;
  }
}
