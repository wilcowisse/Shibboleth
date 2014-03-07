package shibboleth.actions;

import shibboleth.data.DataSource;
import shibboleth.git.Blamer;
import shibboleth.git.Cloner;
import shibboleth.git.JaroWinklerLinker;
import shibboleth.model.Repo;
import shibboleth.util.GithubUtil;

/**
 * Clone a repository into the 'clones' directory, then analyze and link the repository.
 * 
 * Syntax: <tt>clone [repo]</tt>
 * 
 * @see Cloner
 * @see Blamer
 * @see JaroWinklerLinker
 * @author Wilco Wisse
 *
 */
public class CloneAction extends ShibbolethAction{
	
	private DataSource source;
	private Cloner cloner;
	
	public CloneAction(DataSource source){
		this.source=source;
		cloner = new Cloner("clones");
	}
	
	@Override
	public void execute(String[] args) {
		if(args.length > 0 && GithubUtil.isRepoName(args[0])){
			int interval = 0;
			if(args.length>1){
				try{
					interval=Integer.parseInt(args[1]);
				}
				catch(NumberFormatException e){
					interval=0;
				}
			}
			execute(args[0], interval);
		}
		else{
			listener.messagePushed("Wrong syntax!");
		}
		
	}
	
	
	public boolean execute(String repoName, int interval){
		Repo repo = source.getRepo(repoName);
		return execute(repo, interval);
	}
	
	public boolean execute(Repo repo, int interval){
		listener.busyStateChanged(true);
		boolean successful = false;
		if(repo != null){
			long lastCloneTimeStamp = cloner.getLastClonedTime();
			if(cloner.clone(repo, interval) != null){
				long delayTime = System.currentTimeMillis() - lastCloneTimeStamp;
				listener.messagePushed("Cloned " + repo.full_name+". Last clone action was "+ delayTime+ " ms ago." );
				successful = true;
			}
			else{
				listener.messagePushed("Cloning failed!");
			}
		}
		else{
			listener.messagePushed("Clone error: Repo not found in source!");
		}
		listener.busyStateChanged(false);
		return successful;
	}

	@Override
	public String getCommand() {
		return "clone";
	}
	
}
