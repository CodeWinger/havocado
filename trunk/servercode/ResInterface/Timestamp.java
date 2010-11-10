package ResInterface;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.io.*;

public class Timestamp implements Serializable{
	/** A hashtable mapping the name of a computer to a vector of timestamps. */
	private Hashtable<String, Vector<Long>>stamps = new Hashtable<String, Vector<Long>>();
	
	/**
	 * Gets the current computer's name and associates it with the current
	 * time.
	 */
	public void stamp() {
		String name;
		try {
			name = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			name = "Error reading hostname";
		}
		Vector<Long> v = stamps.get(name);
		// If this computer is timestamping for the first time, make a vector
		// of stamps and put this stamp on the list.
		if (v == null) {
			v = new Vector<Long>();
			v.add(new Long(System.currentTimeMillis()));
			stamps.put(name, v);
		}
		// If we've stamped before, add the current time to the list.
		else {
			v.add(new Long(System.currentTimeMillis()));
		}
	}
	
	/**
	 * Returns a list of the computers that have stamped this object.
	 * @return A list of the computers that have stamped this object.
	 */
	public Enumeration<String> getStampers() {
		return stamps.keys();
	}
	
	/**
	 * Returns the timestamps associated with the given name.
	 * @param name The name whose timestamps we're retreiving.
	 * @return The timestamps associated with 'name'.
	 */
	public Vector<Long> getStamps(String name) {
		return stamps.get(name);
	}
}
