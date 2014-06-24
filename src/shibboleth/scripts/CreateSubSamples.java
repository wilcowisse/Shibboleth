package shibboleth.scripts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import shibboleth.model.ExportFolder;
import shibboleth.model.UserChunk;


public class CreateSubSamples {
	
	private String exportFoldername="/home/wilco/Repo/Datasets/concord-consortium/parts";
	
	
	private ExportFolder exportFolder;
	
	
	public CreateSubSamples(){
		exportFolder= new ExportFolder(new File(exportFoldername));
		
		BufferedWriter bw = null;
		
//		try {
//			bw = new BufferedWriter(new FileWriter(new File("experiment1.txt"), false));
//			exportExperiment1(bw);
//			bw.close();
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}

//		try {
//			bw = new BufferedWriter(new FileWriter(new File("experiment2.txt"), false));
//			exportExperiment2(bw);
//			bw.close();
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
		
		try {
			//copyFiles(new File("/home/wilco/Repo/Datasets/jupiter/parts-grouped"));
			exportStats(new File("/home/wilco/Repo/Datasets/concord-consortium/parts-grouped"));
			//sameRepos(new File("/home/wilco/Repo/Datasets/concord-consortium"));
			
			if(false){throw new IOException();}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}
	
	public void copyFiles(File targetFolder){
		targetFolder.mkdir();
		
		for(String user : exportFolder.getUsers()){
			File userDir = new File(targetFolder,user);
			userDir.mkdir();
			
			for(String repo : exportFolder.getRepos(user)){
				File repoDir = new File(userDir,repo.substring(repo.indexOf('#')+1));
				repoDir.mkdir();
				List<File> files = exportFolder.getRepoFiles(user, repo);
				
				for(File source : files){
					File target = new File(repoDir,source.getName());
					try {
						Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public void sameRepos(File targetFolder) throws IOException{
		final Map<String,List<String>> sameRepos = new HashMap<String,List<String>>();
		for(String user : exportFolder.getUsers()){
			for(String repo : exportFolder.getRepos(user)){
				
				String repoName = repo.substring(repo.indexOf('#')+1);
				if(!sameRepos.containsKey(repoName)){
					sameRepos.put(repoName, new ArrayList<String>());
				}
				sameRepos.get(repoName).add(user);
			}
		}
		
		List<String> repos = new ArrayList<String>(sameRepos.keySet());
		Collections.sort(repos, new Comparator<String>() {
	        public int compare(String o1, String o2) {
	            return sameRepos.get(o1).size() - sameRepos.get(o2).size();
	        }
	    });

		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(targetFolder, "sameRepos.txt"), false));
		
		for (String repo : repos) {
		    List<String> users = sameRepos.get(repo);
		    writer.write("#### "+repo+" ####\n");
		    for(String repoUser : users){
		    	writer.write(repoUser+"\n");
		    }
		    writer.write("\n");
		}
		
		writer.close();
	}
	
	public void exportStats(File targetFolder) throws IOException{
		
		BufferedWriter fileDataWriter = new BufferedWriter(new FileWriter(new File(targetFolder, "fileData.csv"), false));
		BufferedWriter fileLocWriter = new BufferedWriter(new FileWriter(new File(targetFolder, "fileLoc.csv"), false));
		BufferedWriter userDataWriter = new BufferedWriter(new FileWriter(new File(targetFolder, "userData.csv"), false));
		BufferedWriter repoDataWriter = new BufferedWriter(new FileWriter(new File(targetFolder, "repoData.csv"), false));
				
		for(Integer size : exportFolder.getFileLengths()){
			fileDataWriter.write(size + "\n");
		}
		
		for(Integer l : exportFolder.getFileLoc()){
			fileLocWriter.write(l + "\n");
		}
		
		
		for(String user : exportFolder.getUsers()){
			List<List<File>> reposOfUserList = new ArrayList<List<File>>(exportFolder.getUserFiles(user));
			
			userDataWriter.write(user + "," + exportFolder.length(reposOfUserList) + "," +exportFolder.loc(reposOfUserList)+"," + reposOfUserList.size() + "\n");
			
			for(List<File> filesOfRepoList : reposOfUserList){
				
				repoDataWriter.write(exportFolder.length(filesOfRepoList)+","+exportFolder.loc(filesOfRepoList)+","+filesOfRepoList.size()+"\n");
			}
		}
		
		fileDataWriter.close();
		fileLocWriter.close();
		userDataWriter.close();
		repoDataWriter.close();
		
	}

	public void keepLargerThan(List<List<File>> filesOfRepo, int size){
		Iterator<List<File>> iterator = filesOfRepo.iterator();
		while(iterator.hasNext()){
		    List<File> currentFileList = iterator.next();
		    if(exportFolder.length(currentFileList)<size){
		        iterator.remove();
		    }
		}
	}
	
	public int getNoOfListsSmallerThan(List<List<File>> reposOfUser, int size){
		int res = 0;
		for(List<File> fileList : reposOfUser){
			if(exportFolder.length(fileList)<size)
				res++;
		}
		return res;
	}

	public List<List<File>> subsum(List<List<File>> filesOfRepos, int partitionNo, int sizeThreshold){
		
		// sort on file descending file length
		Collections.sort(filesOfRepos, new Comparator<List<File>>() {
			@Override
			public int compare(List<File> o1, List<File> o2) {
				if(o1.equals(o2))
					return 0;
				else
					return exportFolder.length(o2) - exportFolder.length(o1);
			}
		});
		
		List<List<File>> partitions = new ArrayList<List<File>>(partitionNo);
		int[] partitionSizes = new int[partitionNo];
		for(int i = 0; i<partitionNo; i++){
			partitions.add(i, new ArrayList<File>());
			partitionSizes[i]=0;
		}
		
		// assign repo lists
		for(List<File> filesOfRepo:filesOfRepos){
			int smallestPartition=-1;
			int smallestPartitionSize=Integer.MAX_VALUE;
			
			for(int i=0;i<partitionNo;i++){
				if(partitionSizes[i]<smallestPartitionSize){
					smallestPartitionSize=partitionSizes[i];
					smallestPartition=i;
				}
			}
			
			partitions.get(smallestPartition).addAll(filesOfRepo);
			partitionSizes[smallestPartition]+=exportFolder.length(filesOfRepo);
		}
		
		// check sizeThreshold
		for(int partitionSize:partitionSizes){
			if(partitionSize<sizeThreshold){
				if(partitionNo==1)
					return new ArrayList<List<File>>(partitionNo);
				else
					return subsum(filesOfRepos, partitionNo-1, sizeThreshold);
			}
		}
		
		return partitions;
	}
	
	public static void main(String[] args) {
		new CreateSubSamples();
	}
	
	
	public static String fileListToString(List<File> files){
		StringBuilder sb = new StringBuilder();
		for (File f : files){
		    sb.append(f.getName());
		    sb.append(" ");
		}
		return sb.toString();
	}
	
//	private boolean verbose=false;
//	
//	/*
//	 * Experiment 1 (2 rounds):
//	 *	Training set: per user de helft (in b) van de code, gegroepeerd per repo
//	 *	Testing set:  per user de andere helft (in b) van de code, gegroepeerd per repo
//	 *
//	 */
//	public void exportExperiment1(BufferedWriter writer){
//		for(String user : exportFolder.getUsers()){
//			List<List<File>> reposOfUserList = new ArrayList<List<File>>(exportFolder.getUserFiles(user));
//			
//			//int optimalNoOfContainers=exportFolder.length(reposOfUser)/sizeThreshold;
//			//int reposSmallerThanThreshold = getNoOfListsSmallerThan(reposOfUserList, sizeThreshold);
//			//int partitionNo = Math.min(maxPartitions, Math.min(reposOfUser.size(), Math.max(1, optimalNoOfContainers-reposSmallerThanThreshold/2)));
//			
//			List<List<File>> partitionedRepos = subsum(reposOfUserList,2,9000);
//			
//			if(partitionedRepos.size()!=2){
//				continue;
//			}
//			
//			List<File> partition1 = partitionedRepos.get(0);
//			List<File> partition2 = partitionedRepos.get(1);
//			String partition1Str  = fileListToString(partition1);
//			String partition2Str  = fileListToString(partition2);
//			
//			try {
//				if(verbose){
//					writer.newLine();
//					writer.write("# Total file size of "+user+": " + exportFolder.length(reposOfUserList)/1000+"kb");
//					writer.newLine();
//					writer.write("# Partition 1: " + exportFolder.length(partition1)/1000 + "kb, "+partition1.size()+" repos.");
//					writer.newLine();
//					writer.write("# Partition 2: " + exportFolder.length(partition2)/1000 + "kb, "+partition2.size()+" repos.");
//					writer.newLine();
//				}
//				writer.write(user);
//				writer.newLine();
//				writer.write("1");
//				writer.newLine();
//				writer.write("1");
//				writer.newLine();
//				writer.write(partition1Str);
//				writer.newLine();
//				writer.write(partition2Str);
//				writer.newLine();
//				
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//
//		}
//	    
//	}
//	
//	
//	/*
//	 * Experiment 2 (random subsampling)
//	 * ..
//	 * ..
//	 */
//	public void exportExperiment2(BufferedWriter writer){
//		
//		int exportedUsers=0;
//		
//		for(String user : exportFolder.getUsers()){
//			List<List<File>> reposOfUserList = new ArrayList<List<File>>(exportFolder.getUserFiles(user));
//			keepLargerThan(reposOfUserList,2000);
//			
//			try {
//				if(reposOfUserList.size()>3){
//					exportedUsers++;
//					Collections.shuffle(reposOfUserList);
//					
//					int half = (int) Math.round(reposOfUserList.size()/2.0);
//					
//					List<List<File>> trainRepos = reposOfUserList.subList(0, half);
//					List<List<File>> testRepos = reposOfUserList.subList(half,reposOfUserList.size()-1);
//					
//					List<File> trainPart = new ArrayList<File>();
//					for(List<File> trainRepo : trainRepos){
//						trainPart.addAll(trainRepo);
//					}
//					String trainPartitionStr = fileListToString(trainPart);
//					
//					if(verbose){
//						writer.newLine();
//						writer.write("# " + exportFolder.length(reposOfUserList)/1000+"kb, " + reposOfUserList.size() +"repos");
//						writer.newLine();
//						writer.write("# Train: " + exportFolder.length(trainPart)/1000 + "kb, "+trainPart.size()+" files (of "+trainRepos.size()+" repos).");
//						writer.newLine();
//					}
//					
//					writer.write(user+"\n");
//					writer.write(1+"\n");
//					writer.write(testRepos.size()+"\n");
//					
//					writer.write(trainPartitionStr);
//					writer.newLine();
//					
//					for(List<File> testRepo : testRepos){
//						String testPartitionStr = fileListToString(testRepo);
//						writer.write(testPartitionStr);
//						if(verbose){
//							writer.write("  ("+exportFolder.length(testRepo)/1000+" kb)");
//						}
//						writer.newLine();
//					}
//	
//
//				} 
//				
//			}catch (IOException e) {
//				e.printStackTrace();
//			}
//
//		}
//		System.out.println("Exported "+exportedUsers+" users.");
//	    
//	}
	
	

}

