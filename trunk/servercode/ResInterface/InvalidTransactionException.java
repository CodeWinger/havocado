package ResInterface;

public class InvalidTransactionException extends Exception {
	public ResInterface.Timestamp t;
	
	public InvalidTransactionException() {
		t = new ResInterface.Timestamp();
	}
	
	public InvalidTransactionException(ResInterface.Timestamp pT){
		t = pT;
	}
}
