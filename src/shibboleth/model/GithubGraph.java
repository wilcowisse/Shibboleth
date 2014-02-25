package shibboleth.model;

import java.awt.Color;
import java.io.File;

public interface GithubGraph {
	
	/**
	 * Add user to the graph, if the graph already contains this user, 
	 * nothing is added.
	 * @param user The user.
	 */
	public void add(SimpleUser user);
	
	/**
	 * Add repo to the graph, if the graph already contains this repo, 
	 * nothing is added.
	 * @param repo The repo.
	 */
	public void add(SimpleRepo repo);
	
	/**
	 * Set color to a node. If color is null, standard 
	 * colors are being applied.
	 * @param nodeName The name of the node
	 * @param color The color
	 */
	public void setColor(String nodeName, Color color);
	
	/**
	 * Remove node with the given node name. Removes neighbors of the given
	 * node if their degree will become equal to 0;
	 * Generally speaking the node name is
	 * either a Github user name or a Github repo name.
	 * @param nodeName
	 * @return Whether a node was indeed removed from the graph.
	 */
	public boolean remove(String nodeName);
	
	/**
	 * Remove all nodes from the graph.
	 */
	public void removeAll();
	
	/**
	 * Add a contribution to the graph.
	 * The user and the repo of the contribution are added as well.
	 * @param c
	 */
	public void addContribution(Contribution c);
	
	/**
	 * Export the graph to the given file.
	 * @param file The path
	 */
	public void export(File file);
	
}
