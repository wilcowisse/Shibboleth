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
		if(walker.isSubtree()){ // this is a dir
			String folderName = walker.getNameString();
			if(folderName.contains("lib") 
			|| folderName.contains("include") 
			|| folderName.contains("party")
			|| folderName.contains("vendor")
			|| folderName.contains("assets")
			|| folderName.contains("node_modules"))
			{
				System.out.println(" Blacklisted folder: " + walker.getPathString());
				return false;
			}
			else{
				return true;
			}
		}
		else{
			String fileName = walker.getNameString();
			return fileName.endsWith(".js") && !fileName.endsWith("min.js");
		}
	}

	@Override
	public boolean shouldBeRecursive() {
		return false;
	}

}
