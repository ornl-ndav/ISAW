/*
 * File:  PackageTreeJPanel.java
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
 * Revision 1.2  2004/03/11 18:53:35  bouzekc
 * Documented file using javadoc statements.
 * Removed usage of the HorizontalInterfacePanelGUI and VerticalInterfacePanelGUI
 * classes.
 *
 * Revision 1.1  2004/02/07 05:09:43  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.panel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import devTools.Hawk.classDescriptor.gui.frame.FileAssociationGUI;
import devTools.Hawk.classDescriptor.gui.frame.HawkDesktop;
import devTools.Hawk.classDescriptor.gui.internalFrame.JavadocsGUI;
import devTools.Hawk.classDescriptor.gui.internalFrame.ShortenedSourceGUI;
import devTools.Hawk.classDescriptor.gui.internalFrame.SingleUMLGUI;
import devTools.Hawk.classDescriptor.gui.internalFrame.SourceCodeGUI;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.modeledObjects.Project;
import devTools.Hawk.classDescriptor.tools.FileAssociationManager;
import devTools.Hawk.classDescriptor.tools.InterfaceUtilities;

/**
 * This is a special JPanel which contains a JTree which displays interfaces in a project by their package 
 * names.  This is how the tree will look.
 * <br> (Project's name)'s Packages
 * <br> |
 * <br> +-Package1
 * <br> ....|
 * <br> ....+-class1
 * <br> ....+-class2
 * <br> ..........
 * <br> ..........
 * <br> |
 * <br> +-Package2
 * <br> .....
 * <br> .....
 * <br> .....
 * @author Dominic Kramer
 */
public class PackageTreeJPanel extends JPanel implements TreeSelectionListener, ActionListener
{
	/**
	 * This is a Vector of Vectors.  Each of these inside Vectors is a Vector of Interface objects.
	 */
	private Vector packageVec;
	/**
	 * This is the project whose interfaces are added to the tree.
	 */
	private Project project;
	/**
	 * This is the JTree that the elements are placed onto.
	 */
	private JTree tree;
	/**
	 * The trees root node.
	 */
	private DefaultMutableTreeNode rootNode;
	/**
	 * The model that handles modifying the tree.
	 */
	private DefaultTreeModel model;
	/**
	 * The menubar associated with this panel.
	 */
	private JMenuBar menuBar;
	/**
	 * The popup menu associated with this panel.
	 */
	private JPopupMenu popup;
	/**
	 * The JInternalFrame that this panel is placed onto.
	 */
	private JInternalFrame frame;
	/**
	 * The JCheckBox that contains the option to shorten package names 
	 * if they are java names.
	 */
	private JCheckBox shortenPackageJavaBox;
	/**
	 * The JCheckBox that contains the option to shorten class names 
	 * if they are java names.
	 */
	private JCheckBox shortenClassJavaBox;
	/**
	 * The JCheckBox that contains the option to shorten package names 
	 * if they are non-java names.
	 */
	private JCheckBox shortenPackageOtherBox;
	/**
	 * The JCheckBox that contains the option to shorten class names 
	 * if they are non-java names.
	 */
	private JCheckBox shortenClassOtherBox;
	/**
	 * The HawkDesktop onto which this panel is added.
	 */
	private HawkDesktop desktop;
	
	/**
	 * Create a new PackageTreeJPanel.
	 * @param pro The project associated with this panel.
	 * @param FRAME The frame that this panel is added to.
	 * @param packageShortJava Set this to true if package names are to be shortened if they are java names.
	 * @param packageShortOther Set this to true if package names are to be shrotened if they are non-java names.
	 * @param classShortJava Set this to true if class names are to be shortened if they are java names.
	 * @param classShortOther Set this to ture if class names are to be shortened if they are non-java names.
	 * @param desk The HawkDesktop onto which this panel is on.
	 */
	public PackageTreeJPanel(Project pro, JInternalFrame FRAME, boolean packageShortJava, boolean packageShortOther, boolean classShortJava, boolean classShortOther, HawkDesktop desk)
	{			
		desktop = desk;
		
		frame = FRAME;
		project = pro;
		packageVec = InterfaceUtilities.getVectorOfVectorOfInterfaces(pro.getInterfaceVec(), packageShortJava, packageShortOther, classShortJava, classShortOther);
		
		rootNode = new DefaultMutableTreeNode(project+"'s Packages");
		model = new DefaultTreeModel(rootNode);
		model.addTreeModelListener(new PackageTreeModelListener());
		tree = new JTree(model);
		tree.setEditable(false);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		JScrollPane treeScrollPane = new JScrollPane(tree);

		fillTree(packageShortJava, packageShortOther, classShortJava, classShortOther);

		add(treeScrollPane);

		//now to make the popup menu
			popup = new JPopupMenu();
			//now to add components to the popup menu
			popup.add(new JLabel("View"));
			popup.add(new JSeparator());

			JMenuItem singleUMLPopupItem = new JMenuItem("Single UML");
			singleUMLPopupItem.addActionListener(this);
			singleUMLPopupItem.setActionCommand("popup.singleUML");
			popup.add(singleUMLPopupItem);

			JMenuItem shortenedSourceItem = new JMenuItem("Shortened Source Code");
			shortenedSourceItem.addActionListener(this);
			shortenedSourceItem.setActionCommand("popup.shortenedSource");
			popup.add(shortenedSourceItem);
			
			JMenuItem sourceItem = new JMenuItem("Source Code");
			sourceItem.addActionListener(this);
			sourceItem.setActionCommand("popup.sourceCode");
			popup.add(sourceItem);
			
			JMenuItem javadocsItem = new JMenuItem("Javadocs");
			javadocsItem.addActionListener(this);
			javadocsItem.setActionCommand("popup.javadocs");
			popup.add(javadocsItem);
		
			JMenuItem horizontalItem = new JMenuItem("Horizontally");
			horizontalItem.addActionListener(this);
			horizontalItem.setActionCommand("popup.horizontal");
			//popup.add(horizontalItem);
			
			JMenuItem verticalItem = new JMenuItem("Vertically");
			verticalItem.addActionListener(this);
			verticalItem.setActionCommand("popup.vertical");
			//popup.add(verticalItem);
		
		//now to make the JMenuBar (this is basically just menubar version of the popup menu)
			menuBar = new JMenuBar();
				JMenu fileMenu = new JMenu("File");
					JMenuItem saveButton = new JMenuItem("Save");
					saveButton.addActionListener(this);
					fileMenu.add(saveButton);
			
					JMenuItem closeButton = new JMenuItem("Close");
					closeButton.addActionListener(this);
					fileMenu.add(closeButton);
	
			JMenu editMenu = new JMenu("Edit");
				JMenuItem associateSourceCodeItem = new JMenuItem("Associate Source Code");
				associateSourceCodeItem.addActionListener(this);
				associateSourceCodeItem.setActionCommand("associate.source.code");
				editMenu.add(associateSourceCodeItem);
	
				JMenuItem associateJavadocsItem = new JMenuItem("Associate Javadocs");
				associateJavadocsItem.addActionListener(this);
				associateJavadocsItem.setActionCommand("associate.javadocs");
				editMenu.add(associateJavadocsItem);

				JMenu viewMenu = new JMenu("View");
					JMenuItem singleUMLItem = new JMenuItem("Single UML");
					singleUMLItem.addActionListener(this);
					singleUMLItem.setActionCommand("popup.singleUML");
					viewMenu.add(singleUMLItem);					
					
					JMenuItem shortenedSourceItem2 = new JMenuItem("Shortened Source Code");
					shortenedSourceItem2.addActionListener(this);
					shortenedSourceItem2.setActionCommand("popup.shortenedSource");
					viewMenu.add(shortenedSourceItem2);
			
					JMenuItem sourceItem2 = new JMenuItem("Source Code");
					sourceItem2.addActionListener(this);
					sourceItem2.setActionCommand("popup.sourceCode");
					viewMenu.add(sourceItem2);
			
					JMenuItem javadocsItem2 = new JMenuItem("Javadocs");
					javadocsItem2.addActionListener(this);
					javadocsItem2.setActionCommand("popup.javadocs");
					viewMenu.add(javadocsItem2);
		
					JMenuItem horizontalItem2 = new JMenuItem("Horizontally");
					horizontalItem2.addActionListener(this);
					horizontalItem2.setActionCommand("popup.horizontal");
					//viewMenu.add(horizontalItem2);
			
					JMenuItem verticalItem2 = new JMenuItem("Vertically");
					verticalItem2.addActionListener(this);
					verticalItem2.setActionCommand("popup.vertical");
					//viewMenu.add(verticalItem2);
				
				JMenu propertiesMenu = new JMenu("Properties");
					JMenu shortenJavaMenu = new JMenu("Shorten Java Names");
						shortenPackageJavaBox = new JCheckBox("For Package Names");
						shortenPackageJavaBox.setSelected(packageShortJava);
						shortenPackageJavaBox.setActionCommand("properties.shorten");
						shortenPackageJavaBox.addActionListener(this);
						shortenJavaMenu.add(shortenPackageJavaBox);
						
						shortenClassJavaBox = new JCheckBox("For Class Names");
						shortenClassJavaBox.setSelected(classShortJava);
						shortenClassJavaBox.setActionCommand("properties.shorten");
						shortenClassJavaBox.addActionListener(this);
						shortenJavaMenu.add(shortenClassJavaBox);
						
					JMenu shortenOtherMenu = new JMenu("Shorten Non-Java Names");
						shortenPackageOtherBox = new JCheckBox("For Package Names");
						shortenPackageOtherBox.setSelected(packageShortOther);
						shortenPackageOtherBox.setActionCommand("properties.shorten");
						shortenPackageOtherBox.addActionListener(this);
						shortenOtherMenu.add(shortenPackageOtherBox);
						
						shortenClassOtherBox = new JCheckBox("For Class Names");
						shortenClassOtherBox.setSelected(classShortOther);
						shortenClassOtherBox.setActionCommand("properties.shorten");
						shortenClassOtherBox.addActionListener(this);
						shortenOtherMenu.add(shortenClassOtherBox);
					propertiesMenu.add(shortenJavaMenu);
					propertiesMenu.add(shortenOtherMenu);
						
				menuBar.add(fileMenu);
				menuBar.add(editMenu);
				menuBar.add(viewMenu);
				menuBar.add(propertiesMenu);
				
		//now to add listeners to the components that will pop up the popup menu
		MouseListener popupListener = new PopupListener();
			this.addMouseListener(popupListener);
			tree.addMouseListener(popupListener);
	}
	
	/**
	 * Get the project associated with this panel.
	 * @return The project.
	 */
	public Project getProject()
	{
		return project;
	}
	
	/**
	 * Get the JCheckBox that contains the option to shorten class names 
	 * if they are java names.
	 * @return The JCheckBox.
	 */
	public JCheckBox getShortenClassJavaBox()
	{
		return shortenClassJavaBox;
	}
	
	/**
	 * Get the JCheckBox that contains the option to shorten class names 
	 * if they are non-java names.
	 * @return The JCheckBox.
	 */
	public JCheckBox getShortenClassOtherBox()
	{
		return shortenClassOtherBox;
	}
	
	/**
	 * Get the JCheckBox that contains the option to shorten package names 
	 * if they are java names.
	 * @return The JCheckBox.
	 */
	public JCheckBox getShortenPackageJavaBox()
	{
		return shortenPackageJavaBox;
	}
	
	/**
	 * Get the JCheckBox that contains the option to shorten package names 
	 * if they are non-java names.
	 * @return The JCheckBox.
	 */
	public JCheckBox getShortenPackageOtherBox()
	{
		return shortenPackageOtherBox;
	}
	
	/**
	 * Fills the tree with nodes.  The nodes use either the full name for classes and interfaces or 
	 * shortened names depending on the parameters supplied.
	 * @param packageShortJava Set this to true if package names are to be shortened if they are java names.
	 * @param packageShortOther Set this to true if package names are to be shortened if they are non-java names.
	 * @param classShortJava Set this to true if class names are to be shortened if they are java names.
	 * @param classShortOther Set this to true if class names are to be shortened if they are non-java names.
	 */
	public void fillTree(boolean packageShortJava, boolean packageShortOther, boolean classShortJava, boolean classShortOther)
	{
		rootNode.removeAllChildren();
		Vector currentIntFVec = new Vector();
		for (int i=0; i<packageVec.size(); i++)
		{
			currentIntFVec = (Vector)packageVec.elementAt(i);
			String newPackageName = ((Interface)currentIntFVec.elementAt(0)).getPgmDefn().getPackage_Name(packageShortJava, packageShortOther);
			DefaultMutableTreeNode node = new DefaultMutableTreeNode( newPackageName );
			rootNode.add(node);
			
			for (int j=0; j<currentIntFVec.size(); j++)
			{
				String newIntFName = ((Interface)currentIntFVec.elementAt(j)).getPgmDefn().getInterface_name(classShortJava, classShortOther);
				node.add(new DefaultMutableTreeNode( newIntFName ));
			}
		}
		//now to expand the root node
			tree.expandPath(new TreePath(rootNode));
		model.reload();
		}
	
	/**
	 * Gets the currently selected Interface from the JTree.  If no nodes are selected or if the node 
	 * selected does not correspond to an Interface, null is returned.
	 * @return The selected Interface or null.
	 */
	public Interface getSelectedInterface()
	{
		Interface intF = null;
		TreePath treePath = tree.getSelectionPath();
		if (treePath != null)
		{
			int count = treePath.getPathCount();
			if (count<3)
				intF = null;
			else
			{
				String str = (String)((DefaultMutableTreeNode)tree.getLastSelectedPathComponent()).getUserObject();
				int i=0;
				int j=0;
				boolean found = false;
				while (!found && i<packageVec.size())
				{
					j=0;
					while (!found && j<((Vector)packageVec.elementAt(i)).size())
					{
						if ( ((Interface)((Vector)packageVec.elementAt(i)).elementAt(j)).getPgmDefn().getInterface_name(shortenClassJavaBox.isSelected(), shortenClassOtherBox.isSelected()).equals(str))
						{
							found = true;
							intF = ((Interface)((Vector)packageVec.elementAt(i)).elementAt(j));
						}
						j++;
					}
					i++;
				}
			}
		}
				
		return intF;
	}
	
	/**
	 * Get the menubar associated with this panel.  This menubar contains 
	 * all of the menus and items that the user can select and this class will 
	 * know what to do with the events.  
	 * @return The JMenuBar.
	 */
	public JMenuBar getJMenuBar()
	{			
		return menuBar;
	}
	
	/**
	 * Handles the JTree's value changing.
	 * @param e The event to process.
	 */
	public void valueChanged(TreeSelectionEvent e)
	{

	}
	
	/**
	 * Handles ActionEvents.
	 */
	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals("Save"))
		{
			
		}
		else if (event.getActionCommand().equals("Close"))
		{
			frame.dispose();
		}
		else if (event.getActionCommand().equals("popup.singleUML"))
		{
			Interface intF = getSelectedInterface();
			
			if (intF != null)
			{
				SingleUMLGUI singleUML = null;
				singleUML = new SingleUMLGUI(intF, intF.getPgmDefn().getInterface_name(true,false), true, false,desktop);		
				
				if (singleUML != null)
				{
					singleUML.setVisible(true);
					desktop.getSelectedDesktop().add(singleUML);
					singleUML.setAsSelected(true);
				}
			}
		}		
		else if (event.getActionCommand().equals("popup.shortenedSource"))
		{
			Interface intF = getSelectedInterface();
			
			if (intF != null)
			{
				ShortenedSourceGUI popupSsg = new ShortenedSourceGUI(intF, getSelectedInterface().getPgmDefn().getInterface_name(true,false), true, false,desktop);
				popupSsg.setVisible(true);
				desktop.getSelectedDesktop().add(popupSsg);
				popupSsg.setAsSelected(true);
			}
		}
		else if (event.getActionCommand().equals("popup.sourceCode"))
		{
			Interface intF = getSelectedInterface();
			
			if (intF != null)
			{
				SourceCodeGUI popupSource = new SourceCodeGUI(intF, getSelectedInterface().getPgmDefn().getInterface_name(),desktop);
				popupSource.setVisible(true);
				desktop.getSelectedDesktop().add(popupSource);
				popupSource.setAsSelected(true);
			}
		}
		else if (event.getActionCommand().equals("popup.javadocs"))
		{
			Interface intF = getSelectedInterface();
			
			if (intF != null)
			{
				JavadocsGUI javagui = new JavadocsGUI(intF,getSelectedInterface().getPgmDefn().getInterface_name(),desktop);
				javagui.setVisible(true);
				desktop.getSelectedDesktop().add(javagui);
				javagui.setAsSelected(true);
			}
		}
		else if (event.getActionCommand().equals("properties.shorten"))
		{
			fillTree(shortenPackageJavaBox.isSelected(), shortenPackageOtherBox.isSelected(), shortenClassJavaBox.isSelected(), shortenClassOtherBox.isSelected());
		}
		else if (event.getActionCommand().equals("associate.source.code"))
		{
			Interface selectedIntF = getSelectedInterface();
			if (selectedIntF != null)
			{
				FileAssociationGUI sourceGUI = new FileAssociationGUI(selectedIntF, FileAssociationManager.JAVASOURCE);
				sourceGUI.setVisible(true);
			}
			else
			{
				//custom title, error icon
					JOptionPane opPane = new JOptionPane();
					JOptionPane.showMessageDialog(opPane,
						"You need to select an interface to associate a source code file with it",
						"Note",
						JOptionPane.INFORMATION_MESSAGE);
			}
		}
		else if (event.getActionCommand().equals("associate.javadocs"))
		{
			Interface selectedIntF = getSelectedInterface();
			if (selectedIntF != null)
			{
				FileAssociationGUI javadocsGUI = new FileAssociationGUI(selectedIntF, FileAssociationManager.JAVADOCS);
				javadocsGUI.setVisible(true);
			}
			else
			{
				//custom title, error icon
					JOptionPane opPane = new JOptionPane();
					JOptionPane.showMessageDialog(opPane,
						"You need to select an interface to associate a javadocs file with it",
						"Note",
						JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
	
	/**
	* This class handles what to do when the user wants to open
	* a popup menu.
	* @author Dominic Kramer
	*/
	class PopupListener extends MouseAdapter 
	{
			public void mousePressed(MouseEvent e)
		{
			maybeShowPopup(e);
			}

		public void mouseReleased(MouseEvent e)
		{
			maybeShowPopup(e);
			}

		private void maybeShowPopup(MouseEvent e)
		{
			if (e.isPopupTrigger())
			{
				popup.show(e.getComponent(),
					e.getX(), e.getY());
			}
		}
	}
	
	/**
	 * Listens to events thrown from the JTree.
	 * @author Dominic Kramer
	 */
	class PackageTreeModelListener implements TreeModelListener
	{
		public void treeNodesChanged(TreeModelEvent e) {}
		public void treeNodesInserted(TreeModelEvent e) {}
		public void treeNodesRemoved(TreeModelEvent e) {}
		public void treeStructureChanged(TreeModelEvent e) {}
	}
}