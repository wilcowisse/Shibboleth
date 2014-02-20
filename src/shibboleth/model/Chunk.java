package shibboleth.model;

import java.util.Date;
import java.util.Objects;

/**
 * A chunk is a contiguous piece of a {@link GitFile} with authorship information.
 * 
 * @author Wilco Wisse
 */
public class Chunk {
	
	/**
	 * The file this chunk belongs to.
	 */
	public GitFile file;
	
	/**
	 * The committer this chunk has been written by.
	 */
	public Committer committer;
	
	/**
	 * When the committer wrote this chunk.
	 */
	public Date when;
	
	/**
	 * The first line number of this chunk.
	 * Note: line numbers start at 0.
	 */
	public int start;
	
	/**
	 * The last line number of this chunk.
	 * Note: line numbers start at 0.
	 */
	public int end;
	
	@Override
	public boolean equals(Object other){
		return other instanceof Chunk ? 
				   (file      != null ? file.equals(((Chunk)other).file)           : ((Chunk)other).file == null)
				&& (committer != null ? committer.equals(((Chunk)other).committer) : ((Chunk)other).committer == null)
				&& (when      != null ? when.equals(((Chunk)other).when)           : ((Chunk)other).when == null)
				&& (start     == ((Chunk)other).start)
				&& (end       == ((Chunk)other).end)
				: false;
	}
	
	@Override
	public int hashCode(){
		return Objects.hash(file, committer,when, start, end);
	}
	
	@Override
	public String toString(){
		return String.format("Chunk: [%s] [%s] %d %d",file, committer, start, end);
	}
}
