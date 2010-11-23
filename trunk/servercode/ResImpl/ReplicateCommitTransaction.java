package ResImpl;

public class ReplicateCommitTransaction implements ReplicationCommand {

	public int id;
	
	public ReplicateCommitTransaction(int pId) {
		id = pId;
	}
	
	public void execute(HavocadoFlesh f) {
		f.overseer.replicateCommit(id);
	}

}
