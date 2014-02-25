package shibboleth.model;

import java.util.Objects;

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
	public SimpleUser user;
	
	/**
	 * Similarity between committer and user.
	 */
	public double similarity;
	
	
	public RecordLink(Committer committer, SimpleUser user){
		this.user=user;
		this.committer=committer;
		this.similarity=1;
	}
	
	/**
	 * Contruct record link
	 * @param committer The {@link Committer}
	 * @param user The {@link User}
	 * @param similarity The degree of similarity between the committer and user.
	 */
	public RecordLink(Committer committer, SimpleUser user, double similarity){
		this.user=user;
		this.committer=committer;
		this.similarity=similarity;
	}
	
	/**
	 * Set the similarity of this link.
	 * @param similarity The similarity between committer and user.
	 */
	public void setSimilarity(double similarity){
		this.similarity = similarity;
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


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof RecordLink))
			return false;
		RecordLink other = (RecordLink) obj;
		if (committer == null) {
			if (other.committer != null)
				return false;
		} else if (!committer.equals(other.committer))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(committer,user);
	}


	
	

}