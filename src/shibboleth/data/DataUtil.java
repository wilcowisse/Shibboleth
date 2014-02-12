package shibboleth.data;

import shibboleth.model.Contribution;
import shibboleth.model.Repo;
import shibboleth.model.SimpleUser;

public class DataUtil {
	
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
