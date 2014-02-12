package shibboleth.model;

/**
 * Represents a contribution to a repo by a user. 
 * <b>Note: not vice versa.</b> .
 * @author Wilco Wisse
 * @see ContributionInfo
 */
public class Contribution {
	
	private SimpleUser user;
	private SimpleRepo repo;
	private ContributionInfo info;
	
	/**
	 * Construct a contribution to repo <tt>r</tt> by user <tt>u</tt>
	 * @param u The user.
	 * @param r The repo.
	 */
	public Contribution(SimpleUser u, SimpleRepo r){
		user=u;
		repo=r;
	}
	
	/**
	 * @return Returns <tt>true</tt> if this contributions has additional 
	 * contribution info. Rationale: the github REST api only provides this
	 * information if we ask for contributions of a particular repo. The
	 * other way around, (if we ask for the repositories a user owns) 
	 * we cannot get this information without excessively querying the REST API. 
	 */
	public boolean hasContributionInfo(){
		return info!=null;
	}
	
	/**
	 * @return Returns contribution info of this contribution. Returns null if this
	 * is not currently available.
	 */
	public ContributionInfo getContributionInfo(){
		return info;
	}
	
	/**
	 * Add contribution info for this contribution.
	 * @param info
	 */
	public void setContributionInfo(ContributionInfo info){
		this.info = info;
	}
	
	/**
	 * @return The user related to this contribution.
	 */
	public SimpleUser getUser(){
		return user;
	}
	
	/**
	 * @return The repo related to this contribution.
	 */
	public SimpleRepo getRepo(){
		return repo;
	}
	
	@Override
	public boolean equals(Object other){
		return (other instanceof Contribution) ? 
				user.login.equals(((Contribution)other).getUser().login) 
				&& repo.full_name.equals(((Contribution)other).getRepo().full_name) 
				: false;
	}
	
	@Override
	public String toString(){
		String contributionInfo = hasContributionInfo() ? info.toString() : " ";
		return String.format("%s %s: %s", repo.full_name, user.login, contributionInfo);
	}
}
