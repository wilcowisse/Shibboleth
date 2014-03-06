package shibboleth.scripts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import shibboleth.Main;
import shibboleth.actions.AnalyzeAction;
import shibboleth.actions.CloneAction;
import shibboleth.actions.ExportAction;
import shibboleth.actions.GephiAction;
import shibboleth.actions.GetAction;
import shibboleth.actions.TokenAction;
import shibboleth.data.JavaScriptFilter;
import shibboleth.gui.ActionListener;
import shibboleth.gui.CliActionListener;
import shibboleth.model.Contribution;
import shibboleth.model.GephiGraph;
import shibboleth.model.Repo;
import shibboleth.util.GithubUtil;

public class EgoScript extends Main {

	private String token;
	
	private GephiGraph graph;
	
	private GetAction get;
	private ExportAction export;
	private AnalyzeAction analyze;
	private TokenAction tokenAction;
	private CloneAction clone;
	private GephiAction gephiExport;
	
	public EgoScript(String token){
		useProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.holmes.nl", 8080)));
		//useProxy(Proxy.NO_PROXY);
		
		graph = new GephiGraph();
		initApp(createSqliteConnection("db/db.sqlite"), graph);
		ActionListener cli = new CliActionListener();
		
		get = new GetAction(mysqlOnGithub, graph);
		get.addActionListener(cli);
		
		export = new ExportAction(sqlOperations);
		export.addActionListener(cli);
		
		analyze = new AnalyzeAction(mysqlOnGithub, github, infoStore, sqlOperations);
		analyze.addActionListener(cli);
		
		tokenAction = new TokenAction(github);
		tokenAction.addActionListener(cli);
		
		clone = new CloneAction(mysqlOnGithub);
		clone.addActionListener(cli);
		
		gephiExport = new GephiAction(graph);
		gephiExport.addActionListener(cli);
		
		this.token = token;
		
		try {
			execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new EgoScript(args[0]);
	}
	
	public void execute() throws IOException{
		tokenAction.execute(token);
		
		File indexFile = new File("assets","analyzed_repos.log");
		BufferedWriter bw = new BufferedWriter(new FileWriter(indexFile, false));
		bw.write("# egoscript");
		bw.newLine();
		
		for(String user : users){
			System.out.println("\n\n\n\n## USER: " + user + "\n");
			
			List<Repo> ownRepos = get.requestContributions(GithubUtil.createUser(user), new JavaScriptFilter(), true);
			
			for(Repo repo : ownRepos){
				List<Contribution> contributionsToRepo = get.requestContributions(repo, true);
				if(contributionsToRepo.size()==1 && !contributionsToRepo.get(0).getUser().login.equals(user)){
					System.out.println("# Repo "+repo.full_name+" has one contribution, but this is " + contributionsToRepo.get(0).getUser().login);
				}
				if(contributionsToRepo.size()==1 && contributionsToRepo.get(0).getUser().login.equals(user)){// only one contributor, which is owner
					bw.write(repo.full_name + "\t");
					clone.execute(repo, 2000);
					
					bw.write("cloned" + "\t");
					analyze.execute("jaro", repo.full_name, 1.0, AnalyzeAction.PROMPT_NEVER);
					bw.write("analyzed" + "\t");
					List<Integer> filesOfRepo = sqlOperations.getFileIdsOfRepo(repo.full_name);
					export.execute(filesOfRepo, false);
					bw.write("exported");
					bw.newLine();
					
				}
				
			}
			
			graph.layout(GephiGraph.YIFAN_HU, 20000);
			gephiExport.execute("assets/graphs/"+user);
			graph.removeAll();
			bw.flush();
			
			Files.copy(new File("db","db.sqlite").toPath(), new File("assets/dbs","db."+user+".sqlite").toPath(), StandardCopyOption.REPLACE_EXISTING);

			if(rate.getRemaining()<100){
				Main.suspend(rate);
			}
			
		}
		
		bw.close();
		System.out.println("Finished.");
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