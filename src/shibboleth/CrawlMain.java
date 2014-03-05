package shibboleth;

import java.util.Scanner;

import shibboleth.actions.ActionExecutor;
import shibboleth.actions.BasicActionExecutor;
import shibboleth.actions.ExplodeAction;
import shibboleth.actions.GetAction;
import shibboleth.data.CrawlSource;
import shibboleth.gui.ActionListener;
import shibboleth.gui.CliActionListener;
import shibboleth.model.CountGraph;
import shibboleth.model.GithubGraph;

public class CrawlMain extends Main {
	
	/**
	 * A GetAction with a @link{CrawlSource} attached.
	 * @author Wilco Wisse
	 */
	public static class CrawlGetAction extends GetAction{
		public CrawlGetAction(CrawlSource source, GithubGraph graph) {
			super(source, graph);
		}
		@Override
		public String getCommand(){
			return "crawl-get";
		}
	}
	
	/**
	 * An ExplodeAction with a @link{CrawlSource} attached.
	 * @author Wilco Wisse
	 */
	public static class CrawlExplodeAction extends ExplodeAction{
		public CrawlExplodeAction(CrawlSource source, GithubGraph graph) {
			super(source, graph);
		}
		@Override
		public String getCommand(){
			return "crawl-explode";
		}
	}
	
	/**
	 * Runs the Shibboleth Application optimized as crawler
	 */
	public CrawlMain(){
		GithubGraph graph = new CountGraph();
		initApp(createSqliteConnection("db/db.sqlite"), graph);
		ActionListener cli = new CliActionListener();
		ActionExecutor executor = new BasicActionExecutor();
		initActions(cli, executor);
		
		// CrawlSource
		CrawlSource	crawlSource = new CrawlSource(mysql, github);
		
		CrawlGetAction crawlGet = new CrawlGetAction(crawlSource, graph);
		crawlGet.addActionListener(cli);
		crawlGet.addExecutor(executor);
		addToExeAction(crawlGet);
		
		CrawlExplodeAction crawlExplode = new CrawlExplodeAction(crawlSource, graph);
		crawlExplode.addActionListener(cli);
		crawlExplode.addExecutor(executor);
		addToExeAction(crawlExplode);
		
		System.out.println("Crawling mode.");
		
		cli.messagePushed("Enter commands. 'q' = quit.");
		Scanner scanner = new Scanner(System.in);
		while(true){
			String command = scanner.nextLine().trim();
			
			if(command.equals("")){
				// do nothing
			}
			else if (command.equals("q")){
				close();
				scanner.close();
				System.exit(0);
			}
			else{
				if(executor.doAction(command) == false){
					System.out.println("Wrong syntax. Enter 'q' to quit.");
				}
				System.out.println();
			}
		}
	}
	
	public static void main(String[] args) {
		new CrawlMain();
	}

}
