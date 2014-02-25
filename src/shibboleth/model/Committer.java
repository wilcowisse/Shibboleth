package shibboleth.model;

import java.util.Objects;

import com.google.api.client.util.Key;

import shibboleth.git.JaroWinklerLinker;

/**
 * A committer is someone who committed to a git project.
 * Assumption: a committer can uniquely be identified by the triple 
 * <tt>(repository name, email, name)</tt>.
 * 
 * In contrary to github, Git is not the sort of application which has 
 * a central database with unique user identification id's. This is 
 * also why Committers should manually be linked to Github users.
 * 
 * @see JaroWinklerLinker
 * 
 * @author Wilco Wisse
 *
 */
public class Committer {
	
	/**
	 * The repo
	 */
	public String repo;
	
	/**
	 * The email of the committer
	 */
	@Key
	public String email;
	
	/**
	 * The name of the committer.
	 */
	@Key
	public String name;
	
	@Override
	public boolean equals(Object other){
		return other instanceof Committer ? 
				   (repo  != null ? repo.equals(((Committer)other).repo)   : ((Committer)other).repo == null)
				&& (email != null ? email.equals(((Committer)other).email) : ((Committer)other).email == null)
				&& (name  != null ? name.equals(((Committer)other).name)   : ((Committer)other).name == null)
				: false;
	}
	
	@Override
	public String toString(){
		return String.format("%s (%s)", name, email);
	}
	
	@Override
	public int hashCode(){
		return Objects.hash(repo,email,name);
	}
}
