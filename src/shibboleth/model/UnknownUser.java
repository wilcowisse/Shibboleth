package shibboleth.model;

/**
 * A github user, but we do not know who it is...
 * 
 * @author Wilco Wisse
 *
 */
public class UnknownUser extends User {
	public final String login = "-unknown-";
    public final int id = -1;
    public final String name = null;
	public final String email = null;
    public final String url = "https://github.com/404";
	public final String type = null;
	public final String company = null;
	public final int repos = -1;
	public final int followers = -1;
	public final int following = -1;
}
