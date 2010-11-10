package ResInterface;

public class TransactionAbortedException extends Exception{
	public ResInterface.Timestamp t;
	
	public TransactionAbortedException() {
		t = new ResInterface.Timestamp();
	}
	
	public TransactionAbortedException(ResInterface.Timestamp pT){
		t = pT;
	}
}
