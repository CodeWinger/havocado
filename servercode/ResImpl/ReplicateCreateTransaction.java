package ResImpl;

public class ReplicateCreateTransaction implements ReplicationCommand {

	public int id;
	
	public ReplicateCreateTransaction(int pId) {
		id = pId;
	}
	
	public void execute(HavocadoFlesh f) {
		f.overseer.replicateCreateTransaction(f.lm, id);
	}

}
