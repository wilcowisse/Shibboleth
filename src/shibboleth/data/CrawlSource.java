package shibboleth.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import shibboleth.data.github.GithubDataSource;
import shibboleth.data.sql.SqlDataStore;
import shibboleth.model.Contribution;
import shibboleth.model.Repo;
import shibboleth.model.User;
import shibboleth.util.GithubUtil;

/**
 * A DataSource optimized for crawling. This cache is a hybrid between the @link{SqlDataStore} and the 
 * @link{HasMapStore}. This store assumes the database is empty initially and keeps track of what contributions
 * are in the database in memory (avoid checking the database for duplicates).
 * 
 * 
 * @author Wilco Wisse
 *
 */
public class CrawlSource implements DataSource{
	private SqlDataStore mysql;
	private GithubDataSource github;
	
	private Set<String> storedAllReposForUser, storedAllContributionsForRepo;
	private Map<Contribution, Tuple> storedContributions;
	
	private static class Tuple{
		public Tuple(int key, boolean hasStoredInfo){
			this.key = key;
			this.hasStoredInfo = hasStoredInfo;
		}
		public int key=-1;
		public boolean hasStoredInfo = false;
	}
	
	public CrawlSource(SqlDataStore cache, GithubDataSource source){
		this.mysql = cache;
		this.github=source;
		storedAllReposForUser = new HashSet<String>(1000);
		storedAllContributionsForRepo = new HashSet<String>(1000);
		storedContributions = new HashMap<Contribution,Tuple>(); // contribution -> (key,storedInfo)
	}
	
	@Override
	public Repo getRepo(String fullRepoName) {
		Repo res = github.getRepo(fullRepoName);
		mysql.storeRepo(res);
		return res;
	}

	@Override
	public User getUser(String userName) {
		User res = github.getUser(userName);
		mysql.storeUser(res);
		return res;
	}

	@Override
	public Repo[] getRepos(String user, RepoFilter filter, boolean ensureAll) {
		if(!ensureAll || storedAllReposForUser.contains(user)){
			Repo[] result = mysql.getRepos(user, filter, ensureAll);
			return result;
		}
		else{
			Repo[] repos = github.getRepos(user, filter, ensureAll);
			
			Contribution[] cs = GithubUtil.reposToContributions(repos, GithubUtil.createUser(user));
			this.storeNewContributions(cs);
			for(Repo r : repos){
				mysql.storeRepo(r);
			}
			
			mysql.storedAllContributionsByUser(user, true);
			storedAllReposForUser.add(user);
			return repos;
		}
	}
	
	@Override
	public Contribution[] getContributions(String repo, boolean ensureAll) {
		
		if(!ensureAll || storedAllContributionsForRepo.contains(repo)) {
			Contribution[] result = mysql.getContributions(repo, ensureAll);
			return result;
		}
		else{
			Contribution[] result = github.getContributions(repo, ensureAll);
			storeNewContributions(result);
			
			mysql.storedAllContributionsForRepo(repo, true);
			storedAllContributionsForRepo.add(repo);
			return result;
		}
	}
	
	/**
	 * Stores the contributions which are not already in the db.
	 * @param cs The contributions.
	 */
	public void storeNewContributions(Contribution[] cs){
		for(Contribution c : cs){
			Tuple t = storedContributions.get(c);
			if(t == null){
				int key = mysql.storeContributionWithoutInfo(c);
				Tuple stored = new Tuple(key, false);
				storedContributions.put(c, stored);
				
				if(c.hasContributionInfo()){
					mysql.storeContributionInfo(key, c.getContributionInfo());
					stored.hasStoredInfo=true;
				}
			}
			else if(!t.hasStoredInfo && c.hasContributionInfo()){
				mysql.storeContributionInfo(t.key, c.getContributionInfo());
				t.hasStoredInfo=true;
			}
			
		}
	}

	@Override
	public Contribution[] getAllContributions() {
		return mysql.getAllContributions();
	}
	

}
