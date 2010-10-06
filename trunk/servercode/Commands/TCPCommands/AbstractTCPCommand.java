package Commands.TCPCommands;

import java.net.*;
import java.io.*;
import Commands.*;

public abstract class AbstractTCPCommand implements Command, Serializable {
  public transient ObjectInputStream carRecv;
  public transient ObjectOutputStream carSend;

  public transient ObjectInputStream roomRecv;
  public transient ObjectOutputStream roomSend;

  public transient ObjectInputStream flightRecv;
  public transient ObjectOutputStream flightSend;
  
  public void setCarStreams(ObjectInputStream in, ObjectOutputStream out) {
  	carRecv = in;
  	carSend = out;
  }
  
  public void setRoomStreams(ObjectInputStream in, ObjectOutputStream out) {
  	roomRecv = in;
  	roomSend = out;
  }
  
  public void setFlightStreams(ObjectInputStream in, ObjectOutputStream out) {
  	flightRecv = in;
  	flightSend = out;
  }
  
  //protected transient Socket toSeed;
  protected transient ObjectInputStream recv;
  protected transient ObjectOutputStream send;
  protected boolean error;
  
  public AbstractTCPCommand() {
    error = false;
    recv = null;
    send = null;
  }
  
  public void setObjectInputStream(ObjectInputStream in) {
  	recv = in;
  }
  
  public void setObjectOutputStream(ObjectOutputStream out) {
  	send = out;
  }
  
  public void clearStreams() {
  	recv = null;
  	send = null;
  }
  
  /*
  public void setSocket(Socket s) {
    toSeed = s;
  }
  
  public void clearSocket() {
    toSeed = null;
  }
  */
  
  public abstract void doCommand() throws Exception;
  
  public synchronized void waitFor() { 
    try {
      wait();
    } catch (java.lang.InterruptedException e) {
       // Set the error flag.
      error = true;
      
      // Print the exception.
      System.out.println("EXCEPTION:");
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }
  
  public synchronized void finished() { notifyAll(); }
  
  public boolean error() { return error; }
  
  public void execute() {
    try{
      // Perform the command.
      doCommand();
      
    } catch (Exception e) {
      // Set the error flag.
      error = true;
      
      // Print the exception.
      System.out.println("EXCEPTION:");
      System.out.println(e.getMessage());
      e.printStackTrace();
      
    } finally {
      // Signal any thread waiting on this object that we are done.
      finished();
    }
  }
}
