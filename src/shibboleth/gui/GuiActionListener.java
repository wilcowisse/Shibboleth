package shibboleth.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import shibboleth.actions.ActionExecutor;

public class GuiActionListener implements ActionListener{
	
	private JFrame f;
	private CommandField field;
	private GraphPanel graphPanel;
	
	public void init(){
		try {
			// Set System L&F
			UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName());
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
			
			f = new JFrame();
			
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.setSize(700, 500);
			f.setLocationRelativeTo(null);
			f.setExtendedState(JFrame.MAXIMIZED_BOTH);
			f.setTitle("Shibboleth Explorer");
			f.setIconImage(Toolkit.getDefaultToolkit().getImage("assets/icon.png"));

			Container contentPane = f.getContentPane();
			contentPane.setLayout(new BorderLayout());

			graphPanel = new GraphPanel();
			contentPane.add(getActionPanel(), BorderLayout.SOUTH);
			contentPane.add(graphPanel, BorderLayout.CENTER);
			
			f.setVisible(true);
			
			// hack to resolve refresh issue of graph panel.
			try {
				Thread.sleep(200);
				reset();
				Thread.sleep(200);
				refresh();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}
	
	private JPanel getActionPanel(){
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(500,30));
		panel.setLayout(new BorderLayout());
		panel.setBackground(Color.BLACK);
		field = new CommandField(30);

		field.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
		panel.add(field,BorderLayout.CENTER);
		return panel;
	}

	public void reset() {
		graphPanel.reset();
	}
	
	public void refresh() {
		graphPanel.refresh();
	}
	
	public ActionExecutor getActionExecutor(){
		return field;
	}
	
	public ActionExecutor getExecutor(){
		return field;
	}
	
	@Override
	public void errorOccurred(Exception e, boolean severe) {
		if(severe){
			e.printStackTrace();
			System.exit(1);
		}
		else{
			JOptionPane.showMessageDialog(f,e.getMessage(),"Shibboleth", JOptionPane.WARNING_MESSAGE);
		}
	}
	
	@Override
	public void graphChanged(String what, boolean resetNeeded) {
		System.out.println(what);
		if(resetNeeded)
			reset();
		else
			refresh();	
	}
	
	@Override
	public void messagePushed(String message, Object[] objects) {
		JLabel label = new JLabel(message);
		JList<Object> list = new JList<Object>(objects);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(-1);
		JScrollPane listScroller = new JScrollPane(list);
		listScroller.setPreferredSize(new Dimension(900, 600));
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(label, BorderLayout.NORTH);
		panel.add(listScroller, BorderLayout.CENTER);
		
		JDialog dialog = new JDialog(f,"Shibboleth", true);
		dialog.setContentPane(panel);
		dialog.setSize(900, 600);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}

	@Override
	public void messagePushed(String message) {
		JOptionPane.showMessageDialog(f,message,"Shibboleth", JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public void busyStateChanged(boolean busy) {
		field.setEnabled(!busy);
	}

}
