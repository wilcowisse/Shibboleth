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
	 * Returns the unkown user
	 * @return
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

	
}
