package shibboleth.git;

import java.io.IOException;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

/**
 * Class to filter javascript file in a git repository.
 * @author Wilco Wisse
 *
 */
public class JSFileFilter extends TreeFilter {

	@Override
	public TreeFilter clone() {
		return this;
	}

	@Override
	public boolean include(TreeWalk walker) throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		
		String pathString = walker.getPathString();
		boolean result = !pathString.startsWith("lib/")
			&& pathString.endsWith("js")
			&& !pathString.endsWith(".min.js");
		return result;
	}

	@Override
	public boolean shouldBeRecursive() {
		return true;
	}

}
