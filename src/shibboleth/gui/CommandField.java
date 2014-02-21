package shibboleth.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import shibboleth.actions.ActionExecutor;
import shibboleth.actions.BasicActionExecutor;
import shibboleth.actions.ShibbolethAction;

/**
 * A field which is capable of initiating {@link ShibbolethAction actions}.
 * 
 * @author Wilco Wisse
 *
 */
public class CommandField extends JTextField implements ActionExecutor, KeyListener{

	private static final long serialVersionUID = 6414983614489863418L;
	
	private BasicActionExecutor executor;
	private CircularFifoQueue<String> commandHistory;
	private List<String> actionTypeList;
	private int historyPointer;
	private int actionTypePointer;
	private String currentAction;
	
	/**
	 * Construct a command field with history size <tt>bufferSize</tt>.
	 * @param bufferSize The number of entries in the history.
	 */
	public CommandField(int bufferSize){
		executor = new BasicActionExecutor();
		commandHistory = new CircularFifoQueue<String>(bufferSize);
		actionTypeList = new ArrayList<String>();
		historyPointer=1;
		actionTypePointer = actionTypeList.size();
		currentAction="";
		
		this.addKeyListener(this);
		setDisabledTextColor(Color.GRAY);
		
		setMargin(new Insets(2, 20, 2, 2));
	}
	
	
	@Override
	public void paint(Graphics g){
		super.paint(g);
		
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		Font font = new Font("Monospaced", Font.PLAIN, 12);
		g2.setFont(font);
		int fontHeight = g2.getFontMetrics().getHeight();
		int y = getHeight()/2 +fontHeight/2-3;
		
		g2.drawString("$", 10, y);
		
	}
	
	private void previousAction(){
		if(historyPointer == commandHistory.size()){
			currentAction = getText();
		}
		if(historyPointer>0 && historyPointer<=commandHistory.size()){
			historyPointer--;
			setText(commandHistory.get(historyPointer));
		}
	}
	
	private void nextAction(){
		if(historyPointer == commandHistory.size()-1){
			historyPointer++;
			setText(currentAction);
		}
		else if(historyPointer<commandHistory.size()-1){
			historyPointer++;
			setText(commandHistory.get(historyPointer));
		}

	}
	
	private void previousActionType(){
		actionTypePointer = (actionTypePointer+actionTypeList.size()) % (actionTypeList.size()+1);
		if(actionTypePointer == actionTypeList.size())
			setText("");
		else{
			setText(actionTypeList.get(actionTypePointer)+"  ");
			setCaretPosition(getText().length());
		}
		
	}
	
	private void nextActionType(){
		actionTypePointer = (actionTypePointer+1) % (actionTypeList.size()+1);
		//System.out.println(actionTypePointer);
		if(actionTypePointer == actionTypeList.size())
			setText("");
		else{
			setText(actionTypeList.get(actionTypePointer)+" ");
			setCaretPosition(getText().length());
		}
	}
	
	@Override
	public void addAction(ShibbolethAction action){
		executor.addAction(action);
		actionTypeList.add(action.getCommand());
		actionTypePointer = actionTypeList.size();
	}
	
	
	@Override
	public boolean doAction(String rawCommand){
		boolean success = executor.doAction(rawCommand);
		
		if(success){
			commandHistory.add(rawCommand);
			setText("");
			currentAction="";
			historyPointer = commandHistory.size();
		}
		
		return success;
			
	}
	
	@Override
	public void keyPressed(KeyEvent k) {
		
		if(k.getKeyCode()==KeyEvent.VK_LEFT){
			if(getText().equals("") || actionTypePointer!=actionTypeList.size())
				previousActionType();
		}
		else if(k.getKeyCode()==KeyEvent.VK_RIGHT){
			if(getText().equals("") || actionTypePointer!=actionTypeList.size())
				nextActionType();
		}
		else{
			actionTypePointer=actionTypeList.size();
			if(k.getKeyCode()==KeyEvent.VK_UP){
				previousAction();
			}
			else if(k.getKeyCode()==KeyEvent.VK_DOWN){
				nextAction();
			}
			else if(k.getKeyCode()==KeyEvent.VK_ENTER){
				doAction(getText());
			}
		}
						
	}
	@Override
	public void keyReleased(KeyEvent k) {
	}
	@Override
	public void keyTyped(KeyEvent k) {
		
	}
	
}