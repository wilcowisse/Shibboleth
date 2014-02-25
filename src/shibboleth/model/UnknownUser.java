package shibboleth.model;

/**
 * A github user, but we do not know who it is...
 * 
 * @author Wilco Wisse
 *
 */
public class UnknownUser extends User {

	
	private static UnknownUser unknown;
	
	private UnknownUser(){
		login = "UNKNOWN";
	    id = -1;
	    name = null;
		email = null;
	    url = "https://github.com/404";
		type = "unknown";
		company = null;
		repos = -1;
		followers = -1;
		following = -1;
	}
	
	/**
	 * Returns the unknown user
	 * @return An instance of the unknown users.
	 */
	public static UnknownUser getInstance(){
		if(unknown==null){
			unknown = new UnknownUser();
		}
		return unknown;
	}
	
	@Override
	public String toString(){
		return "-UNKNOWN-";
	}
	
	@Override
	public boolean equals(Object other){
		if (this == other)
			return true;
		else
			return false;
	}

	
}
