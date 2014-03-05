package shibboleth.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mysql.jdbc.Statement;

import shibboleth.model.Chunk;
import shibboleth.model.Committer;
import shibboleth.model.GitFile;
import shibboleth.model.RecordLink;
import shibboleth.model.UnknownUser;

/**
 * This class acts as data store for analyzed committer information. Generally, 
 * committer information, such as authorship information is extracted 
 * from a cloned repository. 
 * 
 * @author Wilco Wisse
 *
 */
public class CommitInfoStore {
	
	private PreparedStatement insertFile, insertCommitter, insertChunk,insertRecordLink;
	private PreparedStatement selectCommitterId, selectFileId, selectChunkId;
	private PreparedStatement deleteRecordLink;
	
	private List<Committer> committers;
	private List<GitFile> files;
	private List<Chunk> chunks;
	
	private Map<Committer, Integer> committerIds;
	private Map<GitFile, Integer> fileIds;
	
	/**
	 * Construct with given connection
	 * @param connection
	 */
	public CommitInfoStore(Connection connection){
		
		try {
			insertFile			 	=  connection.prepareStatement(Statements.insertFile, Statement.RETURN_GENERATED_KEYS);
			insertCommitter 		=  connection.prepareStatement(Statements.insertCommitter, Statement.RETURN_GENERATED_KEYS);
			insertChunk 			=  connection.prepareStatement(Statements.insertChunk, Statement.RETURN_GENERATED_KEYS);
			selectCommitterId		=  connection.prepareStatement(Statements.selectCommitterId);
			selectFileId			=  connection.prepareStatement(Statements.selectFileId);
			selectChunkId			=  connection.prepareStatement(Statements.selectChunkId);
			insertRecordLink		=  connection.prepareStatement(Statements.insertRecordLink);
			deleteRecordLink		=  connection.prepareStatement(Statements.deleteRecordLink);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		committers 	= new ArrayList<Committer>();
		files 		= new ArrayList<GitFile>();
		chunks 		= new ArrayList<Chunk>();
		committerIds = new HashMap<Committer,Integer>();
		fileIds		= new HashMap<GitFile,Integer>();
	}
	
	/**
	 * Put  a file in the db buffer.
	 * @param file The file in a repository.
	 */
	public void insert(GitFile file){
		if(!files.contains(file))
			files.add(file);
	}
	
	/**
	 * Put a committer in the db buffer.
	 * @param committer The committer to store.
	 */
	public void insert(Committer committer){
		if(!committers.contains(committer))
			committers.add(committer);
	}
	
	/**
	 * Put a chunk in the db buffer.
	 * The committer and the files belonging to this chunk are stored as well.
	 * @param chunk The chunk to store.
	 */
	public void insert(Chunk chunk){
		if(!chunks.contains(chunk)){
			chunks.add(chunk);
			insert(chunk.committer);
			insert(chunk.file);
		}
	}
	
	/**
	 * Get the database record id of the given file.
	 * @param file The file
	 * @return the id of the given Gitfile if the file exists in db, 
	 * otherwise -1.
	 * @throws SQLException
	 */
	public int getFileId(GitFile file) throws SQLException{
		int fileId = -1;
		if(fileIds.containsKey(file)){
			fileId = fileIds.get(file);
		}
		else{
			selectFileId.setString(1,file.repo);
			selectFileId.setString(2,file.head);
			selectFileId.setString(3,file.filePath);
			ResultSet resultSet = selectFileId.executeQuery();
			if(resultSet.next()){
				fileId = resultSet.getInt("id");
				fileIds.put(file, fileId);
			}
			else{
				return -1;
			}
		}
		return fileId;
	}
	
	/**
	 * Get the database record id of the given committer.
	 * @param committer The committer
	 * @return the id of the given committer if the committer exists in db, 
	 * otherwise -1.
	 * @throws SQLException
	 */
	public int getCommitterId(Committer committer) throws SQLException{
		int committerId = -1;
		if(committerIds.containsKey(committer)){
			committerId = committerIds.get(committer);
		}
		else{
			selectCommitterId.setString(1,committer.repo);
			selectCommitterId.setString(2,committer.email);
			selectCommitterId.setString(3,committer.name);
			ResultSet resultSet = selectCommitterId.executeQuery();
			if(resultSet.next()){
				committerId = resultSet.getInt("id");
				committerIds.put(committer, committerId);
			}
			
		}
		return committerId;
	}
	
	/**
	 * Get the database record id of the given chunk.
	 * @param chunk The chunk
	 * @param fileId The DB id belonging to the file of the chunk.
	 * @param committerId The DB id belonging to the committer of the chunk.
	 * @return the id of the given chunk if the chunk exists in db, 
	 * otherwise -1.
	 * @throws SQLException
	 */
	public int getChunkId(Chunk chunk, int fileId, int committerId) throws SQLException{
		int chunkId = -1;
		
		String when = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss").format(chunk.when);
		selectChunkId.setInt(1,fileId);
		selectChunkId.setInt(2,chunk.start);
		selectChunkId.setInt(3,chunk.end);
		selectChunkId.setInt(4,committerId);
		selectChunkId.setString(5,when);
		
		ResultSet resultSet = selectChunkId.executeQuery();
		if(resultSet.next()){
			chunkId = resultSet.getInt("id");
		}
		
		return chunkId;
	}
	
	/**
	 * Flush the committer, file and chunk buffers to the database. In general no
	 * item is stored if the item is already present in the database.
	 */
	public void writeToDB() throws SQLException{
		for(Committer c : committers){
			int committerKey = getCommitterId(c);
			if(committerKey == -1){
				insertCommitter.setString(1, c.repo);
				insertCommitter.setString(2, c.email);
				insertCommitter.setString(3, c.name);
				
				insertCommitter.executeUpdate();
				
				int affected = insertCommitter.getUpdateCount();
				committerKey = -1;
				ResultSet generatedKeys = insertCommitter.getGeneratedKeys();
				if(generatedKeys.next() && affected > 0){
					committerKey = generatedKeys.getInt(1);
					committerIds.put(c, committerKey);
				}
				else{
					System.out.println("Generated no commiter id!!");
				}
			}

		}
		
		for(GitFile f : files){
			int fileKey = getFileId(f);
			if(fileKey == -1){
				insertFile.setString(1, f.repo);
				insertFile.setString(2, f.head);
				insertFile.setString(3, f.filePath);
				
				insertFile.executeUpdate();
				
				int affected = insertFile.getUpdateCount();
				ResultSet generatedKeys = insertFile.getGeneratedKeys();
				if(generatedKeys.next() && affected > 0){
					int generatedKey = generatedKeys.getInt(1);
					fileIds.put(f, generatedKey);
				}
				else{
					System.out.println("Generated no file id");
				}
			}

		}
			
		for(Chunk c : chunks){
			int fileId = getFileId(c.file);
			int committerId = getCommitterId(c.committer);
			
			if(getChunkId(c, fileId, committerId) == -1){
				String when = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss").format(c.when);
				insertChunk.setInt(1, fileId);
				insertChunk.setInt(2, c.start);
				insertChunk.setInt(3, c.end);
				insertChunk.setInt(4, committerId);
				insertChunk.setString(5, when);
				
				insertChunk.executeUpdate();
			}					

		}
		
		committers.clear();
		files.clear();
		chunks.clear();
	}
	
	
	/**
	 * Store a RecordLink in the database.
	 * @param link
	 */
	public void insertRecordLink(RecordLink link){
		try {
			if(!link.user.equals(UnknownUser.getInstance())){
				insertRecordLink.setInt(1, getCommitterId(link.committer));
				insertRecordLink.setString(2, link.user.login);
				insertRecordLink.executeUpdate();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Delete RecordLinks of a committer
	 * @param committerId The db id of the committer.
	 */
	public void deleteRecordLink(int committerId){
		try {
			 deleteRecordLink.setInt(1, committerId);
			 deleteRecordLink.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	
}
