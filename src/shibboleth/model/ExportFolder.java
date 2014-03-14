package shibboleth.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExportFolder{
	private File path;
	
	private Map<String, Map<String, List<File>>> userMap; // username > reponames -> files
	private Map<String, Integer> fileLength;   // filename -> length
	
	public ExportFolder(File path){
		this.path=path;
		
		File[] dirContent = path.listFiles();
		userMap = new HashMap<String, Map<String, List<File>>>();
		fileLength = new HashMap<String, Integer>();
		for(File file : dirContent){
			String filename = file.getName();
			if(filename.indexOf('#') !=-1){
				String userId = filename.substring(0, filename.indexOf('#'));
				String repoId = filename.substring(0, filename.indexOf('#', filename.indexOf('#')+1));
				
				Map<String, List<File>> repoMap;
				if(userMap.containsKey(userId)){
					repoMap = userMap.get(userId);
				}
				else{
					repoMap = new HashMap<String,List<File>>();
					userMap.put(userId, repoMap);
				}
				
				List<File> repoFileList;
				if(repoMap.containsKey(repoId)){
					repoFileList=repoMap.get(repoId);
				}
				else{
					repoFileList = new ArrayList<File>();
					repoMap.put(repoId, repoFileList);
				}
				
				fileLength.put(filename, (int)file.length());
				repoFileList.add(file);
			}
		}
	}
	
	public Set<String> getUsers(){
		return userMap.keySet();
	}
	
	public Collection<List<File>> getRepoFiles(String user){ 
		return userMap.get(user).values();
	}
	
	public Set<String> getRepos(String user){
		if(userMap.containsKey(user)){
			return userMap.get(user).keySet();
		}
		else{
			return null;
		}
	}
	
	public int length(Collection<List<File>> repoList){
		int length = 0;
		for(List<File> files : repoList){
			for(File f : files){
				length += fileLength.get(f.getName());
			}
		}
		return length;
	}
	
	public int length(List<File> files){
		int length = 0;

		for(File f : files){
			length += fileLength.get(f.getName());
		}

		return length;
	}
	
	
//	public Collection<Collection<List<File>>> getDirContent(){
//		Collection<Collection<List<File>>> result = new ArrayList<Collection<List<File>>>();
//		
//		for(Map<String,List<File>> repoList : userMap.values()){
//			result.add(repoList.values());
//		}
//		
//		return result;
//	}
	

	
	
}