package shibboleth.model;

/**
 * A tuple which represents a Git chunk mapped to a Github user.
 * 
 * @author Wilco Wisse
 *
 */
public class UserChunk extends Chunk{
	private SimpleUser user;
	
	/**
	 * Construct a UserChunk (i.e. a Chunk mapped to a User).
	 * @param user The user belonging to this Chunk.
	 */
	public UserChunk(SimpleUser user){
		this.user=user;
	}

	public SimpleUser getUser() {
		return user;
	}

	public void setUser(SimpleUser user) {
		this.user = user;
	}
	
	
	
	public boolean hasSameUserAs(UserChunk other){
		if(other == null)
			return false;
		else
			return user.equals(other.user);
	}
	
	public String toString(){
		return "User("+user.toString() + ") "+ super.toString();
	}
	
	
}
