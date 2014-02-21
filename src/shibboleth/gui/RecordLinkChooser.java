package shibboleth.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import shibboleth.model.RecordLink;
import shibboleth.model.UnknownUser;
import shibboleth.model.User;

/**
 * GUI for human evaluation of Link records.
 * @see RecordLink
 * @author Wilco Wisse
 */
public class RecordLinkChooser extends JDialog implements ListSelectionListener, java.awt.event.ActionListener{

	private static final long serialVersionUID = 3417019149047029029L;
	
	public static final int CANCELED = 0;
	public static final int SAVED = 1;
	
	private JTable table;
	private LinkTableModel tableModel;
	private List<User> users;
	
	private JComboBox<User> combo;
	private JButton cancelButton, applyButton;
	
	private int userChoice = CANCELED;
	
	public static class LinkTableModel extends AbstractTableModel{
		private static final long serialVersionUID = -4460703108615189840L;
		List<RecordLink> list;
		public LinkTableModel(List<RecordLink> list){
			this.list=list;
		}
		
		@Override
		public Object getValueAt(int row, int col) {
			if(col==0)
				return list.get(row).committer;
			else if (col==1)
				return list.get(row).user;
			else if (col==2)
				return Math.floor(list.get(row).similarity * 100) / 100;
			else
				return null;
		}
		
		@Override
		public int getRowCount() {
			return list.size();
		}
		
		@Override
		public int getColumnCount() {
			return 3;
		}
		
		@Override
		public String getColumnName(int col){
			if(col==0)
				return "Committer";
			else if (col==1)
				return "User";
			else if (col==2)
				return "Proximity";
			else
				return null;
		}
		
		public void setUser(int index, User u){
			if(index >= 0 && index <list.size()){
				list.get(index).user=u;
				fireTableCellUpdated(index, 1);
			}
		}
		
		public User getUser(int index){
			return list.get(index).user;
		}
	}
	
	/**
	 * Construct a record link chooser.
	 * @param recordLinks All record links of one single repo.
	 * @param users All github users of that repo.
	 */
	public RecordLinkChooser(List<RecordLink> recordLinks, List<User> users){
		super(null, Dialog.ModalityType.TOOLKIT_MODAL);
		
		this.users = users;

		// Set System L&F
		try {
			UIManager.setLookAndFeel(
			UIManager.getSystemLookAndFeelClassName());
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
		tableModel = new LinkTableModel(recordLinks);
		table = new JTable(tableModel);
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(this);		
		table.getColumnModel().getColumn(2).setPreferredWidth(120);
		table.getColumnModel().getColumn(2).setMaxWidth(200);
		Container c = getContentPane();
		c.setLayout(new BorderLayout());
		c.add(new JScrollPane(table), BorderLayout.CENTER);
		c.add(getToolbar(), BorderLayout.NORTH);
		
		setSize(900, 600);
		setTitle("Evaluate record links");
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setIconImage(Toolkit.getDefaultToolkit().getImage("icon.png"));
		getRootPane().setDefaultButton(applyButton);
		setVisible(true);
	}
	
	/**
	 * @return The close option of the user: either {@link #CANCELED} or {@link #SAVED}.
	 */
	public int getUserChoice(){
		return userChoice;
	}
	
	private JPanel getToolbar() {
		JPanel toolbar = new JPanel();
		toolbar.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		combo = new JComboBox<User>();
		for(User u : users){
			combo.addItem(u);
			if(u==null){
				System.out.println("null user");
			}
		}
		combo.addItem(UnknownUser.getInstance());
		combo.addActionListener(this);
		toolbar.add(combo);
		
		toolbar.add(new JLabel("         "));
		
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		toolbar.add(cancelButton);
		
		applyButton = new JButton("Save");
		applyButton.addActionListener(this);
		toolbar.add(applyButton);
		
		return toolbar;
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() || table.getSelectedRow() == -1) 
            return;
        
		int selectedRow = table.getSelectedRow();
		User u = tableModel.getUser(selectedRow);
		combo.setSelectedItem(u);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == combo){
			int selectedIndex = combo.getSelectedIndex();
			User selectedUser = combo.getItemAt(selectedIndex);
			
			int selectedRow = table.getSelectedRow();
			if(selectedRow != -1)
				tableModel.setUser(selectedRow, selectedUser);
		}
		else if(e.getSource()==applyButton){
			userChoice = SAVED;
			dispose();
		}
		else if(e.getSource()==cancelButton){
			userChoice = CANCELED;
			dispose();
		}
		
	}
	
	/**
	 * Evaluate the given record links.
	 * @param recordLinks All record links of one single repo.
	 * @param users All github users of that repo.
	 * @return The close option of the user: either {@link #CANCELED} or {@link #SAVED}.
	 */
	public static int evaluateLinks(List<RecordLink> recordLinks, List<User> users) {
		RecordLinkChooser chooser = new RecordLinkChooser(recordLinks, users);
		return chooser.getUserChoice();
	}

	
	
}
