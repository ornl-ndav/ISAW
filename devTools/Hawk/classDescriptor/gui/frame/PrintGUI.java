/*
 * File:  PrintGUI.java
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
 * Revision 1.2  2004/03/11 18:55:21  bouzekc
 * Documented file using javadoc statements.
 *
 * Revision 1.1  2004/02/07 05:08:51  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.frame;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.modeledObjects.Project;
import devTools.Hawk.classDescriptor.threads.ASCIIPrintThread;

/**
 * This class creates a window allowing the user to print information about Interface objects to a file.  The window contains 
 * a section to change the options for printing.  It also contains a JTree to allow the user to choose which Interface objects to print.
 * @author Dominic Kramer
 */
public class PrintGUI extends JFrame implements ActionListener, ItemListener, TreeSelectionListener
{
	/**
	 * A Vector of Project objects.  Each project in this Vector is placed into the JTree in the window displayed.  Below each project, the 
	 * project's Interfaces are entered into the tree.
	 */
	private Vector projectVec;  // a vector of projects
	/**
	 * This holds the name of the file to print the information to.
	 */
	private JTextField fileField;
	/**
	 * This is the tree which holds all of the project and interfaces can can be selected to print.
	 */
	private JTree tree;
	/**
	 * This is the list which contains all of the currently selected interfaces that are to be printed.
	 */
	private JList list;
	/**
	 * This is the model that is used to modify the JList list.
	 */
	private DefaultListModel model;
	/**
	 * This is a Vector of Interface objects containing all of the interfaces selected to be printed.
	 */
	private Vector selectedIntFVec;
	
	//these are the components in introPanel
		/**
		 *  This checkbox that is placed on the window to allow the user to choose if he/she wants to print
		 * the introduction.
		 */
		private JCheckBox useIntroCheckBox;
		/**
		 * This holds the title of the document that is printed.
		 */
		private JTextField titleField;
		/**
		 * This holds the author's name who printed the document which will appear on the document.
		 */
		private JTextField authorField;
		/**
		 * This holds the date that will appear on the document when the document was printed.
		 */
		private JTextField dateField;
		/**
		 * This checkbox allows the user to select whether or not to use the current date.
		 */
		private JCheckBox useCurrentDateBox;
		/**
		 * This text area is used to allow the user to type a description about the document.  This 
		 * description is printed in the introduction section of the document.
		 */
		private JTextArea descTextArea;
		
	//these are the other checkboxes
		/**
		 * This allows the user to decide if he/she wants to add a table of contents to the printout.
		 */
		private JCheckBox useContentsCheckBox;
		/**
		 * This allows the user to decide if he/she wants to add the package list to the printout.
		 */
		private JCheckBox usePackageListCheckBox;
		/**
		 * This allows the user to decide if he/she wants to add the UML diagrams for each Interface 
		 * object to the printout.  The UML diagrams are printed alphabetically.
		 */
		private JCheckBox useAlphaUMLCheckBox;
		/**
		 * This allows the user to decide if he/she wants to add the shortened source code for each interface object 
		 * to the printout.  The shortened source code diagrams are printed alphabetically.
		 */
		private JCheckBox useShortenedSourceCheckBox;
		/**
		 * This allows the user to decide if he/she wants to shorten java names in the single UML printouts.
		 */
		private JCheckBox singleUMLShortenJava;
		/**
		 * This allows the user to decide if he/she wants to shorten non-java names in the single UML printouts.
		 */
		private JCheckBox singleUMLShortenNonJava;
		/**
		 * This allows the user to decide if he/she wants to shorten java names in the shortened source 
		 * code printouts.
		 */
		private JCheckBox shortenedSourceShortenJava;
		/**
		 * This allows the user to decide if he/she wants to shorten non-java names in the shorten source code 
		 * printouts.
		 */
		private JCheckBox shortenedSourceShortenNonJava;
	
	/**
	 * This constructor cretes the printout window.
	 * @param vec This is the Vector of Project objects displayed in the window's tree.
	 * @param rootNodeTitle This is the title of the root node in the window's tree.
	 */
	public PrintGUI(Vector vec, String rootNodeTitle)
	{
		projectVec = vec;
		selectedIntFVec = new Vector();
		
		setTitle("Print To File");
		setSize(200,200);
		addWindowListener(new WindowDestroyer());
		
		Container pane = getContentPane();

		//the main panel which everything is added on
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			
			JPanel mainPanel1 = new JPanel();
			mainPanel1.setLayout(new BorderLayout());
		
			JPanel mainPanel2 = new JPanel();
			mainPanel2.setLayout(new BorderLayout());
			
		//this is the panel that contians the buttons at the bottom of the gui
			JPanel mainButtonPanel = new JPanel();
			mainButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			//now to add the components to the panel
				mainButtonPanel.add(new JLabel("Print to file:  "));
				fileField = new JTextField(25);
				mainButtonPanel.add(fileField);
				
				JButton browseButton = new JButton("Browse");
				browseButton.addActionListener(this);
				mainButtonPanel.add(browseButton);
				
				JButton closeButton = new JButton("Close");
				closeButton.addActionListener(this);
				mainButtonPanel.add(closeButton);
				
				JButton printButton = new JButton("Print");
				printButton.addActionListener(this);
				mainButtonPanel.add(printButton);
			mainPanel.add(mainButtonPanel, BorderLayout.SOUTH);
				
		//now to work on the left tab		
			JPanel treePanel1 = new JPanel();
			treePanel1.setLayout(new BorderLayout());
		
			JPanel treePanel1ButtonPanel = new JPanel();
			treePanel1ButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
			//now to make the buttons
				JButton addButton = new JButton("Add");
				addButton.addActionListener(this);
				treePanel1ButtonPanel.add(addButton);
				
				JButton addAllButton = new JButton("Add All");
				addAllButton.addActionListener(this);
				treePanel1ButtonPanel.add(addAllButton);
				
				JButton removeButton = new JButton("Remove");
				removeButton.addActionListener(this);
				treePanel1ButtonPanel.add(removeButton);
				
		//so now all of the buttons are added to the panel
		//now to add the panel to the tree panel
			treePanel1.add(treePanel1ButtonPanel, BorderLayout.SOUTH);
	
		//now to make the tree
			DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootNodeTitle);
			tree = new JTree(root);
			tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
			tree.addTreeSelectionListener(this);
			JScrollPane treeScrollPane = new JScrollPane(tree);
			//now to add the tree to its panel
				treePanel1.add(treeScrollPane, BorderLayout.CENTER);
		
		//now to make the JList which holds all of the selected projects and interfaces to be printed
			model = new DefaultListModel();
			list = new JList(model);
			JScrollPane listScrollPane = new JScrollPane(list);
			
		//now to make the JSplitPane that these are on
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treePanel1, listScrollPane);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(400);
		
		mainPanel1.add(splitPane, BorderLayout.CENTER);
		
		//now to work on the right tab
			//now to make the first panel											
			JPanel otherCheckBoxPanel = new JPanel();
			otherCheckBoxPanel.setLayout(new GridLayout(5,0));
			
			JPanel introPanel = new JPanel();
				
				//the following are used in the introduction options gui
					titleField = new JTextField(20);
					authorField = new JTextField(20);
					dateField = new JTextField(20);
						//now to get the date
							String date = DateFormat.getDateInstance(DateFormat.LONG).format(new Date(System.currentTimeMillis()));
							dateField.setText(date);
					descTextArea = new JTextArea(10,45);
						descTextArea.setEditable(true);
					useCurrentDateBox = new JCheckBox("Use current date");
						useCurrentDateBox.addActionListener(this);
						useCurrentDateBox.setSelected(true);
						useCurrentDateBox.addItemListener(this);

				introPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
					useIntroCheckBox = new JCheckBox("Print Introduction");
							useIntroCheckBox.addActionListener(this);
							useIntroCheckBox.setSelected(true);
							introPanel.add(useIntroCheckBox);				
					JButton introOptionsButton = new JButton("Options");
							introOptionsButton.addActionListener(this);
							introOptionsButton.setActionCommand("intro.options");
							introPanel.add(introOptionsButton);

				JPanel contentsPanel = new JPanel();
					contentsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
						useContentsCheckBox = new JCheckBox("Print the Table of Contents");
							useContentsCheckBox.setSelected(true);
							useContentsCheckBox.addItemListener(this);
							contentsPanel.add(useContentsCheckBox);
						contentsPanel.add(new JLabel());
				
				JPanel packageListPanel = new JPanel();
				packageListPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
					usePackageListCheckBox = new JCheckBox("Print the Package List");
						usePackageListCheckBox.setSelected(true);
						usePackageListCheckBox.addItemListener(this);
						packageListPanel.add(usePackageListCheckBox);
						packageListPanel.add(new JLabel());				


				JPanel umlPanel = new JPanel();
				umlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

				//these JCheckBoxes are used in the SingleUMLOptionsGUI
					singleUMLShortenJava = new JCheckBox("Shorten Java Names");
						singleUMLShortenJava.setSelected(true);
					singleUMLShortenNonJava = new JCheckBox("Shorten Non-Java Names");
						singleUMLShortenNonJava.setSelected(false);

					useAlphaUMLCheckBox = new JCheckBox("Print UML Diagrams");
						useAlphaUMLCheckBox.setSelected(true);
						useAlphaUMLCheckBox.addItemListener(this);
						umlPanel.add(useAlphaUMLCheckBox);				
					JButton singleUMLOptionsButton = new JButton("Options");
						singleUMLOptionsButton.addActionListener(this);
						singleUMLOptionsButton.setActionCommand("singleUML.options");
						umlPanel.add(singleUMLOptionsButton);

				//these JCheckBoxes are used in the ShortenedSourceOptionsGUI	
					shortenedSourceShortenJava = new JCheckBox("Shorten Java Names");
						shortenedSourceShortenJava.setSelected(true);
					shortenedSourceShortenNonJava = new JCheckBox("Shorten Non-Java Names");
						shortenedSourceShortenNonJava.setSelected(false);
				
				JPanel shortenedSourcePanel = new JPanel();
				shortenedSourcePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
					useShortenedSourceCheckBox = new JCheckBox("Print Shortened Source Code");
						useShortenedSourceCheckBox.setSelected(true);
						useShortenedSourceCheckBox.addItemListener(this);
						shortenedSourcePanel.add(useShortenedSourceCheckBox);
					JButton shortenedSourceOptionsButton = new JButton("Options");
						shortenedSourceOptionsButton.addActionListener(this);
						shortenedSourceOptionsButton.setActionCommand("shortenedSource.options");
						shortenedSourcePanel.add(shortenedSourceOptionsButton);
				
				otherCheckBoxPanel.add(introPanel);
				otherCheckBoxPanel.add(contentsPanel);
				otherCheckBoxPanel.add(packageListPanel);
				otherCheckBoxPanel.add(umlPanel);
				otherCheckBoxPanel.add(shortenedSourcePanel);
				
				mainPanel2.add(otherCheckBoxPanel, BorderLayout.CENTER);
				
		JTabbedPane tabbedPane = new JTabbedPane();
			tabbedPane.addTab("Choose Interfaces", mainPanel1);
			tabbedPane.addTab("Properties", mainPanel2);
			
		mainPanel.add(tabbedPane, BorderLayout.CENTER);
		pane.add(mainPanel);
		
		//now to add the information to the JTree
		Vector projectNodeVec = new Vector();
			//now to add the projects to the JTree
			for (int i = 0; i < vec.size(); i++)
			{
				projectNodeVec.add(new DefaultMutableTreeNode( ((Project)vec.elementAt(i))) );
			}
			
			for (int i = 0; i < projectNodeVec.size(); i++)
			{
				root.add( (DefaultMutableTreeNode)projectNodeVec.elementAt(i) );
			}
			//Now to add the interfaces to the JTree
				for (int i = 0; i < vec.size(); i++)
				{
						Vector intFVec = ((Project)vec.elementAt(i)).getInterfaceVec();
						
						for (int j = 0; j < intFVec.size(); j++)
						{
							((DefaultMutableTreeNode)projectNodeVec.elementAt(i)).add( new DefaultMutableTreeNode( ((Interface)intFVec.elementAt(j)) ));
						}
				}
			//now to expand the root node
			tree.expandPath(new TreePath(root));
			pack();
	}
	
	/**
	 * This returns the model associated with the list which displays the currently selected 
	 * interfaces.
	 * @return A DefaultListModel
	 */
	public DefaultListModel getModel()
	{
		return model;
	}
	
	/**
	 * This returns a Vector of Interface objects.  Each Interface object in this Vector is one of the 
	 * interfaces that the user chose to print to the document.
	 * @return A Vector of Interface objects.
	 */
	public Vector getVectorOfSelectedInterfaces()
	{
		return selectedIntFVec;
	}
	
	/**
	 * This returns the print window.
	 * @return The print window.
	 */
	public JFrame getFrame()
	{
		return this;
	}
	
	/**
	 * This returns the JTextField which holds the file to print the document to.
	 * @return A JTextField.
	 */	
	public JTextField getFileField()
	{
		return fileField;
	}
	
	/**
	 * This returns the JCheckBox that holds the user's choice of whether or not to 
	 * print the introduction.
	 * @return A JCheckBox.
	 */
	public JCheckBox getUseIntroCheckBox()
	{
		return useIntroCheckBox;
	}
	
	/**
	 * This returns the JTextField that holds the title of the document.
	 * @return A JTextField.
	 */
	public JTextField getTitleField()
	{
		return titleField;
	}
	
	/**
	 * This returns the JTextField that holds the author of the document.
	 * @return A JTextField.
	 */
	public JTextField getAuthorField()
	{
		return authorField;
	}
	
	/**
	 * This returns the JTextField that contains the date that document was printed.
	 * @return A JTextField.
	 */
	public JTextField getDateField()
	{
		return dateField;
	}
	
	/**
	 * This returns the JCheckBox that describes whether or not to use the current date.  If 
	 * this checkbox is selected, an ActionEvent is thrown and the current date is printed in the 
	 * JTextField dateField.  If the checkbox is de-selected, the JTextField, dateField is cleared.
	 * @return A JCheckBox.
	 */
	public JCheckBox getUseCurrentDateBox()
	{
		return useCurrentDateBox;
	}
	
	/**
	 * This returns the JTextArea which holds the user's description about the document that is 
	 * going to be printed.
	 * @return A JTextArea.
	 */
	public JTextArea getDescTextArea()
	{
		return descTextArea;
		
	}
	
	/**
	 * Thisreturns the JCheckBox which describes if the user wants to print the table of contents.
	 * @return A JCheckBox.
	 */
	public JCheckBox getUseContentsCheckBox()
	{
		return useContentsCheckBox;
	}
	
	/**
	 * This returns the JCheckBox which descirbes if the user wants to print the package list.
	 * @return A JCheckBox.
	 */
	public JCheckBox getUsePackageListCheckBox()
	{
		return usePackageListCheckBox;
	}
	
	/**
	 * This returns the JCheckBox which describes if the user wants to print the UML documentation 
	 * alphabetically for all of the interfaces he/she selected.
	 * @return A JCheckBox.
	 */
	public JCheckBox getUseAlphaUMLCheckBox()
	{
		return useAlphaUMLCheckBox;
	}
	
	/**
	 * This returns the JCheckBox which describes if the user wants to print the shortened source 
	 * code alphabetically for all of the interfaces he/she selected.
	 * @return A JCheckBox.
	 */
	public JCheckBox getUseShortenedSourceCheckBox()
	{
		return useShortenedSourceCheckBox;
	}
	
	/***
	 * This returns the JCheckBox which describes if the user wants to shorten java names for single UML 
	 * diagrams.
	 * @return A JCheckBox
	 */
	public JCheckBox getSingleUMLShortenJavaCheckBox()
	{
		return singleUMLShortenJava;
	}
	
	/**
	 * This returns the JCheckBox which describes if the user wants to shorten non-java names for single UML 
	 * diagrams.
	 * @return A JCheckBox
	 */
	public JCheckBox getSingleUMLShortenNonJavaCheckBox()
	{
		return singleUMLShortenNonJava;
	}
	
	/**
	 * This returns the JCheckBox which describes if the user wants to shorten java names for shortened source code 
	 * printouts.
	 * @return A JCheckBox
	 */
	public JCheckBox getShortenedSourceShortenJavaCheckBox()
	{
		return shortenedSourceShortenJava;
	}
	
	/**
	 * This returns the JCheckBox which describes if the user wants to shorten non-java names for shortened source 
	 * code printouts.
	 * @return A JCheckBox.
	 */
	public JCheckBox getShortenedSourceShortenNonJavaCheckBox()
	{
		return shortenedSourceShortenNonJava;
	}
	
	/**
	 * This handles ActionEvents.
	 */
	public void actionPerformed(ActionEvent event) 
	{
		if (event.getActionCommand().equals("Browse"))
		{
			JFrame frame = new JFrame();
			frame.setSize(500,400);
			Container framePane = frame.getContentPane();
			
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			
			JFileChooser chooser = new JFileChooser();
			int returnVal = chooser.showDialog(frame, "Select");
			
			mainPanel.add(chooser, BorderLayout.CENTER);
				
			framePane.add(mainPanel);
			framePane.setVisible(true);
			
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				fileField.setText(chooser.getSelectedFile().getAbsoluteFile().toString());
			}			
		}
		else if (event.getActionCommand().equals("Print"))
		{
			dispose();
			ASCIIPrintThread printThread = new ASCIIPrintThread(this);
			printThread.start();
			//HTMLPrintThread thread = new HTMLPrintThread(this);
			//thread.start();
		}
		else if (event.getActionCommand().equals("Close"))
		{
			dispose();
		}
		else if (event.getActionCommand().equals("Add"))
		{
			Enumeration en = null;
			DefaultMutableTreeNode node = null;
			TreePath[] selectedPathArray = tree.getSelectionPaths();
			DefaultMutableTreeNode treeNode = null;
			
			int num = 0;
					
			for (int i =0; i < selectedPathArray.length; i++)
			{
				num = selectedPathArray[i].getPathCount();
				if (num == 3)
				{
					treeNode = (DefaultMutableTreeNode)(selectedPathArray[i].getLastPathComponent());
					if (treeNode != null)
					{
						model.addElement(treeNode.toString());
						selectedIntFVec.add((Interface)(treeNode.getUserObject()));
					}
				}
				else if (num == 2)
				{
					en = ((DefaultMutableTreeNode)(selectedPathArray[i].getLastPathComponent())).children();
					while (en.hasMoreElements())
					{
						node = (DefaultMutableTreeNode)en.nextElement();
						if (node != null)
						{
							selectedIntFVec.add((Interface)(node.getUserObject()));
							model.addElement(((Interface)selectedIntFVec.lastElement()).toString());
						}
					}
				}
			}
		}
		else if (event.getActionCommand().equals("Add All"))
		{
			Project project = null;
			for (int i=0; i<projectVec.size(); i++)
			{
				project = (Project)projectVec.elementAt(i);
				for (int j=0; j<project.getInterfaceVec().size(); j++)
				{
					selectedIntFVec.add((Interface)(project.getInterfaceVec().elementAt(j)));
					model.addElement((Interface)selectedIntFVec.lastElement());
				}
			}
		}
		else if (event.getActionCommand().equals("Remove"))
		{
			int[] indexArray = list.getSelectedIndices();
			
			for (int i=0; i<indexArray.length; i++)
			{
				model.remove(indexArray[i]);
				selectedIntFVec.remove(indexArray[i]);
				
				for (int j=i+1; j<indexArray.length; j++)
					indexArray[j] = indexArray[j] - 1;
			}
		}
		else if (event.getActionCommand().equals("intro.options"))
		{
			IntroductionOptionsGUI gui = new IntroductionOptionsGUI();
			gui.setVisible(true);
		}
		else if (event.getActionCommand().equals("singleUML.options"))
		{
			SingleUMLOptionsGUI gui = new SingleUMLOptionsGUI();
			gui.setVisible(true);
		}
		else if (event.getActionCommand().equals("shortenedSource.options"))
		{
			ShortenedSourceOptionsGUI gui = new ShortenedSourceOptionsGUI();
			gui.setVisible(true);
		}
	}
	
	/**
	 * Handles ItemEvents.  This has to be here because this class implements ItemListener.
	 */
	public void itemStateChanged(ItemEvent event)
	{
		Object source = event.getItemSelectable();
		
		if (source == useIntroCheckBox)
		{
		}
	}
	
	/**
	 * Handles TreeSelectionEvents.  This has to be here because this class implements 
	 * TreeSelectionListener.
	 */
	public void valueChanged(TreeSelectionEvent event)
	{}
	
	/**
	 * This class handles closing the print window.
	 * @author Dominic Kramer
	 */
	private class WindowDestroyer extends WindowAdapter
	{
		/**
		 * Handles WindowEvents.
		 */
		public void windowClosing(WindowEvent event)
		{
			dispose();
		}
	}
	
	/**
	 * This class is used to create a window which holds the introduction options.
	 * @author Dominic Kramer
	 */
	class IntroductionOptionsGUI extends JFrame implements ActionListener, ItemListener
	{
		/**
		 * Creates the window.
		 */
		public IntroductionOptionsGUI()
		{
			JPanel introPanel = new JPanel();
			introPanel.setLayout(new BorderLayout());
			
			JPanel introOptionsPanel = new JPanel();
			introOptionsPanel.setLayout(new GridLayout(3,0));
						
					JLabel titleLabel = new JLabel("Title = ");
							
					JLabel authorLabel = new JLabel("Author = ");
												
					JLabel dateLabel = new JLabel("Date = ");
							
					JLabel descLabel = new JLabel("Description = ");
					JScrollPane descTextAreaScrollPane = new JScrollPane(descTextArea);							
						//descTextArea.setFocusable(false);
											
				JPanel panel1 = new JPanel();
				panel1.setLayout(new FlowLayout(FlowLayout.LEFT));
					panel1.add(titleLabel);
					panel1.add(titleField);

				JPanel panel2 = new JPanel();		
				panel2.setLayout(new FlowLayout(FlowLayout.LEFT));
					panel2.add(authorLabel);
					panel2.add(authorField);
						
				JPanel panel3 = new JPanel();
				panel3.setLayout(new FlowLayout(FlowLayout.LEFT));
					panel3.add(dateLabel);
					panel3.add(dateField);
					panel3.add(useCurrentDateBox);
					
				introOptionsPanel.add(panel1);
				introOptionsPanel.add(panel2);
				introOptionsPanel.add(panel3);
						
				introPanel.add(introOptionsPanel, BorderLayout.CENTER);
					
				JPanel descJPanel = new JPanel();
				descJPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
				descJPanel.add(descLabel);
				descJPanel.add(descTextAreaScrollPane);
				introPanel.add(descJPanel, BorderLayout.SOUTH);
				
				JMenuBar bar = new JMenuBar();
				JMenu fileMenu = new JMenu("File");
					JMenuItem closeItem = new JMenuItem("Close");
					closeItem.addActionListener(this);
					fileMenu.add(closeItem);
				bar.add(fileMenu);
				setJMenuBar(bar);
				
				getContentPane().add(introPanel);
				pack();
		}
		
		/**
		 * Handles ActionEvents.
		 */		
		public void actionPerformed(ActionEvent event)
		{
			if (event.getActionCommand().equals("Use current date"))
			{
				if (useCurrentDateBox.isSelected())
				{
					dateField.setText(DateFormat.getDateInstance(DateFormat.LONG).format(new Date(System.currentTimeMillis())));
				}
				else
				{
					dateField.setText("");
				}
			}
			else if (event.getActionCommand().equals("Close"))
				dispose();
		}
		
		/**
		 * Handles ItemEvents.
		 */		
		public void itemStateChanged(ItemEvent event)
		{
			Object source = event.getItemSelectable();
		
			if (source == useIntroCheckBox)
			{
			}
		}
	}
	
	/**
	 * This class is used to create a window displaying the options for single UML diagrams.
	 * @author Dominic Kramer
	 */
	class SingleUMLOptionsGUI extends JFrame implements ActionListener
	{
		/**
		 * Creates the window.
		 */
		public SingleUMLOptionsGUI()
		{			
			JMenuBar bar = new JMenuBar();
				JMenu fileMenu = new JMenu("File");
					JMenuItem fileMenuItem = new JMenuItem("Close");
					fileMenuItem.addActionListener(this);
				fileMenu.add(fileMenuItem);
			bar.add(fileMenu);
			
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new GridLayout(2,0));
				mainPanel.add(singleUMLShortenJava);
				mainPanel.add(singleUMLShortenNonJava);
			
			setJMenuBar(bar);
			getContentPane().add(mainPanel);
			pack();
		}
		
		/**
		 * Handles the ActionEvents.
		 */
		public void actionPerformed(ActionEvent event)
		{
			if (event.getActionCommand().equals("Close"))
				dispose();
		}
	}
	
	/**
	 * This class is used to create a window which displays the options printing the 
	 * shortened source code.
	 * @author Dominic Kramer
	 */
	class ShortenedSourceOptionsGUI extends JFrame implements ActionListener
	{
		/**
		 * Creates the window.
		 */
		public ShortenedSourceOptionsGUI()
		{
			JMenuBar bar = new JMenuBar();
				JMenu fileMenu = new JMenu("File");
					JMenuItem fileMenuItem = new JMenuItem("Close");
					fileMenuItem.addActionListener(this);
				fileMenu.add(fileMenuItem);
			bar.add(fileMenu);
			
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new GridLayout(2,0));
				mainPanel.add(shortenedSourceShortenJava);
				mainPanel.add(shortenedSourceShortenNonJava);
			
			setJMenuBar(bar);
			getContentPane().add(mainPanel);
			pack();
		}
		
		/**
		 * Handles ActionEvents.
		 */		
		public void actionPerformed(ActionEvent event)
		{
			if (event.getActionCommand().equals("Close"))
				dispose();
		}
	}
}
