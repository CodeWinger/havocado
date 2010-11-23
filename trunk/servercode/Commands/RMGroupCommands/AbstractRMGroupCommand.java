package Commands.RMGroupCommands;

import java.io.Serializable;

import ResImpl.HavocadoSeed;
import ResInterface.Timestamp;

public abstract class AbstractRMGroupCommand implements Serializable{

	private static final long serialVersionUID = 1L;
	protected transient HavocadoSeed hs;
	protected boolean error;

	protected int previousQty = 0;
	protected int previousPrice = 0;
	protected Timestamp timestamp;

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestampObject(Timestamp pTimestamp) {
		timestamp = pTimestamp;
	}

	public abstract void doCommand(HavocadoSeed hs) throws Exception;

	public boolean error() { return error; }

	public void execute(HavocadoSeed hs) {
		try{
			if(timestamp == null) {
				timestamp = new Timestamp();
			}

			// Perform the command.
			doCommand(hs);

		} catch (Exception e) {
			// Set the error flag.
			error = true;

			// Print the exception.
			System.out.println("EXCEPTION:");
			System.out.println(e.getMessage());
			e.printStackTrace();

		}
	}
}
