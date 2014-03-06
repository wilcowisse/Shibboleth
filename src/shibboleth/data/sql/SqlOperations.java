package shibboleth.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import shibboleth.model.Committer;
import shibboleth.model.GitFile;
import shibboleth.model.RecordLink;
import shibboleth.model.SimpleUser;
import shibboleth.model.UnknownUser;
import shibboleth.model.UserChunk;
import shibboleth.util.GithubUtil;

public class SqlOperations {
	
	private PreparedStatement 
		  selectCommittersByRepo
		, selectRecordLinksByRepo
		, selectUserChunksOfFile
		, selectFiles
		, selectFilesOfRepo
	;


	public SqlOperations(Connection connection){
		try {
			selectCommittersByRepo 	=  connection.prepareStatement(Statements.selectCommittersByRepo);
			selectRecordLinksByRepo =  connection.prepareStatement(Statements.selectRecordLinksByRepo);
			selectUserChunksOfFile	=  connection.prepareStatement(Statements.selectUserChunksOfFile);
			selectFiles				=  connection.prepareStatement(Statements.selectAllFiles);
			selectFilesOfRepo		=  connection.prepareStatement(Statements.selectFilesOfRepo);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	/**
	 * @return All committers which contributed to the given <tt>repo</tt>.
	 */
	public List<Committer> getCommitters(String repo){
		List<Committer> committerList = new ArrayList<Committer>();
		try {
			selectCommittersByRepo.setString(1, repo);
			ResultSet resultSet = selectCommittersByRepo.executeQuery();
			
			while(resultSet.next()){
				Committer c = new Committer();
				c.email=resultSet.getString("email");
				c.name =resultSet.getString("name");
				c.repo =resultSet.getString("repo");
				committerList.add(c);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return committerList;
	}
	
	public List<RecordLink> getRecordLinks(String repo){
		List<RecordLink> recordLinkList = new ArrayList<RecordLink>();
		try {
			selectRecordLinksByRepo.setString(1, repo);
			ResultSet resultSet = selectRecordLinksByRepo.executeQuery();
			
			while(resultSet.next()){
				Committer c = new Committer();
				c.email=resultSet.getString("email");
				c.name =resultSet.getString("name");
				c.repo = repo;
				
				String userName = resultSet.getString("user");
				SimpleUser user = null;
				if(userName == null)
					user=UnknownUser.getInstance();
				else
					user = new SimpleUser(userName);
				
				recordLinkList.add(new RecordLink(c, user, 1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return recordLinkList;
	}
	
	/**
	 * Select the id's of the files in the database
	 * @return A list with all file id's.
	 */
	public List<Integer> getAllFileIds(){
		List<Integer> result = new ArrayList<Integer>();
		try {
			ResultSet resultSet = selectFiles.executeQuery();
			while(resultSet.next()){
				result.add(resultSet.getInt("id"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Select the id's of the files of the given repo.
	 * @param repo The repo.
	 * @return File id's of the repo.
	 */
	public List<Integer> getFileIdsOfRepo(String repo){
		List<Integer> result = new ArrayList<Integer>();
		try {
			selectFilesOfRepo.setString(1, repo);
			ResultSet resultSet = selectFilesOfRepo.executeQuery();
			while(resultSet.next()){
				result.add(resultSet.getInt("id"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Select all chunks of a file.
	 * @param file The id of the file
	 * @return All chunks of the file with the given id.
	 * @throws SQLException 
	 */
	public List<UserChunk> getFileChunks(int file) throws SQLException{
		List<UserChunk> result = new ArrayList<UserChunk>(1000);
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
		
		selectUserChunksOfFile.setInt(1, file);
		ResultSet resultSet = selectUserChunksOfFile.executeQuery();
		while(resultSet.next()){
			int start 			= resultSet.getInt("start");
			int end				= resultSet.getInt("end");
			String when			= resultSet.getString("time");
			GitFile gitfile 	= new GitFile();
			gitfile.filePath	= resultSet.getString("file_path");
			gitfile.head 		= resultSet.getString("head");
			gitfile.repo		= resultSet.getString("repo");
			Committer committer = new Committer();
			committer.email 	= resultSet.getString("email");
			committer.name  	= resultSet.getString("name");
			committer.repo		= resultSet.getString("repo");
			UserChunk userChunk = new UserChunk(GithubUtil.createUser(resultSet.getString("user")));
			userChunk.committer	= committer;
			userChunk.file		= gitfile;
			userChunk.start		= start;
			userChunk.end		= end;
			try {
				userChunk.when	= dateFormat.parse(when);
			} catch (ParseException e) {
				userChunk.when	= new Date();
			}
			result.add(userChunk);
		}

		return result;
	}
}
