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
			execute("yifanhu", 2000);
		}
		else if(args.length == 1){
			execute(args[0], 2000);
		}
		else if(args.length == 2){
			
			int time=2000;
			try{
				time = Integer.parseInt(args[1]);
			}
			catch(NumberFormatException e){}
			graph.layout(args[0], time);
			
		}
		else{
			listener.messagePushed("Wrong syntax");
		}
		
	}
	
	public void execute(String layout, int time){
		listener.busyStateChanged(true);
		graph.layout(layout, time);
		listener.graphChanged("Layout graph: "+layout+" "+time, true);
		listener.busyStateChanged(false);
	}

	@Override
	public String getCommand() {
		return "layout";
	}
	
}