package shibboleth.util;

import java.util.ArrayList;
import java.util.List;

import shibboleth.model.UserChunk;

public class BlameUtil {

	/**
	 * Groups the chunks of the same user together in lists.
	 * @param chunks The chunks, each belonging to the same file.
	 * @return A lists with lists which have chunks of the same user
	 */
	public static List<List<UserChunk>> format(List<UserChunk> chunks){
		List<List<UserChunk>> result = new ArrayList<List<UserChunk>>();
		
		UserChunk lastChunk = null;
		List<UserChunk> lastList = new ArrayList<UserChunk>();
		for(UserChunk chunk : chunks) {
			assert chunk.follows(lastChunk);
			assert chunk.hasSameFileAs(lastChunk);
			if(chunk.hasSameUserAs(lastChunk)){
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
}
