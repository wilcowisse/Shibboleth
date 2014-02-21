package shibboleth.model;

import com.google.api.client.util.Key;

/**
 * Data class which represents a Github user.
 * @author Wilco Wisse
 *
 */
public class User extends SimpleUser{
	
	@Key
    public int id;
	
    @Key
    public String name;
    
    @Key
	public String email;
    
    @Key
    public String url;
    
    @Key
	public String type;

    @Key
	public String company;
    
    @Key("public-repos")
	public int repos;
    
    @Key
	public int followers;
    
    @Key
	public int following;
    
    /**
     * @return The user represented as a String array with user properties.
     */
    public String[] getInfoArray(){
    	String n= name==null ? "":name;
    	String e= email==null ? "":email;
    	String c= company==null ?"":company;
    	String t= type==null ? "":type;
		return new String[]{
				"Login: "+ login,
				"Name: " + n,
				"Email: " + e,
				"Id: " + id,
				"Company: " + c,
				"# followers: " + followers,
				"# following: " + following,
				"# repos: " + repos,
				"Type: " + t,
				"Url: " + url};
    }
    
    @Override
    public String toString(){
    	String res = login;
    	if(name != null && !name.equals(""))
    		res+=": "+name;
    	if(email != null && !email.equals(""))
    		res+=" ("+email+")";
    	return res;
    }
    

}
