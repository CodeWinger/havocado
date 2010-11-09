

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;

import ResInterface.Timestamp;

public class ScriptLogger {
	
	private PrintStream out;
	
	
	public ScriptLogger(String scriptName) throws FileNotFoundException {
		out = new PrintStream(scriptName+".log");
	}
	
	public void log(Timestamp t) {
		Enumeration<String> comps = t.getStampers();
		Vector<Long> v;
		String s = comps.nextElement();
		while (s != null) {
			v = t.getStamps(s);
			for (Long stamp : v) {
				out.print(s+": "+stamp);
			}
		}
	}
	
	public void stop() {
		out.close();
	}
}