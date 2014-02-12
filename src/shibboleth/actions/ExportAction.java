package shibboleth.actions;

import java.io.File;

import shibboleth.model.GitGraph;

public class ExportAction extends ShibbolethAction {

	private GitGraph graph;
	
	public ExportAction(GitGraph graph){
		this.graph=graph;
	}
	
	@Override
	public void execute(String[] args) {
		if(args.length==1){
			File path = new File(args[0]);
			graph.export(path);
			
			if(listener!=null)
				listener.messagePushed("Saved graph at " + path.getAbsolutePath());
		}
	}
	
	@Override
	public String getCommand() {
		return "export";
	}

}
