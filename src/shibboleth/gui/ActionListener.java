package shibboleth.gui;

/**
 * Interface between controller and model.
 * 
 * @author Wilco Wisse
 *
 */
public interface ActionListener{
	
	/**
	 * Called when an error occurred.
	 * @param e The exception.
	 * @param severe Indicated wheter it is a severe error.
	 */
	public void errorOccurred(Exception e, boolean severe);
	
	/**
	 * Called when graph has changed.
	 * @param what Description of what has changed in the graph.
	 * @param resetNeeded Indicates whether the gui which displays
	 * the graph should be reset, e.g. this is the case if the size 
	 * of the graph changed substantially.
	 */
	public void graphChanged(String what, boolean resetNeeded);
	
	/**
	 * Indicates whether an action is ongoing in the backend.
	 * @param busy is true iff an action if performed in the backend.
	 */
	public void busyStateChanged(boolean busy);
		
	/**
	 * A message from the backend.
	 * @param message The message.
	 * @param objects Objects to display.
	 */
	public void messagePushed(String message, Object[] objects);
	
	/**
	 * A message from the backend.
	 * @param message
	 */
	public void messagePushed(String message);
	
	
	
	
}