package shibboleth.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExportFolder{
	
	private Map<String, Map<String, List<File>>> userMap; // username > reponames -> files
	private Map<String, Integer> fileLength;   // filename -> length
	private Map<String, Integer> locs;
	
	public ExportFolder(File path){
		
		File[] dirContent = path.listFiles();
		userMap = new HashMap<String, Map<String, List<File>>>();
		fileLength = new HashMap<String, Integer>();
		locs = new HashMap<String,Integer>();
		
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
				
				BufferedReader reader;
				int lines = 0;
				try {
					reader = new BufferedReader(new FileReader(file));
					while (reader.readLine() != null) lines++;
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
				
				locs.put(filename,lines);
				
				fileLength.put(filename, (int)file.length());
				repoFileList.add(file);
			}
		}
	}
	
	public Set<String> getUsers(){
		return userMap.keySet();
	}
	
	public Set<String> getRepos(String user){
		if(userMap.containsKey(user)){
			return userMap.get(user).keySet();
		}
		else{
			return null;
		}
	}
	
	public Collection<List<File>> getUserFiles(String user){ 
		return userMap.get(user).values();
	}
	
	public List<File> getRepoFiles(String user, String repo){
		return userMap.get(user).get(repo);
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
	
	public int loc(Collection<List<File>> repoList){
		int loc = 0;
		for(List<File> files : repoList){
			for(File f : files){
				loc += locs.get(f.getName());
			}
		}
		return loc;
	}
	
	public int loc(List<File> files){
		int loc = 0;

		for(File f : files){
			loc += locs.get(f.getName());
		}

		return loc;
	}
	
	
	public Collection<Integer> getFileLengths(){
		return fileLength.values();
	}
	
	public Collection<Integer> getFileLoc(){
		return locs.values();
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