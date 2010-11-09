package ResInterface;

public class ReturnTuple<T> {
	public T result;
	public Timestamp timestamp;
	
	public ReturnTuple(T pResult, Timestamp pTimestamp) {
		result = pResult;
		timestamp = pTimestamp;
	}
}
