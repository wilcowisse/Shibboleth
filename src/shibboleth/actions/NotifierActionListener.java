package shibboleth.actions;

import java.util.ArrayList;
import java.util.List;

import shibboleth.gui.ActionListener;

/**
 * An action listener which notifies all added action listeners
 * 
 * @author Wilco Wisse
 *
 */
public class NotifierActionListener implements ActionListener {
	
	private List<ActionListener> listeners;
	
	public NotifierActionListener(){
		listeners = new ArrayList<ActionListener>();
	}
	
	@Override
	public void errorOccurred(Exception e, boolean severe) {
		for(ActionListener l: listeners){
			l.errorOccurred(e, severe);
		}

	}

	@Override
	public void graphChanged(String what, boolean resetNeeded) {
		for(ActionListener l: listeners){
			l.graphChanged(what, resetNeeded);
		}

	}

	@Override
	public void busyStateChanged(boolean busy) {
		for(ActionListener l: listeners){
			l.busyStateChanged(busy);
		}

	}

	@Override
	public void messagePushed(String message, Object[] objects) {
		for(ActionListener l: listeners){
			l.messagePushed(message, objects);
		}

	}

	@Override
	public void messagePushed(String message) {
		for(ActionListener l: listeners){
			l.messagePushed(message);
		}

	}
	
	public void addActionListener(ActionListener l){
		listeners.add(l);
	}

}
