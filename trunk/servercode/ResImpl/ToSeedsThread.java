package ResImpl;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Calendar;

import Commands.Command;
import Commands.TCPCommands.*;
import Commands.RMICommands.*;

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
	    hf.reserveItem(c.id, c.customer, Car.getKey(c.location), c.location);
	}
	else if (command instanceof ReserveCarRMICommand) {
	    ReserveCarRMICommand c = (ReserveCarRMICommand)command;
	    hf.reserveItem(c.id, c.customer, Car.getKey(c.location), c.location);
	}
	else if (command instanceof ReserveFlightTCPCommand) {
	    ReserveFlightTCPCommand c = (ReserveFlightTCPCommand)command;
	    hf.reserveItem(c.id, c.customer, Flight.getKey(c.flightNumber), String.valueOf(c.flightNumber));
	}
	else if (command instanceof ReserveFlightRMICommand) {
	    ReserveFlightRMICommand c = (ReserveFlightRMICommand)command;
	    hf.reserveItem(c.id, c.customer, Flight.getKey(c.flightNumber), String.valueOf(c.flightNumber));
	}
	else if (command instanceof ReserveRoomTCPCommand) {
	    ReserveRoomTCPCommand c = (ReserveRoomTCPCommand)command;
	    hf.reserveItem(c.id, c.customer, Hotel.getKey(c.location), c.location);
	}
	else if (command instanceof ReserveRoomRMICommand) {
	    ReserveRoomRMICommand c = (ReserveRoomRMICommand)command;
	    hf.reserveItem(c.id, c.customer, Hotel.getKey(c.location), c.location);
	}
    }

    private void customerAction(Command command) {
	if (command instanceof NewCustomerTCPCommand) {
	    NewCustomerTCPCommand c = (NewCustomerTCPCommand) command;
	    // Generate a globally unique ID for the new customer
	    int cid = Integer.parseInt( String.valueOf(c.id) +
					String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
					String.valueOf( Math.round( Math.random() * 100 + 1 )));
	    Customer cust = new Customer( cid );
	    hf.writeData( c.id, cust.getKey(), cust );
	    c.customer = cid;
	}
	else if (command instanceof NewCustomerRMICommand) {
	    NewCustomerRMICommand c = (NewCustomerRMICommand) command;
	    // Generate a globally unique ID for the new customer
	    int cid = Integer.parseInt( String.valueOf(c.id) +
					String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
					String.valueOf( Math.round( Math.random() * 100 + 1 )));
	    Customer cust = new Customer( cid );
	    hf.writeData( c.id, cust.getKey(), cust );
	    c.customer = cid;
	}
	else if (command instanceof NewCustomerWithIdTCPCommand) {
	    NewCustomerWithIdTCPCommand c = (NewCustomerWithIdTCPCommand) command;
	    Customer cust = (Customer) hf.readData( c.id, Customer.getKey(c.cid) );
	    if( cust == null ) {
		cust = new Customer(c.cid);
		hf.writeData( c.id, cust.getKey(), cust );
		c.success = true;
	    } else {
		c.success = false;
	    }
	}
	else if (command instanceof NewCustomerWithIdRMICommand) {
	    NewCustomerWithIdRMICommand c = (NewCustomerWithIdRMICommand) command;
	    Customer cust = (Customer) hf.readData( c.id, Customer.getKey(c.cid) );
	    if( cust == null ) {
		cust = new Customer(c.cid);
		hf.writeData( c.id, cust.getKey(), cust );
		c.success = true;
	    } else {
		c.success = false;
	    }
	}
	else if (command instanceof QueryCustomerInfoTCPCommand) {
	    QueryCustomerInfoTCPCommand c = (QueryCustomerInfoTCPCommand) command;
	    Customer cust = (Customer) hf.readData( c.id, Customer.getKey(c.customer) );
	    if( cust == null ) {
		c.customerInfo = "";   // NOTE: don't change this--WC counts on this value indicating a customer does not exist...
	    } else {
		String s = cust.printBill();
		c.customerInfo = s;
	    }
	}
	else if (command instanceof QueryCustomerInfoRMICommand) {
	    QueryCustomerInfoRMICommand c = (QueryCustomerInfoRMICommand) command;
	    Customer cust = (Customer) hf.readData( c.id, Customer.getKey(c.customer) );
	    if( cust == null ) {
		c.customerInfo = "";   // NOTE: don't change this--WC counts on this value indicating a customer does not exist...
	    } else {
		String s = cust.printBill();
		c.customerInfo = s;
	    }
	}
	else if (command instanceof DeleteCustomerTCPCommand) {
	    DeleteCustomerTCPCommand c = (DeleteCustomerTCPCommand) command;
	    Customer cust = (Customer) hf.readData( c.id, Customer.getKey(c.customer) );
	    if( cust == null ) {
		c.success = false;
	    } else {			
		// Increase the reserved numbers of all reservable items which the customer reserved. 
		RMHashtable reservationHT = cust.getReservations();
		for(Enumeration e = reservationHT.keys(); e.hasMoreElements();){		
		    String reservedkey = (String) (e.nextElement());
		    ReservedItem reserveditem = cust.getReservedItem(reservedkey);
		    ReservableItem item  = (ReservableItem) hf.readData(id, reserveditem.getKey());
		    item.setReserved(item.getReserved()-reserveditem.getCount());
		    item.setCount(item.getCount()+reserveditem.getCount());
		}
			
		// remove the customer from the storage
		hf.removeData(id, cust.getKey());
		c.success = true;
	    }
	}
	else if (command instanceof DeleteCustomerRMICommand) {
	    DeleteCustomerRMICommand c = (DeleteCustomerRMICommand) command;
	    Customer cust = (Customer) hf.readData( c.id, Customer.getKey(c.customer) );
	    if( cust == null ) {
		c.success = false;
	    } else {			
		// Increase the reserved numbers of all reservable items which the customer reserved. 
		RMHashtable reservationHT = cust.getReservations();
		for(Enumeration e = reservationHT.keys(); e.hasMoreElements();){		
		    String reservedkey = (String) (e.nextElement());
		    ReservedItem reserveditem = cust.getReservedItem(reservedkey);
		    ReservableItem item  = (ReservableItem) hf.readData(id, reserveditem.getKey());
		    item.setReserved(item.getReserved()-reserveditem.getCount());
		    item.setCount(item.getCount()+reserveditem.getCount());
		}
			
		// remove the customer from the storage
		hf.removeData(id, cust.getKey());
		c.success = true;
	    }
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
		customerAction(c);
		System.out.println("Executing command.");
		c.execute();
	    }
	    else
		Thread.yield();
	}
    }
}
