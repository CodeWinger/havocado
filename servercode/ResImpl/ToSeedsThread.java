package ResImpl;

import java.util.concurrent.ConcurrentLinkedQueue;

import Commands.Command;

public class ToSeedsThread extends Thread {
    
    private ConcurrentLinkedQueue<Command> clq;
    private HavocadoFlesh hf;

    private boolean isReservation(Command c) {
	return (c instanceof ReserveCarTCPCommand || c instanceof ReserveCarRMICommand ||
		c instanceof ReserveFlightTCPCommand || c instanceof ReserveFlightRMICommand ||
		c instanceof ReserveRoomTCPCommand || c instanceof ReserveRoomRMICommand);
    }

    private void reserve(Command command) {
	if (command instanceof ReserveCarTCPCommand) {
	    ReserveCarTCPCommand c = (ReserveCarTCPCommand)command;
	    hf.reserveItem(c.id, c.customerID, Car.getKey(c.location), c.location);
	}
	else if (command instanceof ReserveCarRMICommand) {
	    ReserveCarRMICommand c = (ReserveCarRMICommand)command;
	    hf.reserveItem(c.id, c.customerID, Car.getKey(c.location), c.location);
	}
	else if (command instanceof ReserveFlightTCPCommand) {
	    ReserveFlightTCPCommand c = (ReserveFlightTCPCommand)command;
	    hf.reserveItem(c.id, c.customerID, Flight.getKey(c.flightNum), String.valueOf(c.flightNum));
	}
	else if (command instanceof ReserveFlightRMICommand) {
	    ReserveFlightRMICommand c = (ReserveFlightRMICommand)command;
	    hf.reserveItem(c.id, c.customerID, Flight.getKey(c.flightNum), String.valueOf(c.flightNum));
	}
	else if (command instanceof ReserveRoomTCPCommand) {
	    ReserveRoomTCPCommand c = (ReserveRoomTCPCommand)command;
	    hf.reserveItem(c.id, c.customerID, Hotel.getKey(c.location), c.location);
	}
	else if (command instanceof ReserveRoomRMICommand) {
	    ReserveRoomRMICommand c = (ReserveRoomRMICommand)command;
	    hf.reserveItem(c.id, c.customerID, Hotel.getKey(c.location), c.location);
	}
    }

    public ToSeedsThread(ConcurrentLinkedQueue<Command> pclq, HavocadoFlesh phf) {
	clq = pclq;
	hf = phf;
    }

    public void run() {
	Command c;
	while (true) {
	    c = clq.poll();
	    if (c != null) {
		if (isReservation(c)) {
		    reserve(c);
		}
		System.out.println("Executing command.");
		c.execute();
	    }
	    else
		Thread.yield();
	}
    }
}
