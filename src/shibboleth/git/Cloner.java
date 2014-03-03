package shibboleth.git;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import shibboleth.model.Repo;
import shibboleth.model.SimpleRepo;

/**
 * Clones remote Git repositories.
 * 
 * @author Wilco Wisse
 *
 */
public class Cloner {
	
	private String baseDir;
	
	/**
	 * Construct a cloner which clones repositories in a new sub directory 
	 * of the given <tt>directory</tt>.
	 * @param directory
	 */
	public Cloner(String directory){
		this.baseDir=directory;
	}
	
	/**
	 * Clone the given repository.
	 * @param repo The repo to be cloned.
	 * @return The directory where the repo was cloned to.
	 */
	public File clone(Repo repo){
		File cloneDir = getClonePath(repo);
		
		if(cloneDir.exists())
			return cloneDir;
		
		try {
			Git.cloneRepository()
			.setURI(repo.clone_url)
			.setDirectory(cloneDir)
			.call();
		} catch (GitAPIException e) {
			e.printStackTrace();
			return null;
		}
		return cloneDir;
	}
	
	/**
	 * Get the parent directory name of a repo.
	 * @return The parent directory name of repo <tt>r</tt>.
	 */
	public static String getDirName(SimpleRepo r){
		return r.full_name.replace('/', '-');
	}
	
	/**
	 * Get the path where a repo will be stored
	 * @return The full path of repo <tt>r</tt>.
	 */
	public File getClonePath(Repo r){
		return new File(baseDir+System.getProperty("file.separator")+getDirName(r));
	}
	

}
