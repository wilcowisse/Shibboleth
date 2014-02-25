package shibboleth.git;

import java.util.List;

import shibboleth.model.RecordLink;

/**
 * Functionality to link Github users to Git committers.
 * 
 * @author Wilco Wisse
 *
 */
public interface Linker {

	/**
	 * Link committers to github users
	 * @return A list with links from committers to users.
	 */
	public List<RecordLink> link();
}
