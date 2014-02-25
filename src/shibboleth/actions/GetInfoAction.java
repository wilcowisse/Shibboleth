package shibboleth.actions;

import java.util.List;

import shibboleth.data.DataSource;
import shibboleth.data.JavaScriptFilter;
import shibboleth.data.RepoFilter;
import shibboleth.data.TransparantFilter;
import shibboleth.model.Contribution;
import shibboleth.model.Repo;
import shibboleth.model.User;

/**
 * Retrieve a Github artifact, i.e. a user, repo or contributions. Subsequenly notify listener
 * to display this artifact. Does neither save the database nor add to the graph.
 * 
 * Syntax: 
 * <tt>info [repo|user]</tt> -- Display info
 * <tt>info -c [repo|user|-all] (-l)</tt> -- Retrieve and display contributions available locally, -l = all languages
 * <tt>info -ca [repo|user] (-l)</tt> -- Retrieve and display all contributions
 * 
 * @author Wilco Wisse
 *
 */
public class GetInfoAction extends ShibbolethAction{
	
	private DataSource source;
	
	public GetInfoAction(DataSource source){
		this.source = source;
	}
	
	@Override
	public void execute(String[] args) {

		if(args.length == 1){
			String argument = args[0];
			if(argument.indexOf('/')==-1){
				requestUser(argument);
			}
			else{
				requestRepo(argument);
			}
		}
		else if(args.length > 1 && args[0].equals("c")) {
			String argument = args[1];
			if(argument.indexOf('/')==-1){
				RepoFilter filter = null;
				if(args.length==3 && "-l".equals(args[2]))
					filter=new TransparantFilter();
				else
					filter=new JavaScriptFilter();
				requestUserExpand(argument, filter, false);
			}
			else{
				requestRepoExpand(argument, false);
			}
		}
		else if(args.length > 1 && args[0].equals("ca")) {
			String argument = args[1];
			if(argument.indexOf('/')==-1){
				RepoFilter filter = null;
				if(args.length==3 && "-l".equals(args[2]))
					filter=new TransparantFilter();
				else
					filter=new JavaScriptFilter();
				requestUserExpand(argument,filter, true);
			}
			else{
				requestRepoExpand(argument, true);
			}
		}
		else {
			listener.messagePushed("Wrong syntax");
		}
	}
	
	public void requestUser(String userName){
		User u = source.getUser(userName);
		if(u != null){
			String message = "User info";
			String[] info = u.getInfoArray();
			
			if(listener != null)
				listener.messagePushed(message, info);
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
			String message = "Repo info";
			String[] info = r.getInfoArray();
			
			listener.messagePushed(message, info);
		}
		else{
			try {
				throw new Exception("Repo not found!");
			} catch (Exception e) {
				listener.errorOccurred(e, false);
			}
		}
	}
	
	private void requestUserExpand(String userName, RepoFilter filter, boolean ensureAll){
		List<Repo>  reposByUser = source.getRepos(userName, filter, ensureAll);
		
		if(reposByUser!=null){
			String message = "Repos "+userName+" contributed to:";
			listener.messagePushed(message, reposByUser.toArray());
		}
		else{
			try {
				throw new Exception("User not found!");
			} catch (Exception e) {
				listener.errorOccurred(e, false);
			}
		}		
	}
	
	private void requestRepoExpand(String repoName, boolean ensureAll){
		List<Contribution> contributions = source.getContributions(repoName, ensureAll);
		
		if(contributions!=null){
			String message = "Contributions to "+repoName+":";
			if(listener != null)
				listener.messagePushed(message, contributions.toArray());
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
		return "info";
	}

}
