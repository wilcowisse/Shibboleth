package shibboleth.util;

import java.util.ArrayList;
import java.util.List;

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
	public static List<Contribution> reposToContributions(List<Repo> rs, SimpleUser user){
		List<Contribution> res = null;
		if(rs != null){
			res = new ArrayList<Contribution>(rs.size());
			for(Repo r : rs){
				res.add(new Contribution(user, r));
			}
		}
		else{
			res=new ArrayList<Contribution>();
		}
		
		return res;
	}
	
	public static List<SimpleUser> contributionsToUsers(List<Contribution> cs){
		List<SimpleUser> res = new ArrayList<SimpleUser>(cs.size());
		for(Contribution c : cs){
			res.add(c.getUser());
		}
		return res;
	}
	
}
