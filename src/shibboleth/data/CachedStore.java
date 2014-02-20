package shibboleth.data;

import shibboleth.model.Contribution;
import shibboleth.model.Repo;
import shibboleth.model.User;

/**
 * A CachedSource can be used to 'chain' a <tt>DataStore</td> on top of another
 * <tt>DataStore</tt>. The first one acts as cache and the second one as source
 * of that cache. 
 * The source is accessed if the cache does not contain a requested object.
 * 
 * Note: The provided write functionality is written to the source and not to 
 * the cache.
 * 
 * @see CachedSource
 * @author Wilco Wisse
 *
 */
public class CachedStore extends CachedSource implements DataStore{
	
	private DataStore store;
	
	/**
	 * Construct a store where the read functionality of the <tt>source</tt> is cached
	 * by the <tt>store<tt>.
	 * <tt>DataSource</tt>.
	 * @param store
	 * @param source
	 */
	public CachedStore(DataStore store, DataSource source) {
		super(store, source);
		this.store=store;
	}

	@Override
	public void storeRepo(Repo repo) {
		store.storeRepo(repo);		
	}

	@Override
	public void storeUser(User user) {
		store.storeUser(user);
	}

	@Override
	public void storeContribution(Contribution c) {
		store.storeContribution(c);
	}

	@Override
	public void storeNewContributions(Contribution[] cs) {
		store.storeNewContributions(cs);
	}

	@Override
	public int deleteRepo(String fullRepoName) {
		return store.deleteRepo(fullRepoName);
	}

	@Override
	public int deleteUser(String userName) {
		return store.deleteUser(userName);
	}

	@Override
	public int deleteContributionsByUser(String user) {
		return store.deleteContributionsByUser(user);
	}

	@Override
	public int deleteContributionsByRepo(String repo) {
		return store.deleteContributionsByRepo(repo);
	}

	@Override
	public void storedAllContributionsForRepo(String repo, boolean flag) {
		store.storedAllContributionsForRepo(repo, flag);
	}

	@Override
	public void storedAllContributionsByUser(String user, boolean flag) {
		store.storedAllContributionsByUser(user, flag);
	}
	
}
