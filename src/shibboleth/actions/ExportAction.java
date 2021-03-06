package shibboleth.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import shibboleth.data.sql.SqlOperations;
import shibboleth.git.Exporter;
import shibboleth.model.UserChunk;

public class ExportAction extends ShibbolethAction {

	private SqlOperations sqlOperations;
	private List<String> blackList;
	
	public ExportAction(SqlOperations sqlOp){
		this.sqlOperations = sqlOp;
		
		blackList = new ArrayList<String>();
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
	}
	
	@Override
	public void execute(String[] args) {
		boolean fragmented = args.length > 0 && args[0].equals("-f");
		List<Integer> files = sqlOperations.getAllFileIds();
		execute(files,fragmented);
	}
	
	public void execute(List<Integer> files, boolean fragmented){
		
		Exporter exporter = new Exporter(new File("export"), new File("clones"), blackList);
		
			for(Integer file : files){
				try {
					List<UserChunk> fileChunks = sqlOperations.getFileChunks(file);
					exporter.export(fileChunks, fragmented);
				} catch (SQLException | IOException e) {
					listener.errorOccurred(e, false);
				}
				
			}
		
		listener.messagePushed("Exported " + files.size() + " files");
	}
	
	@Override
	public String getCommand() {
		return "export";
	}

}
