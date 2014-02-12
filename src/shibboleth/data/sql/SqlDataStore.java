package shibboleth.data.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;

import shibboleth.data.DataStore;
import shibboleth.data.DataUtil;
import shibboleth.data.RepoFilter;
import shibboleth.data.TransparantFilter;
import shibboleth.model.Contribution;
import shibboleth.model.ContributionInfo;
import shibboleth.model.Repo;
import shibboleth.model.SimpleRepo;
import shibboleth.model.SimpleUser;
import shibboleth.model.User;

/**
 * This class represents a data store on top of a MySql Database
 * 
 * @author Wilco Wisse
 *
 */
public class SqlDataStore implements DataStore{
	
	private PreparedStatement selectRepoSt, selectUserSt, selectReposByUserSt, selectContributionsSt, selectSingleContributionSt, selectAllContributionsSt;
	private PreparedStatement insertRepoSt, insertUserSt, insertContributionSt, insertContributionInfoSt;
	private PreparedStatement countReposSt, countUsersSt, countContributionsSt, countContributionsInfoSt;
	private PreparedStatement deleteRepoSt, deleteUserSt, deleteContributionByRepoSt, deleteContributionByUserSt, deleteContributionInfoByRepoSt, deleteContributionInfoByUserSt;
	
	private PreparedStatement insertStoredLinksSt, deleteStoredLinksSt, selectStoredLinksSt;
	
	
	/**
	 * Construct with given connection
	 * @param connection
	 */
	public SqlDataStore(Connection connection){
		try {
			
			selectRepoSt 				= connection.prepareStatement(Statements.selectRepo);
			insertRepoSt 				= connection.prepareStatement(Statements.insertRepo);
			countReposSt				= connection.prepareStatement(Statements.countRepos);
			deleteRepoSt				= connection.prepareStatement(Statements.deleteRepo);
			selectReposByUserSt 		= connection.prepareStatement(Statements.selectReposByUser);
			
			selectUserSt 				= connection.prepareStatement(Statements.selectUser);
			insertUserSt 				= connection.prepareStatement(Statements.insertUser);
			deleteUserSt				= connection.prepareStatement(Statements.deleteUser);
			countUsersSt				= connection.prepareStatement(Statements.countUsers);
			
			selectAllContributionsSt	= connection.prepareStatement(Statements.selectAllContributions);
			selectContributionsSt 		= connection.prepareStatement(Statements.selectContributions);
			selectSingleContributionSt	= connection.prepareStatement(Statements.selectSingleContribution);

			insertContributionSt 		= connection.prepareStatement(Statements.insertContribution, Statement.RETURN_GENERATED_KEYS);
			deleteContributionByRepoSt	= connection.prepareStatement(Statements.deleteContributionsByRepo);
			deleteContributionInfoByRepoSt	= connection.prepareStatement(Statements.deleteContributionInfoByRepo);
			
			deleteContributionByUserSt	= connection.prepareStatement(Statements.deleteContributionsByUser);
			deleteContributionInfoByUserSt	= connection.prepareStatement(Statements.deleteContributionInfoByUser);
			countContributionsSt		= connection.prepareStatement(Statements.countContributions);
			
			insertContributionInfoSt 	= connection.prepareStatement(Statements.insertContributionInfo);
			countContributionsInfoSt	= connection.prepareStatement(Statements.countContributionsInfo);
			insertContributionInfoSt 	= connection.prepareStatement(Statements.insertContributionInfo);
			
			insertStoredLinksSt			= connection.prepareStatement(Statements.insertStoredLinks);
			deleteStoredLinksSt			= connection.prepareStatement(Statements.deleteStoredLinks);
			selectStoredLinksSt			= connection.prepareStatement(Statements.selectStoredLinks);
					
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public Repo getRepo(String fullRepoName) {
		Repo result = null;
		try {
			selectRepoSt.setString(1, fullRepoName);
			ResultSet resultSet = selectRepoSt.executeQuery();
			
			if(resultSet.next()){
				result = new Repo();
				result.id 			= 	resultSet.getInt("id");
				result.full_name 	= 	resultSet.getString("full_name");
				result.owner 		= 	new SimpleUser(resultSet.getString("owner"));
				result.url 			= 	resultSet.getString("url");
				result.clone_url 	= 	resultSet.getString("clone_url");
				result.parent 		= 	new SimpleRepo(resultSet.getString("parent"));
				result.fork 		= 	resultSet.getBoolean("fork");
				result.forks_count 	=	resultSet.getInt("forks_count");
				result.size 		= 	resultSet.getInt("size");
				result.language 	= 	resultSet.getString("language");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public User getUser(String userName) {
		User result = null;
		try {
			selectUserSt.setString(1, userName);
			ResultSet resultSet = selectUserSt.executeQuery();
			
			if(resultSet.next()){
				result = new User();
				result.id 			= 	resultSet.getInt("id");
				result.login 		= 	resultSet.getString("login");
				result.name 		= 	resultSet.getString("name");
				result.email 		= 	resultSet.getString("email");
				result.url 			= 	resultSet.getString("url");
				result.type 		= 	resultSet.getString("type");
				result.company 		= 	resultSet.getString("company");
				result.repos 		= 	resultSet.getInt("repos");
				result.followers 	= 	resultSet.getInt("followers");
				result.following 	= 	resultSet.getInt("following");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	@Override
	public Repo[] getRepos(String user, RepoFilter filter, boolean ensureAll) {
		if(!ensureAll || containsRecordLink(user)) {
			List<Repo> repoList = new ArrayList<Repo>();
			try {
				selectReposByUserSt.setString(1, user);
				ResultSet resultSet = selectReposByUserSt.executeQuery();
				while(resultSet.next()){
					Repo repo = new Repo();
					repo.id 			= 	resultSet.getInt("id");
					repo.full_name 		= 	resultSet.getString("full_name");
					repo.owner 			= 	new SimpleUser(resultSet.getString("owner"));
					repo.url 			= 	resultSet.getString("url");
					repo.clone_url 		= 	resultSet.getString("clone_url");
					repo.parent 		= 	new SimpleRepo(resultSet.getString("parent"));
					repo.fork 			= 	resultSet.getBoolean("fork");
					repo.forks_count	=	resultSet.getInt("forks_count");
					repo.size 			= 	resultSet.getInt("size");
					repo.language 		= 	resultSet.getString("language");
					
					if(filter.accepts(repo))
						repoList.add(repo);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return repoList.toArray(new Repo[]{});
		}
		else{
			return null;
		}
	}
	
	@Override
	public Contribution[] getContributions(String repo, boolean ensureAll) {
		if(!ensureAll || containsRecordLink(repo)) {
			Contribution[] result = null;
			try {
				selectContributionsSt.setString(1, repo);
				result = getContributions(selectContributionsSt);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return result;
		}
		else{
			return null;
		}
	}
	
	@Override
	public Contribution[] getAllContributions() {
		return getContributions(selectAllContributionsSt);
	}
	
	
	private Contribution[] getContributions(PreparedStatement statement) {
		Contribution[] result = null;
		try {
			ResultSet resultSet = statement.executeQuery();
			
			List<Contribution> contributionList = new ArrayList<Contribution>();
			while(resultSet.next()){
				SimpleUser u 	= new SimpleUser(resultSet.getString("user_name"));
				int count 		= resultSet.getInt("count");
				int percentage 	= resultSet.getInt("percentage");
				SimpleRepo r 	= new SimpleRepo(resultSet.getString("repo_name"));
				Contribution c 	= new Contribution(u,r);
				
				if(count!=0){
					ContributionInfo info = new ContributionInfo(count, percentage);
					c.setContributionInfo(info);
				}
				
				contributionList.add(c);
			}
			
			result = contributionList.toArray(new Contribution[]{});

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	@Override
	public void storeContributions(Contribution[] cs) {
		for(Contribution c : cs){
			storeContribution(c);
		}
	}

	@Override
	public void storeRepo(Repo repo) {
		try {
			String p= repo.parent==null ? "" : repo.parent.full_name;
			String o = repo.owner==null?"":repo.owner.login;
			String l =repo.language==null ? "" : repo.language;
			insertRepoSt.setInt(1, repo.id);
			insertRepoSt.setString(2, repo.full_name);
			insertRepoSt.setString(3, o);
			insertRepoSt.setString(4, repo.url);
			insertRepoSt.setString(5, repo.clone_url);
			insertRepoSt.setString(6, p);
			insertRepoSt.setBoolean(7, repo.fork);
			insertRepoSt.setInt(8, repo.forks_count);
			insertRepoSt.setInt(9, repo.size);
			insertRepoSt.setString(10, l);
			
			insertRepoSt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	@Override
	public void storeUser(User user) {
		
		try {
			String n= user.name==null ? "":user.name;
	    	String e= user.email==null ? "":user.email;
	    	String c= user.company==null ? "":user.company;
	    	String t= user.type==null ? "":user.type;
			insertUserSt.setInt(1, user.id);
			insertUserSt.setString(2, user.login);
			insertUserSt.setString(3, n);
			insertUserSt.setString(4, e);
			insertUserSt.setString(5, user.url);
			insertUserSt.setString(6, t);
			insertUserSt.setString(7, c);
			insertUserSt.setInt(8, user.repos);
			insertUserSt.setInt(9, user.followers);
			insertUserSt.setInt(10, user.following);
			
			insertUserSt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void storeContribution(Contribution c) {	
		String repoName=c.getRepo().full_name;
		String userName=c.getUser().login;
		if(containsContribution(repoName, userName)){
			if(c.hasContributionInfo()){
				try {
					selectSingleContributionSt.setString(1, repoName);
					selectSingleContributionSt.setString(2, userName);
					ResultSet resultSet = selectSingleContributionSt.executeQuery();
					if(resultSet.next()){
						int id = resultSet.getInt("id");
						storeContributionInfo(id, c.getContributionInfo());
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		else{
			try {
				insertContributionSt.setString(1, c.getRepo().full_name);
				insertContributionSt.setString(2, c.getUser().login);
				insertContributionSt.executeUpdate();
				
				int key = -1;
				ResultSet generatedKeys = insertContributionSt.getGeneratedKeys();
				if(generatedKeys.next()){
					key = generatedKeys.getInt(1);
				}
				else{
					throw new SQLException("Creating contribution failed, no generated key obtained.");
				}
				
				if(c.hasContributionInfo())
					storeContributionInfo(key, c.getContributionInfo());
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Store Contribution in the database
	 * @param key The unique id of the contribution
	 * @param info  Contribution info belonging to the contribution with <tt>key</tt>.
	 */
	public void storeContributionInfo(int key, ContributionInfo info){
		try {
			insertContributionInfoSt.setInt(1, key);
			insertContributionInfoSt.setInt(2, info.count);
			insertContributionInfoSt.setInt(3, info.percentage);
			insertContributionInfoSt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean containsUser(String userName) {
		try {
			countUsersSt.setString(1, userName);
			return resultCount(countUsersSt)>0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean containsRepo(String repoName) {
		try {
			countReposSt.setString(1, repoName);
			return resultCount(countReposSt)>0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean containsContribution(String repo, String user) {
		try {
			countContributionsSt.setString(1, repo);
			countContributionsSt.setString(2, user);
			return resultCount(countContributionsSt)>0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean containsContributionInfo(String repo, String user) {
		try {
			countContributionsInfoSt.setString(1, repo);
			countContributionsInfoSt.setString(2, user);
			return resultCount(countContributionsSt)>0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private int resultCount(PreparedStatement st){
		ResultSet resultSet;
		try {
			resultSet = st.executeQuery();
			if(resultSet.next()) {
			    int count = resultSet.getInt(1);
			    return count;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	@Override
	public int deleteRepo(String fullRepoName) {
		int res = 0;
		try {
			deleteContributionsByRepo(fullRepoName);
			deleteRepoSt.setString(1, fullRepoName);
			res = deleteRepoSt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}

	@Override
	public int deleteUser(String userName) {
		int res = 0;
		try {
			deleteContributionsByUser(userName);
			deleteUserSt.setString(1, userName);
			res = deleteUserSt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}

	@Override
	public int deleteContributionsByUser(String user) {
		int res = 0;
		try {
			storedAllContributionsByUser(user, false);
			for(Contribution c : DataUtil.reposToContributions(
					getRepos(user, new TransparantFilter(), false), new SimpleUser(user)))
			{
				storedAllContributionsForRepo(c.getRepo().full_name, false);
			}
			
			deleteContributionInfoByUserSt.setString(1, user);
			res = deleteContributionInfoByUserSt.executeUpdate();
			deleteContributionByUserSt.setString(1, user);
			res += deleteContributionByUserSt.executeUpdate();
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}

	@Override
	public int deleteContributionsByRepo(String repo) {
		int res = 0;
		try {
			Contribution[] contributionsByUser = getContributions(repo, false);
			storedAllContributionsForRepo(repo, false);
			for(Contribution c : contributionsByUser){
				storedAllContributionsByUser(c.getUser().login, false);
			}
			
			deleteContributionInfoByRepoSt.setString(1, repo);
			res = deleteContributionInfoByRepoSt.executeUpdate();
			deleteContributionByRepoSt.setString(1, repo);
			res += deleteContributionByRepoSt.executeUpdate();
			
			storedAllContributionsForRepo(repo, false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}


	@Override
	public void storedAllContributionsForRepo(String repo, boolean flag) {
		try {
			if(flag){
				insertStoredLinksSt.setString(1, repo);
				insertStoredLinksSt.setString(2, "repo");
				insertStoredLinksSt.executeUpdate();
			}
			else{
				deleteStoredLinksSt.setString(1, repo);
				deleteStoredLinksSt.executeUpdate();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	
	@Override
	public void storedAllContributionsByUser(String user, boolean flag) {
		try {
			if(flag){
				insertStoredLinksSt.setString(1, user);
				insertStoredLinksSt.setString(2, "user");
				insertStoredLinksSt.executeUpdate();
			}
			else{
				deleteStoredLinksSt.setString(1, user);
				deleteStoredLinksSt.executeUpdate();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	private boolean containsRecordLink(String key){
		boolean result = false;
		try {
			selectStoredLinksSt.setString(1, key);
			ResultSet resultSet = selectStoredLinksSt.executeQuery();
			result = resultSet.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	@SuppressWarnings("unused")
	public static void main(String[] args){
		
		Connection connection = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://localhost/shibboleth?user=root&password=pass");
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		SqlDataStore store = new SqlDataStore(connection);
		
		Contribution c = new Contribution(new SimpleUser("emile"), new SimpleRepo("jan/projectx"));
		c.setContributionInfo(new ContributionInfo(100, 100));
		
		
//		store.storeContribution(c);

//		store.storedAllContributionsByUser("test",false);
//		
//		User jan = store.getUser("jan");
//		User piet = store.getUser("piet");
//		
//		System.out.println(jan);
//		System.out.println(piet);
//		
//		Repo[] reposOfJan = store.getReposByUser("jan");
//		System.out.println(Arrays.toString(reposOfJan));
//		
//		Contribution[] reposOfJan = store.getContributions("jan/projectx");
//		
//		for(Contribution c : reposOfJan){
//			System.out.print(c);
//			if(c.hasContributionInfo()){
//				System.out.println(c.getContributionInfo());
//			}
//			else{
//				System.out.println();
//			}
//			
//		}
//		
//		User jan = store.getUser("jan");
//		jan.id=100;
//		jan.login = "test";
//		store.storeUser(jan);
//		
//		
//		Repo projectx = store.getRepo("jan/projectx");
//		projectx.id=100;
//		projectx.full_name = "test/test";
//		store.storeRepo(projectx);
//		
//		Contribution c = new Contribution(new SimpleUser("emile"), new SimpleRepo("jan/projectx"));
//		c.setContributionInfo(new ContributionInfo(100, 100));
//		//store.storeContribution(c);
//		
	}
	

}
