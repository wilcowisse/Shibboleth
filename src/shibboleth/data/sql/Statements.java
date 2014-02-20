package shibboleth.data.sql;

/**
 * Class with various MySql queries.
 * 
 * @author Wilco Wisse
 *
 */
public class Statements {
	
	public static String db_name = "";
	
	/********************************************/
	
	public static final String selectRepo = 
	"SELECT * FROM Repos WHERE full_name=?;";
	
	
	
	public static final String insertRepo = 
	"REPLACE INTO Repos VALUES (?,?,?,?,?,?,?,?,?,?);";
	
	
	
	public static final String countRepos = 
	"SELECT count(*) FROM Repos WHERE full_name=?;";
	
	
	public static final String deleteRepo =
	"DELETE FROM Repos WHERE full_name=?;";
	
	/********************************************/
	
	public static final String selectUser = 
	"SELECT * FROM Users WHERE login=?;";
	
	
	
	public static final String insertUser = 
	"REPLACE INTO Users VALUES (?,?,?,?,?,?,?,?,?,?);";
	
	
	
	public static final String countUsers = 
	"SELECT count(*) FROM Users WHERE login=?;";
	
	
	
	public static final String deleteUser =
	"DELETE FROM Users WHERE login=?;";
	
	
	
	public static final String selectReposByUser =
	"SELECT Repos.* FROM Contributions " +
	"JOIN Repos ON Contributions.repo_name = Repos.full_name " +
	"WHERE user_name=?;";
	
	
	
	/********************************************/
	
	public static final String selectAllContributions = "SELECT repo_name, user_name, count, percentage FROM Contributions " +
	"LEFT JOIN ContributionInfo ON Contributions.id = ContributionInfo.contribution_id;";
	
	
	public static final String selectContributions = 
	"SELECT repo_name, user_name, count, percentage FROM Contributions " +
	"LEFT JOIN ContributionInfo ON Contributions.id = ContributionInfo.contribution_id " +
	"WHERE repo_name=?;";
	
	
	public static final String selectSingleContribution = 
	"SELECT Contributions.id, repo_name, user_name, count, percentage FROM Contributions " +
	"LEFT JOIN ContributionInfo ON Contributions.id = ContributionInfo.contribution_id " +
	"WHERE repo_name=? AND user_name=?;";
	
	
	public static final String countContributions = 
	"SELECT count(*) FROM Contributions WHERE repo_name=? AND user_name = ?";
	
	
	public static final String insertContribution = 
	"INSERT INTO Contributions VALUES (null, ?, ?);";
			
	
	/********************************************/
	
	
	public static final String countContributionsInfo = 
	"SELECT count(*) FROM Contributions " +
	"JOIN ContributionInfo ON Contributions.id = ContributionInfo.contribution_id " +
	"WHERE repo_name=? AND user_name=?;";

	
	public static final String insertContributionInfo = 
	"REPLACE INTO ContributionInfo VALUES (?, ?, ?);";
	
	
	public static final String deleteContributionInfoByRepo = 
	"DELETE FROM ContributionInfo WHERE contribution_id IN (SELECT Contributions.id FROM Contributions WHERE repo_name=?)";
	public static final String deleteContributionsByRepo = 
	"DELETE FROM Contributions WHERE repo_name=?";
// mysql equivalent, not compatible with sqlite :(
//	"DELETE Contributions, ContributionInfo " +
//	"FROM Contributions LEFT JOIN ContributionInfo ON Contributions.id = contribution_id " +
//	"WHERE repo_name=?";
	
	
	public static final String deleteContributionInfoByUser = 
	"DELETE FROM ContributionInfo WHERE contribution_id IN (SELECT Contributions.id FROM Contributions WHERE user_name=?)";
	public static final String deleteContributionsByUser = 
	"DELETE FROM Contributions WHERE user_name=?";
// mysql equivalent, not compatible with sqlite :(
//	public static final String deleteContributionsByUser =
//	"DELETE Contributions, ContributionInfo " +
//	"FROM Contributions LEFT JOIN ContributionInfo ON Contributions.id = contribution_id " +
//	"WHERE user_name=?";
	
	/********************************************/
	
	
	public static final String insertCommitter = 
	"INSERT INTO Committers(id,repo,email,name) " +
	"SELECT null, ?, ?, ? " + (db_name.equals("mysql") ? "FROM dual " : "") +
	"WHERE NOT EXISTS (" +
	"SELECT * FROM Committers " +
	"WHERE repo=? AND email=? AND name=?)";
	
	
	public static final String insertFile = 
	"INSERT INTO Files(id,repo,head,file_path) " +
	"SELECT null, ?, ?, ? " + (db_name.equals("mysql") ? "FROM dual " : "") +
	"WHERE NOT EXISTS (" +
	"SELECT * FROM Files " +
	"WHERE repo=? AND head=? AND file_path=?)";
	
	
	public static final String insertChunk = 
	"INSERT INTO Chunks(id,file_id, start,end,committer_id, time) " +
	"SELECT null,?,?,?,?,? " + (db_name.equals("mysql") ? "FROM dual " : "") +
	"WHERE NOT EXISTS (" +
	"SELECT * FROM Chunks " +
	"WHERE file_id=? AND start=? AND end=? AND committer_id=? AND time=?)";
	
	public static final String selectCommitterId = 
	"SELECT id FROM Committers WHERE repo=? AND email=? AND name=?;";
	
	public static final String selectFileId = 
	"SELECT id FROM Files WHERE repo=? AND head=? AND file_path=?;";
	
	public static final String selectCommittersByRepo =
	"SELECT * FROM Committers WHERE repo=?;";
	

	/*******************************************/

	
	public static final String insertRecordLink = 
	"REPLACE INTO RecordLinks VALUES (?,?);";
	
	
	/*******************************************/
	
	public static String insertStoredLinks = 
	"REPLACE INTO StoredLinks VALUES(?,?);";



	public static String deleteStoredLinks = 
	"DELETE FROM StoredLinks WHERE name=?;";



	public static String selectStoredLinks =
	"SELECT * FROM StoredLinks WHERE name=?;";



	
	
}
