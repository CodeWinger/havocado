package Commands.RMGroupCommands;

import ResImpl.HavocadoSeed;

public class CancelReservationRMGroupCommand extends AbstractRMGroupCommand {
	
	private static final long serialVersionUID = 1L;
	private int id;
	private int customerID;
	private String reservedkey;

	public CancelReservationRMGroupCommand(int id, int customerID, String reservedkey) {
		this.id = id;
		this.customerID = customerID;
		this.reservedkey = reservedkey;
	}

	@Override
	public void doCommand(HavocadoSeed hs) throws Exception {
		hs.cancelReservation(id, customerID, reservedkey);
	}
	
	public String toString() {
		return "CancelReservation "+id+" customerID: "+customerID+" reservedkey: "+reservedkey;
	}

}
