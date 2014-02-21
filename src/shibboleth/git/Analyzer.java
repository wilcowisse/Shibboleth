package shibboleth.git;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;

import shibboleth.data.sql.CommitInfoStore;
import shibboleth.model.Chunk;
import shibboleth.model.Committer;
import shibboleth.model.GitFile;

/**
 * This class provides functionality to analyze a repository on authorship
 * information of JavaScript files in this repository. 
 * 
 * Basically the git <tt>blame</tt> command is used to detect authorship information.
 * 
 * @author Wilco Wisse
 *
 */
public class Analyzer {
	
	private File directory;
	private String repo;

	private CommitInfoStore infoStore;
	
	/**
	 * Construct an analyzer for the <tt>repo</tt> in the given <tt>directory</tt>
	 * @param repo The name of the repo to be analyzed.
	 * @param directory The directory the repo is cloned to.
	 * @param infoStore The commitInfoStore to store analyzed data into.
	 */
	public Analyzer(String repo, File directory, CommitInfoStore infoStore){
		this.directory = directory;
		this.repo = repo;
		this.infoStore=infoStore;
	}
	
	/**
	 * Start analyzing. This method retrieves all 
	 * {@link shibboleth.model.Chunk chunks} of all
	 * {@link shibboleth.model.GitFile files} in the
	 * cloned repository.
	 */
	public void analyze(){
		Repository repository = null;
		try {
			repository = FileRepositoryBuilder.create(new File(directory,".git"));
			
			Ref head = repository.getRef("HEAD");
	        
	        RevWalk walk = new RevWalk(repository);
	        RevCommit commit = walk.parseCommit(head.getObjectId());
	        RevTree tree = commit.getTree();
	        
	        TreeWalk treeWalk = new TreeWalk(repository);
	        treeWalk.addTree(tree);
	        treeWalk.setRecursive(true);
	        treeWalk.setFilter(new JSFileFilter());
	        
	        while (treeWalk.next()) {
	        	@SuppressWarnings("unused")
				ObjectId id = treeWalk.getObjectId(0);
	        					
				BlameResult blameResult = new BlameCommand(repository)
					.setFilePath(treeWalk.getPathString())
					.call();
				
				GitFile file = new GitFile();
				file.repo = repo;
				file.head = ObjectId.toString(head.getObjectId());
				file.filename = treeWalk.getPathString();
				
				PersonIdent currentIdent = null;
				Chunk currentChunk = null;
				RawText raw = blameResult.getResultContents();
				for(int i=0; i<raw.size(); i++){
					PersonIdent newIdent = blameResult.getSourceAuthor(i);
					if(newIdent.equals(currentIdent) && currentChunk != null){
						currentChunk.end++;
					}
					else{
						if(currentChunk != null)
							infoStore.insert(currentChunk);
						Committer c = new Committer();
						c.repo=repo;
						c.email=newIdent.getEmailAddress();
						c.name=newIdent.getName();
						currentChunk = new Chunk();
						currentChunk.file=file;
						currentChunk.start=i;
						currentChunk.end=i;
						currentChunk.committer=c;
						currentChunk.when = newIdent.getWhen();
						currentIdent=newIdent;
					}
				}
				// store last chunk of this file
				if(currentChunk != null)
					infoStore.insert(currentChunk);
	        }
	       
	        
	        repository.close();
	        
	        try {
				infoStore.writeToDB();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	        
		} catch (IOException | GitAPIException e) {
			e.printStackTrace();
		}

	}
	
	
//	public static void main(String[] args){
//		Connection connection = null;
//		try {
//			Class.forName("com.mysql.jdbc.Driver");
//			connection = DriverManager.getConnection("jdbc:mysql://localhost/shibboleth?user=root&password=pass");
//		} catch (SQLException | ClassNotFoundException e) {
//			e.printStackTrace();
//		}
//		
//		CommitInfoStore infoStore = new CommitInfoStore(connection);
//		
//		DataSource source = new MySqlDataStore(connection);
//		
//		Analyser a = new Analyser("livingston/autoSize", new File("clones/livingston-autoSize"), infoStore);
//		a.analyse();
//	}
	
	
}
