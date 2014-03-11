package shibboleth.scripts;

import java.io.File;
import java.io.IOException;

import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.MixedGraph;
import org.gephi.graph.api.Node;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

import shibboleth.util.GithubUtil;


public class CreateRepoGraph {

	private static final File IMPORT = new File("yifanhu-exportx.gexf");
	private static final File EXPORT = new File("yifanhu-export.gexf");
	
	
	public static void transform(GraphModel graphModel){
		
		MixedGraph graph = graphModel.getMixedGraph();
		System.out.print(" Nodes: " + graph.getNodeCount());
	    System.out.print(". Edges: " + graph.getEdgeCount());
	    System.out.println(".");
	    
		Node[] allNodes = graph.getNodes().toArray();
		
		
		int i = 0, total = graph.getNodeCount();
		for(Node node : allNodes){
			
			if(i>7500)
				break;
			
			if(i % (Math.ceil(total/200f)) == 0){
				System.out.println(" "+i+"/"+total + " (" +graph.getNodeCount()+")");
			}
			
			if(GithubUtil.isUserName(node.getNodeData().getLabel())){
				Node[] repoNeighbors = graph.getNeighbors(node).toArray();
				for(Node repoNeighbor1 : repoNeighbors){
					for(Node repoNeighbor2 : repoNeighbors){
						if((repoNeighbor1 != repoNeighbor2) && !isProbablyAFork(repoNeighbor1, repoNeighbor2)){
							Edge repoEdge = graph.getEdge(repoNeighbor1, repoNeighbor2);
							if(repoEdge == null){
								repoEdge = graphModel.factory().newEdge(repoNeighbor1, repoNeighbor2, 0.5f, false);
								graph.addEdge(repoEdge);
							}
							else{
								repoEdge.setWeight(repoEdge.getWeight()+0.5f);
							}
						}
					}
				}
				graph.removeNode(node);
			}
				
			i++;
		}

	}
	
	public static boolean isProbablyAFork(Node n1, Node n2){
		String node1Label = n1.getNodeData().getLabel();
		String node2Label = n2.getNodeData().getLabel();
		return node1Label.substring(node1Label.indexOf('/')).equals(node2Label.substring(node2Label.indexOf('/')));
	}
	
	/**
	 * Grate repo graph
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.out.println("Start");
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
		Workspace workspace = pc.getCurrentWorkspace();
		//Get controllers and models
	    ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        // Get Graph Model
	    GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
	    
	    System.out.println("Import file");
	    //Import file
	    Container container;
	    try {
	    	container = importController.importFile(IMPORT);
	    	container.getLoader().setEdgeDefault(EdgeDefault.MIXED);
	    	container.setAllowAutoNode(false);  //Don't create missing nodes
	    } catch (Exception ex) {
			ex.printStackTrace();
			return;
	    }
		
	    //Append imported data to GraphAPI
	    System.out.print("Imported graph. ");
	    importController.process(container, new DefaultProcessor(), workspace);

        // Transform
        System.out.println("Transform...");
        transform(graphModel);
	    
        System.out.println("Export.");
	    //Export full graph
		ExportController ec = Lookup.getDefault().lookup(ExportController.class);
		try {
		    ec.exportFile(EXPORT);
		} catch (IOException ex) {
			ex.printStackTrace();
			return;
		}
		System.out.println("Finished!");

	}
	
	
	

}