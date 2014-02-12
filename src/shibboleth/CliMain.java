package shibboleth;

import java.util.Scanner;

import shibboleth.actions.ActionExecutor;
import shibboleth.actions.BasicActionExecutor;
import shibboleth.gui.ActionListener;
import shibboleth.gui.CliActionListener;

/**
 * Runs the Shibboleth Application with a CLI interface.
 * 
 * @author Wilco Wisse
 *
 */
public class CliMain extends Main {

	public CliMain(){
		initApp();
		ActionListener cli = new CliActionListener();
		ActionExecutor executor = new BasicActionExecutor();
		
		initActions(cli, executor);
		
		Scanner scanner = new Scanner(System.in);
		cli.messagePushed("Enter commands. Provide an empty string to quit.");
		while(true){
			String command = scanner.nextLine();
			
			if(command.equals("")){
				close();
				scanner.close();
				System.exit(0);
			}
			else{
				executor.doAction(command);
			}
		}
		
	}
	
	public static void main(String[] args) {
		new CliMain();
	}

}
