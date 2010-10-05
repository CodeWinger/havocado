package ResImpl;

import java.net.*;
import java.io.*;

import java.util.concurrent.ConcurrentLinkedQueue;

import Commands.Command;
import Commands.TCPCommands.*;

public class SeedTCPThread extends Thread {
    private HavocadoSeed seed;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket socket;

    public SeedTCPThread(Socket s, HavocadoSeed hs) {
	socket = s;
	seed = hs;
	in = new ObjectInputStream(socket);
	out = new ObjectOutputStream(socket);
    }

    public void run() {
	TCPCommand c;
	while (true) {
	    c = (TCPCommand) in.readObject();
	    handle(c);
	    out.writeObject(c);
	}
    }

    private handle(TCPCommand c) {
	if (c instanceof AddCarsTCPCommand) {
	    c.success = seed.addCars(seed.id, seed.location, seed.count, seed.price);
	}
	else if (c instanceof DeleteCarsTCPCommand) {
	    c.success = seed.deleteCars(seed.id, seed.location);
	}
	else if (c instanceof QueryCarsPriceTCPCommand) {
	    c.price = seed.queryCarsPrice(seed.id, seed.location);
	}
	else if (c instanceof QueryCarsTCPCommand) {
	    c.numCars =  seed.queryCars(seed.id, seed.location);
	}
	else if (c instanceof ReserveCarTCPCommand) {
	    c.success = seed.reserveCar(seed.id, seed.customerID, seed.location);
	}
	else if (c instanceof AddFlightsTCPCommand) {
	    c.success = seed.addFlights(seed.id, seed.location, seed.count, seed.price);
	}
	else if (c instanceof DeleteFlightsTCPCommand) {
	    c.success = seed.deleteFlights(seed.id, seed.location);
	}
	else if (c instanceof QueryFlightsPriceTCPCommand) {
	    c.price = seed.queryFlightsPrice(seed.id, seed.location);
	}
	else if (c instanceof QueryFlightsTCPCommand) {
	    c.numFlights = seed.queryFlights(seed.id, seed.location);
	}
	else if (c instanceof ReserveFlightTCPCommand) {
	    c.success = seed.reserveFlight(seed.id, seed.customerID, seed.location);
	}
	else if (c instanceof AddRoomsTCPCommand) {
	    c.success = seed.addRooms(seed.id, seed.location, seed.count, seed.price);
	}
	else if (c instanceof DeleteRoomsTCPCommand) {
	    c.success = seed.deleteRooms(seed.id, seed.location);
	}
	else if (c instanceof QueryRoomsPriceTCPCommand) {
	    c.price = seed.queryRoomsPrice(seed.id, seed.location);
	}
	else if (c instanceof QueryRoomsTCPCommand) {
	    c.numRooms = seed.queryRooms(seed.id, seed.location);
	}
	else if (c instanceof ReserveRoomTCPCommand) {
	    c.success = seed.queryRooms(seed.id, seed.customerID, seed.location);
	}

}