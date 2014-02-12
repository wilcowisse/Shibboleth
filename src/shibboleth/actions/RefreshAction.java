package shibboleth.actions;

/**
 * Repaint graph.
 * 
 * Syntax: <tt>refresh</tt>.
 * 
 * @author Wilco Wisse
 *
 */
public class RefreshAction extends ShibbolethAction {

	@Override
	public void execute(String[] args) {
		if(listener!=null)
			listener.graphChanged("Refresh", false);
	}

	@Override
	public String getCommand() {
		return "refresh";
	}

}
