package shibboleth.scripts;

import java.io.File;
import java.io.FilenameFilter;

public class WalkFiles {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int removeRepos = 0;
		System.out.println("Start\n");
		File dir = new File("/home/wilco/Repo/Datasets/concord-consortium/parts-grouped");
		String[] users = dir.list(new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).isDirectory();
		  }
		});
		
		for(String user:users){
			
			File userDir = new File(dir,user);
			String[] userRepos = userDir.list(new FilenameFilter() {
			  @Override
			  public boolean accept(File current, String name) {
			    return new File(current, name).isDirectory();
			  }
			});
			
			long userSize =0;
			
			for(String repo:userRepos){
				File repoDir = new File(userDir,repo);
				long size = folderSize(repoDir);
				userSize+=size;
				if(size < 1000){
					//removeRepos++;
					//deleteDirectory(repoDir);
					//System.out.println(repoDir);
				}
				
				//System.out.println(repo + " " + size);
				//System.out.println(repo+" "+user+" "+size);
				//System.out.print(user+" "+repo.replaceFirst("(-.*?)-", "$1#")+"|");
			}
			
			
			//System.out.println(user + "\t" + userSize);
			
			
			if(userRepos.length < 2){
				//System.out.println(user);
				//deleteDirectory(userDir);
			}
					
			
		}
		//System.out.println(removeRepos);

	}
	
	public static long folderSize(File directory) {
	    long length = 0;
	    for (File file : directory.listFiles()) {
	        if (file.isFile())
	            length += file.length();
	    }
	    return length;
	}
	
	public static boolean deleteDirectory(File directory) {
	    if(directory.exists()){
	        File[] files = directory.listFiles();
	        if(null!=files){
	            for(int i=0; i<files.length; i++) {
	                if(files[i].isDirectory()) {
	                    deleteDirectory(files[i]);
	                }
	                else {
	                    files[i].delete();
	                }
	            }
	        }
	    }
	    return(directory.delete());
	}

}
