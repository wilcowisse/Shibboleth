package shibboleth.model;

import com.google.api.client.util.Key;

/**
 * Represents a Github user of which we only know his username.
 * @author Wilco Wisse
 *
 */
public class SimpleUser{
	
	public SimpleUser(){
	}
	
	public SimpleUser(String login){
		this.login = login;
	}
	
	@Key
    public String login;
	
	@Override
    public String toString(){
    	return login;
    }
	
	/**
	 * Two Users are equal iff their login names match.
	 */
	@Override
	public boolean equals(Object other){
		return (other instanceof SimpleUser) ? login.equals(((SimpleUser)other).login) : false;
	}
    
	@Override
	public int hashCode(){
		return login.hashCode();
	}
}