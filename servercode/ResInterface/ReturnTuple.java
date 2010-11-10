package ResInterface;

import java.io.*;

public class ReturnTuple<T> implements Serializable{
	public T result;
	public Timestamp timestamp;
	
	public ReturnTuple(T pResult, Timestamp pTimestamp) {
		result = pResult;
		timestamp = pTimestamp;
	}
}
