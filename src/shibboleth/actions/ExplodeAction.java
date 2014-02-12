package shibboleth.actions;

import java.util.ArrayList;
import java.util.List;

import shibboleth.data.DataStore;
import shibboleth.data.DataUtil;
import shibboleth.data.JavaScriptFilter;
import shibboleth.data.RepoFilter;
import shibboleth.model.Contribution;
import shibboleth.model.GitGraph;
import shibboleth.model.Repo;
import shibboleth.model.SimpleRepo;
import shibboleth.model.SimpleUser;
import shibboleth.model.User;

/**
 * Perform a DFS search of a given depth on a node.
 * 
 * Syntax: <tt>explode [repo|user] [depth]</tt>.
 * 
 * @author Wilco Wisse
 *
 */
public class ExplodeAction extends ShibbolethAction{
	
	private DataStore store;
	private GitGraph graph;
	
	private List<SimpleUser> explodedUsers;
	private List<SimpleRepo> explodedRepos;
	private List<Contribution> explodedContributions;
	
	public ExplodeAction(DataStore store, GitGraph graph){
		this.store=store;
		this.graph=graph;
	}
	
	@Override
	public void execute(String[] args) {
		if(args.length > 0){
			String argument = args[0];
			RepoFilter filter = new JavaScriptFilter();
			
			int depth = 0;
			try{
				depth = Integer.parseInt(args[1]);
			}
			catch(NumberFormatException e){
				depth = 0;
			}
			
			explodedUsers = new ArrayList<SimpleUser>();
			explodedRepos = new ArrayList<SimpleRepo>();
			explodedContributions = new ArrayList<Contribution>();
			
			boolean all = false;
			if(args.length==3 && "-a".equals(args[2]))
				all=true;
			
			
			if(argument.indexOf('/')==-1) {
				User user = store.getUser(argument);
				explode(user, filter, all, depth);
			}
			else{
				Repo repo = store.getRepo(argument);
				explode(repo, filter, all, depth);
			}
			
			if(args.length==3 && "-d".equals(args[2])){
				for(SimpleUser u : explodedUsers){
					store.deleteUser(u.login);
					graph.remove(u.login);
				}
				for(SimpleRepo r : explodedRepos){
					store.deleteRepo(r.full_name);
					graph.remove(r.full_name);
				}
			}
			else{
				for(Contribution c : explodedContributions){
					graph.addContribution(c);
				}
			}
			
			if(listener != null){
				listener.graphChanged("Exploded "+ explodedUsers.size() + " users and " +  explodedRepos.size() + " repos", false);
			}
			
			explodedUsers.clear();
			explodedRepos.clear();
				
		}
	}

	@Override
	public String getCommand() {
		return "explode";
	}
	
	public void explode(SimpleUser u, RepoFilter filter, boolean ensureAll, int depth){
		if(u!=null && !explodedUsers.contains(u)){
			if(depth>0) {
				explodedUsers.add(u);
				//System.out.println(depth + " " + u);
				Repo[] reposByUser = store.getRepos(u.login, filter, ensureAll);
				for(Contribution c : DataUtil.reposToContributions(reposByUser, u)){
					explodedContributions.add(c);
					explode(c.getRepo(), filter, ensureAll, depth-1);
				}
			}
			else if(depth == 0){
				explodedUsers.add(u);
			}
		}
	}
	
	public void explode(SimpleRepo r, RepoFilter filter, boolean ensureAll, int depth){
		if(r!=null && !explodedRepos.contains(r)){
			if(depth>0){
				explodedRepos.add(r);
				//System.out.println(depth + " " + r);
				Contribution[] cs = store.getContributions(r.full_name, ensureAll);
				for(Contribution c : cs) {
					explodedContributions.add(c);
					explode(c.getUser(), filter, ensureAll, depth-1);
				}
									
			}
			else if(depth == 0){
				explodedRepos.add(r);
			}
		
		}
	}
	
	
}
