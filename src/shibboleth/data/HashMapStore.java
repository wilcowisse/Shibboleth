package shibboleth.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import shibboleth.model.Contribution;
import shibboleth.model.ContributionInfo;
import shibboleth.model.Repo;
import shibboleth.model.SimpleRepo;
import shibboleth.model.SimpleUser;
import shibboleth.model.User;

/**
 * A Github Data Store that stores Github objects in HashMaps and Lists.
 * 
 * @author Wilco Wisse
 *
 */
public class HashMapStore implements DataStore{
		
	private List<Contribution> contributions;
	private List<String> storedAllReposForUser, storedAllContributionsForRepo;
	private Map<String, Repo> repos;
	private Map<String, User> users;
	
	public HashMapStore(){
		users = new HashMap<String,User>();
		repos = new HashMap<String, Repo>();
		contributions = new ArrayList<Contribution>();
		storedAllReposForUser = new ArrayList<String>();
		storedAllContributionsForRepo = new ArrayList<String>();
	}
	
	@Override
	public User getUser(String login){
		User u = users.get(login);
		return u;
	}
	
	@Override
	public Contribution[] getContributions(String reponame, boolean ensureAll){
		if(!ensureAll || storedAllContributionsForRepo.contains(reponame)){
			List<Contribution> result = new ArrayList<Contribution>();
			for(Contribution c: contributions){
				if(c.getRepo().full_name.equals(reponame)){
					result.add(c);
				}
			}
			return result.toArray(new Contribution[]{});
		}
		else{
			return null;
		}
	}
	
	@Override
	public Repo getRepo(String reponame){
		Repo r = repos.get(reponame);
		return r;
	}
	
	@Override
	public Repo[] getRepos(String username, RepoFilter filter, boolean ensureAll){
		if(!ensureAll || storedAllReposForUser.contains(username)){
			List<Repo> result = new ArrayList<Repo>();
			for(Contribution c: contributions){
				if(c.getUser().login.equals(username)){
					Repo r = repos.get(c.getRepo().full_name);
					assert r!= null;
					if(r != null)
						result.add(r);
				}
			}
			return result.toArray(new Repo[]{});
		}
		else{
			return null;
		}
			
	}
	
	
	/**
	 * Empty this entire store
	 */
	public void clear(){
		users.clear();
		contributions.clear();
		repos.clear();
		storedAllReposForUser.clear();
		storedAllContributionsForRepo.clear();
	}

	@Override
	public boolean containsUser(String userName) {
		return users.containsKey(userName);
	}

	@Override
	public boolean containsRepo(String repoName) {
		return repos.containsKey(repoName);
	}

	@Override
	public boolean containsContribution(String repo, String user) {
		return getContribution(repo, user) != null;
	}

	@Override
	public boolean containsContributionInfo(String repo, String user) {
		Contribution c = getContribution(repo, user);
		if(c != null)
			return c.hasContributionInfo();
		else	
			return false;
	}
	
	@Override
	public void storeRepo(Repo repo) {
		repos.put(repo.full_name, repo);
	}

	@Override
	public void storeUser(User user) {
		users.put(user.login, user);
	}
	
	/**
	 * Stores a contribution. 
	 * Does not store the repo or user of the given contribution.
	 */
	@Override
	public void storeContribution(Contribution c) {
		if(!contributions.contains(c))
			contributions.add(c);
	}
	
	@Override
	public void storeContributions(Contribution[] cs) {
		for(Contribution c : cs){
			storeContribution(c);
		}
	}

	@Override
	public int deleteRepo(String fullRepoName) {
		deleteContributionsByRepo(fullRepoName);
		Repo removed = repos.remove(fullRepoName);
		return removed == null ? 0 : 1;
	}

	@Override
	public int deleteUser(String userName) {
		deleteContributionsByUser(userName);
		User removed = users.remove(userName);
		return removed == null ? 0 : 1;
	}

	@Override
	public int deleteContributionsByUser(String user) {
		int removed = 0;
		for (Iterator<Contribution> iterator = contributions.iterator(); iterator.hasNext();) {
			  Contribution c = iterator.next();
			  if(c.getUser().login.equals(user)){
				  iterator.remove();
				  storedAllContributionsForRepo(c.getRepo().full_name, false);
				  storedAllContributionsByUser(c.getUser().login, false);
				  removed++;
			  }
			  
		}
		return removed;
	}

	@Override
	public int deleteContributionsByRepo(String repo) {
		int removed = 0;
		for (Iterator<Contribution> iterator = contributions.iterator(); iterator.hasNext();) {
			  Contribution c = iterator.next();
			  if(c.getRepo().full_name.equals(repo)){
				  iterator.remove();
				  storedAllContributionsForRepo(c.getRepo().full_name, false);
				  storedAllContributionsByUser(c.getUser().login, false);
				  removed++;
			  }
			  
		}
		return removed;
	}

	@Override
	public Contribution[] getAllContributions() {
		return contributions.toArray(new Contribution[]{});
		
	}
	
	private Contribution getContribution(String repo, String user){
		Contribution result = null;
		for(Contribution c: contributions){
			if(c.getUser().login.equals(user) && c.getRepo().full_name.equals(repo)){
				return c;
			}
		}
		return result;
	}

	@Override
	public void storedAllContributionsForRepo(String repo, boolean stored) {
		if(stored && !storedAllContributionsForRepo.contains(repo))
			storedAllContributionsForRepo.add(repo);
		else
			storedAllContributionsForRepo.remove(repo);
		
	}
	
	@Override
	public void storedAllContributionsByUser(String user, boolean stored) {
		if(stored && !storedAllReposForUser.contains(user))
			storedAllReposForUser.add(user);
		else
			storedAllReposForUser.remove(user);
	}
	
	
	
	public static void main(String[] args){
		HashMapStore store=new HashMapStore();
		
		Repo r = new Repo();
		r.full_name="testrepo1";
		Repo s = new Repo();
		s.full_name="testrepo2";
		store.storeRepo(r);
		store.storeRepo(s);
		
		Contribution c = new Contribution(new SimpleUser("testuser"), new SimpleRepo("testrepo1"));
		c.setContributionInfo(new ContributionInfo(100, 100));
		store.storeContribution(c);
		Contribution d = new Contribution(new SimpleUser("testuser"), new SimpleRepo("testrepo2"));
		d.setContributionInfo(new ContributionInfo(100, 100));
		store.storeContribution(d);
		
		//store.storedAllContributionsForRepo("testrepo", true);
		//Contribution[] reposOfJan = store.getRepoContributions("testrepo", true);
		
		store.storedAllContributionsByUser("testuser", false);
		System.out.println(Arrays.toString(store.getRepos("testuser", new TransparantFilter(), true)));
		
//		if(reposOfJan != null){
//			for(Contribution con : reposOfJan){
//				System.out.print(con);
//				if(con.hasContributionInfo()){
//					System.out.println(con.getContributionInfo());
//				}
//				else{
//					System.out.println();
//				}
//				
//			}
//		}
//		else
//			System.out.print("hoi");
		
		
		
	}
	
	
	

}