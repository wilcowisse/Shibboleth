package shibboleth.actions;

import shibboleth.gui.ActionListener;

/**
 * A ShibbolethAction represents a high level command of the application.
 * 
 * @author Wilco Wisse
 *
 */
public abstract class ShibbolethAction {
	
	protected final NotifierActionListener listener = new NotifierActionListener();
	
	/**
	 * Set a listener for this action.
	 * @param l The listener
	 * @return this
	 */
	public ShibbolethAction addActionListener(ActionListener l){
		listener.addActionListener(l);
		return this;
	}
	
	/**
	 * Adds this action to the provided executor.
	 * @param executor The executor to add this action to.
	 * @return <tt>this</tt> (for method chaining).
	 */
	public ShibbolethAction addExecutor(ActionExecutor executor){
		executor.addAction(this);
		return this;
	}
	
	/**
	 * Execute this action.
	 * @param args
	 */
	public abstract void execute(String[] args);
	
	/**
	 * Get the command. This could be used in a command line based GUI.
	 * @return The command of this action.
	 */
	public abstract String getCommand();
	
}