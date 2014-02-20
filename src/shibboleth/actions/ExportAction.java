package shibboleth.actions;

import java.io.File;

import shibboleth.model.GithubGraph;

public class ExportAction extends ShibbolethAction {

	private GithubGraph graph;
	
	public ExportAction(GithubGraph graph){
		this.graph=graph;
	}
	
	@Override
	public void execute(String[] args) {
		if(args.length==1){
			String filename = args[0];
			if(filename.indexOf('.') == -1){
				filename+="gexf";
			}
			
			File path = new File(filename);
			
			graph.export(path);
			
			listener.messagePushed("Saved graph at " + path.getAbsolutePath());
		}
	}
	
	@Override
	public String getCommand() {
		return "export";
	}

}
