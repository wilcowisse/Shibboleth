package shibboleth.model;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * A GrihubGraph which does nothing else than counting how many users, repos and contributions have been added.
 * 
 * @author Wilco Wisse
 *
 */
public class CountGraph implements GithubGraph {
	
	private int userCount=0, repoCount=0, contributionCount=0;
	
	@Override
	public void add(SimpleUser user) {
		userCount++;
	}

	@Override
	public void add(SimpleRepo repo) {
		repoCount++;
	}

	@Override
	public void setColor(String nodeName, Color color) {
	}

	@Override
	public boolean remove(String nodeName) {
		return false;
	}

	@Override
	public void removeAll() {
		userCount=0;
		repoCount=0;
		contributionCount=0;
	}

	@Override
	public void addContribution(Contribution c) {
		contributionCount++;

	}

	@Override
	public void export(File file) {
		try {
			PrintWriter writer = new PrintWriter(file);
			writer.println("User count: "+userCount);
			writer.println("Repo count: "+repoCount);
			writer.println("Coontribution count: "+contributionCount);
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public int getUserCount(){
		return userCount;
	}
	
	public int getRepoCount(){
		return repoCount;
	}
	
	public int getContributionCount(){
		return contributionCount;
	}
	
	
}
