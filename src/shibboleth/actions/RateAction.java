package shibboleth.actions;

import java.text.SimpleDateFormat;

import shibboleth.data.github.RateLimitValue;

/**
 * Show Github rate information.
 * 
 * Syntax: <tt>rate</tt>.
 * 
 * @author Wilco Wisse
 *
 */
public class RateAction extends ShibbolethAction{

	private RateLimitValue rate;
	
	public RateAction(RateLimitValue rate){
		this.rate=rate;
	}
	
	@Override
	public void execute(String[] args) {
		assert listener != null;
		String message = new StringBuilder()
		.append("Limit: " + rate.getLimit()).append('\n')
		.append("Remaining: " + rate.getRemaining()).append('\n')
		.append("Expiration:") + new SimpleDateFormat("H:mm").format(rate.getReset())
		.toString();
		listener.messagePushed(message);
	}

	@Override
	public String getCommand() {
		return "rate";
	}
	
	
}
