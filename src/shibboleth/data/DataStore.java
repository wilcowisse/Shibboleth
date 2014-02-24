package shibboleth.data;

import java.util.List;

import shibboleth.model.Contribution;
import shibboleth.model.Repo;
import shibboleth.model.User;

/**
 * This class extends a DataSource in order to provide write functionality 
 * to a source.
 * @author Wilco Wisse
 *
 */
public interface DataStore extends DataSource{
	
	/**
	 * Store a repo, if repo has not been stored.
	 * @param repo The repo to be stored, should not be <tt>null</tt>.
	 */
	public void storeRepo(Repo repo);
	
	/**
	 * Store a user, if user has not been stored.
	 * @param user The user to be stored, should not be <tt>null</tt>.
	 */
	public void storeUser(User user);
	
	/**
	 * Store a contribution.
	 * @param c The contribution to be stored, should not be <tt>null</tt>.
	 */
	public void storeContribution(Contribution c);
	
	/**
	 * Store multiple contributions. Stores only those contributions which 
	 * do not exist in this store.
	 * @param cs an array with contributions to be stored.
	 */
	public void storeNewContributions(List<Contribution> cs);
	
	/**
	 * Remove all repos with the given repo name.
	 * Also the contribtutions of this repo are
	 * removed.
	 * @param fullRepoName The name of the repo
	 * @return The number of deleted items.
	 */
	public int deleteRepo(String fullRepoName);
	
	/**
	 * Remove all users with the given user name.
	 * Als the contributions by this user are 
	 * removed.
	 * @param userName The name of the user
	 * @return The number of deleted items.
	 */
	public int deleteUser(String userName);
	
	/**
	 * Remove all contributions of a user
	 * @param user The name of the user.
	 * @return The number of deleted items.
	 */
	public int deleteContributionsByUser(String user);
	
	/**
	 * Remove all contributions for a repo
	 * @param repo The name of the repo.
	 * @return The number of deleted items.
	 */
	public int deleteContributionsByRepo(String repo);
	
	/**
	 * Method to indicate whether all contribution to the given repo have been saved 
	 * in this data store. This enables {@link #getContributions(String, boolean)}
	 * to return a non <tt>null</tt> result if we call this function with <tt>ensureAll</tt>
	 * set to <tt>true</tt>.
	 * @param repo The repo
	 * @param flag Indicates whether all contributions have been saved or not.
	 */
	public void storedAllContributionsForRepo(String repo, boolean flag);
	
	/**
	 * Method to indicate whether all contribution by the given user have been saved 
	 * in this data store. This enables {@link #getRepos(String, RepoFilter, boolean)}
	 * to return a non <tt>null</tt> result if we call this function with <tt>ensureAll</tt>
	 * set to <tt>true</tt>.
	 * @param user The user
	 * @param flag Indicates whether all related contributions have been saved
	 */
	public void storedAllContributionsByUser(String user, boolean flag);
	
}
