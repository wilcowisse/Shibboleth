package shibboleth.actions;

import java.awt.Color;
import java.util.List;

import shibboleth.data.DataSource;
import shibboleth.data.TransparantFilter;
import shibboleth.model.Contribution;
import shibboleth.model.GithubGraph;
import shibboleth.model.Repo;
import shibboleth.model.SimpleUser;
import shibboleth.util.GithubUtil;

public class HighlightAction extends ShibbolethAction {

	private GithubGraph graph;
	private DataSource source;

	public HighlightAction(GithubGraph graph, DataSource source){
		this.graph = graph;
		this.source = source;
	}
	
	@Override
	public void execute(String[] args) {
		if(args.length==1){
			String node = args[0];
			graph.setColor(node, Color.YELLOW);
			listener.graphChanged("Highlight", false);
		}
		else if(args.length==2 && args[0].equals("c")){
			String node = args[1];

			if(GithubUtil.isRepoName(node)){
				List<Contribution> cs = source.getContributions(node, false);
				for(SimpleUser user : GithubUtil.contributionsToUsers(cs)){
					graph.setColor(user.login, Color.YELLOW);
				}
			}
			else{
				List<Repo> rs = source.getRepos(node, new TransparantFilter(), false);
				for(Repo r : rs){
					graph.setColor(r.full_name, Color.YELLOW);
				}
			}
			listener.graphChanged("Highlight", false);
			
		}
		else{
			listener.messagePushed("Wrong syntax");
		}
	}

	@Override
	public String getCommand() {
		return "whereis";
	}

}
