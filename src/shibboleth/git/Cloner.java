package shibboleth.git;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.dircache.InvalidPathException;

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
	private long lastClonedTime;
	
	/**
	 * Construct a cloner which clones repositories in a new sub directory 
	 * of the given <tt>directory</tt>.
	 * @param directory
	 */
	public Cloner(String directory){
		this.baseDir=directory;
		lastClonedTime=0;
	}
	
	/**
	 * Clone the given repository.
	 * @param repo The repo to be cloned.
	 * @param interval The minimum time between clone operations in ms.
	 * @return The directory where the repo was cloned to.
	 */
	public File clone(Repo repo, long interval) {
		File cloneDir = getClonePath(repo);
		if(cloneDir.exists())
			return cloneDir;
		
		long now = System.currentTimeMillis();
		long waitTime = lastClonedTime - now + interval;
		
		if(waitTime>0){
			try {
				System.out.println("Waiting " + waitTime + " ms before cloning " + repo.full_name);
				TimeUnit.MILLISECONDS.sleep(waitTime);
			} catch (InterruptedException e) {
				System.err.println("Sleep error");
			}
		}
		
		return clone(repo);
	}
	
	private File clone(Repo repo){
		
		File cloneDir = getClonePath(repo);
		if(cloneDir.exists())
			return cloneDir;
		
		CloneCommand cloner = Git.cloneRepository()
				.setURI(repo.clone_url)
				.setDirectory(cloneDir);
		
		try {
			cloner.call();
		} 
		catch(TransportException e){ //e.g. Unexpected end of file from server
			// let's try to recover
			System.err.println("Clone Error: " + e.getMessage());
			System.err.print("Trying to recover by sleeping and trying to clone again... ");
			
			try {
				delete(cloneDir);
				TimeUnit.SECONDS.sleep(10);
				cloner.call();
				lastClonedTime=System.currentTimeMillis();
				System.err.println("Successful");
				return cloneDir;
			}
			catch(Exception ex){
				System.err.println("Unsuccessful: " + ex.getMessage());
				return null;
			}
			
		}
		catch(InvalidPathException e){ // e.g. period at end is ignored by Windows
			System.err.println("Clone Error: " + e.getMessage());
			System.err.println("Go ahead...");
			return cloneDir;
		}
		catch(JGitInternalException e){ //e.g. The filename, directory name, or volume label syntax is incorrect on windows
			System.err.println("Clone Error: " + e.getMessage());
			try{
				delete(cloneDir);
			}
			catch(Exception ex){
				System.err.println("And could not delete clone dir: " + e.getMessage());
			}
			return null;
		}
		catch (GitAPIException e) {
			System.err.println("Clone Error: " + e.getMessage());
			try{
				delete(cloneDir);
			}
			catch(Exception ex){
				System.err.println("And could not delete clone dir: " + e.getMessage());
			}
			return null;
		}
		
		lastClonedTime=System.currentTimeMillis();
		return cloneDir;
	}

	/**
	 * Delete dir recursively.
	 * @param f The file or dir.
	 * @throws IOException
	 */
	private void delete(File f) throws IOException {
		if(!f.exists()){
			return;
		}
		if (f.isDirectory()) {
			for (File c : f.listFiles())
				delete(c);
		}
		if (!f.delete()){
			  throw new IOException("Failed to delete file: " + f);
		}
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
	
	public long getLastClonedTime(){
		return lastClonedTime;
	}
	

}
