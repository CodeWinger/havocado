package ResImpl;

public class ReplicateSetLock implements ReplicationCommand {

	public int id;
	public final int lockType;
	public final String resource;
	
	public ReplicateSetLock(int pId, int pLockType, String pResource) {
		id = pId;
		lockType = pLockType;
		resource = pResource;
	}
	
	public void execute(HavocadoFlesh f) {
		// TODO Auto-generated method stub

	}

}
