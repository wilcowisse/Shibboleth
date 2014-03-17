package shibboleth.scripts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import shibboleth.model.ExportFolder;
import shibboleth.model.UserChunk;


public class CreateSubSamples {
	
	private String exportFoldername="/home/wilco/tmp/export3";
	private int sizeThreshold=9000; //b
	private boolean verbose=false;
	private ExportFolder exportFolder;
	
	
	public CreateSubSamples(){
		exportFolder= new ExportFolder(new File(exportFoldername));
		
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(new File("experiment1_2.txt"), false));
			exportExperiment1_2(bw);
			bw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}
	

	public List<List<File>> subsum(List<List<File>> filesOfRepos, int partitionNo){
		
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
					return subsum(filesOfRepos, partitionNo-1);
			}
		}
		
		return partitions;
	}
	
	public int getNoOfListsSmallerThan(List<List<File>> reposOfUser, int size){
		int res = 0;
		for(List<File> fileList : reposOfUser){
			if(exportFolder.length(fileList)<size)
				res++;
		}
		return res;
	}
	
	/*
	 * Experiment 1 (2 rounds):
	 *	Training set: per user de helft (in b) van de code, gegroepeerd per repo
	 *	Testing set:  per user de andere helft (in b) van de code, gegroepeerd per repo
	 *	Training set: per user de andere helft (in b) van de code, gegroepeerd per repo
	 *	Testing set:  per user de helft (in b) van de code, gegroepeerd per repo
	 *
	 * Experiment 2 (2 rounds):
	 *  Training set: per user de helft (in b) van de code, gegroepeerd per repo.
	 *  Testing set:  per user alle geschikte samples, gegroepeerd per repo.
	 *  Training set: per user alle geschikte samples, gegroepeerd per repo.
	 *  Testing set:  per user de helft (in b) van de code, gegroepeerd per repo.
	 */
	public void exportExperiment1_2(BufferedWriter writer){
		for(String user : exportFolder.getUsers()){
			List<List<File>> reposOfUserList = new ArrayList<List<File>>(exportFolder.getRepoFiles(user));
			
			//int optimalNoOfContainers=exportFolder.length(reposOfUser)/sizeThreshold;
			//int reposSmallerThanThreshold = getNoOfListsSmallerThan(reposOfUserList, sizeThreshold);
			//int partitionNo = Math.min(maxPartitions, Math.min(reposOfUser.size(), Math.max(1, optimalNoOfContainers-reposSmallerThanThreshold/2)));
			
			List<List<File>> partitionedRepos = subsum(reposOfUserList,2);
			
			if(partitionedRepos.size()!=2){
				continue;
			}
			
			List<File> partition1 = partitionedRepos.get(0);
			List<File> partition2 = partitionedRepos.get(1);
			String partition1Str  = fileListToString(partition1);
			String partition2Str  = fileListToString(partition2);
			
			try {
				if(verbose){
					writer.newLine();
					writer.write("# Total file size of "+user+": " + exportFolder.length(reposOfUserList)/1000+"kb");
					writer.newLine();
					writer.write("# Partition 1: " + exportFolder.length(partition1)/1000 + "kb, "+partition1.size()+" repos.");
					writer.newLine();
					writer.write("# Partition 2: " + exportFolder.length(partition2)/1000 + "kb, "+partition2.size()+" repos.");
					writer.newLine();
				}
				writer.write(user);
				writer.newLine();
				writer.write("1");
				writer.newLine();
				writer.write("1");
				writer.newLine();
				writer.write(partition1Str);
				writer.newLine();
				writer.write(partition2Str);
				writer.newLine();
				
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	    
	}
	
	/*
	 * Experiment 3 (n fold cross validation)
	 * Training set: per user (n-1)/n samples van de code.
	 * Testing set:  de nth sample van de code
	 * ..
	 * ..
	 */
	public void exportExperiment3(Collection<List<File>> partitionedRepos){
		
	}
	
	public static String fileListToString(List<File> files){
		StringBuilder sb = new StringBuilder();
		for (File f : files){
		    sb.append(f.getName());
		    sb.append(" ");
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		new CreateSubSamples();
	}

}

