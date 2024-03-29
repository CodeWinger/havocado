package ResImpl;

import java.net.*;
import java.io.*;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.rmi.RemoteException;

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
	try {
	    out = new ObjectOutputStream(socket.getOutputStream());
	    in = new ObjectInputStream(socket.getInputStream());
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
	start();
    }

    public void run() {
	AbstractTCPCommand c;
	while (true) {
	    try {
		c = (AbstractTCPCommand) in.readObject();
		System.out.println("Caught command.");
		handle(c);
		out.writeObject(c);
		System.out.println("Replied to command.");
		out.flush();
		out.reset();
	    }
	    catch (Exception e) {
		e.printStackTrace();
		break;
	    }
	}
    }

    private void handle(AbstractTCPCommand command) throws RemoteException {
	if (command instanceof AddCarsTCPCommand) {
	    AddCarsTCPCommand c = (AddCarsTCPCommand)command;
	    c.success = seed.addCars(c.id, c.location, c.numCars, c.price);
	}
	else if (command instanceof DeleteCarsTCPCommand) {
	    DeleteCarsTCPCommand c = (DeleteCarsTCPCommand)command;
	    c.success = seed.deleteCars(c.id, c.location);
	}
	else if (command instanceof QueryCarsPriceTCPCommand) {
	    QueryCarsPriceTCPCommand c = (QueryCarsPriceTCPCommand)command;
	    c.price = seed.queryCarsPrice(c.id, c.location);
	}
	else if (command instanceof QueryCarsTCPCommand) {
	    QueryCarsTCPCommand c = (QueryCarsTCPCommand)command;
	    c.numCars =  seed.queryCars(c.id, c.location);
	}
	else if (command instanceof ReserveCarTCPCommand) {
	    ReserveCarTCPCommand c = (ReserveCarTCPCommand)command;
	    c.success = seed.reserveCar(c.id, c.customer, c.location);
	}
	else if (command instanceof AddFlightTCPCommand) {
	    AddFlightTCPCommand c = (AddFlightTCPCommand)command;
	    c.success = seed.addFlight(c.id, c.flightNum, c.flightSeats, c.flightPrice);
	}
	else if (command instanceof DeleteFlightTCPCommand) {
	    DeleteFlightTCPCommand c = (DeleteFlightTCPCommand)command;
	    c.success = seed.deleteFlight(c.id, c.flightNum);
	}
	else if (command instanceof QueryFlightPriceTCPCommand) {
	    QueryFlightPriceTCPCommand c = (QueryFlightPriceTCPCommand)command;
	    c.price = seed.queryFlightPrice(c.id, c.flightNumber);
	}
	else if (command instanceof QueryFlightTCPCommand) {
	    QueryFlightTCPCommand c = (QueryFlightTCPCommand)command;
	    c.numSeats = seed.queryFlight(c.id, c.flightNumber);
	}
	else if (command instanceof ReserveFlightTCPCommand) {
	    ReserveFlightTCPCommand c = (ReserveFlightTCPCommand)command;
	    c.success = seed.reserveFlight(c.id, c.customer, c.flightNumber);
	}
	else if (command instanceof AddRoomsTCPCommand) {
	    AddRoomsTCPCommand c = (AddRoomsTCPCommand)command;
	    c.success = seed.addRooms(c.id, c.location, c.numRooms, c.price);
	}
	else if (command instanceof DeleteRoomsTCPCommand) {
	    DeleteRoomsTCPCommand c = (DeleteRoomsTCPCommand)command;
	    c.success = seed.deleteRooms(c.id, c.location);
	}
	else if (command instanceof QueryRoomsPriceTCPCommand) {
	    QueryRoomsPriceTCPCommand c = (QueryRoomsPriceTCPCommand)command;
	    c.price = seed.queryRoomsPrice(c.id, c.location);
	}
	else if (command instanceof QueryRoomsTCPCommand) {
	    QueryRoomsTCPCommand c = (QueryRoomsTCPCommand)command;
	    c.numRooms = seed.queryRooms(c.id, c.location);
	}
	else if (command instanceof ReserveRoomTCPCommand) {
	    ReserveRoomTCPCommand c = (ReserveRoomTCPCommand)command;
	    c.success = seed.reserveRoom(c.id, c.customer, c.location);
	}
	else if (command instanceof NewCustomerWithIdTCPCommand) {
	    NewCustomerWithIdTCPCommand c = (NewCustomerWithIdTCPCommand)command;
	    c.success = seed.newCustomer(c.id, c.cid);
	}
	else if (command instanceof DeleteCustomerTCPCommand) {
	    DeleteCustomerTCPCommand c = (DeleteCustomerTCPCommand)command;
	    c.success = seed.deleteCustomer(c.id, c.customer);
	}
	else if (command instanceof QueryCustomerInfoTCPCommand) {
	    QueryCustomerInfoTCPCommand c = (QueryCustomerInfoTCPCommand)command;
	    c.customerInfo = seed.queryCustomerInfo(c.id, c.customer);
	}
    }
}