package exceptions;

public class TransactionAbortedException extends Exception{
	public ResInterface.Timestamp t;
	
	public TransactionAbortedException(ResInterface.Timestamp pT){
		t = pT;
	}
}
