package shibboleth.actions;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import shibboleth.data.DataSource;
import shibboleth.data.sql.CommitInfoStore;
import shibboleth.git.Analyzer;
import shibboleth.git.Cloner;
import shibboleth.git.Linker;
import shibboleth.gui.RecordLinkChooser;
import shibboleth.model.RecordLink;
import shibboleth.model.Repo;
import shibboleth.model.UnknownUser;

public class AnalyzeAction extends ShibbolethAction {

	private DataSource source;
	private CommitInfoStore infoStore;
	
	public AnalyzeAction(DataSource source, CommitInfoStore infoStore){
		this.source=source;
		this.infoStore=infoStore;
	}
	
	@Override
	public void execute(String[] args) {
		if(args.length>1){
			String repoName = args[0];
			double accuracy = Double.parseDouble(args[1]);
			
			Repo repo = source.getRepo(repoName);
			Cloner cloner = new Cloner("clones");
			File cloneDir = cloner.clone(repo);
			
			assert cloneDir.exists();
			
			// analyze
			Analyzer a = new Analyzer(repo.full_name, cloneDir, infoStore);
			a.analyze();
			
			//link
			Linker linker = new Linker(repo.full_name, infoStore, source);
			List<RecordLink> links = linker.link(accuracy);
			
			int selectedAction = RecordLinkChooser.SAVED;
			if(args.length>2 && args[2].equals("-l")){
				selectedAction = RecordLinkChooser.evaluateLinks(links, linker.getUsers());
			}
			else if(args.length>2 && args[2].equals("-lc")){
				double worstSimilarity = 1;
				for(RecordLink link : links){
					worstSimilarity = Math.min(worstSimilarity, link.similarity);
				}
				if(worstSimilarity < accuracy){
					selectedAction = RecordLinkChooser.evaluateLinks(links, linker.getUsers());
				}
			}
			
			if(selectedAction == RecordLinkChooser.SAVED){
				for(RecordLink link : links) {
					if(!link.user.equals(UnknownUser.getInstance())){
						infoStore.insertRecordLink(link);
					}
					else{
						try {
							infoStore.deleteRecordLink(infoStore.getCommitterId(link.committer));
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				}
			}
			
		}
		else{
			listener.messagePushed("Wrong syntax");
		}

	}

	@Override
	public String getCommand() {
		return "analyze";
	}

}
