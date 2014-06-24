package shibboleth.scripts;

import java.io.File;
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
import shibboleth.gui.LogActionListener;
import shibboleth.model.Contribution;
import shibboleth.model.GephiGraph;
import shibboleth.model.Repo;
import shibboleth.model.User;
import shibboleth.util.GithubUtil;

public class CoreScript extends Main {

	private String token;
	
	private GephiGraph graph;
	
	private GetAction get;
	private ExportAction export;
	private AnalyzeAction analyze;
	private TokenAction tokenAction;
	private CloneAction clone;
	private GephiAction gephiExport;
	private ActionListener log;
	
	public CoreScript(String token){
		useProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.holmes.nl", 8080)));
		//useProxy(Proxy.NO_PROXY);
		
		graph = new GephiGraph();
		initApp(createSqliteConnection("db/db.sqlite"), graph);
		log = new LogActionListener();
		
		get = new GetAction(mysqlOnGithub, graph);
		get.addActionListener(log);
		
		export = new ExportAction(sqlOperations);
		export.addActionListener(log);
		
		analyze = new AnalyzeAction(mysqlOnGithub, github, infoStore, sqlOperations);
		analyze.addActionListener(log);
		
		tokenAction = new TokenAction(github);
		tokenAction.addActionListener(log);
		
		clone = new CloneAction(mysqlOnGithub);
		clone.addActionListener(log);
		
		gephiExport = new GephiAction(graph);
		gephiExport.addActionListener(log);

		this.token = token;
		
		execute();
	
	}

	public static void main(String[] args) {
		new CoreScript(args[0]);
	}
	
	public void execute() {
		tokenAction.execute(token);
		
		for(String repoName : repos){
			log.messagePushed("\n\nREPO: " + repoName + "\n");
			Repo repo = get.requestRepo(repoName);
			
			if(clone.execute(repo, 2000)){
				log.messagePushed("   Analyzing "+ repo.full_name);
				analyze.execute("jaro", repo, 0.9, AnalyzeAction.PROMPT_NEVER);
				
				log.messagePushed("   Exporting "+ repo.full_name);
				List<Integer> filesOfRepo = sqlOperations.getFileIdsOfRepo(repo.full_name);
				export.execute(filesOfRepo, true);
				
				log.messagePushed("   Rate remaining: "+Integer.toString(rate.getRemaining()));
			}
			else{
				log.messagePushed("   Repo "+repo.full_name+" ignored, because cloning failed.");
			}

			if(rate.getRemaining()>-1 && rate.getRemaining()<200){
				Main.suspend(rate);
			}
			
		}
		gephiExport.execute("assets/graphs/coregraph1.gephi");
		log.messagePushed("\n\nFINISHED!");
	}
	
	private String[] repos = new String[]{
			
	};

}