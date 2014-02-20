package shibboleth.model;

import com.google.api.client.util.Key;

/**
 * Represents a Github repo, of which we only know its name.
 * @author Wilco Wisse
 *
 */
public class SimpleRepo{
	
	public SimpleRepo(){
	}
	
	public SimpleRepo(String full_name){
		this.full_name = full_name;
	}
	
	@Key
    public String full_name;
	
	public String getShortName(){
		return full_name.substring(full_name.indexOf('/'));
	}
	
	@Override
    public String toString(){
    	return full_name;
    }
	
	@Override
	public boolean equals(Object other){
		return (other instanceof SimpleRepo) ? full_name.equals(((SimpleRepo)other).full_name) : false;
	}
	
	@Override
	public int hashCode(){
		return full_name.hashCode();
	}
}