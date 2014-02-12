package shibboleth.actions;

import java.util.List;

import shibboleth.data.DataSource;
import shibboleth.data.sql.CommitInfoStore;
import shibboleth.git.Analyzer;
import shibboleth.git.Cloner;
import shibboleth.git.Linker;
import shibboleth.gui.RecordLinkChooser;
import shibboleth.model.RecordLink;
import shibboleth.model.Repo;

/**
 * Clone a repository into the 'clones' directory, then analyze and link the repository.
 * 
 * Syntax: <tt>clone [repo]</tt>
 * 
 * @see Cloner
 * @see Analyzer
 * @see Linker
 * @author Wilco Wisse
 *
 */
public class CloneAction extends ShibbolethAction{
	
	private DataSource source;
	private CommitInfoStore infoStore;
	
	public CloneAction(DataSource source, CommitInfoStore infoStore){
		this.source=source;
		this.infoStore=infoStore;
	}
	
	@Override
	public void execute(String[] args) {
		if(args.length == 1){
			if(listener != null)
				listener.busyStateChanged(true);
			
			Repo repo = source.getRepo(args[0]);
			
			// clone
			Cloner cloner = new Cloner("clones");
			cloner.clone(repo);
			
			// analyze
			Analyzer a = new Analyzer(repo.full_name, cloner.getClonePath(repo), infoStore);
			a.analyze();
			
			//link
			Linker linker = new Linker(repo.full_name, infoStore, source);
			List<RecordLink> links = linker.link();
			
			int selectedAction = RecordLinkChooser.evaluateLinks(links, linker.getUsers());
			
			if(selectedAction == RecordLinkChooser.SAVED){
				for(RecordLink link : links){
					infoStore.insertRecordLink(link);
				}
			}
			
			if(listener != null){
				listener.busyStateChanged(false);
				listener.messagePushed("Cloned repo " + repo.full_name);
			}
		}
		
	}

	@Override
	public String getCommand() {
		return "clone";
	}
	
}
