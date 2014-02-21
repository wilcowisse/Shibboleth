package shibboleth;

import java.util.Scanner;

import shibboleth.actions.ActionExecutor;
import shibboleth.actions.BasicActionExecutor;
import shibboleth.gui.ActionListener;
import shibboleth.gui.CliActionListener;
import shibboleth.model.GephiGraph;

/**
 * Runs the Shibboleth Application with a CLI interface.
 * 
 * @author Wilco Wisse
 *
 */
public class CliMain extends Main {
	
	public CliMain(){
		//initApp(createMySqlConnection("root", "pass"), new GephiGraph());
		initApp(createSqliteConnection("db/db.sqlite"), new GephiGraph());
		
		ActionListener cli = new CliActionListener();
		ActionExecutor executor = new BasicActionExecutor();
		initActions(cli, executor);
		
		cli.messagePushed("Enter commands. Provide 'q' to quit.");
		Scanner scanner = new Scanner(System.in);
		while(true){
			String command = scanner.nextLine().trim();
			
			if(command.equals("")){
				// do nothing
			}
			else if (command.equals("q")){
				close();
				scanner.close();
				System.exit(0);
			}
			else{
				if(executor.doAction(command) == false){
					System.out.println("Wrong syntax.");
				}
				System.out.println();
			}
		}
	}
	
	public static void main(String[] args) {
		new CliMain();
	}

}
