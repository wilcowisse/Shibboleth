package shibboleth.actions;

import shibboleth.data.DataStore;
import shibboleth.model.GithubGraph;
import shibboleth.util.GithubUtil;

/**
 * Delete a repo or a user from the graph AND the given datastore.
 * 
 * Syntax: <tt>explode [repo|user] [depth]</tt>.
 * 
 * @author Wilco Wisse
 *
 */
public class DeleteAction extends ShibbolethAction{
	
	private GithubGraph graph;
	private DataStore store;
	
	public DeleteAction(GithubGraph graph, DataStore store){
		this.graph=graph;
		this.store=store;
	}
	
	@Override
	public void execute(String[] args) {
		if(args.length == 1){
			
			
		}
		else{
			listener.messagePushed("Wrong syntax");
		}
	}
	
	public void execute(String node){
		graph.remove(node);
		
		if(GithubUtil.isUserName(node)){
			store.deleteUser(node);
		}
		else{
			store.deleteRepo(node);
		}	
		
		listener.graphChanged(String.format("Deleted %s:",node), false);
	}

	@Override
	public String getCommand() {
		return "delete";
	}

}
