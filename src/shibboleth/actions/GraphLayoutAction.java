package shibboleth.actions;

import shibboleth.model.GephiGraph;

/**
 * Apply layout algorithm to the graph.
 * 
 * Syntax:
 * <tt>layout [yifanhu|forceatlas] [duration]</tt> -- Apply layout for duration.
 * <tt>layout [random] [size]</tt> -- Apply random layout with given size.
 * 
 * @author Wilco Wisse
 *
 */
public class GraphLayoutAction extends ShibbolethAction{

	private GephiGraph graph;
	
	public GraphLayoutAction(GephiGraph graph){
		this.graph = graph;
	}
	
	@Override
	public void execute(String[] args){
		assert listener != null;
		
		if(args.length == 0){
			graph.layout("yifanhu", 500);
			listener.graphChanged("Layout graph: yifanhu 500", true);
		}
		else if(args.length == 1){
			graph.layout(args[0], 500);
			listener.graphChanged("Layout graph", true);
		}
		else if(args.length == 2){
			listener.busyStateChanged(true);
			graph.layout(args[0], Integer.parseInt(args[1]));
			listener.busyStateChanged(false);
			listener.graphChanged("Layout graph", true);
		}
		else{
			listener.messagePushed("Wrong syntax");
		}
		
	}

	@Override
	public String getCommand() {
		return "layout";
	}
	
}