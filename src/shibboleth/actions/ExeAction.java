package shibboleth.actions;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

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
		    	boolean sleep = false;
		    	if(args.length>1){
		    		try{
		    			rateThreshold = Integer.parseInt(args[1]);
		    		}
		    		catch(NumberFormatException e){
		    			rateThreshold = 1;
		    		}
		    		
		    		if(args.length>2 && args[2].equals("-s"))
		    			sleep = true;
		    	}
		    	
		    	int commandNo = 0;
		    	String line = null;
		        while ((line = br.readLine()) != null) {
		        	commandNo++;
		        	System.out.println(commandNo + ". " + line);
		        	if(line.trim().equals(""))
		        		continue;
		        	int currentRate = rate.getRemaining();
		        	if(currentRate >= 0 && currentRate <= rateThreshold){
		        		if(sleep){
		        			suspend(commandNo, line);
		        		}
		        		else{
		        			terminate(commandNo, line);
		        			break;
		        		}
		        	}

		        	
		        	boolean success = executor.doAction(line);
		        	
		        	if(!success){
		        		listener.messagePushed("The command " + line + " failed");
		        	}
		        	System.out.println();
		        }
		    } 
		    catch(IOException e){
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
		listener.messagePushed(suspendMessage);
		System.out.println("Bye... Gonna sleep. Zzzzzzz...");
		
		long expireDate = rate.getReset().getTime();
		long wakeupInterval = TimeUnit.MINUTES.toMillis(5);
		long lateNess = TimeUnit.SECONDS.toMillis(30);
		long now = System.currentTimeMillis();
		long wait = expireDate+lateNess-now;
		
		while(wait > 0) {
			System.out.println("Remaining: " 
					+ TimeUnit.MILLISECONDS.toMinutes(wait) + " minutes");
			long sleepTime = Math.min(wait, wakeupInterval);
			try {
				TimeUnit.MILLISECONDS.sleep(sleepTime);
			} catch (InterruptedException e) {
				System.err.println("Sleep error");
				e.printStackTrace();
				System.exit(1);
			}
			
			now = System.currentTimeMillis();
			wait = expireDate+lateNess-now;
		}
		

	}
	
	public void terminate(int lineNo, String line){
		String suspendMessage = String.format("Executing terminated at line %d: %s. \n" +
				"The number of remaining API requests is %d.", lineNo, line, rate.getRemaining());
		System.out.println(suspendMessage);
		
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
