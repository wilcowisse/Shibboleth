package shibboleth;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import shibboleth.actions.ActionExecutor;
import shibboleth.actions.AnalyzeAction;
import shibboleth.actions.CloneAction;
import shibboleth.actions.DeleteAction;
import shibboleth.actions.ExeAction;
import shibboleth.actions.ExplodeAction;
import shibboleth.actions.ExportAction;
import shibboleth.actions.GephiAction;
import shibboleth.actions.GetAction;
import shibboleth.actions.InfoAction;
import shibboleth.actions.HelpAction;
import shibboleth.actions.HideAction;
import shibboleth.actions.HighlightAction;
import shibboleth.actions.RateAction;
import shibboleth.actions.RefreshAction;
import shibboleth.actions.ShibbolethAction;
import shibboleth.actions.ToggleLabelAction;
import shibboleth.actions.TokenAction;
import shibboleth.data.CachedSource;
import shibboleth.data.CachedStore;
import shibboleth.data.DataSource;
import shibboleth.data.DataStore;
import shibboleth.data.HashMapStore;
import shibboleth.data.ReadOnlyCachedSource;
import shibboleth.data.github.GithubDataSource;
import shibboleth.data.github.RateLimitValue;
import shibboleth.data.sql.CommitInfoStore;
import shibboleth.data.sql.SqlDataStore;
import shibboleth.data.sql.SqlOperations;
import shibboleth.data.sql.Statements;
import shibboleth.gui.ActionListener;
import shibboleth.model.GithubGraph;

/**
 * Controller of the application.
 * 
 * @author Wilco Wisse
 *
 */
public abstract class Main{
	
	protected GithubGraph graph;
	protected RateLimitValue rate;
	protected Connection connection;
	protected GithubDataSource github;
	protected SqlDataStore mysql;
	protected DataSource githubCached,mysqlOnTopOfGithub, mysqlOnGithubReadOnly, mysqlOnCacheOnGithubReadOnly ;
	protected DataStore hashCache, mysqlOnGithubStore;
	protected CommitInfoStore infoStore;
	protected SqlOperations sqlOperations;
	
	private ExeAction exeAction;
	
	public void initApp(Connection connection, GithubGraph graph){
		this.connection = connection;
		this.graph = graph;
		
		initIO();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    @Override
		    public void run() {
		        close();
		    }
		});
	}
	
	public Connection createMySqlConnection(String user, String pass){
		Connection con=null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(String.format("jdbc:mysql://localhost/shibboleth?user=%s&password=%s", user, pass));
			Statements.db_name = "mysql";
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return con;
	}
	
	public Connection createSqliteConnection(String file){
		Connection con=null;
		try {
			Class.forName("org.sqlite.JDBC");
			con = DriverManager.getConnection(String.format("jdbc:sqlite:%s", file));
			Statements.db_name = "sqlite";
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return con;
	}
	
	
	public void initIO(){
		
		// Commit info store
		infoStore = new CommitInfoStore(connection);
		
		// SQL Operations
		sqlOperations = new SqlOperations(connection);
		
		rate = new RateLimitValue();
		
		// Datasource: github api
		github = new GithubDataSource(rate);
		
		// Datastore: hashmap cache 
		hashCache = new HashMapStore();
		
		// Datasource: Github cached with hashmaps
		githubCached = new CachedSource(hashCache, github);
		
		// Datastore: Mysql 
		mysql = new SqlDataStore(connection);
		
		// Datasource: Mysql on top of github cache
		mysqlOnTopOfGithub = new CachedSource(mysql, github);
		
		// Datastore: Mysql on top of github cache
		mysqlOnGithubStore = new CachedStore(mysql, github);
		
		// Datasource: Mysql on top of Github (readonly on db)
		mysqlOnGithubReadOnly = new ReadOnlyCachedSource(mysql, github);
		
		// Datasource: Mysql on top of Cache on top of Github
		mysqlOnCacheOnGithubReadOnly = new ReadOnlyCachedSource(mysql, githubCached);
		
	}
	
	public void initActions(ActionListener listener, ActionExecutor executor){
		
		exeAction = new ExeAction(rate);
		exeAction.addActionListener(listener);
		executor.addAction(exeAction);
		
		new ToggleLabelAction()
			.addActionListener(listener)
			.addExecutor(executor)
			.addExecutor(exeAction);
		
		new InfoAction(mysqlOnCacheOnGithubReadOnly)
			.addActionListener(listener)
			.addExecutor(executor)
			.addExecutor(exeAction);

		new RateAction(rate)
			.addActionListener(listener)
			.addExecutor(executor)
			.addExecutor(exeAction);
		
		new GetAction(mysqlOnTopOfGithub, graph)
			.addActionListener(listener)
			.addExecutor(executor)
			.addExecutor(exeAction);
		
		new HideAction(graph)
			.addActionListener(listener)
			.addExecutor(executor)
			.addExecutor(exeAction);
		
		new DeleteAction(graph, mysql)
			.addActionListener(listener)
			.addExecutor(executor)
			.addExecutor(exeAction);
		
		new RefreshAction()
			.addActionListener(listener)
			.addExecutor(executor)
			.addExecutor(exeAction);
		
		new TokenAction(github)
			.addActionListener(listener)
			.addExecutor(executor)
			.addExecutor(exeAction);
		
		new CloneAction(mysqlOnTopOfGithub)
			.addActionListener(listener)
			.addExecutor(executor)
			.addExecutor(exeAction);
		
		new AnalyzeAction(mysqlOnTopOfGithub, github, infoStore, sqlOperations)
			.addActionListener(listener)
			.addExecutor(executor)
			.addExecutor(exeAction);
		
		new ExplodeAction(mysqlOnTopOfGithub, graph)
			.addActionListener(listener)
			.addExecutor(executor)
			.addExecutor(exeAction);
		
		new GephiAction(graph)
			.addActionListener(listener)
			.addExecutor(executor)
			.addExecutor(exeAction);
		
		new HighlightAction(graph, mysql)
			.addActionListener(listener)
			.addExecutor(executor)
			.addExecutor(exeAction);
		
		new ExportAction(sqlOperations)
			.addActionListener(listener)
			.addExecutor(executor)
			.addExecutor(exeAction);
		
		new HelpAction()
			.addActionListener(listener)
			.addExecutor(executor);
	}
	
	public void addToExeAction(ShibbolethAction action){
		exeAction.addAction(action);
	}
		
	public void close(){
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		String startupType = "gui";
		Proxy proxy = Proxy.NO_PROXY;
		
		int i = 0; 
		while(i<args.length){
			String arg = args[i];
			if(arg.equals("-cli")){
				startupType = "cli";
				i++;
				continue;
			}
			else if(arg.equals("-gui")){
				startupType = "gui";
				i++;
				continue;
			}
			else if(arg.equals("-crawl")){
				startupType = "crawl";
				i++;
				continue;
			}
			else if(arg.equals("-proxy") && i+2 < args.length){
				String host = args[i+1];
				int port = Integer.parseInt(args[i+2]);
				proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
				i=i+3;
			}
			else{
				System.out.println("Unknown param: " + arg);
				System.out.println("Usage: java -jar Shibboleth.jar [-cli|-gui|-crawl] [-proxy host port]");
				System.exit(0);
			}
		}
		
		if(!proxy.equals(Proxy.NO_PROXY)){
			System.out.println("Using proxy " + proxy);
			final Proxy proxy2 = proxy;
			ProxySelector.setDefault(new ProxySelector() {
				@Override
				public List<Proxy> select(URI uri) {
					return Arrays.asList(proxy2);
				}
				@Override
				public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
					ioe.printStackTrace();
				}
			});
		}
			
		if(startupType.equals("gui")){
			
			new GuiMain();
		}
		else if(startupType.equals("cli")){
			new CliMain();
		}
		else if(startupType.equals("crawl")){
			new CrawlMain();
		}
		
		
	}
	
	/**
	 * Suspend application till rate limit value reset time.
	 * @param rate RateLimitValue to sleep for.
	 */
	public static void suspend(RateLimitValue rate){
		System.out.println("Request remaining: "+rate.getRemaining());
		System.out.println("Bye... Gonna sleep. Zzzzzzz...");
		
		long expireDate = rate.getReset().getTime();
		long wakeupInterval = TimeUnit.MINUTES.toMillis(5);
		long lateNess = TimeUnit.SECONDS.toMillis(30);
		long now = System.currentTimeMillis();
		long wait = expireDate+lateNess-now;
		
		while(wait > 0) {
			System.out.println("Time remaining: "+ TimeUnit.MILLISECONDS.toMinutes(wait) + " minutes");
			long sleepTime = Math.min(wait, wakeupInterval);
			try {
				TimeUnit.MILLISECONDS.sleep(sleepTime);
			} catch (InterruptedException e) {
				System.err.println("Sleep error");
				e.printStackTrace();
				System.exit(1);
			}
			
			now = System.currentTimeMillis();
			wait = expireDate+lateNess-now;
		}
		
	}
	
	
}
