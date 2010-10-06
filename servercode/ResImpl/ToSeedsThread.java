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

    private void customerAction(Command command) {
	if (command instanceof NewCustomerTCPCommand) {
	    NewCustomerTCPCommand c = (NewCustomerTCPCommand) command;
	    // Generate a globally unique ID for the new customer
	    int cid = Integer.parseInt( String.valueOf(c.id) +
					String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
					String.valueOf( Math.round( Math.random() * 100 + 1 )));
	    Customer cust = new Customer( cid );
	    writeData( id, cust.getKey(), cust );
	    c.customer = cid;
	}
	else if (command instanceof NewCustomerRMICommand) {
	    NewCustomerRMICommand c = (NewCustomerRMICommand) command;
	    // Generate a globally unique ID for the new customer
	    int cid = Integer.parseInt( String.valueOf(c.id) +
					String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
					String.valueOf( Math.round( Math.random() * 100 + 1 )));
	    Customer cust = new Customer( cid );
	    writeData( id, cust.getKey(), cust );
	    c.customer = cid;
	}
	else if (command instanceof NewCustomerWithIdTCPCommand) {
	    NewCustomerWithIdTCPCommand c = (NewCustomerWithIdTCPCommand) command;
	    Customer cust = (Customer) readData( c.id, Customer.getKey(c.customerID) );
	    if( cust == null ) {
		cust = new Customer(c.customerID);
		writeData( c.id, cust.getKey(), cust );
		c.success = true;
	    } else {
		c.success = false;
	    }
	}
	else if (command instanceof NewCustomerWithIdRMICommand) {
	    NewCustomerWithIdRMICommand c = (NewCustomerWithIdRMICommand) command;
	    Customer cust = (Customer) readData( c.id, Customer.getKey(c.customerID) );
	    if( cust == null ) {
		cust = new Customer(c.customerID);
		writeData( c.id, cust.getKey(), cust );
		c.success = true;
	    } else {
		c.success = false;
	    }
	}
	else if (command instanceof QueryCustomerInfoTCPCommand) {
	    QueryCustomerInfoTCPCommand c = (QueryCustomerInfoTCPCommand) command;
	    Customer cust = (Customer) readData( c.id, Customer.getKey(customerID) );
	    if( cust == null ) {
		c.customerInfo = "";   // NOTE: don't change this--WC counts on this value indicating a customer does not exist...
	    } else {
		String s = cust.printBill();
		c.customerInfo = s;
	    }
	}
	else if (command instanceof QueryCustomerInfoRMICommand) {
	    QueryCustomerInfoRMICommand c = (QueryCustomerInfoRMICommand) command;
	    Customer cust = (Customer) readData( c.id, Customer.getKey(customerID) );
	    if( cust == null ) {
		c.customerInfo = "";   // NOTE: don't change this--WC counts on this value indicating a customer does not exist...
	    } else {
		String s = cust.printBill();
		c.customerInfo = s;
	    }
	}
	else if (command instanceof DeleteCustomerTCPCommand) {
	    DeleteCustomerTCPCommand c = (DeleteCustomerTCPCommand) command;
	    Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
	    if( cust == null ) {
		c.success = false;
	    } else {			
		// Increase the reserved numbers of all reservable items which the customer reserved. 
		RMHashtable reservationHT = cust.getReservations();
		for(Enumeration e = reservationHT.keys(); e.hasMoreElements();){		
		    String reservedkey = (String) (e.nextElement());
		    ReservedItem reserveditem = cust.getReservedItem(reservedkey);
		    ReservableItem item  = (ReservableItem) readData(id, reserveditem.getKey());
		    item.setReserved(item.getReserved()-reserveditem.getCount());
		    item.setCount(item.getCount()+reserveditem.getCount());
		}
			
		// remove the customer from the storage
		removeData(id, cust.getKey());
		c.success = true;
	    }
	}
	else if (command instanceof DeleteCustomerRMICommand) {
	    DeleteCustomerRMICommand c = (DeleteCustomerRMICommand) command;
	    Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
	    if( cust == null ) {
		c.success = false;
	    } else {			
		// Increase the reserved numbers of all reservable items which the customer reserved. 
		RMHashtable reservationHT = cust.getReservations();
		for(Enumeration e = reservationHT.keys(); e.hasMoreElements();){		
		    String reservedkey = (String) (e.nextElement());
		    ReservedItem reserveditem = cust.getReservedItem(reservedkey);
		    ReservableItem item  = (ReservableItem) readData(id, reserveditem.getKey());
		    item.setReserved(item.getReserved()-reserveditem.getCount());
		    item.setCount(item.getCount()+reserveditem.getCount());
		}
			
		// remove the customer from the storage
		removeData(id, cust.getKey());
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
