package shibboleth.model;

/**
 * A contribution with a unique key
 * 
 * @author Wilco Wisse
 *
 */
public class ContributionId extends Contribution {

	private int key;
	
	public ContributionId(int key, SimpleUser u, SimpleRepo r) {
		super(u, r);
		this.key=key;
	}
	
	public int getKey(){
		return key;
	}
	
	public void setKey(int key){
		this.key = key;
	}
	
	public static ContributionId fromContribution(int key, Contribution c){
		return new ContributionId(key, c.getUser(), c.getRepo());
	}

}
