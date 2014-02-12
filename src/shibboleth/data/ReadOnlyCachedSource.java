package shibboleth.data;

import shibboleth.model.Contribution;
import shibboleth.model.Repo;
import shibboleth.model.User;

/**
 * This class provides the same functionality as the {@link CachedSource}, but with 
 * the difference that the cache is treated read only.
 * @author Wilco Wisse
 *
 */
public class ReadOnlyCachedSource implements DataSource {
	
	private DataStore cache;
	private DataSource source;
	
	public ReadOnlyCachedSource(DataStore cache, DataSource source){
		this.cache=cache;
		this.source=source;
	}
	
	public User getUser(String login){
		User u = cache.getUser(login);
		if(u == null){ // cache miss
			u = source.getUser(login);
		}
		return u;
	}
	
	public Contribution[] getContributions(String reponame, boolean ensureAll){
		Contribution[] cs = cache.getContributions(reponame, ensureAll);
		if(cs == null){ // cache miss
			cs = source.getContributions(reponame, ensureAll);
		}
		return cs;
	}
	
	public Repo getRepo(String reponame) {
		Repo r = cache.getRepo(reponame);
		if(r == null){ // cache miss
			r = source.getRepo(reponame);
		}
		return r;
	}
	
	public Repo[] getRepos(String username, RepoFilter filter, boolean ensureAll){
		Repo[] rs = cache.getRepos(username, filter, ensureAll);
		if(rs == null){ // cache miss
			rs = source.getRepos(username, filter, ensureAll);
		}
		return rs;
	}

	@Override
	public boolean containsUser(String userName) {
		return cache.containsUser(userName);
	}

	@Override
	public boolean containsRepo(String repoName) {
		return cache.containsRepo(repoName);
	}

	@Override
	public boolean containsContribution(String repo, String user) {
		return cache.containsContribution(repo, user);
	}

	@Override
	public boolean containsContributionInfo(String repo, String user) {
		return cache.containsContributionInfo(repo, user);
	}

	@Override
	public Contribution[] getAllContributions() {
		return cache.getAllContributions();
	}

}