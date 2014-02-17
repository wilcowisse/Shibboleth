package shibboleth.actions;

import java.awt.Color;

import shibboleth.data.DataSource;
import shibboleth.model.Contribution;
import shibboleth.model.GitGraph;
import shibboleth.model.SimpleUser;
import shibboleth.util.GithubUtil;

public class HighlightAction extends ShibbolethAction {

	private GitGraph graph;
	private DataSource source;

	public HighlightAction(GitGraph graph, DataSource source){
		this.graph = graph;
		this.source = source;
	}
	
	@Override
	public void execute(String[] args) {
		if(args.length==1){
			String node = args[0];
			graph.setColor(node, Color.YELLOW);
		}
		if(args.length==2 && args[0].equals("c")){
			String node = args[1];
			if(GithubUtil.isRepoName(node)){
				Contribution[] cs = source.getContributions(node, false);
				for(SimpleUser user : GithubUtil.contributionsToUsers(cs)){
					graph.setColor(user.login, Color.YELLOW);
				}
			}
			else{
				listener.messagePushed("Wrong syntax");
			}
			
		}

	}

	@Override
	public String getCommand() {
		return "whereis";
	}

}
