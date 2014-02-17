package shibboleth.actions;

import shibboleth.model.GitGraph;

/**
 * Hide node, i.e. remove from graph. But do not remove from database.
 * 
 * Syntax: <tt>hide [repo|user|-all]</tt>.
 * 
 * @author Wilco Wisse
 *
 */
public class HideAction extends ShibbolethAction {
	
	private GitGraph graph;
	
	public HideAction(GitGraph graph){
		this.graph=graph;
	}
	@Override
	public void execute(String[] args) {
		if(args.length == 1 && args[0].equals("-all")){
			graph.removeAll();
			listener.graphChanged("Removed all nodes", false);
		}
		else if(args.length == 1){
			boolean res = graph.remove(args[0]);
			if(!res && listener!=null)
				listener.messagePushed("No node named"+args[0]+ " found.");
			else if(res)
				listener.graphChanged("Node "+args[0]+ " removed." , false);
			
		}
		else{
			listener.messagePushed("Wrong syntax");
		}

	}

	@Override
	public String getCommand() {
		return "hide";
	}

}
