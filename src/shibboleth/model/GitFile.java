package shibboleth.model;

import java.util.Objects;

/**
 * Represents a file of a given commit in a cloned git repository.
 * A file can uniquely be identified by the triple 
 * <tt>(repository name, commit hash, fileName)</tt>.
 * @author Wilco Wisse
 */
public class GitFile {
	
	/**
	 * The github repo name.
	 */
	public String repo;
	
	/**
	 * The commit hash for the commit this file belongs to.
	 */
	public String head;
	
	/**
	 * The file name of this file.
	 */
	public String filePath;
	
	@Override
	public String toString(){
		return String.format("File: %s %s %s", repo, head, filePath);
	}
	
	@Override
	public boolean equals(Object other){
		return other instanceof GitFile ? 
				   (repo     != null ? repo.equals(((GitFile)other).repo)         : ((GitFile)other).repo == null)
				&& (head     != null ? head.equals(((GitFile)other).head)         : ((GitFile)other).head == null)
				&& (filePath != null ? filePath.equals(((GitFile)other).filePath) : ((GitFile)other).filePath == null)
				: false;
	}
	
	@Override
	public int hashCode(){
		return Objects.hash(repo,head,filePath);
	}
}
