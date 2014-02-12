package shibboleth;

import shibboleth.gui.GuiActionListener;

/**
 * Runs the Shibboleth Application with a GUI interface.
 * 
 * @author Wilco Wisse
 *
 */
public class GuiMain extends Main {

	private GuiActionListener gui;
	
	public GuiMain(){
		initApp();
		gui = new GuiActionListener();
		gui.init();
		initActions(gui, gui.getActionExecutor());
	}
		
	public static void main(String[] args) {
		new GuiMain();
	}

}
