package shibboleth.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import shibboleth.data.github.GithubDataSource;
import shibboleth.data.sql.SqlDataStore;
import shibboleth.model.Contribution;
import shibboleth.model.ContributionId;
import shibboleth.model.Repo;
import shibboleth.model.User;
import shibboleth.util.GithubUtil;

/**
 * A DataSource optimized for crawling. This cache is a hybrid between the {@link SqlDataStore} and the 
 * {@link HashMapStore}. This store assumes the database is empty initially and keeps track of what contributions
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
	private Map<Integer,ContributionId> storedContributions;
	
	public CrawlSource(SqlDataStore cache, GithubDataSource source){
		this.mysql = cache;
		this.github=source;
		storedAllReposForUser = new HashSet<String>(1000);
		storedAllContributionsForRepo = new HashSet<String>(1000);
		storedContributions = new HashMap<Integer,ContributionId>();
		
		System.out.print("Retrieving contributions from db...");
		for(ContributionId c : mysql.getAllContributionIds()){
			storedContributions.put(c.hashCode(), c);
		}
		System.out.print("done.");
		
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
	public List<Repo> getRepos(String user, RepoFilter filter, boolean ensureAll) {
		if(!ensureAll || storedAllReposForUser.contains(user)){
			List<Repo> result = mysql.getRepos(user, filter, ensureAll);
			return result;
		}
		else{
			List<Repo> repos = github.getRepos(user, filter, ensureAll);
			
			List<Contribution> cs = GithubUtil.reposToContributions(repos, GithubUtil.createUser(user));
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
	public List<Contribution> getContributions(String repo, boolean ensureAll) {
		
		if(!ensureAll || storedAllContributionsForRepo.contains(repo)) {
			List<Contribution> result = mysql.getContributions(repo, ensureAll);
			return result;
		}
		else{
			List<Contribution> result = github.getContributions(repo, ensureAll);
			this.storeNewContributions(result);
			
			mysql.storedAllContributionsForRepo(repo, true);
			storedAllContributionsForRepo.add(repo);
			return result;
		}
	}
	
	/**
	 * Stores the contributions which are not already in the db.
	 * @param cs The contributions.
	 */
	public void storeNewContributions(List<Contribution> cs){
		for(Contribution c : cs){
			ContributionId t = storedContributions.get(c.hashCode());
			
			if(t == null) {
				t = mysql.storeContributionWithoutInfo(c);
				storedContributions.put(t.hashCode(), t);
				
				if(c.hasContributionInfo()){
					mysql.storeContributionInfo(t.getKey(), c.getContributionInfo());
				}
			}
			else if(!t.hasContributionInfo() && c.hasContributionInfo()) {
				mysql.storeContributionInfo(t.getKey(), c.getContributionInfo());
				t.setContributionInfo(c.getContributionInfo());
			}
			// else: do nothing
			
		}
	}

	@Override
	public List<Contribution> getAllContributions() {
		return mysql.getAllContributions();
	}
	

}
