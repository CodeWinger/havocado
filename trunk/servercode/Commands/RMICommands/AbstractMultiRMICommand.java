package Commands.RMICommands;

import java.util.LinkedList;

import ResInterface.MemberInfo;
import ResInterface.ResourceManager;

public abstract class AbstractMultiRMICommand extends AbstractRMICommand {

	public LinkedList<MemberInfo> carRmGroup;
	public LinkedList<MemberInfo> flightRmGroup;
	public LinkedList<MemberInfo> roomRmGroup;
	
	public transient ResourceManager carRm;
	public transient ResourceManager flightRm;
	public transient ResourceManager roomRm;

	public AbstractMultiRMICommand(
			LinkedList<MemberInfo> pCarRmGroup, 
			LinkedList<MemberInfo> pFlightRmGroup,
			LinkedList<MemberInfo> pRoomRmGroup) {
		super(pCarRmGroup);
		
	    carRmGroup = pCarRmGroup;
	    flightRmGroup = pFlightRmGroup;
	    roomRmGroup = pRoomRmGroup;
	}

	/**
	 * Override the populateResourceManagers function.
	 */
	@Override
	protected void populateResourceManagers() throws Exception {
		ResourceManager c = getAvailableRM(carRmGroup);
		ResourceManager f = getAvailableRM(flightRmGroup);
		ResourceManager r = getAvailableRM(roomRmGroup);
		if (c == null || f == null || r == null) {
			throw new Exception("One resource manager is unavailable");
		}
		carRm = c;
		flightRm = f;
		roomRm = r;
	}
}
