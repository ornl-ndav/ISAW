/*
 * File:  FileAssociationGUI.java
 *
 * Copyright (C) 2004 Dominic Kramer
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 *
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Dominic Kramer <kramerd@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA and by
 * the National Science Foundation under grant number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.5  2004/05/26 19:31:43  kramer
 * Now the gui allows the user to select multiple files from the list.
 * Added a JTable to display information about which files have been
 * associated.
 *
 * Revision 1.4  2004/03/15 20:30:15  dennis
 * Changed to use RobustFileFilter from new package,
 * gov.anl.ipns.Util.File
 *
 * Revision 1.3  2004/03/12 19:46:14  bouzekc
 * Changes since 03/10.
 *
 * Revision 1.1  2004/02/07 05:08:50  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.frame;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import gov.anl.ipns.Util.File.RobustFileFilter;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.tools.FileAssociationManager;
import devTools.Hawk.classDescriptor.tools.FileAssociationManager.LoggedInterface;

/**
 * @author kramer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class FileAssociationGUI extends JFrame implements ActionListener, ListSelectionListener
{
	/**
	 * Manager used to associate the correct files.
	 */
	private FileAssociationManager manager;
	/**
	 * The list containing all of the files and directories to search through when looking for the 
	 * files to associate.
	 */
	private JList list;
	/**
	 * The model that handles modification of the Jlist list.
	 */
	private DefaultListModel model;
	/**
	 * The main panel on which everything on the window is placed.
	 */
	private JPanel mainPanel;
	/**
	 * Set to true to signify that this window has been opened to associate files to only one 
	 * interface.
	 */
	private boolean singleSelection;
	/**
	 * The JButton that the user selects to remove a filename from the list.
	 */
	private JButton removeButton;
//	/**
//	 * The tabbed pane onto which the files to user for associating and the results are displayed.
//	 */
//	private JTabbedPane tabbedPane;
	
	/**
	 * If this constructor is used, it is assumed you want to associate each Interface in the Vector 
	 * of Interface objects with a file.
	 * @param intfVec The Vector of Interface objects to have files attached to.
	 * @param filetype Either FileAssociationManager.JAVASOURCE or FileAssociationManager.JAVADOCS
	 */
	public FileAssociationGUI(Vector intfVec, int filetype, String title)
	{
		manager = new FileAssociationManager(intfVec, filetype);
		singleSelection = false;
		setTitle(title);
		
			StringBuffer string1 = new StringBuffer("Select the files and directories used to search for the correct ");
			if (filetype == FileAssociationManager.JAVADOCS)
				string1.append("javadocs files.");
			else if (filetype == FileAssociationManager.JAVASOURCE)
				string1.append("source files.");
			string1.append("  \nThe directories will be recursively scanned and the correct files will \n"+
								  "automatically assigned to the correct class.");
		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setLineWrap(false);
		textArea.setText(string1.toString());
		
		performDefaultActions();

		textArea.setBackground(mainPanel.getBackground());

		mainPanel.add(textArea, BorderLayout.NORTH);
		pack();
	}
	
	/**
	 * Create a new FileAssociationGUI.  This constructor is used if you want to associate files for only 
	 * one class or interface.
	 * @param intF The Interface object representing the class or interface.
	 * @param filetype Either FileAssociationManager.JAVASOURCE or FileAssociationManager.JAVADOCS
	 */
	public FileAssociationGUI(Interface intF, int filetype)
	{
		manager = new FileAssociationManager(intF, filetype);
		singleSelection = true;

		if (filetype == FileAssociationManager.JAVADOCS)
			setTitle("Assigning Javadocs Files for the "+intF.getPgmDefn().getInterface_type()+" "+intF.getPgmDefn().getInterface_name());
		else if (filetype == FileAssociationManager.JAVASOURCE)
			setTitle("Assigning Java Source Files for the "+intF.getPgmDefn().getInterface_type()+" "+intF.getPgmDefn().getInterface_name());
		
			StringBuffer string1 = new StringBuffer("Select the file corresponding to the correct ");
			if (filetype == FileAssociationManager.JAVADOCS)
				string1.append("javadocs file.");
			else if (filetype == FileAssociationManager.JAVASOURCE)
				string1.append("source file.");

		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setLineWrap(false);
		textArea.setText(string1.toString());
		
		performDefaultActions();

		textArea.setBackground(mainPanel.getBackground());

		mainPanel.add(textArea, BorderLayout.NORTH);
		pack();
	}
	
	/**
	 * Adds all of the default items to the window which would be there if the window was used 
	 * to associate java source code files or javadocs files.
	 *
	 */
	private void performDefaultActions()
	{
		Container pane = getContentPane();
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
				
		//now to make the list
			model = new DefaultListModel();
			list = new JList(model);
			list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			list.addListSelectionListener(this);
			JScrollPane scrollPane = new JScrollPane(list);
			mainPanel.add(scrollPane, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		//now to make the buttons
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(this);
		closeButton.setActionCommand("close");
		
		JButton browseButton = new JButton("Select File");
		browseButton.addActionListener(this);
		browseButton.setActionCommand("browse");
		
		removeButton = new JButton("Remove File");
		removeButton.addActionListener(this);
		removeButton.setActionCommand("remove");
		removeButton.setEnabled(false);
		
		JButton okButton = new JButton("Associate");
		okButton.addActionListener(this);
		okButton.setActionCommand("ok");
		
		buttonPanel.add(closeButton);
		buttonPanel.add(removeButton);
		buttonPanel.add(browseButton);
		buttonPanel.add(okButton);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		pane.add(mainPanel);
	}
	
	/**
	 * Get all of the filenames that are selected from the list.
	 * @return The selected filenames.
	 */
	public String[] getSelectedFiles()
	{
		int[] indexArr = list.getSelectedIndices();
		Vector vec = new Vector();
		for (int i=0; i<indexArr.length; i++)
		{
			if (indexArr[i] >= 0)
				vec.add(model.elementAt(indexArr[i]));
		}
		String[] fileArr = new String[vec.size()];
		for (int i=0; i<vec.size(); i++)
			fileArr[i] = (String)vec.elementAt(i);
			
		return fileArr;
	}
	
	/**
	 * Handles ActionEvents.
	 */
	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals("close"))
		{
			dispose();
		}
		else if (event.getActionCommand().equals("remove"))
		{
			int[] indexArr = list.getSelectedIndices();
			for (int i=indexArr.length-1; i>=0; i--)
			{
				if (indexArr[i]>=0)
					model.removeElementAt(indexArr[i]);
			}
			
			if (model.isEmpty())
				removeButton.setEnabled(false);
		}
		else if (event.getActionCommand().equals("browse"))
		{
			JFrame frame = new JFrame();
			frame.setSize(500,400);
			Container framePane = frame.getContentPane();
			
			JPanel mainChooserPanel = new JPanel();
			mainChooserPanel.setLayout(new BorderLayout());
			
			JFileChooser chooser = new JFileChooser();
				RobustFileFilter filter = new RobustFileFilter();
				if (manager.getFileType() == FileAssociationManager.JAVASOURCE)
					filter.addExtension("java");
				else if (manager.getFileType() == FileAssociationManager.JAVADOCS)
				{
					filter.addExtension("html");
					filter.addExtension("htm");
				}
				chooser.setFileFilter(filter);
			chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			chooser.setMultiSelectionEnabled(true);
						
			int returnVal = chooser.showDialog(frame, "Select");
			
			mainChooserPanel.add(chooser, BorderLayout.CENTER);
				
			framePane.add(mainChooserPanel);
			framePane.setVisible(true);
			
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				if (singleSelection == true)
					model.removeAllElements();
				File[] selectedFileArr = chooser.getSelectedFiles();
				for (int i=0; i<selectedFileArr.length; i++)
					model.addElement(selectedFileArr[i].getAbsoluteFile().toString());
				
				removeButton.setEnabled(true);
			}
		}
		else if (event.getActionCommand().equals("ok"))
		{
			JPanel secondTabPanel = new JPanel();
			JTabbedPane tabbedPane = new JTabbedPane();
				tabbedPane.addTab("File Selector",mainPanel);
				tabbedPane.addTab("Results",secondTabPanel);
				getContentPane().add(tabbedPane);

			tabbedPane.setSelectedIndex(1);
			
			Vector fileNameVec = new Vector();
			for (int i=0; i<model.size(); i++)
				fileNameVec.add((String)(model.elementAt(i)));
			
			manager.resetModificationFlags();
			manager.associateFiles(fileNameVec);
			
			JTable table = new JTable(new AssociationTableModel(manager));
			JScrollPane scrollPane = new JScrollPane(table);
			secondTabPanel.removeAll();
			secondTabPanel.add(scrollPane);
			table.doLayout();
//			table.setPreferredScrollableViewportSize(new Dimension(700,700));			
		}
	}
	
	/**
	 * Handles a list changing.
	 * @param e The event that is being handled.
	 */
	public void valueChanged(ListSelectionEvent e)
	{
	}
	
	private class AssociationTableModel extends AbstractTableModel
	{
		private String[] nameArr;
		private Object[][] valueArr;
		private FileAssociationManager fileManager;
		
		public AssociationTableModel(FileAssociationManager fManager)
		{
			fileManager = fManager;
			
			nameArr = new String[5];
			nameArr[0] = "Name";
			nameArr[1] = "Assigned";
			nameArr[2] = "Modified";
			nameArr[3] = "File";
			nameArr[4] = "Message";
			
			valueArr = new Object[manager.getLoggedInterfaceVector().size()][nameArr.length];
			
			LoggedInterface loggedIntf = manager.new LoggedInterface();
			for (int i=0; i<manager.getLoggedInterfaceVector().size(); i++)
			{
				loggedIntf = (LoggedInterface)(manager.getLoggedInterfaceVector().elementAt(i));
				
//				JTextArea intfArea = new JTextArea();
//					intfArea.setText(loggedIntf.getInterface().getPgmDefn().getInterface_name());
//					intfArea.setEditable(false);
//					JScrollPane intfScrollPane = new JScrollPane(intfArea);
				valueArr[i][0] = loggedIntf.getInterface().getPgmDefn().getInterface_name();
//					JCheckBox assignedBox = new JCheckBox();
//					assignedBox.setSelected();
				valueArr[i][1] = new Boolean(!manager.getAppropriateFilename(loggedIntf.getInterface()).trim().equals(""));
//					JCheckBox modifiedBox = new JCheckBox();
//					modifiedBox.setSelected(loggedIntf.isModified());
				valueArr[i][2] = new Boolean(loggedIntf.isModified());
				valueArr[i][3] = manager.getAppropriateFilename(loggedIntf.getInterface());
				valueArr[i][4] = loggedIntf.getLog();
			}
		}
		
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return false;
		}
		
		public int getColumnCount()
		{
			return nameArr.length;
		}
		
		public String getColumnName(int column)
		{
			return nameArr[column];
		}
		
		public Class getColumnClass(int index)
		{
			return getValueAt(0,index).getClass();
		}

		public int getRowCount()
		{
			return fileManager.getLoggedInterfaceVector().size();
		}

		public Object getValueAt(int rowIndex, int columnIndex)
		{
			return valueArr[rowIndex][columnIndex];
		}
		
	}
}
	
