package shibboleth.actions;


/**
 * Notifies listener to show help info.
 * 
 * Syntax: <tt></tt>.
 * 
 * @author Wilco Wisse
 *
 */
public class HelpAction extends ShibbolethAction{

	@Override
	public void execute(String[] args) {
		String message = "Available commands:";
		String[] commands = new String[]{
	    "analyze jaro|commits|saved repo accuracy (-s), -s = silent.",
		"clone repo -- Clone repository to the clones/ dir.",
		"delete repo|user -- Remove given node from db.",
		"exe file rateThreshold (-s), -s = sleep if rate limit < threshold.",
		"explode repo|user depth (-a)-- BFS of given depth on node, -a = ensureAll.",
		"export (-f) -- Export the files in this db, -f = export only files written by one user.",
		"gephi filename -- Export gexf file.",
		"get repo|user -- Retrieve and add to graph.",
		"get -c repo|user|-all (-l) -- Retrieve contributions available locally, -l = ignore lang=js filter.",
		"get -ca repo|user (-l) -- Retrieve all contributions.",
		"get -cu repo -- Retrieve all full user descriptions (Github API intensive).",
		"hide repo|user|-all -- Hide given node in graph.",
		"help -- show this message.",
		"info repo|user -- Display info.",
		"info -c repo|user (-l) -- Display contribution info available locally, -l = ignore lang=js filter.",
		"info -ca repo|user (-l) -- Display all contribution info",
		"labels -- Toggle labels.",
		"layout yifanhu|forceatlas duration -- Apply layout.",
		"layout random size -- Apply random layout of size.",
		"refresh -- Repaint graph.",
		"rate -- Show Github api account limits.",
		"token token -- Provide Github access token.", 
		"whereis name -- Highlight the given node.",
		"whereis c name -- Highlight neighbors of the given node."
		};
		
		listener.messagePushed(message, commands);

		
	}

	@Override
	public String getCommand() {
		return "help";
	}

}
