package shibboleth.data;

import shibboleth.model.Repo;

/**
 * A filter which accepts all projects.
 * 
 * @author Wilco Wisse
 *
 */
public class TransparantFilter implements RepoFilter {

	@Override
	public boolean accepts(Repo r) {
		return true;
	}

}
