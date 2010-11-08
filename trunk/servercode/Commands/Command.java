package Commands;

public interface Command {
  public void execute();
  public void undo();
  public void waitFor();
  public void finished();
  public boolean error();
}
