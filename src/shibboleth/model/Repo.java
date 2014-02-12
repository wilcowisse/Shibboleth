package shibboleth.model;

import com.google.api.client.util.Key;

/**
 * Data class which represents a Github repository.
 * @author Wilco Wisse
 *
 */
public class Repo extends SimpleRepo{
	
	@Key
    public int id;
	
    @Key
    public SimpleUser owner;
    
    @Key
    public String url;
    
    @Key
    public String clone_url;

    @Key
    public SimpleRepo parent;
    
    @Key
    public boolean fork;
    
    @Key
    public int forks_count;
    
    @Key
    public int size;
    
    @Key
    public String language;
    
    public String[] getInfoArray(){
    	String p = parent!=null?parent.full_name:"";
    	String o = owner!=null?owner.login:"";
    	String l = language!=null?language:"";
    	
		return new String[]{
			"Name: " + full_name,
			"Id: " + id,
			"Owner: " + o,
			"Is forked: " + fork,
			"Parent: " + p,
			"Forks count: " + forks_count,
			"Size (kb): " + size,
			"Url: " + url,
			"Clone url: " + clone_url, 
			"Language: " + l
		};
    }
    
}
