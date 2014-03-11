package shibboleth.actions;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import shibboleth.data.DataSource;
import shibboleth.data.github.GithubDataSource;
import shibboleth.data.sql.BlameInfoStore;
import shibboleth.data.sql.SqlOperations;
import shibboleth.git.Blamer;
import shibboleth.git.Cloner;
import shibboleth.git.CommitLinker;
import shibboleth.git.JaroWinklerLinker;
import shibboleth.git.Linker;
import shibboleth.gui.RecordLinkChooser;
import shibboleth.model.Commit;
import shibboleth.model.Committer;
import shibboleth.model.Contribution;
import shibboleth.model.RecordLink;
import shibboleth.model.Repo;
import shibboleth.model.SimpleUser;
import shibboleth.model.UnknownUser;
import shibboleth.model.User;
import shibboleth.util.GithubUtil;

public class AnalyzeAction extends ShibbolethAction {

	public static final int PROMPT_ALWAYS=0;
	public static final int PROMPT_NEVER=1;
	public static final int PROMPT_UNLINKED=0;
	
	private DataSource source;
	private GithubDataSource github;
	private BlameInfoStore infoStore;
	private SqlOperations sqlOperations;
	
	public AnalyzeAction(DataSource source, GithubDataSource github, BlameInfoStore infoStore, SqlOperations sqlOp){
		this.source=source;
		this.infoStore=infoStore;
		this.github=github;
		this.sqlOperations=sqlOp;
	}
	
	@Override
	public void execute(String[] args) {
		if(args.length>2){
			String type = args[0];
			String repoName = args[1];
			double accuracy = Double.parseDouble(args[2]);
			int promptType = args.length>3 && args[3].equals("-s") ? PROMPT_UNLINKED : PROMPT_ALWAYS;
			execute(type, repoName, accuracy, promptType);
		}
		else{
			listener.messagePushed("Wrong syntax");
		}	

	}
	
	public void execute(String type, String repoName, double accuracy, int promptType){
		Repo repo = source.getRepo(repoName);
		execute(type, repo, accuracy, promptType);
	}
	
	public void execute(String type, Repo repo, double accuracy, int promptType){

			Cloner cloner = new Cloner("clones");
			File cloneDir = cloner.clone(repo,2000);
			assert cloneDir.exists();
			
			// blame
			if(!type.equals("saved")){
				Blamer b = new Blamer(repo.full_name, cloneDir, infoStore);
				b.blame();
			}
			
			// link
			List<RecordLink> links;
			
			List<User> users = new ArrayList<User>();
			if(type.equals("jaro")){
				users = getUsers(repo.full_name);
				List<Committer> committers = sqlOperations.getCommitters(repo.full_name);
				Linker linker = new JaroWinklerLinker(committers, users, accuracy);
				links = linker.link();
			}
			else if(type.equals("commits")){
				List<Commit> commits = github.getCommits(repo.full_name);
				Linker linker = new CommitLinker(commits);
				links=linker.link();
			}
			else if(type.equals("saved")){
				links=sqlOperations.getRecordLinks(repo.full_name);
			}
			else{
				listener.messagePushed("Wrong syntax");
				return;
			}
			
			// link unlinked committers by jaro winkler distance
			if(!type.equals("jaro")){
				List<Committer> unknownCommitters = new ArrayList<Committer>();
				Iterator<RecordLink> linkIter = links.iterator(); 
				while(linkIter.hasNext()){
					RecordLink l = linkIter.next();
					if(l.user.equals(UnknownUser.getInstance())){
						linkIter.remove();
						unknownCommitters.add(l.committer);
					}
				}
				
				if(unknownCommitters.size() > 0) {
					users = getUsers(repo.full_name);
					Linker jaroLinker = new JaroWinklerLinker(unknownCommitters, users, accuracy);
					List<RecordLink> unkownLinkedWithJaro  = jaroLinker.link();
					links.addAll(unkownLinkedWithJaro);
				}
			}
			
			List<SimpleUser> linkUsers = new ArrayList<SimpleUser>();
			for(SimpleUser u : users){
				linkUsers.add(u);
			}
			List<SimpleUser> allContributors = GithubUtil.contributionsToUsers(source.getContributions(repo.full_name, true));
			for(SimpleUser u : allContributors){
				if(!linkUsers.contains(u))
					linkUsers.add(u);
			}
			
			double worstSimilarity = 1;
			for(RecordLink link : links){
				worstSimilarity = Math.min(worstSimilarity, link.similarity);
			}
			
			int selectedAction = RecordLinkChooser.SAVED;
			
			if(promptType==PROMPT_ALWAYS){
				selectedAction = RecordLinkChooser.evaluateLinks(links, linkUsers);
			}
			else if(promptType==PROMPT_UNLINKED && worstSimilarity < accuracy){
				selectedAction = RecordLinkChooser.evaluateLinks(links, linkUsers);
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
				
				try {
					infoStore.writeToDB();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			listener.messagePushed(String.format("Analyzed (%s) %s %f", type, repo.full_name, accuracy));
		
	}
	
	private List<User> getUsers(String repo) {
		List<User> users = new ArrayList<User>();
		List<Contribution> contributions = source.getContributions(repo, true);
		for(Contribution c : contributions){
			User user = source.getUser(c.getUser().login);
			if(user != null)
				users.add(user);
			else
				System.out.println("User not found while linking");
		}
		return users;
	}

	@Override
	public String getCommand() {
		return "analyze";
	}

}
