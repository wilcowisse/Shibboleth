package shibboleth.actions;

import shibboleth.data.DataStore;
import shibboleth.model.GitGraph;

/**
 * Delete a repo or a user from the graph AND the given datastore.
 * 
 * Syntax: <tt>explode [repo|user] [depth]</tt>.
 * 
 * @author Wilco Wisse
 *
 */
public class DeleteAction extends ShibbolethAction{
	private GitGraph graph;
	private DataStore store;
	
	public DeleteAction(GitGraph graph, DataStore store){
		this.graph=graph;
		this.store=store;
	}
	
	@Override
	public void execute(String[] args) {
		if(args.length == 1){
			String argument = args[0];
			graph.remove(argument);
			
			if(argument.indexOf('/')==-1){
				store.deleteUser(argument);
			}
			else{
				store.deleteRepo(argument);
			}	
			if(listener!=null){
				listener.graphChanged(String.format("Deleted %s:",argument), false);
			}
			
		}
		else{
			listener.messagePushed("Wrong syntax");
		}

	}

	@Override
	public String getCommand() {
		return "delete";
	}

}
