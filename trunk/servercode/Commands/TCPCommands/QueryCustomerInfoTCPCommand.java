package Commands.TCPCommands;

import java.net.*;
import java.io.*;

public class QueryCustomerInfoTCPCommand extends AbstractTCPCommand {
  public int id;
  public int customer;
  
  public String customerInfo;
  
  public QueryCustomerInfoTCPCommand( int pId, int pCustomer) {
    super();
    id = pId;
    customer = pCustomer;
    
    customerInfo = null;
  }
  
  
  
  public void doCommand() throws Exception {
    if(toSeed == null) { throw new Exception("seed socket is null."); }
    
    ObjectInputStream recv = new ObjectInputStream(toSeed.getInputStream());
    ObjectOutputStream send = new ObjectOutputStream(toSeed.getOutputStream());
    
    // send myself to the server.
    send.writeObject(this); 
    // the server will spit out a mirrored image of me, take what i need from it.
    QueryCustomerInfoTCPCommand mirror = (QueryCustomerInfoTCPCommand) recv.readObject();
    
    // Store the returned object.
    this.customerInfo = mirror.customerInfo;
  }
}