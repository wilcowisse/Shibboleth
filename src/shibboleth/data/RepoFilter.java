package shibboleth.data;

import shibboleth.model.Repo;

/**
 * A utility class for filtering repos based on their properties.
 * 
 * @author Wilco Wisse
 *
 */
public interface RepoFilter {
	/**
	 * @return Returns <tt>true</tt> iff the given repo should not be rejected
	 * by this filter.
	 */
	public boolean accepts(Repo repo);
}
