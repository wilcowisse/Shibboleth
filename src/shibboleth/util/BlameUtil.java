package shibboleth.util;

import java.util.ArrayList;
import java.util.List;

import shibboleth.model.UserChunk;

public class BlameUtil {

	/**
	 * Groups the chunks of the same user together in lists.
	 * @param chunks A list of chunks, each chunk belonging to the same file.
	 * @return A lists with lists which have chunks of the same user
	 */
	public static List<List<UserChunk>> format(List<UserChunk> chunks){
		List<List<UserChunk>> result = new ArrayList<List<UserChunk>>();
		
		UserChunk lastChunk = null;
		List<UserChunk> lastList = new ArrayList<UserChunk>();
		for(UserChunk chunk : chunks) {
			assert lastChunk == null || chunk.hasSameFileAs(lastChunk);
			if(chunk.hasSameUserAs(lastChunk) && chunk.follows(lastChunk)){
				lastList.add(chunk);
			}
			else{
				lastList = new ArrayList<UserChunk>();
				lastList.add(chunk);
				result.add(lastList);
			}
			lastChunk=chunk;
			
		}
		return result;
		
	}
	
	/**
	 * Check whether a list of chunks is contiguous.
	 * @param chunks The chunks
	 * @return Returns true if all chunks belong to the same file and each chunks follows its predecessor.
	 */
	public static boolean isContiguous(List<UserChunk> chunks){
		UserChunk lastChunk = null;
		for(UserChunk chunk : chunks) {
			if(!chunk.hasSameFileAs(lastChunk))
				return false;
			if(!chunk.follows(lastChunk))
				return false;
			lastChunk=chunk;
		}
		return true;
	}
}
