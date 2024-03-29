

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Queue;
import java.util.Vector;
import ResInterface.Timestamp;

import ResInterface.Timestamp;

public class ScriptLogger {
	
	private PrintStream out;
	
	
	public ScriptLogger(String scriptName) throws FileNotFoundException {
		out = new PrintStream(scriptName+".log");
	}
	
	public void log(Timestamp t) {
		/*Enumeration<String> comps = t.getStampers();
		Vector<Long> v;
		String s;
		while (comps.hasMoreElements()) {
			s = comps.nextElement();
			v = t.getStamps(s);
			for (Long stamp : v) {
				out.println(s+": "+stamp);
			}
		}*/
		Queue<String> v = t.getStamps();
		for (String s : v) {
			out.println(s);
		}
		out.println();
	}
	
	public void stop() {
		out.close();
	}
}
