package ResImpl;

import java.net.*;
import java.io.*;

import java.util.concurrent.ConcurrentLinkedQueue;

import Commands.Command;
import Commands.TCPCommands.*;

public class FleshTCPThread extends Thread {
    private ConcurrentLinkedQueue<Command> clq;
    private ObjectInputStream in, carIn, flightIn, roomIn;
    private ObjectOutputStream out, carOut, flightOut, roomOut;
    private Socket clientSocket, carSocket, flightSocket, roomSocket;
    
    private boolean carOnly(Command c) {
	return (c instanceof AddCarsTCPCommand || c instanceof DeleteCarsTCPCommand ||
		c instanceof QueryCarsPriceTCPCommand || c instanceof QueryCarsTCPCommand ||
		c instanceof ReserveCarTCPCommand);
    }

    private boolean flightOnly(Command c) {
	return (c instanceof AddFlightTCPCommand || c instanceof DeleteFlightTCPCommand ||
		c instanceof QueryFlightPriceTCPCommand || c instanceof QueryFlightPriceTCPCommand ||
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
	carSocket = carS;
	flightSocket = flightS;
	roomSocket = roomS;
	carIn = pci;
	carOut = pco;
	flightIn = pfi;
	flightOut = pfo;
	roomIn = pri;
	roomOut = pro;
	start();
    }

    public void run() {
	AbstractTCPCommand c;

	while (true) {
	    try {
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
		}
		clq.add(c);
		c.waitFor();
		c.clearStreams();
		out.writeObject(c);
		out.flush();
		out.reset();
		System.out.println("Returned reply.");
	    }
	    catch (Exception e) {
		e.printStackTrace();
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