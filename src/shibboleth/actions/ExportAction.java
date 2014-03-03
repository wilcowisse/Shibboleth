package shibboleth.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import shibboleth.data.sql.CommitInfoStore;
import shibboleth.git.Exporter;
import shibboleth.model.UserChunk;

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
		boolean fragmentedFiles = true;
		if(args.length > 0 && args[0].equals("-f"))
			fragmentedFiles = false;
		
		List<Integer> files = infoStore.getAllFileIds();
		
		for(Integer file : files){
			List<UserChunk> chunksOfFile = infoStore.getFileChunks(file);
			exporter.export(chunksOfFile, fragmentedFiles);
		}
		listener.messagePushed("Exported " + files.size() + " files");

	}

	@Override
	public String getCommand() {
		return "export";
	}

}
