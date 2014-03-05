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
			execute(args[0]);
		}
		else {
			listener.messagePushed("Wrong syntax");
		}
	}

	public void execute(String token){
		github.setAccessToken(token);
		listener.messagePushed("Set Github access token: "+token);
	}
	
	@Override
	public String getCommand() {
		return "token";
	}

}
