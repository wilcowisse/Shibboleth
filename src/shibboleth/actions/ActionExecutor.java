package shibboleth.actions;

/**
 * Interface which represents all classes that can execute commands.
 * 
 * @author Wilco Wisse
 *
 */
public interface ActionExecutor {
	
	/**
	 * Translate a command to a concrete {@link ShibbolethAction}, and execute that
	 * action.
	 * @param rawCommand The command.
	 * @return return <tt>true</tt> iff the action has been executed 
	 * (i.e. the action is recognized).
	 */
	public boolean doAction(String rawCommand);
	
	/**
	 * Add an action to choose from.
	 */
	public void addAction(ShibbolethAction action);

}
