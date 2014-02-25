package shibboleth.actions;

import shibboleth.data.DataSource;
import shibboleth.git.Blamer;
import shibboleth.git.Cloner;
import shibboleth.git.JaroWinklerLinker;
import shibboleth.model.Repo;

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
	
	public CloneAction(DataSource source){
		this.source=source;
	}
	
	@Override
	public void execute(String[] args) {
		if(args.length == 1){
			listener.busyStateChanged(true);
			Repo repo = source.getRepo(args[0]);
			
			// clone
			if(repo != null){
				Cloner cloner = new Cloner("clones");
				if(cloner.clone(repo)!=null)
					listener.messagePushed("Cloned repo " + repo.full_name);
				else
					listener.messagePushed("Cloning failed!");
			}
			else{
				listener.messagePushed("Repo not found on github!");
			}
			
			listener.busyStateChanged(false);
		}
		
	}

	@Override
	public String getCommand() {
		return "clone";
	}
	
}
