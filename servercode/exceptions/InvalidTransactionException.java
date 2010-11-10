package exceptions;

public class InvalidTransactionException extends Exception {
	public ResInterface.Timestamp t;
	
	public InvalidTransactionException(ResInterface.Timestamp pT){
		t = pT;
	}
}
