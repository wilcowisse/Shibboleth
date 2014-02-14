package shibboleth.gui;

public class CliActionListener implements ActionListener {
	
	@Override
	public void errorOccurred(Exception e, boolean severe) {
		System.err.println(e);
	}

	@Override
	public void graphChanged(String what, boolean resetNeeded) {
		System.out.println(what);
	}

	@Override
	public void busyStateChanged(boolean busy) {
	}

	@Override
	public void messagePushed(String message, Object[] objects) {
		System.out.println();
		System.out.println(" | MESSAGE");
		printBar(message.length());
		System.out.println(" | " + message);
		printBar(message.length());
		
		for(Object obj : objects){
			System.out.println(" | " + obj);
		}
		printBar(message.length());
		System.out.println();
	}

	@Override
	public void messagePushed(String message) {
		System.out.println();
		System.out.println(" | MESSAGE");
		printBar(message.length());
		System.out.println(" | " + message);
		printBar(message.length());
		System.out.println();
	}
	
	private void printBar(int messageLength){
		System.out.print(" | ");
		for(int i=0; i<messageLength; i++)
			System.out.print('-');
		System.out.println();
	}

}
