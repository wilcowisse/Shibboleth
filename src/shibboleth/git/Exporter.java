package shibboleth.git;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import shibboleth.data.sql.CommitInfoStore;
import shibboleth.model.Chunk;
import shibboleth.model.GitFile;
import shibboleth.model.UserChunk;

/**
 * Export the code from a repo of a sorted list of chunks.
 * 
 * @author Wilco Wisse
 *
 */
public class Exporter {
	private File exportPath, clonePath;
	private List<String> pathBlackList;
	
	/**
	 * Construct an exporter
	 * @param exportPath The directory to export to
	 * @param clonePath The directory where the cloned repos can be found.
	 * @param chunks The list of chunks, <i>sorted on path and line start</i>.
	 * @param fragmented Whether fragmented files will be accepted.
	 */
	public Exporter(File exportPath, File clonePath, List<String> pathBlackList){
		this.exportPath = exportPath;
		this.clonePath = clonePath;
		this.pathBlackList=pathBlackList;
	}
	
	public void export(List<UserChunk> chunksOfFile, boolean fragmented){
		
		List<List<UserChunk>> formattedChunks = format(chunksOfFile);
		if(fragmented || formattedChunks.size() == 1) {
			UserChunk first = chunksOfFile.get(0);
			String subPath = first.committer.repo.replace('/', '-');
			subPath += "/" + first.file.filePath;
			File source = new File(clonePath, subPath);
			File target = new File(exportPath, subPath.replace('/', '-') + ".complete");
			
			if(pathBlackList.contains(first.file.filePath)){
				return;
			}
			
			try {
				Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}

			int i = 0;
			for(List<UserChunk> formattedChunk : formattedChunks){
				write(formattedChunk,i);
				i++;
			}
		}
		
	}

	public List<List<UserChunk>> format(List<UserChunk> chunks){
		List<List<UserChunk>> result = new ArrayList<List<UserChunk>>();
		
		UserChunk lastChunk = null;
		List<UserChunk> lastList = new ArrayList<UserChunk>();
		for(UserChunk chunk : chunks) {
			assert chunk.follows(lastChunk);
			if(chunk.hasSameUserAs(lastChunk)){
				lastList.add(chunk);
			}
			else{
				if(lastList.size()>0)
					result.add(lastList);
				lastList = new ArrayList<UserChunk>();
				lastList.add(chunk);
			}
			lastChunk=chunk;
			
		}
		
		return result;
		
	}
	
	public void write(List<UserChunk> chunks, int pos){
		if(chunks.size() < 1)
			return;
		
		UserChunk first = chunks.get(0);
		String subPath = first.committer.repo.replace('/', '-'); 
		subPath += "/" + first.file.filePath;
		File source = new File(clonePath, subPath);
		
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(source.getPath()));
			String fileContents = Charset.defaultCharset().decode(ByteBuffer.wrap(encoded)).toString();
			String[] fileLines = fileContents.split("\\r?\\n");

			String[] slice = Arrays.copyOfRange(fileLines, chunks.get(0).start, chunks.get(chunks.size()-1).end+1);
			File target = new File(exportPath, subPath.replace('/', '-') + "." + pos + "." + chunks.get(0).getUser());
			copy(slice, target);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public void copy(String[] lines, File dest) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(dest, false));
        for(int i = 0; i<lines.length-1; i++){
        	bw.write(lines[i]);
            bw.newLine();
        }
        if(lines.length>0){
        	bw.write(lines[lines.length-1]);
        }
        bw.close();

	}
}
