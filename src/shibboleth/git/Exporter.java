package shibboleth.git;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

import shibboleth.model.UserChunk;
import shibboleth.util.BlameUtil;

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
	 * @param pathBlackList paths which must not be exported.
	 */
	public Exporter(File exportPath, File clonePath, List<String> pathBlackList){
		this.exportPath = exportPath;
		this.clonePath = clonePath;
		this.pathBlackList=pathBlackList;
	}
	
	/**
	 * Copies the file fragments of several repos to one location.
	 * @param formattedChunks Chunks of one file, grouped by author. See also
	 * {@link BlameUtil#format(List)}.
	 * @throws IOException
	 */
	public void export(List<List<UserChunk>> formattedChunks) throws IOException{
		assert formattedChunks.size() > 0;
		assert formattedChunks.get(0).size() > 0;
		
		UserChunk first = formattedChunks.get(0).get(0);
		String subPath = first.committer.repo.replace('/', '-');
		subPath += "/" + first.file.filePath;
		File source = new File(clonePath, subPath);
		File target = new File(exportPath, subPath.replace('/', '-') + ".complete");
		
		if(pathBlackList.contains(first.file.filePath)){
			return;
		}
		
		Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
		
		if(formattedChunks.size()>1){
			int i = 0;
			for(List<UserChunk> formattedChunk : formattedChunks){
				write(formattedChunk, i);
				i++;
			}
		}
	}
	
	private void write(List<UserChunk> chunks, int pos) throws IOException{
		if(chunks.size() < 1)
			return;
		
		UserChunk first = chunks.get(0);
		String subPath = first.committer.repo.replace('/', '-'); 
		subPath += "/" + first.file.filePath;
		File source = new File(clonePath, subPath);
		
		byte[] encoded = Files.readAllBytes(Paths.get(source.getPath()));
		String fileContents = Charset.defaultCharset().decode(ByteBuffer.wrap(encoded)).toString();
		String[] fileLines = fileContents.split("\\r?\\n");

		String[] slice = Arrays.copyOfRange(fileLines, chunks.get(0).start, chunks.get(chunks.size()-1).end+1);
		File target = new File(exportPath, subPath.replace('/', '-') + "." + pos + "." + chunks.get(0).getUser());
		copy(slice, target);

	}

	private void copy(String[] lines, File dest) throws IOException {
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
