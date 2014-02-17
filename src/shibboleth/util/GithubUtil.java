package shibboleth.util;

import shibboleth.model.Contribution;
import shibboleth.model.Repo;
import shibboleth.model.SimpleRepo;
import shibboleth.model.SimpleUser;

public class GithubUtil {

	public static boolean isUserName(String name){
		return name.indexOf('/')==-1;
	}
	
	public static boolean isRepoName(String name){
		return name.indexOf('/')!=-1;
	}
	
	public static SimpleUser createUser(String name){
		return new SimpleUser(name);
	}
	
	public static SimpleRepo createRepo(String name){
		return new SimpleRepo(name);
	}

	/**
	 * Converts an array of repos to an array of contributions.
	 * @param rs The repos
	 * @param user The user which contributed to these repos.
	 * @return An array with contributions for the repos in <tt>rs</tt>
	 * and an empty array if <tt>rs==null</tt>.
	 */
	public static Contribution[] reposToContributions(Repo[] rs, SimpleUser user){
		Contribution[] res = null;
		if(rs != null){
			res = new Contribution[rs.length];
			for(int i=0;i<rs.length;i++){
				res[i] = new Contribution(user, rs[i]);
			}
		}
		else{
			res=new Contribution[]{};
		}
		
		return res;
	}
	
}
