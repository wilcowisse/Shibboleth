package shibboleth.model;


import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.layout.plugin.AutoLayout;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.layout.plugin.forceAtlas.ForceAtlasLayout;
import org.gephi.layout.plugin.random.Random;
import org.gephi.layout.plugin.random.RandomLayout;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.api.RankingController;
import org.gephi.ranking.api.Transformer;
import org.gephi.ranking.plugin.transformer.AbstractSizeTransformer;
import org.openide.util.Lookup;

import shibboleth.util.GithubUtil;

/**
 * A (bipartite) directed graph containing users and repos and their relationship.
 * The edge direction is from repos to users.
 * <ul>
 * 	<li> The edge size depends on the percentage of contributions a user did to a 
 * repo. And ranges between {@link #EDGE_SIZE_MIN} and {@link #EDGE_SIZE_MAX}.</li>
 *  <li> The node size depends on its out degree and ranges between 
 *  {@link #NODE_SIZE_MIN} and {@link #NODE_SIZE_MAX}.</li>
 * </ul>
 * 
 * @author Wilco Wisse
 */
public class GitGraph {
	
	/**
	 * Minimum edge size. 
	 */
	public static float EDGE_SIZE_MIN = 1f;
	public static float EDGE_SIZE_MAX = 5f;
	public static float NODE_SIZE_MIN = 10f;
	public static float NODE_SIZE_MAX = 20f;
	
	/**
	 * A string identifying a layout algorithm.
	 */
	public static final String 
			YIFAN_HU = "yifanhu",
			FORCE_ATLAS = "forceatlas",
			RANDOM = "random";
	
	private GraphModel graphModel;
	private DirectedGraph directedGraph;
	private AttributeColumn typeCol;
	private Workspace workspace;
	private RankingController rankingController;
	@SuppressWarnings("rawtypes")
	private AbstractSizeTransformer sizeTransformer;
	@SuppressWarnings("rawtypes")
	private Ranking degreeRanking;
	
	private Node initialNode;
	
	@SuppressWarnings("rawtypes")
	public GitGraph(){
		// bah... singletons :$
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        workspace = pc.getCurrentWorkspace();
                
        AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
        typeCol = attributeModel.getNodeTable().addColumn("type", AttributeType.STRING);
		graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		directedGraph = graphModel.getDirectedGraph();
		
		rankingController = Lookup.getDefault().lookup(RankingController.class);
        sizeTransformer = (AbstractSizeTransformer) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_SIZE);
        sizeTransformer.setMinSize(NODE_SIZE_MIN);
        sizeTransformer.setMaxSize(NODE_SIZE_MAX);
        
        initialNode = graphModel.factory().newNode("empty");
        initialNode.getNodeData().setColor(0.5f,0.5f,0.5f);
        initialNode.getNodeData().setLabel("empty");
        initialNode.getNodeData().getAttributes().setValue(typeCol.getIndex(), "empty");
        initialNode.getNodeData().setSize(NODE_SIZE_MAX);
        directedGraph.addNode(initialNode);
		
	}
	
	private void beforeAdd(){
		if(directedGraph.contains(initialNode)){
			directedGraph.removeNode(initialNode);
		}
	}
		
	private void rank(){
		if(directedGraph.getNodeCount() == 0)
			return;
		rankingController = Lookup.getDefault().lookup(RankingController.class);
        degreeRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, Ranking.OUTDEGREE_RANKING);
		rankingController.transform(degreeRanking,sizeTransformer);
	}
	
	/**
	 * Add user to the graph, if the graph already contains this user, 
	 * nothing is added.
	 * @param user The user.
	 * @return The Node belonging to the user.
	 */
	public Node add(SimpleUser user){
		Node userNode = directedGraph.getNode(user.login);
		if(userNode == null){
			userNode = graphModel.factory().newNode(user.login);
			setColor(userNode, new Color(84, 235, 122));
			userNode.getNodeData().setLabel(user.login);
			userNode.getNodeData().getAttributes().setValue(typeCol.getIndex(), "user");
			userNode.getNodeData().setSize(NODE_SIZE_MIN);
			beforeAdd();
			directedGraph.addNode(userNode);
		}
		return userNode;
	}
	
	/**
	 * Add repo to the graph, if the graph already contains this repo, 
	 * nothing is added.
	 * @param repo The repo.
	 * @return The Node belonging to the repo.
	 */
	public Node add(SimpleRepo repo){
		Node repoNode = directedGraph.getNode(repo.full_name);
		if(repoNode == null){
			repoNode = graphModel.factory().newNode(repo.full_name);
			setColor(repoNode, new Color(164,215,235));
			repoNode.getNodeData().setLabel(repo.full_name);
			repoNode.getNodeData().getAttributes().setValue(typeCol.getIndex(), "repo");
			repoNode.getNodeData().setSize(NODE_SIZE_MIN);
			beforeAdd();
			directedGraph.addNode(repoNode);
		}
		return repoNode;
	}
	
	/**
	 * Set color to a node. If color is null, standard 
	 * colors are being applied.
	 * @param nodeName The name of the node
	 * @param color The color
	 */
	public void setColor(String nodeName, Color color){
		if(color==null && GithubUtil.isRepoName(nodeName))
			color = new Color(164,215,235);
		else if(color==null && GithubUtil.isUserName(nodeName))
			color = new Color(84, 235, 122);
		
		Node node = directedGraph.getNode(nodeName);
		if(node!=null)
			setColor(node, color);
	}
	
	/**
	 * Set color to a node
	 * @param node The node
	 * @param color The color
	 */
	public void setColor(Node node, Color color){
		node.getNodeData().setColor(color.getRed()/255f,color.getGreen()/255f,color.getBlue()/255f);
	}
	
	/**
	 * Remove node with the given node name. Removes neighbors of the given
	 * node if their degree will become equal to 0;
	 * Generally speaking the node name is
	 * either a Github user name or a Github repo name.
	 * @param nodeName
	 * @return Whether a node was indeed removed from the graph.
	 */
	public boolean remove(String nodeName){
		Node node = directedGraph.getNode(nodeName);
		if(node != null && node != initialNode){
			
			Node[] neighbors = directedGraph.getNeighbors(node).toArray();
			for(Node neighbor : neighbors){
				if(directedGraph.getDegree(neighbor) == 1)
					directedGraph.removeNode(neighbor);
			}
			
			directedGraph.removeNode(node);
			rank();
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * Remove all nodes from the graph.
	 */
	public void removeAll(){
		Node[] allNodes = directedGraph.getNodes().toArray();
		for(Node node : allNodes){
			directedGraph.removeNode(node);
		}
		rank();
	}
	
	/**
	 * Add a contribution to the graph.
	 * The user and the committer of the contribution are added as well.
	 * @param c
	 */
	public void addContribution(Contribution c){
		
		SimpleUser user = c.getUser();
		SimpleRepo repo = c.getRepo();
		
		Node userNode = add(user);
		Node repoNode = add(repo);
		
		Edge edge =  directedGraph.getEdge(repoNode, userNode);
		if(edge == null){
			edge = graphModel.factory().newEdge(repoNode, userNode, EDGE_SIZE_MIN, true);
			directedGraph.addEdge(edge);
		}
		
		if(c.hasContributionInfo()){
			setColor(repoNode, new Color(84, 192, 235));
			float percentage = ((float)c.getContributionInfo().percentage)/100f;
			float weight = EDGE_SIZE_MIN + (EDGE_SIZE_MAX - EDGE_SIZE_MIN) * percentage;
			edge.setWeight(weight);
		}
		rank();

	}
	
	/**
	 * Layout this graph.
	 * @param type The name of the layout algorithm.
	 * One out of {@link #YIFAN_HU}, {@link #RANDOM}, {@link #FORCE_ATLAS}
	 * @param param
	 */
	public void layout(String type, int param){
		if(type.equals(YIFAN_HU))
			layoutYifanHu(param);
		else if(type.equals(FORCE_ATLAS))
			layoutForceAtlas(param);
		else if(type.equals(RANDOM))
			layoutRandom(param);
	}
	
	/**
	 * Layout this graph with the YifanHu algorithm for the specified time.
	 * @param milliSecs Time in milli seconds.
	 */
	public void layoutYifanHu(int milliSecs){
		AutoLayout autoLayout = new AutoLayout(milliSecs, TimeUnit.MILLISECONDS);
		autoLayout.setGraphModel(graphModel);
		
		YifanHuLayout yifanHuLayout = new YifanHuLayout(null, new StepDisplacement(2f));
		autoLayout.addLayout(yifanHuLayout, 1f);
		
		autoLayout.execute();
	}
	
	/**
	 * Layout this graph with the ForceAtlas algorithm for the specified time.
	 * @param milliSecs Time in milli seconds.
	 */
	public void layoutForceAtlas(int milliSecs){
		AutoLayout autoLayout = new AutoLayout(milliSecs, TimeUnit.MILLISECONDS);
		autoLayout.setGraphModel(graphModel);
		
		ForceAtlasLayout secondLayout = new ForceAtlasLayout(null);
        AutoLayout.DynamicProperty adjustBySizeProperty = AutoLayout.createDynamicProperty("forceAtlas.adjustSizes.name", Boolean.TRUE, 0.1f);//True after 10% of layout time
        AutoLayout.DynamicProperty repulsionProperty = AutoLayout.createDynamicProperty("forceAtlas.repulsionStrength.name", new Double(10.), 0f);//500 for the complete period
        
		autoLayout.addLayout(secondLayout, 1f, new AutoLayout.DynamicProperty[]{adjustBySizeProperty, repulsionProperty});
		autoLayout.execute();
	}
	
	/**
	 * Apply a random layout
	 * @param size The size to scatter nodes on.
	 */
	public void layoutRandom(int size){
		RandomLayout layout = new RandomLayout(new Random(),size);
        layout.setGraphModel(graphModel);
        layout.initAlgo();
        layout.goAlgo();
        layout.endAlgo();
	}
	
	/**
	 * Export the graph as gephi file.
	 * @param file The path
	 */
	public void export(File file){
		ExportController ec = Lookup.getDefault().lookup(ExportController.class);
		GraphExporter exporter = (GraphExporter) ec.getExporter("gexf");
		exporter.setWorkspace(workspace);
		try {
			file.createNewFile();
		    ec.exportFile(file, exporter);
		} 
		catch (IOException ex) {
		    ex.printStackTrace();
		}
	}
	
}
