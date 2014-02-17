package shibboleth.data.github;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Data class for holding Github REST API limit values.
 * @author Wilco Wisse
 *
 */
public class RateLimitValue {
	
	public static final int LIMIT=0, REMAINING=1, RESET=2;
	
	private int limit;
	private int remaining;
	private Date reset;
	
	/**
	 * Construct with limit=-1, remaining=-1 and reset=January 1st, 1970 (UTC).
	 */
	public RateLimitValue(){
		limit=-1;
		remaining=-1;
		reset = new Date(0);
	}
	
	/**
	 * Set the given value.
	 * @param type What to set, out of 
	 * {@link RateLimitValue#LIMIT}, 
	 * {@link RateLimitValue#REMAINING} or 
	 * {@link RateLimitValue#RESET}
	 * @param value The value to set.
	 * @return <tt>this</tt> (for chaining).
	 */
	public RateLimitValue set(int type, int value){
		switch(type){
		case LIMIT: limit=value; break;
		case REMAINING: remaining=value; break;
		case RESET: reset=new Date((long)value*1000);
		}
		return this;
	}
	
	/**
	 * @return The limit in requests/hour.
	 */
	public int getLimit(){
		return limit;
	}
	
	/**
	 * @return The number of remaining requests.
	 */
	public int getRemaining(){
		return remaining;
	}
	
	/**
	 * @return The time when the number remaining requests will be reset.
	 */
	public Date getReset(){
		return reset;
	}
	
	
	
	@Override
	public String toString(){
		String expTime =  new SimpleDateFormat("H:mm").format(reset);
		return String.format("Rate: %d/%d, %s", remaining, limit, expTime);
	}
}
