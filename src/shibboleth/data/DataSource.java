package shibboleth.data;

import java.util.List;

import shibboleth.model.Contribution;
import shibboleth.model.Repo;
import shibboleth.model.User;

/**
 * This interface represents a source which can provide Github artifacts.
 * 
 * @author Wilco Wisse
 * @see DataStore
 */

public interface DataSource {
	
	
	/**
	 * Retrieve a repo from the source.
	 * 
	 * @param 	fullRepoName The full name of the repo, e.g. octocat/HelloWorld.
	 * @return	The repo if the repo is found in this source, null otherwise.
	 */
	public Repo getRepo(String fullRepoName);
	
	/**
	 * Retrieve a user from the source
	 * 
	 * @param 	userName the name of the user, e.g. octocat.
	 * @return	The user if the user is found in this source, null otherwise.
	 */
	public User getUser(String userName);
	
	/**
	 * Retrieve all repos which {@code user} owns. 
	 * These are not all repos which the user contributed to! 
	 * 
	 * @param 	user The user to retrieve repos for.
	 * @param 	filter A filter to filter which repos should be retrieved.
	 * @param 	ensureAll Determines if <i>all</i> repos of the user should be 
	 * returned, or if a subset is sufficient. This option is useful for caches:
	 * If it is fine to return only the appropriate repos which are currently in
	 * the cache ensureAll should be <tt>false</tt>, otherwise ensureAll should
	 * be <tt>true</tt>, and the cache is bypassed.
	 * @return 	The repos of the user, <tt>null</tt> if we cannot ensure that the
	 * entire set of repos which the user owns will be returned.
	 */
	public List<Repo> getRepos(String user, RepoFilter filter, boolean ensureAll);
	
	/**
	 * Retrieve all Contributions for a repo.
	 * @param 	repo The repo to retrieve contributions to. 
	 * @param 	ensureAll Determines if <i>all</i> repos of the user should be 
	 * returned, or if a subset is sufficient. See also the 
	 * {@link #getUser(String) getUser} method.
	 * @return The contributions for the given repo.
	 */
	public List<Contribution> getContributions(String repo, boolean ensureAll);
	
	/**
	 * @return All contributions this source contains.
	 */
	public List<Contribution> getAllContributions();
		
	
}
