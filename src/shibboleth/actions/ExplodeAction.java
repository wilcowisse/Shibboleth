package shibboleth.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import shibboleth.data.DataSource;
import shibboleth.data.JavaScriptFilter;
import shibboleth.data.RepoFilter;
import shibboleth.model.Contribution;
import shibboleth.model.GithubGraph;
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
	
	private DataSource source;
	private GithubGraph graph;
	private RepoFilter filter;
	
	private List<String> explodedUsers;
	private List<String> explodedRepos;
	private List<Contribution> explodedContributions;
	private Map<String, Integer> nodeDepths;
	
	private Queue<String> bfsQueue;
	
	public ExplodeAction(DataSource source, GithubGraph graph){
		this.source=source;
		this.graph=graph;
		this.filter = new JavaScriptFilter();
	}
	
	@Override
	public void execute(String[] args) {
		if(args.length > 1){
			String node = args[0];
			int depth = 0;
			try{
				depth = Integer.parseInt(args[1]);
			}
			catch(NumberFormatException e){
				depth = 0;
			}
			boolean ensureAll = args.length==3 && "-a".equals(args[2]);
			
			execute(node, depth, ensureAll);
			
		}
		else {
			listener.messagePushed("Wrong syntax!");
		}
	}
	
	public void execute(String node, int depth, boolean ensureAll){
		explodedUsers = new ArrayList<String>();
		explodedRepos = new ArrayList<String>();
		explodedContributions = new ArrayList<Contribution>();
		nodeDepths = new HashMap<String, Integer>();
		bfsQueue = new LinkedList<String>();

		explode(node,depth, ensureAll);
		
		for(Contribution c : explodedContributions) {
			graph.addContribution(c);
		}
		
		listener.graphChanged("Exploded "+ explodedUsers.size() + " users and " +  explodedRepos.size() + " repos", false);
		
		explodedUsers.clear();
		explodedRepos.clear();
		explodedContributions.clear();
	}

	@Override
	public String getCommand() {
		return "explode";
	}
	
	
	public void explode(String node, int depth, boolean ensureAll){
		bfsQueue.add(node);
		nodeDepths.put(node, 0);
		
		while(!bfsQueue.isEmpty()){
			String head = bfsQueue.poll();

			if(!isVisited(head)){
				visitNode(head);
				int currentDepth = nodeDepths.get(head);
				//System.out.println("Visit " + head + " at depth "+currentDepth);
				
				if(currentDepth<depth) {
					for(String child : getChildren(head, ensureAll)){
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
	
	public String[] getChildren(String node, boolean ensureAll){
		if(GithubUtil.isUserName(node)){
			List<Repo>  reposByUser = source.getRepos(node, filter, ensureAll);
			List<Contribution> cs = GithubUtil.reposToContributions(reposByUser, GithubUtil.createUser(node));
			String[] res = new String[cs.size()];
			for(int i=0; i<cs.size(); i++){
				Contribution c = cs.get(i);
				if(!explodedContributions.contains(c))
					explodedContributions.add(c);
				res[i] = c.getRepo().full_name;
			}
			return res;
		}
		else{
			List<Contribution> cs = source.getContributions(node, ensureAll);
			String[] res = new String[cs.size()];
			for(int i=0; i<cs.size(); i++){
				Contribution c = cs.get(i);
				int index = explodedContributions.indexOf(c);
				if(index == -1)
					explodedContributions.add(c);
				else if(c.hasContributionInfo()){
					explodedContributions.get(index).setContributionInfo(c.getContributionInfo());
				}
				res[i] = c.getUser().login;
			}
			return res;
		}

	}
	
	
	
}
