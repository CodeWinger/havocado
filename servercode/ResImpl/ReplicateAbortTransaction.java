package ResImpl;

public class ReplicateAbortTransaction implements ReplicationCommand {

	public int id;
	
	public ReplicateAbortTransaction(int pId) {
		id = pId;
	}
	
	public void execute(HavocadoFlesh f) {
		// TODO Auto-generated method stub

	}

}
