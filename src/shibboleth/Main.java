package shibboleth;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import shibboleth.actions.ActionExecutor;
import shibboleth.actions.CloneAction;
import shibboleth.actions.DeleteAction;
import shibboleth.actions.ExeAction;
import shibboleth.actions.ExplodeAction;
import shibboleth.actions.ExportAction;
import shibboleth.actions.GetAction;
import shibboleth.actions.GraphLayoutAction;
import shibboleth.actions.GetInfoAction;
import shibboleth.actions.HelpAction;
import shibboleth.actions.HideAction;
import shibboleth.actions.RateAction;
import shibboleth.actions.RefreshAction;
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
import shibboleth.data.sql.Statements;
import shibboleth.gui.ActionListener;
import shibboleth.model.GitGraph;

/**
 * Controller of the application.
 * 
 * @author Wilco Wisse
 *
 */
public abstract class Main{
	
	private GitGraph graph;
	private RateLimitValue rate;
	private Connection connection;
	private GithubDataSource github;
	private DataSource githubCached,mysqlOnTopOfGithub, mysqlCachedReadOnly;
	private DataStore mysql,hashCache, mysqlStoreOnTopOfGithub;
	private CommitInfoStore infoStore;
	
	
	public Main(){
		initApp();
	}
	
	public void initApp(){
		// connection = getMySqlConnection("root", "pass");
		// OR:
		initApp(createSqliteConnection("db/db.sqlite"));
	}
	
	public void initApp(Connection connection){
		this.connection = connection;
		graph = new GitGraph();
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
		
		// Datasource: github api
		rate = new RateLimitValue();
		github = new GithubDataSource(rate);
		github.setAccessToken(null);
		
		// Datastore: hashmap cache 
		hashCache = new HashMapStore();
		
		// Datasource: Github cached with hashmaps
		githubCached = new CachedSource(hashCache, github);
		
		// Datastore: Mysql 
		mysql = new SqlDataStore(connection);
		
		// Datasource: Mysql on top of github cache
		mysqlOnTopOfGithub = new CachedSource(mysql, githubCached);
		
		// Datastore: Mysql on top of github cache
		mysqlStoreOnTopOfGithub = new CachedStore(mysql, githubCached);
		
		// Datasource: Mysql on top of Github (readonly on db)
		mysqlCachedReadOnly = new ReadOnlyCachedSource(mysql, githubCached);
				
	}
	
	public void initActions(ActionListener listener, ActionExecutor executor){
		
		ExeAction exeAction = new ExeAction(rate);
		exeAction.setActionListener(listener);
		executor.addAction(exeAction);
		
		new ToggleLabelAction()
			.setActionListener(listener)
			.addExecutor(executor)
			.addExecutor(exeAction);
		
		new GraphLayoutAction(graph)
			.setActionListener(listener)
			.addExecutor(executor)
			.addExecutor(exeAction);
		
		new GetInfoAction(mysqlCachedReadOnly)
			.setActionListener(listener)
			.addExecutor(executor)
			.addExecutor(exeAction);

		new RateAction(rate)
			.setActionListener(listener)
			.addExecutor(executor)
			.addExecutor(exeAction);
		
		new GetAction(mysqlOnTopOfGithub, graph)
			.setActionListener(listener)
			.addExecutor(executor)
			.addExecutor(exeAction);
		
		new HideAction(graph)
			.setActionListener(listener)
			.addExecutor(executor)
			.addExecutor(exeAction);
		
		new DeleteAction(graph, mysql)
			.setActionListener(listener)
			.addExecutor(executor)
			.addExecutor(exeAction);
		
		new RefreshAction()
			.setActionListener(listener)
			.addExecutor(executor)
			.addExecutor(exeAction);
		
		new TokenAction(github)
			.setActionListener(listener)
			.addExecutor(executor)
			.addExecutor(exeAction);
		
		new CloneAction(mysqlOnTopOfGithub, infoStore)
			.setActionListener(listener)
			.addExecutor(executor)
			.addExecutor(exeAction);
		
		new ExplodeAction(mysqlStoreOnTopOfGithub, graph)
			.setActionListener(listener)
			.addExecutor(executor)
			.addExecutor(exeAction);
		
		new ExportAction(graph)
			.setActionListener(listener)
			.addExecutor(executor)
			.addExecutor(exeAction);
		
		new HelpAction()
			.setActionListener(listener)
			.addExecutor(executor);
	}
		
	public void close(){
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
}
