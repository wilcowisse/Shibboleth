package shibboleth.scripts;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

import shibboleth.Main;
import shibboleth.actions.AnalyzeAction;
import shibboleth.actions.ExportAction;
import shibboleth.actions.GetAction;
import shibboleth.data.JavaScriptFilter;
import shibboleth.gui.ActionListener;
import shibboleth.gui.LogActionListener;
import shibboleth.model.Contribution;
import shibboleth.model.CountGraph;
import shibboleth.model.GithubGraph;
import shibboleth.model.Repo;
import shibboleth.util.GithubUtil;


/** 
 * Analyze existing recordlinks in db with different accuracy.
 * 
 * @author wilco
 *
 */
public class AnalyzeScript extends Main {

	private GithubGraph graph;
	private GetAction get;
	private ExportAction export;
	private AnalyzeAction analyze;
	private ActionListener log;
	
	public AnalyzeScript(){
		useProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.holmes.nl", 8080)));
		//useProxy(Proxy.NO_PROXY);
		
		graph = new CountGraph();
		initApp(createSqliteConnection("db/db.sqlite"), graph);
		log = new LogActionListener();
		
		get = new GetAction(mysql, graph);
		get.addActionListener(log);
		
		export = new ExportAction(sqlOperations);
		export.addActionListener(log);
		
		analyze = new AnalyzeAction(mysql, github, infoStore, sqlOperations);
		analyze.addActionListener(log);
		
		try {
			execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new AnalyzeScript();
	}
	
	public void execute() throws IOException{
		
		final String USER_POINTER = null;
		final String REPO_POINTER = null;
		boolean hasSeenRepoPointer=false;
		boolean hasSeenUserPointer=false;
		
		for(String user : users) {
			if(USER_POINTER != null && !hasSeenUserPointer && !user.equals(USER_POINTER)){
				log.messagePushed("Skipped "+user);
				continue;
			}
			else{
				hasSeenUserPointer=true;
			}
			
			log.messagePushed("\n## USER: " + user + "\n");
			List<Repo> ownRepos = get.requestContributions(GithubUtil.createUser(user), new JavaScriptFilter(), true);
			
			assert ownRepos != null; // all ownrepos must be in db
			
			for(Repo repo : ownRepos){
				if(REPO_POINTER != null && !hasSeenRepoPointer && !repo.full_name.equals(REPO_POINTER)){
					log.messagePushed("Skipped "+repo);
					continue;
				}
				else{
					hasSeenRepoPointer=true;
				}
				
				List<Contribution> contributionsToRepo = get.requestContributions(repo, true);
				assert contributionsToRepo != null; // all contributions must be in db
				
				if(contributionsToRepo.size()==1 && !contributionsToRepo.get(0).getUser().login.equals(user)){
					log.messagePushed("# Repo "+repo.full_name+" has one contribution, but this is " + contributionsToRepo.get(0).getUser().login);
				}
				if(contributionsToRepo.size()==1 && contributionsToRepo.get(0).getUser().login.equals(user)){ // only one contributor, which is owner
					analyze.execute("saved", repo, 0.90, AnalyzeAction.PROMPT_NEVER);
					List<Integer> filesOfRepo = sqlOperations.getFileIdsOfRepo(repo.full_name);
					export.execute(filesOfRepo, false);

				}
				
			}
			
			
		}
		
		log.messagePushed("Finished.");
	}
	
	private String[] users = new String[]{
			"creationix",
			"bahamas10",
			"piroor",
			"shane-tomlinson",
			"DamonOehlman",
			"sindresorhus",
			"substack",
			"dominictarr",
			"tmpvar",
			"NHQ",
			"ForbesLindesay",
			"Raynos",
			"carlos8f",
			"twilson63",
			"juliangruber",
			"ajlopez",
			"neekey",
			"possibilities",
			"pgte",
			"jesusabdullah",
			"indutny",
			"bmeck",
			"fengmk2",
			"timoxley",
			"prashtx",
			"mapmeld",
			"mmalecki",
			"lepture",
			"maxogden",
			"chilijung",
			"jrburke",
			"erikvold",
			"isaacs",
			"scottgonzalez",
			"jcreamer898",
			"cadorn",
			"dscape",
			"jedp",
			"3rd-Eden",
			"epeli",
			"zaach",
			"lloyd",
			"brianyang",
			"marti1125",
			"TooTallNate",
			"caolan",
			"popomore",
			"langpavel",
			"alex-seville",
			"shama"
	};

}