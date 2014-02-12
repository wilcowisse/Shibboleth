package shibboleth.actions;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is a simple Action Executor. It stores added actions in a hash map,
 * and interprets the command to execute the appropriate action.
 * @author Wilco Wisse
 *
 */
public class BasicActionExecutor implements ActionExecutor {

	private Map<String, ShibbolethAction> actionList;
	
	public BasicActionExecutor(){
		actionList = new HashMap<String, ShibbolethAction>();
	}
	
	@Override
	public boolean doAction(String rawCommand) {
		ShibbolethAction action = null;
		String[] args = {};
		
		rawCommand = rawCommand.trim();
		int spacePos = rawCommand.indexOf(' ');
		if(spacePos == -1){
			action = actionList.get(rawCommand);
		}
		else{
			args = rawCommand.substring(spacePos).trim().split("\\s+");
			action = actionList.get(rawCommand.substring(0, spacePos));
		}
		
		if(action != null){
			action.execute(args);
			return true;
		}
		else{
			return false;
		}
	}

	@Override
	public void addAction(ShibbolethAction action) {
		actionList.put(action.getCommand(), action);

	}

}
