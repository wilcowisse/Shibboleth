package shibboleth.actions;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import shibboleth.data.github.RateLimitValue;


/**
 * Execute all commands listed in a file.
 * @author Wilco Wisse
 *
 */
public class ExeAction extends ShibbolethAction implements ActionExecutor{
	
	private ActionExecutor executor;
	private RateLimitValue rate;
	
	public ExeAction(RateLimitValue rate){
		this.rate = rate;
		executor = new BasicActionExecutor();
	}
	
	@Override
	public void execute(String[] args) {
		assert rate != null;
		
		if(args.length > 0){
			
			BufferedReader br = null;
		    try {
		    	br = new BufferedReader(new FileReader("commands/"+args[0]));
		    	
		    	int rateThreshold = 1;
		    	if(args.length==2){
		    		try{
		    			rateThreshold = Integer.parseInt(args[1]);
		    		}
		    		catch(NumberFormatException e){
		    			rateThreshold = 1;
		    		}
		    	}
		    	
		    	int commandNo = 0;
		    	String line = null;
		        while ((line = br.readLine()) != null) {
		        	int currentRate = rate.getRemaining();
		        	if(currentRate>=0 && currentRate<=rateThreshold){
		        		suspend(commandNo, line);
		        		break;
		        	}
		        	
		        	commandNo++;
		        	System.out.println(commandNo + ". " + line);
		        	if(line.trim().equals(""))
		        		continue;
		        	
		        	
		        	boolean success = executor.doAction(line);
		        	
		        	if(!success){
		        		if(listener != null)
		        			listener.messagePushed("The command " + line + " failed");
		        		else
		        			System.out.println("The command " + line + " FAILED");
		        	}
		        	System.out.println();
		        }
		    } 
		    catch(IOException e){
		    	if(listener != null)
        			listener.errorOccurred(e, false);

		    }
		    finally{
		    	try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		    }
		    
		}
	}

	public void suspend(int lineNo, String line){
		String suspendMessage = String.format("Executing suspended at line %d: %s. \n" +
				"The number of remaining API requests is %d.", lineNo, line, rate.getRemaining());
		System.out.println(suspendMessage);
		
		if(listener != null)
			listener.messagePushed(suspendMessage);

	}
	@Override
	public String getCommand() {
		return "exe";
	}
	
	@Override
	public boolean doAction(String rawCommand) {
		return executor.doAction(rawCommand);
	}

	@Override
	public void addAction(ShibbolethAction action) {
		executor.addAction(action);
	}


}
