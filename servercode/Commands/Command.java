package Commands;

public interface Command {
  public void execute();
  public void undoCommand();
  public void waitFor();
  public void finished();
  public boolean error();
}
