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
 * @author kramer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class PrintGUI extends JFrame implements ActionListener, ItemListener, TreeSelectionListener
{
	private Vector projectVec;  // a vector of projects
	private JTextField fileField;
	private JTree tree;
	private JList list;
	private DefaultListModel model;
	private Vector selectedIntFVec;
	
	//these are the components in introPanel
		private JCheckBox useIntroCheckBox;
		private JTextField titleField;
		private JTextField authorField;
		private JTextField dateField;
		private JCheckBox useCurrentDateBox;
		private JTextArea descTextArea;
		
	//these are the other checkboxes
		private JCheckBox useContentsCheckBox;
		private JCheckBox usePackageListCheckBox;
		private JCheckBox useAlphaUMLCheckBox;
		private JCheckBox useShortenedSourceCheckBox;
		
		private JCheckBox singleUMLShortenJava;
		private JCheckBox singleUMLShortenNonJava;
		private JCheckBox shortenedSourceShortenJava;
		private JCheckBox shortenedSourceShortenNonJava;
		
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
		pack();
		
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
	}
	public DefaultListModel getModel()
	{
		return model;
	}
	
	public Vector getVectorOfSelectedInterfaces()
	{
		return selectedIntFVec;
	}
	
	public JFrame getFrame()
	{
		return this;
	}
	
	public JTextField getFileField()
	{
		return fileField;
	}

	public JCheckBox getUseIntroCheckBox()
	{
		return useIntroCheckBox;
	}
	
	public JTextField getTitleField()
	{
		return titleField;
	}
	
	public JTextField getAuthorField()
	{
		return authorField;
	}
	
	public JTextField getDateField()
	{
		return dateField;
	}
	
	public JCheckBox getUseCurrentDateBox()
	{
		return useCurrentDateBox;
	}
	
	public JTextArea getDescTextArea()
	{
		return descTextArea;
		
	}
		
	public JCheckBox getUseContentsCheckBox()
	{
		return useContentsCheckBox;
	}
	
	public JCheckBox getUsePackageListCheckBox()
	{
		return usePackageListCheckBox;
	}
	
	public JCheckBox getUseAlphaUMLCheckBox()
	{
		return useAlphaUMLCheckBox;
	}
	
	public JCheckBox getUseShortenedSourceCheckBox()
	{
		return useShortenedSourceCheckBox;
	}
	
	public JCheckBox getSingleUMLShortenJavaCheckBox()
	{
		return singleUMLShortenJava;
	}
	
	public JCheckBox getSingleUMLShortenNonJavaCheckBox()
	{
		return singleUMLShortenNonJava;
	}
	
	public JCheckBox getShortenedSourceShortenJavaCheckBox()
	{
		return shortenedSourceShortenJava;
	}
	
	public JCheckBox getShortenedSourceShortenNonJavaCheckBox()
	{
		return shortenedSourceShortenNonJava;
	}
	
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
	
	public void itemStateChanged(ItemEvent event)
	{
		Object source = event.getItemSelectable();
		
		if (source == useIntroCheckBox)
		{
		}
	}
	
	public void valueChanged(TreeSelectionEvent event)
	{}
	
	private class WindowDestroyer extends WindowAdapter
	{
		public void windowClosing(WindowEvent event)
		{
			dispose();
		}
	}
	
	class IntroductionOptionsGUI extends JFrame implements ActionListener, ItemListener
	{
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
		
		public void itemStateChanged(ItemEvent event)
		{
			Object source = event.getItemSelectable();
		
			if (source == useIntroCheckBox)
			{
			}
		}
	}
	
	class SingleUMLOptionsGUI extends JFrame implements ActionListener
	{
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
		
		public void actionPerformed(ActionEvent event)
		{
			if (event.getActionCommand().equals("Close"))
				dispose();
		}
	}

	class ShortenedSourceOptionsGUI extends JFrame implements ActionListener
	{
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
		
		public void actionPerformed(ActionEvent event)
		{
			if (event.getActionCommand().equals("Close"))
				dispose();
		}
	}
}
