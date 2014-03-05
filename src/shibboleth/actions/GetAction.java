package shibboleth.actions;

import java.util.ArrayList;
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
			else if(GithubUtil.isUserName(argument)){
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
				if(repo != null)
					requestContributions(repo, false);
			}	
		}
		else if(args.length > 1 && args[0].equals("ca")) {
			String argument = args[1];
			if(GithubUtil.isUserName(argument)){
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
				if(repo != null)
					requestContributions(repo, true);
			}
		}
		else if(args.length > 1 && args[0].equals("cu")) {
			String argument = args[1];
			if(GithubUtil.isRepoName(argument)){
				Repo repo = source.getRepo(argument);
				getUsers(repo);
			}
		}
		else {
			listener.messagePushed("Wrong syntax");
		}
	}

	public List<User> getUsers(SimpleRepo repo){
		List<User> users = new ArrayList<User>();
		List<Contribution> cs = source.getContributions(repo.full_name, true);
		for(Contribution c : cs)
			users.add(source.getUser(c.getUser().login));
		return users;
	}
	
	public List<Contribution> requestAllContributions(){
		List<Contribution> cs = source.getAllContributions();
		for(Contribution c : cs)
			graph.addContribution(c);
		
		listener.graphChanged("Added all contributions.", false);
		return cs;
	}
	
	public List<Repo> requestContributions(SimpleUser u, RepoFilter filter, boolean ensureAll){
		if(u != null){
			List<Repo> rs = source.getRepos(u.login, filter, ensureAll);
			
			for(Contribution c : GithubUtil.reposToContributions(rs, u)){
				graph.addContribution(c);
				System.out.println(c.getRepo().full_name);
			}
			
			listener.graphChanged("Added " + rs.size() +" contributions of "+u.login, false);
			return rs;
		}
		else{
			return null;
		}
	}
	
	public List<Contribution> requestContributions(SimpleRepo r, boolean ensureAll){
		if(r != null){
			List<Contribution> cs = source.getContributions(r.full_name, ensureAll);
			for(Contribution c : cs)
				graph.addContribution(c);
			
			listener.graphChanged("Added " + cs.size() +" contributions for "+r.full_name, false);
			return cs;
		}
		else{
			return null;
		}
		
	}
	
	public User requestUser(String userName){
		User u = source.getUser(userName);
		if(u != null){
			graph.add(u);
			listener.graphChanged("Added user "+userName, false);
		}
		else{
			listener.messagePushed("User "+userName+" not found!");
		}
		return u;
	}
	
	public Repo requestRepo(String repoName){
		Repo r = source.getRepo(repoName);
		if(r != null){
			graph.add(r);
			listener.graphChanged("Added repo "+repoName, false);
		}
		else{
			listener.messagePushed("Repo "+repoName+" not found!");
		}
		return r;
	}

	@Override
	public String getCommand() {
		return "get";
	}

}
