package ResImpl;

import java.io.Serializable;

public interface ReplicationCommand extends Serializable{
	public void execute(HavocadoFlesh f);
}
