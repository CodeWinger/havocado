package Commands.TCPCommands;

import java.net.*;
import java.io.*;

public class NewCustomerTCPCommand extends AbstractTCPCommand {
  public int id;
  
  public int customer;
  
  public NewCustomerTCPCommand( int pId) {
    super();
    id = pId;
    
    customer = -1;
  }
  
  
  
  public void doCommand() throws Exception {
  /*
    if(recv == null || send == null) { throw new Exception("One of the streams is null."); }
    
    //ObjectInputStream recv = new ObjectInputStream(toSeed.getInputStream());
    //ObjectOutputStream send = new ObjectOutputStream(toSeed.getOutputStream());
    
    // send myself to the server.
    send.writeObject(this); send.flush(); send.reset(); 
    // the server will spit out a mirrored image of me, take what i need from it.
    NewCustomerTCPCommand mirror = (NewCustomerTCPCommand) recv.readObject();
    
    // Store the returned object.
    this.customer = mirror.customer;
  */
  }
}
