package shibboleth.data;

import java.util.List;

import shibboleth.model.Contribution;
import shibboleth.model.Repo;
import shibboleth.model.User;
import shibboleth.util.GithubUtil;

/**
 * A CachedSource can be used to 'chain' a <tt>DataStore</tt> on top of a
 * <tt>DataSource</tt>. The data store acts as cache on top of the source. 
 * The source is accessed if the cache does not contain a requested object.
 * Subsequently the retrieved objects from the source are stored in the cache.
 * 
 * @see CachedStore
 * @author Wilco Wisse
 *
 */
public class CachedSource implements DataSource {
	
	private DataStore cache;
	private DataSource source;
	

	/**
	 * Construct a cache where the <tt>DataStore</tt> caches the given 
	 * <tt>DataSource</tt>.
	 * @param cache
	 * @param source
	 */
	public CachedSource(DataStore cache, DataSource source){
		this.cache=cache;
		this.source=source;
	}
	
	@Override
	public User getUser(String login){
		User u = cache.getUser(login);
		if(u == null){ // cache miss
			u = source.getUser(login);
			if(u != null)
				cache.storeUser(u);
		}
		return u;
	}
	
	@Override
	public Repo getRepo(String reponame){
		Repo r = cache.getRepo(reponame);
		if(r == null){ // cache miss
			r = source.getRepo(reponame);
			if(r != null)
				cache.storeRepo(r);
		}
		return r;
	}
	
	@Override
	public List<Contribution> getContributions(String reponame, boolean ensureAll){
		List<Contribution> cs = cache.getContributions(reponame, ensureAll);
		if(cs == null){ // cache miss
			cs = source.getContributions(reponame, ensureAll);
			if(cs != null){
				cache.storeNewContributions(cs);
				if(ensureAll)
					cache.storedAllContributionsForRepo(reponame, true);
			}
		}
		return cs;
	}
	
	@Override
	public List<Repo> getRepos(String username, RepoFilter filter, boolean ensureAll){
		List<Repo> rs = cache.getRepos(username, filter, ensureAll);
		if(rs == null){ // cache miss
			rs = source.getRepos(username, filter, ensureAll);
			if(rs != null) {
				List<Contribution> cs = GithubUtil.reposToContributions(rs, GithubUtil.createUser(username));
				cache.storeNewContributions(cs);
				
				for(Repo r : rs){
					cache.storeRepo(r);
				}
				if(ensureAll)
					cache.storedAllContributionsByUser(username, true);
			}
		}
		return rs;
	}

	/**
	 * @return All contributions in the cache. Not all contribution in the source!
	 */
	@Override
	public List<Contribution> getAllContributions() {
		return cache.getAllContributions();
	}

}
