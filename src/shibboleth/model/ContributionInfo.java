package shibboleth.model;


/**
 * A class which provides information about a contribution.
 * @author Wilco Wisse
 */
public class ContributionInfo {
	
	/**
	 * Construct contribution information class which indicates the number of
	 * contributions to a repo and what percentage this is.
	 * @param count The number of contributions.
	 * @param percentage The percentage, i.e. percentage = count/totalNumberOfContributions.
	 */
	public ContributionInfo(int count, int percentage){
		this.count=count;
		this.percentage=percentage;
	}
	
	/**
	 * Number of contributions
	 */
	public int count;
	
	/**
	 * Percentage of contributions.
	 */
	public int percentage;
	
	@Override
	public String toString(){
		return count + " contributions (" + percentage + "%) ";
	}

}
