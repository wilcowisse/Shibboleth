package shibboleth;

import shibboleth.actions.GraphLayoutAction;
import shibboleth.gui.GuiActionListener;
import shibboleth.model.GephiGraph;

/**
 * Runs the Shibboleth Application with a GUI interface.
 * 
 * @author Wilco Wisse
 *
 */
public class GuiMain extends Main {
	
	public GuiMain(){
		GephiGraph graph = new GephiGraph();
		initApp(createSqliteConnection("db/db.sqlite"), graph);
		
		GuiActionListener gui = new GuiActionListener();
		gui.init();
		initActions(gui, gui.getActionExecutor());
		
		new GraphLayoutAction(graph)
		.addActionListener(gui)
		.addExecutor(gui.getActionExecutor());
		
	}
		
	public static void main(String[] args) {
		new GuiMain();
	}

}
