package shibboleth.git;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import shibboleth.data.DataSource;
import shibboleth.data.DataStore;
import shibboleth.data.sql.CommitInfoStore;
import shibboleth.data.sql.SqlDataStore;
import shibboleth.gui.RecordLinkChooser;
import shibboleth.model.Committer;
import shibboleth.model.Contribution;
import shibboleth.model.RecordLink;
import shibboleth.model.User;
import shibboleth.winkler.JaroWinklerDistance;

/**
 * This class provides functionality to link Github users to Git committers.
 * 
 * @author Wilco Wisse
 *
 */
public class Linker {

	private String repo;
	private List<Committer> committers;
	private List<User> users;
	
	/**
	 * Construct Linker
	 * @param repo The repository to link.
	 * @param infoStore The store to retrieve committers from.
	 * @param source The source to retrieve users from.
	 */
	public Linker(String repo, CommitInfoStore infoStore, DataSource source){
		this.repo = repo;
		this.committers = infoStore.getCommitters(repo);
		try {
			this.users = getUsers(source);
		} catch (Exception e) {
			this.users = new ArrayList<User>();
			e.printStackTrace();
		}
	}
	
	/**
	 * Link committers of this repository to Github users.
	 * @return A list with recordlinks. The RecordLink similarity 
	 * ({@link RecordLink#similarity}) is computed by means of the
	 * Jaro Winkler distance.
	 * @see <a href="http://alias-i.com/lingpipe/docs/api/com/aliasi/spell/JaroWinklerDistance.html">
	 * http://alias-i.com/lingpipe/docs/api/com/aliasi/spell/JaroWinklerDistance.html</a>
	 */
	public List<RecordLink> link(){
		
		List<RecordLink> links = new ArrayList<RecordLink>();
		try{
			for(Committer committer : committers){
				RecordLink bestLink = null;
				for(User user : users){
					double proximity = 0;
					if(user.name != null && committer.name != null)
						proximity=Math.max(proximity, JaroWinklerDistance.JARO_WINKLER_DISTANCE.proximity(user.name, committer.name));
					if(user.email != null && committer.email != null)
						proximity=Math.max(proximity, JaroWinklerDistance.JARO_WINKLER_DISTANCE.proximity(user.email, committer.email));
					proximity=Math.max(proximity, JaroWinklerDistance.JARO_WINKLER_DISTANCE.proximity(user.login, committer.name));
					RecordLink link = new RecordLink(committer,user,proximity);
					bestLink = bestOne(bestLink, link);
				}
				links.add(bestLink);
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
	
	/**
	 * @return All the users of the repository.
	 */
	public List<User> getUsers(){
		return users;
	}
	
	private List<User> getUsers(DataSource source) throws Exception{
		List<User> users = new ArrayList<User>();
		Contribution[] contributions = source.getContributions(repo, false);
		for(Contribution c : contributions){
			User user = source.getUser(c.getUser().login);
			if(user != null)
				users.add(user);
			else
				throw new Exception("user " +c.getUser().login+" not found in db while linking.");
		}
		return users;
	}
	
	public static void main(String[] args){
		Connection connection = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://localhost/shibboleth?user=root&password=pass");
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		DataStore store = new SqlDataStore(connection);
		CommitInfoStore infoStore = new CommitInfoStore(connection);
		
		Linker linker = new Linker("livingston/autoSize", infoStore, store);
		List<RecordLink> links = linker.link();
		
		
		int selectedAction = RecordLinkChooser.evaluateLinks(links, linker.getUsers());
		
		if(selectedAction == RecordLinkChooser.SAVED){
			for(RecordLink link : links){
				infoStore.insertRecordLink(link);
			}
		}
	
	}
	
}


