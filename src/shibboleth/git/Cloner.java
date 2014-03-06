package shibboleth.git;

import java.io.File;
import java.util.concurrent.TimeUnit;

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
	private static long lastClonedTime;
	
	/**
	 * Construct a cloner which clones repositories in a new sub directory 
	 * of the given <tt>directory</tt>.
	 * @param directory
	 */
	public Cloner(String directory){
		this.baseDir=directory;
		lastClonedTime=0;
	}
	
	public File clone(Repo repo, long interval) {
		long now = System.currentTimeMillis();
		long waitTime = lastClonedTime - now + interval;
		
		if(waitTime>0){
			try {
				System.out.println("Waiting " + waitTime + "msecs before cloning " + repo.full_name);
				TimeUnit.MILLISECONDS.sleep(waitTime);
			} catch (InterruptedException e) {
				System.err.println("Sleep error");
			}
		}
		
		return clone(repo);
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
		lastClonedTime=System.currentTimeMillis();
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
