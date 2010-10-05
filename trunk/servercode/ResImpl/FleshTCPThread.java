package ResImpl;

import java.net.*;
import java.io.*;

import java.util.concurrent.ConcurrentLinkedQueue;

import Commands.Command;
import Commands.TCPCommands.*;

public class FleshTCPThread extends Thread {
    private ConcurrentLinkedQueue<Command> clq;
    private ObjectInputStream in;
    private ObjectOutputStream out;
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

    public FleshTCPThread(ConcurrentLinkedQueue pclq, Socket clientS, Socket carS, Socket flightS, Socket roomS) {
	clq = pclq;
	clientSocket = clientS;
	carSocket = carS;
	flightSocket = flightS;
	roomSocket = roomS;
	start();
    }

    public void run() {
	AbstractTCPCommand c;
	try {
	    out = new ObjectOutputStream(clientSocket.getOutputStream());
	    in = new ObjectInputStream(clientSocket.getInputStream());
	}
	catch (Exception e) {
	    System.out.println("Error connecting to client.");
	    e.printStackTrace();
	}

	while (true) {
	    try {
		c = (AbstractTCPCommand) in.readObject();
		System.out.println("Caught command.");
		if (carOnly(c))
		    c.setSocket(carSocket);
		else if (flightOnly(c))
		    c.setSocket(flightSocket);
		else if (roomOnly(c))
		    c.setSocket(roomSocket);
		else {
		    // Handle combined commands.
		}
		clq.add(c);
		c.waitFor();
		c.clearSocket();
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