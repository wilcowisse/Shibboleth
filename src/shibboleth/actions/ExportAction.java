package shibboleth.actions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import shibboleth.data.sql.CommitInfoStore;
import shibboleth.git.Exporter;
import shibboleth.model.UserChunk;
import shibboleth.util.BlameUtil;

public class ExportAction extends ShibbolethAction {

	private CommitInfoStore infoStore;

	public ExportAction(CommitInfoStore infoStore){
		this.infoStore = infoStore;
	}
	
	@Override
	public void execute(String[] args) {
		List<String> blackList = new ArrayList<String>();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("export/_blacklist.txt"));
			String line;
			while ((line = br.readLine()) != null) {
				if(!line.trim().equals(""))
					blackList.add(line.trim());
			}
			br.close();
		} catch (IOException e) {
			listener.errorOccurred(e, false);
		}
		
		Exporter exporter = new Exporter(new File("export"), new File("clones"), blackList);
		boolean fragmented = true;
		if(args.length > 0 && args[0].equals("-f"))
			fragmented = false;
		
		List<Integer> files = infoStore.getAllFileIds();
		
		for(Integer file : files){
			List<UserChunk> fileChunks = infoStore.getFileChunks(file);
			try {
				List<List<UserChunk>> formattedChunks = BlameUtil.format(fileChunks);
				if(fragmented || formattedChunks.size() == 1) {
					exporter.export(formattedChunks);
					writeIndex(formattedChunks);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		listener.messagePushed("Exported " + files.size() + " files");

	}
	
	public void writeIndex(List<List<UserChunk>> formattedChunks) throws IOException{
		UserChunk first = formattedChunks.get(0).get(0);
		String subPath = first.committer.repo.replace('/', '-'); 
		subPath += "-" + first.file.filePath;
		File indexFile = new File("export", subPath+".log");
		File completeFile = new File("export", subPath+".complete");
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(indexFile, false));
	    
		for(List<UserChunk> chunks : formattedChunks){
			 int start = chunks.get(0).start;
			 int end = chunks.get(chunks.size()-1).end;
			 String user = chunks.get(0).getUser().login;
			 bw.write(completeFile.getName() + "\t" + user + "\t" + start + "\t" + end);
	         bw.newLine();
		}
	    bw.close();
	}

	@Override
	public String getCommand() {
		return "export";
	}

}
