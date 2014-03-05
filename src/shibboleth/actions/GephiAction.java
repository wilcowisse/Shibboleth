package shibboleth.actions;

import java.io.File;

import shibboleth.model.GithubGraph;

public class GephiAction extends ShibbolethAction {

	private GithubGraph graph;
	
	public GephiAction(GithubGraph graph){
		this.graph=graph;
	}
	
	@Override
	public void execute(String[] args) {
		if(args.length>0){
			String fileName = args[0];
			execute(fileName);
		}
		else{
			listener.messagePushed("Wrong syntax");
		}
	}
	
	public void execute(String filename){
		if(filename.indexOf('.') == -1){
			filename+=".gexf";
		}
		
		File path = new File(filename);
		graph.export(path);
		listener.messagePushed("Saved graph at " + path.getAbsolutePath());
	}
	
	@Override
	public String getCommand() {
		return "gephi";
	}

}
