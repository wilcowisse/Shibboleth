package shibboleth.data;

import shibboleth.model.Repo;

/**
 * A filter that only accepts projects which are written in JavaScript.
 * @author Wilco Wisse
 *
 */
public class JavaScriptFilter implements RepoFilter {

	@Override
	public boolean accepts(Repo r) {
		return "JavaScript".equals(r.language);
	}

}
