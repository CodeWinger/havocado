package ResImpl;

import java.io.Serializable;
import java.util.LinkedList;

import ResInterface.MemberInfo;

public class RMInfoMessage implements Serializable{
	
    public LinkedList<MemberInfo> carGroup = null;
    public LinkedList<MemberInfo> flightGroup = null;
    public LinkedList<MemberInfo> roomGroup = null;
    
    public RMInfoMessage() { 
    	// do nothing.
    }
    
    public void setCarGroup(LinkedList<MemberInfo> pCarGroup) {
    	carGroup = pCarGroup;
    }
    
    public void setFlightGroup(LinkedList<MemberInfo> pFlightGroup) {
    	flightGroup = pFlightGroup;
    }
    
    public void setRoomGroup(LinkedList<MemberInfo> pRoomGroup) {
    	roomGroup = pRoomGroup;
    }
    
    public LinkedList<MemberInfo> getCarGroup() {
    	return carGroup;
    }
    
    public LinkedList<MemberInfo> getFlightGroup() {
    	return flightGroup;
    }
    
    public LinkedList<MemberInfo> getRoomGroup() {
    	return roomGroup;
    }
}
