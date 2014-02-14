package shibboleth.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import shibboleth.data.DataStore;
import shibboleth.data.JavaScriptFilter;
import shibboleth.data.RepoFilter;
import shibboleth.model.Contribution;
import shibboleth.model.GitGraph;
import shibboleth.model.Repo;
import shibboleth.util.GithubUtil;

/**
 * Perform a BFS search of a given depth on a node.
 * 
 * Syntax: <tt>explode [repo|user] [depth]</tt>.
 * 
 * @author Wilco Wisse
 *
 */
public class ExplodeAction extends ShibbolethAction{
	
	private DataStore store;
	private GitGraph graph;
	private RepoFilter filter;
	private boolean ensureAll;
	
	private List<String> explodedUsers;
	private List<String> explodedRepos;
	private List<Contribution> explodedContributions;
	private Map<String, Integer> nodeDepths;
	
	private Queue<String> bfsQueue;
	
	public ExplodeAction(DataStore store, GitGraph graph){
		this.store=store;
		this.graph=graph;
	}
	
	@Override
	public void execute(String[] args) {
		if(args.length > 1){
			explodedUsers = new ArrayList<String>();
			explodedRepos = new ArrayList<String>();
			explodedContributions = new ArrayList<Contribution>();
			nodeDepths = new HashMap<String, Integer>();
			bfsQueue = new LinkedList<String>();
			ensureAll = false;
			if(args.length==3 && "-a".equals(args[2]))
				ensureAll=true;
			
			String argument = args[0];
			filter = new JavaScriptFilter();
			
			int depth = 0;
			try{
				depth = Integer.parseInt(args[1]);
			}
			catch(NumberFormatException e){
				depth = 0;
			}
			
			explode(argument,depth);
			
			if(args.length==3 && "-d".equals(args[2])){
				for(String u : explodedUsers){
					graph.remove(u);
					store.deleteUser(u);
				}
				for(String r : explodedRepos){
					graph.remove(r);
					store.deleteRepo(r);
				}
			}
			else{
				for(Contribution c : explodedContributions){
					graph.addContribution(c);
				}
			}
			
			if(listener != null){
				listener.graphChanged("Exploded "+ explodedUsers.size() + " users and " +  explodedRepos.size() + " repos", false);
			}
			
			explodedUsers.clear();
			explodedRepos.clear();
			explodedContributions.clear();
		}
		else if(listener != null){
			listener.messagePushed("Wrong syntax!");
		}
	}

	@Override
	public String getCommand() {
		return "explode";
	}
	
	
	public void explode(String node, int depth){
		bfsQueue.add(node);
		nodeDepths.put(node, 0);
		
		while(!bfsQueue.isEmpty()){
			String head = bfsQueue.poll();

			if(!isVisited(head)){
				visitNode(head);
				int currentDepth = nodeDepths.get(head);
				//System.out.println("Visit " + head + " at depth "+currentDepth);
				
				if(currentDepth<depth) {
					for(String child : getChildren(head)){
						//System.out.println("added " + child + " to queue");
						nodeDepths.put(child, currentDepth+1);
						bfsQueue.add(child);
					}
				}
			}
		}
		
		
	}
	
	public void visitNode(String node){
		if(GithubUtil.isUserName(node)){
			explodedUsers.add(node);
		}
		else{
			explodedRepos.add(node);
		}
	}
	
	public boolean isVisited(String node){
		if(GithubUtil.isUserName(node)){
			return explodedUsers.contains(node);
		}
		else{
			return explodedRepos.contains(node);
		}
	}
	
	public String[] getChildren(String node){
		if(GithubUtil.isUserName(node)){
			Repo[] reposByUser = store.getRepos(node, filter, ensureAll);
			Contribution[] cs = GithubUtil.reposToContributions(reposByUser, GithubUtil.createUser(node));
			String[] res = new String[cs.length];
			for(int i=0; i<cs.length; i++){
				Contribution c = cs[i];
				if(!explodedContributions.contains(c))
					explodedContributions.add(c);
				res[i] = c.getRepo().full_name;
			}
			return res;
		}
		else{
			Contribution[] cs = store.getContributions(node, ensureAll);
			String[] res = new String[cs.length];
			for(int i=0; i<cs.length; i++){
				Contribution c = cs[i];
				if(!explodedContributions.contains(c))
					explodedContributions.add(c);
				res[i] = c.getUser().login;
			}
			return res;
		}

	}
	
	
	
}
