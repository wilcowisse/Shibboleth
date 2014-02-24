package shibboleth.actions;

import java.util.List;

import shibboleth.data.DataSource;
import shibboleth.data.JavaScriptFilter;
import shibboleth.data.RepoFilter;
import shibboleth.data.TransparantFilter;
import shibboleth.model.Contribution;
import shibboleth.model.GithubGraph;
import shibboleth.model.Repo;
import shibboleth.model.SimpleRepo;
import shibboleth.model.SimpleUser;
import shibboleth.model.User;
import shibboleth.util.GithubUtil;

/**
 * Retrieve a Github artifact, i.e. a user, repo or contributions. Subsequently store it to the 
 * database and add to the graph.
 * 
 * Syntax: 
 * <tt>get [repo|user]</tt> -- Retrieve and add to graph
 * <tt>get -c [repo|user|-all] (-l)</tt> -- Retrieve contributions available locally, -l = all languages
 * <tt>get -ca [repo|user] (-l)</tt> -- Retrieve all contributions
 * <tt>get -cu [repo]</tt> -- Retrieve all full user descriptions (Github API intensive).
 * 
 * @author Wilco Wisse
 *
 */
public class GetAction extends ShibbolethAction{
	
	private DataSource source;
	private GithubGraph graph;
	
	public GetAction(DataSource source, GithubGraph graph){
		this.source = source;
		this.graph = graph;
	}
	
	@Override
	public void execute(String[] args) {
		if(args.length == 1){
			String argument = args[0];

			if(argument.indexOf('/')==-1)
				requestUser(argument);
			else
				requestRepo(argument);
		}
		else if(args.length > 1 && args[0].equals("c")) {
			String argument = args[1];
			if(argument.equals("-all"))
				requestAllContributions();
			else if(argument.indexOf('/')==-1){
				User user = source.getUser(argument);
				RepoFilter filter = null;
				if(args.length==3 && "-l".equals(args[2]))
					filter=new TransparantFilter();
				else
					filter=new JavaScriptFilter();
				requestContributions(user,filter, false);
			}
			else{
				Repo repo = source.getRepo(argument);
				requestContributions(repo, false);
			}	
		}
		else if(args.length > 1 && args[0].equals("ca")) {
			String argument = args[1];
			if(argument.indexOf('/')==-1){
				User user = source.getUser(argument);
				RepoFilter filter = null;
				if(args.length==3 && "-l".equals(args[2]))
					filter=new TransparantFilter();
				else
					filter=new JavaScriptFilter();
				requestContributions(user,filter, true);
			}
			else{
				Repo repo = source.getRepo(argument);
				requestContributions(repo, true);
			}
		}
		else if(args.length > 1 && args[0].equals("cu")) {
			String argument = args[1];
			if(argument.indexOf('/')!=-1){
				Repo repo = source.getRepo(argument);
				List<Contribution> cs = source.getContributions(repo.full_name, false);
				for(Contribution c : cs)
					source.getUser(c.getUser().login);
			}
		}
		else {
			listener.messagePushed("Wrong syntax");
		}
	}
	
	public void requestAllContributions(){
		List<Contribution> cs = source.getAllContributions();
		for(Contribution c : cs)
			graph.addContribution(c);
		
		listener.graphChanged("Added all contributions.", false);
	}
	
	public void requestContributions(SimpleUser u, RepoFilter filter, boolean ensureAll){
		if(u != null){
			List<Repo> rs = source.getRepos(u.login, filter, ensureAll);
			
			for(Contribution c : GithubUtil.reposToContributions(rs, u)){
				graph.addContribution(c);
			}
			
			listener.graphChanged("Added " + rs.size() +" contributions of "+u.login, false);
			
		}
		else{
			try {
				throw new Exception("User not found!");
			} catch (Exception e) {
				if(listener != null)
					listener.errorOccurred(e, false);
				else
					System.out.println(e.getMessage());
			}
		}
	}
	
	public void requestContributions(SimpleRepo r, boolean ensureAll){
		if(r != null){
			List<Contribution> cs = source.getContributions(r.full_name, ensureAll);
			for(Contribution c : cs)
				graph.addContribution(c);
			
			listener.graphChanged("Added " + cs.size() +" contributions for "+r.full_name, false);
			
		}
		else{
			try {
				throw new Exception("Repo not found!");
			} catch (Exception e) {
				listener.errorOccurred(e, false);
			}
		}
	}
	
	public void requestUser(String userName){
		User u = source.getUser(userName);
		if(u != null){
			graph.add(u);
			listener.graphChanged("Added user "+userName, false);
		}
		else{
			try {
				throw new Exception("User not found!");
			} catch (Exception e) {
				listener.errorOccurred(e, false);
			}
		}
	}
	
	public void requestRepo(String repoName){
		Repo r = source.getRepo(repoName);
		
		if(r != null){
			graph.add(r);
			listener.graphChanged("Added repo "+repoName, false);
		}
		else{
			try {
				throw new Exception("Repo not found!");
			} catch (Exception e) {
				listener.errorOccurred(e, false);
			}
		}
	}

	@Override
	public String getCommand() {
		return "get";
	}

}
