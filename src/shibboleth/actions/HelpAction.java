package shibboleth.actions;

import java.util.Arrays;

/**
 * Notifies listener to show help info.
 * 
 * Syntax: <tt></tt>.
 * 
 * @author Wilco Wisse
 *
 */
public class HelpAction extends ShibbolethAction{

	@Override
	public void execute(String[] args) {
		String message = "Available commands:";
		String[] commands = new String[]{
		"labels -- Toggle labels",
		"layout [yifanhu|forceatlas] [duration] -- Apply layout",
		"layout [random] [size] -- Apply random layout of size",
		"info [repo|user] -- Display info",
		"info -c [repo|user] (-l) -- Display contribution info available locally, -l = ignore lang=js filter",
		"info -ca [repo|user] (-l) -- Display all contribution info",
		"get [repo|user] -- Retrieve and add to graph",
		"get -c [repo|user|-all] (-l) -- Retrieve contributions available locally",
		"get -ca [repo|user] (-l) -- Retrieve all contributions",
		"get -cu [repo] -- Retrieve all full user descriptions (Github API intensive)",
		"clone [repo] -- Clone repo and analyse contributions (Github API intensive)",
		"explode [repo|user] [depth] -- DFS of given depth on node",
		"explode [repo|user] [depth] (-a|-d)-- Explode with option 'ensure all' or 'delete'.",
		"hide [repo|user|-all] -- Hide given node in graph",
		"delete [repo|user] -- Remove given node from db",
		"refresh -- Repaint graph",
		"rate -- Show github api account limits",
		"token [token] -- Provide Github access token"
		};
		
		if(listener != null)
			listener.messagePushed(message, commands);
		else{
			System.out.println(message);
			System.out.println(Arrays.toString(commands));
		}
		
	}

	@Override
	public String getCommand() {
		return "help";
	}

}
