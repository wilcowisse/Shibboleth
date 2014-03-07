package shibboleth.gui;

public class LogActionListener implements ActionListener {
	
	@Override
	public void errorOccurred(Exception e, boolean severe) {
		System.out.println(e);
	}

	@Override
	public void graphChanged(String what, boolean resetNeeded) {
		System.out.println("Graph: "+what);
	}

	@Override
	public void busyStateChanged(boolean busy) {
	}

	@Override
	public void messagePushed(String message, Object[] objects) {
		System.out.println(message);
		for(Object obj : objects){
			System.out.println("   " + obj);
		}

	}

	@Override
	public void messagePushed(String message) {
		System.out.println(message);
	}
	

}
