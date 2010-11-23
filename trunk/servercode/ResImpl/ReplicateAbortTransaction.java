package ResImpl;

public class ReplicateAbortTransaction implements ReplicationCommand {

	public int id;
	
	public ReplicateAbortTransaction(int pId) {
		id = pId;
	}
	
	public void execute(HavocadoFlesh f) {
		// tell the overseer to replicate the abort command.
		f.overseer.replicateAbort(id);
	}
}
