package shibboleth.git;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import shibboleth.model.Committer;
import shibboleth.model.RecordLink;
import shibboleth.model.UnknownUser;
import shibboleth.model.User;
import shibboleth.winkler.JaroWinklerDistance;

/**
 * This linker links commiters to users by using the Jaro Winkler distance.
 * 
 * @author Wilco Wisse
 *
 */
public class JaroWinklerLinker implements Linker{

	private List<Committer> committers;
	private List<User> users;
	private double accuracy;
	
	/**
	 * Construct Linker
	 * @param committers The committers to be linked.
	 * @param users The users to be linked to.
	 * @param accuracy If similarity between user and committer is below this threshold, this committer is linked to the @link{UnknownUser}.
	 */
	public JaroWinklerLinker(List<Committer> committers, List<User> users, double accuracy){
		this.accuracy = accuracy;
		this.committers = committers;
		this.users = users;
	}
	
	/**
	 * Link committers to Github users.
	 * @return A list with recordlinks. The RecordLink similarity 
	 * ({@link RecordLink#similarity}) is computed by means of the
	 * Jaro Winkler distance.
	 * @see <a href="http://alias-i.com/lingpipe/docs/api/com/aliasi/spell/JaroWinklerDistance.html">
	 * http://alias-i.com/lingpipe/docs/api/com/aliasi/spell/JaroWinklerDistance.html</a>
	 */
	@Override
	public List<RecordLink> link(){
		List<RecordLink> links = new ArrayList<RecordLink>();
		try{
			for(Committer committer : committers){
				RecordLink bestLink = null;
				for(User user : users){
					double similarity = 0;
					if(user.name != null && committer.name != null)
						similarity=Math.max(similarity, JaroWinklerDistance.JARO_WINKLER_DISTANCE.proximity(user.name, committer.name));
					if(user.email != null && committer.email != null){
						String userEmailName = user.email.indexOf('@') == -1 ? user.email : user.email.substring(0,user.email.indexOf('@'));
						String committerEmailName = committer.email.indexOf('@') == -1 ? committer.email : committer.email.substring(0,committer.email.indexOf('@'));
						similarity=Math.max(similarity, JaroWinklerDistance.JARO_WINKLER_DISTANCE.proximity(userEmailName, committerEmailName));
					}
					similarity=Math.max(similarity, JaroWinklerDistance.JARO_WINKLER_DISTANCE.proximity(user.login, committer.name));
					RecordLink link = new RecordLink(committer,user, similarity);
					bestLink = bestOne(bestLink, link);
				}
				
				if(bestLink.similarity>=accuracy){
					links.add(bestLink);
				}
				else{
					RecordLink unknownLink = new RecordLink(committer, UnknownUser.getInstance(), bestLink.similarity);
					links.add(unknownLink);
				}
			}
		} catch(Exception e){
			e.printStackTrace();
		}
		Collections.sort(links);
		return links;
	}
	
	private RecordLink bestOne(RecordLink one, RecordLink two){
		if(one == null)
			return two;
		else if(one.compareTo(two) > 0)
			return one;
		else
			return two;
	}

	
}


