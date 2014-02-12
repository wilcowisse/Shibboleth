package shibboleth.model;

/**
 * Represents a match between a git committer and a Github user.
 * 
 * @author Wilco Wisse
 *
 */
public class RecordLink implements Comparable<RecordLink>{
	
	/**
	 * The committer.
	 */
	public Committer committer;
	
	/**
	 * The user.
	 */
	public User user;
	
	/**
	 * Similarity between committer and user.
	 */
	public double similarity;
	
	/**
	 * A link between a committer and a user.
	 * @param committer The {@link Committer}
	 * @param user The {@link User}
	 * @param similarity The degree of similarity between the committer and user.
	 */
	public RecordLink(Committer committer, User user,  double similarity){
		this.user=user;
		this.committer=committer;
		this.similarity=similarity;
	}
	
	@Override
	public int compareTo(RecordLink other) {
		if(other == null)
			return 10000;
		else
			return (int) (10000d*(this.similarity-other.similarity));
	}
	
	@Override
	public String toString(){
		return committer + " --> " + user;
	}

}