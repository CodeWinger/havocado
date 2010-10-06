package ResImpl;

import java.net.*;
import java.io.*;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Calendar;

import Commands.Command;
import Commands.TCPCommands.*;

public class FleshTCPThread extends Thread {
    private ConcurrentLinkedQueue<Command> clq;
    private ObjectInputStream in, carIn, flightIn, roomIn;
    private ObjectOutputStream out, carOut, flightOut, roomOut;
    private Socket clientSocket;
    
    private boolean carOnly(Command c) {
	return (c instanceof AddCarsTCPCommand || c instanceof DeleteCarsTCPCommand ||
		c instanceof QueryCarsPriceTCPCommand || c instanceof QueryCarsTCPCommand ||
		c instanceof ReserveCarTCPCommand);
    }

    private boolean flightOnly(Command c) {
	return (c instanceof AddFlightTCPCommand || c instanceof DeleteFlightTCPCommand ||
		c instanceof QueryFlightPriceTCPCommand || c instanceof QueryFlightTCPCommand ||
		c instanceof ReserveFlightTCPCommand);
    }

    private boolean roomOnly(Command c) {
	return (c instanceof AddRoomsTCPCommand || c instanceof DeleteRoomsTCPCommand ||
		c instanceof QueryRoomsPriceTCPCommand || c instanceof QueryRoomsTCPCommand ||
		c instanceof ReserveRoomTCPCommand);
    }

    public FleshTCPThread(ConcurrentLinkedQueue pclq, Socket clientS, ObjectInputStream pci, ObjectOutputStream pco,
			  ObjectInputStream pfi, ObjectOutputStream pfo, ObjectInputStream pri, ObjectOutputStream pro) {
	clq = pclq;
	clientSocket = clientS;
	carIn = pci;
	carOut = pco;
	flightIn = pfi;
	flightOut = pfo;
	roomIn = pri;
	roomOut = pro;
	try {
	    out = new ObjectOutputStream(clientSocket.getOutputStream());
	    in = new ObjectInputStream(clientSocket.getInputStream());
	}
	catch (Exception e) {
	    e.printStackTrace();
	    System.exit(0);
	}
	start();
    }

    public void run() {
	AbstractTCPCommand c;

	while (true) {
	    try {
		int rid = 0;
		NewCustomerTCPCommand nc = null;
		c = (AbstractTCPCommand) in.readObject();
		System.out.println("Caught command.");
		if (carOnly(c)) {
		    c.setObjectInputStream(carIn);
		    c.setObjectOutputStream(carOut);
		}
		else if (flightOnly(c)) {
		    c.setObjectInputStream(flightIn);
		    c.setObjectOutputStream(flightOut);
		}
		else if (roomOnly(c)) {
		    c.setObjectInputStream(roomIn);
		    c.setObjectOutputStream(roomOut);
		}
		else {
		    // Handle combined commands.
		    if (c instanceof NewCustomerTCPCommand) {
			nc = (NewCustomerTCPCommand)c;
			rid = Integer.parseInt( String.valueOf(nc.id) +
						String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
						String.valueOf( Math.round( Math.random() * 100 + 1 )));
			NewCustomerWithIdTCPCommand cwi = new NewCustomerWithIdTCPCommand(nc.id, rid);
			c = cwi;
		    }
		    c.setCarStreams(carIn, carOut);
		    c.setFlightStreams(flightIn, flightOut);
		    c.setRoomStreams(roomIn, roomOut);
		}
		clq.add(c);
		c.waitFor();
		c.clearStreams();
		// Special behaviour if c was a NewCustomer.
		if (rid != 0) {
		    nc.customer = rid;
		    c = nc;
		}
		out.writeObject(c);
		out.flush();
		out.reset();
		System.out.println("Returned reply.");
	    }
	    catch (Exception e) {
		System.out.println("Client disconnected.");
		break;
	    }
	}

	try {
	    in.close();
	    out.close();
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }
}