package shibboleth.actions;

import shibboleth.data.github.GithubDataSource;

/**
 * Provide Github api token.
 * 
 * Syntax: <tt>token [token]</tt>.
 * 
 * @see GithubDataSource#setAccessToken(String)
 * @author Wilco Wisse
 *
 */
public class TokenAction extends ShibbolethAction {
	
	private GithubDataSource github;
	
	public TokenAction(GithubDataSource github){
		this.github = github;
	}
	
	@Override
	public void execute(String[] args) {
		if(args.length == 1){
			github.setAccessToken(args[0]);
			if(listener != null){
				listener.messagePushed("Set Github access token: "+args[0]);
			}
		}
		else if(listener != null){
			listener.messagePushed("Wrong syntax");
		}
		
		
	}

	@Override
	public String getCommand() {
		return "token";
	}

}
