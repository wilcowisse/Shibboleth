package shibboleth.actions;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import shibboleth.data.DataSource;
import shibboleth.data.github.GithubDataSource;
import shibboleth.data.sql.CommitInfoStore;
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

	private DataSource source;
	private GithubDataSource github;
	private CommitInfoStore infoStore;
	
	public AnalyzeAction(DataSource source, GithubDataSource github, CommitInfoStore infoStore){
		this.source=source;
		this.infoStore=infoStore;
		this.github=github;
	}
	
	@Override
	public void execute(String[] args) {
		if(args.length>2){
			String type = args[0];
			
			String repoName = args[1];
			double accuracy = Double.parseDouble(args[2]);
			
			Repo repo = source.getRepo(repoName);
			Cloner cloner = new Cloner("clones");
			File cloneDir = cloner.clone(repo);
			
			assert cloneDir.exists();
			
			// analyze
			Blamer b = new Blamer(repo.full_name, cloneDir, infoStore);
			b.analyze();
			
			//link
			List<RecordLink> links;
			
			List<User> users = new ArrayList<User>();
			if(type.equals("jaro")){
				users = getUsers(repoName);
				List<Committer> committers = infoStore.getCommitters(repoName);
				Linker linker = new JaroWinklerLinker(committers, users, accuracy);
				links = linker.link();
			}
			else if(type.equals("commits")){
				List<Commit> commits = github.getCommits(repoName);
				Linker linker = new CommitLinker(commits);
				links=linker.link();
			}
			else if(type.equals("saved")){
				links=infoStore.getRecordLinks(repoName);
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
					users = getUsers(repoName);
					Linker jaroLinker = new JaroWinklerLinker(unknownCommitters, users, accuracy);
					List<RecordLink> unkownLinkedWithJaro  = jaroLinker.link();
					links.addAll(unkownLinkedWithJaro);
				}
			}
			
			List<SimpleUser> linkUsers = new ArrayList<SimpleUser>();
			for(SimpleUser u : users){
				linkUsers.add(u);
			}
			List<SimpleUser> allContributors = GithubUtil.contributionsToUsers(source.getContributions(repoName, true));
			for(SimpleUser u : allContributors){
				if(!linkUsers.contains(u))
					linkUsers.add(u);
			}
			
			double worstSimilarity = 1;
			for(RecordLink link : links){
				worstSimilarity = Math.min(worstSimilarity, link.similarity);
			}
			
			int selectedAction = RecordLinkChooser.SAVED;
			
			
			if(!(args.length>3 && args[3].equals("-s"))){
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
			
		}
		else{
			listener.messagePushed("Wrong syntax");
		}

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
