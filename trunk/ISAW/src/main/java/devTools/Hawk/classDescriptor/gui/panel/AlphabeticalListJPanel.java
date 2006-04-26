/*
 * File:  AlphabeticalListJPanel.java
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
 * Revision 1.4  2004/06/04 23:40:08  kramer
 * Fixed some Javadoc errors.
 *
 * Revision 1.3  2004/05/26 20:03:23  kramer
 *
 * Added the methods:
 *   getWaitingComponents()
 *   setEveryButtonIsSelected()
 *   setEveryButtonIsEnabled()
 * Optimized the fillList() method
 * Created a new renderer for the JList in the gui
 * Added gui components to filter and search the items displayed in the list
 *
 * Revision 1.2  2004/03/12 19:46:16  bouzekc
 * Changes since 03/10.
 *
 */
 package devTools.Hawk.classDescriptor.gui.panel;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import devTools.Hawk.classDescriptor.gui.ExternallyControlledFrame;
import devTools.Hawk.classDescriptor.gui.MouseNotifiable;
import devTools.Hawk.classDescriptor.gui.frame.HawkDesktop;
import devTools.Hawk.classDescriptor.gui.internalFrame.InternalFrameUtilities;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.modeledObjects.InterfaceDefn;
import devTools.Hawk.classDescriptor.modeledObjects.Project;
import devTools.Hawk.classDescriptor.tools.InterfaceUtilities;
import devTools.Hawk.classDescriptor.tools.SearchUtilities;
import devTools.Hawk.classDescriptor.tools.SystemsManager;

/**
 * This is a specialized JPanel which displays all of the Interface objects in a Project object in 
 * alphabetical order.  The JPanel contains a JList in a JScrollPane to display the Interface objects.  
 * There is also a popup menu and JMenus that can be obtained from methods in this class.  This class 
 * handles all the actions that might be thrown when the user selects a item from one of the menus except 
 * if the user selects close.  The window onto which this JPanel is placed has to handle the ActionEvent 
 * thrown when the user selects the close option.
 * @author Dominic Kramer
 */
public class AlphabeticalListJPanel extends JPanel implements ActionListener, ListSelectionListener, MouseNotifiable
{
	//the following are declared here to allow actionPerformed() to find them
	//they are components placed on the GUI
		/** 
		 * The Jlist that contains all of the interfacees and/or classes
		* in a project in alphabetical order
		*/
		protected JList alphaList;
		
		/**
		* The popup menu that appears when the user right clicks on the JList
		*/
		protected JPopupMenu popup;
		
	//the following are attributes that describe the class
		/**
		* This contains a reference to the ProjectGUI which had an event performed
		* on it to cause this GUI to be opened
		*/
		protected Project project;
		/**
		 * The HawkDesktop that this window is in.
		 */
		protected HawkDesktop desktop;
		/**
		* The checkbox allowing the user to select if they want to shorten java names.
		*/
		protected JCheckBox shortenJavaBox;
		/**
		* The checkbox allowing the user to select if they want to shorten non-java names.
		*/
		protected JCheckBox shortenOtherBox;
		/**
		 * The component on which the list is added.
		 */
		protected ExternallyControlledFrame frame;
		
		protected JToggleButton viewInner;
		protected JToggleButton viewOuter;
		protected JToggleButton viewAbstract;
		protected JToggleButton viewConcrete;
		protected JToggleButton viewClasses;
		protected JToggleButton viewInterfaces;
//		protected JLabel label;
		protected JTextField label;
		protected JPanel searchPanel;
		protected JButton clearButton;
		protected JButton searchButton;
		protected JTextField searchField;
//		protected JComboBox comboBox;
		protected JCheckBox checkBox;
		
	/**
	 * Create a new AlphabeticalListGUI.
	 * @param PRO The Project whose data is written.
	 * @param shortJava True if you want a name to be shortened if it is a java name.  For 
	 * example, java.lang.String would be shortened to String.
	 * @param shortOther True if you want a name to be shortened if it is a non-java name.
	 * @param desk The HawkDesktop that this window is on.
	 * @param comp The component onto which the panel is placed.  If the component is a JFrame or JInternalFrame the 
	 * panel's size automatically changes to fit the contents of the list when the contents change.
	 */
	public AlphabeticalListJPanel(Project PRO, boolean shortJava, boolean shortOther, HawkDesktop desk, ExternallyControlledFrame comp)
	{
		//Now to define the Project that the information is obtained from
		//to fill in data in the GUI
			project = PRO;
			desktop = desk;
			frame = comp;
			label = new JTextField(20);
				label.setBackground(this.getBackground());
				label.setEditable(false);
/*
			String[] strArr = new String[2];
				strArr[0]="All Data";
				strArr[1]="Currently Displayed Data";
			comboBox = new JComboBox(strArr);
				comboBox.addActionListener(this);
				comboBox.setActionCommand("comboBox");
				ToolTipManager.sharedInstance().registerComponent(comboBox);
				comboBox.setToolTipText("Set whether to perform the search and/or filters on all the classes and interfaces or only those that are currently displayed.");
*/
				checkBox = new JCheckBox("Search/filter within results");
					checkBox.setSelected(false);
					checkBox.addActionListener(this);
					checkBox.setActionCommand("checkBox");
					ToolTipManager.sharedInstance().registerComponent(checkBox);
					checkBox.setToolTipText("Specify if you want to search/filter the currently displayed classes and interfaces");
				shortenJavaBox = new JCheckBox();
				shortenJavaBox.setSelected(shortJava);
				ToolTipManager.sharedInstance().registerComponent(shortenJavaBox);
			shortenOtherBox = new JCheckBox();
				shortenOtherBox.setSelected(shortOther);
				ToolTipManager.sharedInstance().registerComponent(shortenOtherBox);
		
			searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				JLabel textLabel = new JLabel("Name:  ");
				searchField = new JTextField(20);
				searchButton = new JButton("Search");
					searchButton.addActionListener(this);
					searchButton.setActionCommand("search");
					ToolTipManager.sharedInstance().registerComponent(searchButton);
					searchButton.setToolTipText("Searches all of the currently displayed classes and/or interfaces.");
				clearButton = new JButton("Reset");
					clearButton.addActionListener(this);
					clearButton.setActionCommand("reset");
					ToolTipManager.sharedInstance().registerComponent(clearButton);
					clearButton.setToolTipText("Resets the search and filters specifications and displays all classes and interfaces.");
			searchPanel.add(textLabel);
			searchPanel.add(searchField);
			searchPanel.add(clearButton);
			searchPanel.add(searchButton);
		
		setLayout(new GridLayout(1,1));
		
		//these are used for the toolbar
		viewInner = getImagedJToggleButton("view_inner.png","Inner");
			viewInner.setActionCommand("toolbar.inner");
			viewInner.addActionListener(this);
			viewInner.setSelected(false);
			ToolTipManager.sharedInstance().registerComponent(viewInner);
			viewInner.setToolTipText("View only inner classes or interfaces");
		viewOuter = getImagedJToggleButton("view_outer.png","Outer");
			viewOuter.setActionCommand("toolbar.outer");
			viewOuter.addActionListener(this);
			viewOuter.setSelected(false);
			ToolTipManager.sharedInstance().registerComponent(viewOuter);
			viewOuter.setToolTipText("View only outer classes or interfaces");
		viewAbstract = getImagedJToggleButton("view_abstract.png","Abstract");
			viewAbstract.setActionCommand("toolbar.abstract");
			viewAbstract.addActionListener(this);
			viewAbstract.setSelected(false);
			ToolTipManager.sharedInstance().registerComponent(viewAbstract);
			viewAbstract.setToolTipText("View only interfaces or abstract classes");
		viewConcrete = getImagedJToggleButton("view_concrete.png","Concrete");
			viewConcrete.setActionCommand("toolbar.concrete");
			viewConcrete.addActionListener(this);
			viewConcrete.setSelected(false);
			ToolTipManager.sharedInstance().registerComponent(viewConcrete);
			viewConcrete.setToolTipText("View only concrete classes");
		viewClasses = getImagedJToggleButton("view_classes.png","Classes");
			viewClasses.setActionCommand("toolbar.classes");
			viewClasses.addActionListener(this);
			viewClasses.setSelected(false);
			ToolTipManager.sharedInstance().registerComponent(viewClasses);
			viewClasses.setToolTipText("View only classes");
		viewInterfaces = getImagedJToggleButton("view_interfaces.png","Interfaces");
			viewInterfaces.setActionCommand("toolbar.interfaces");
			viewInterfaces.addActionListener(this);
			viewInterfaces.setSelected(false);
			ToolTipManager.sharedInstance().registerComponent(viewInterfaces);
			viewInterfaces.setToolTipText("View only interfaces");
		
		label.setText("All classes and interfaces are displayed");
		
		//Now to make the JList
			//now to make the list for the gui		
			//this model allows you to modify the list
			alphaList = new JList();
			alphaList.setListData(project.getInterfaceVec());
			alphaList.addListSelectionListener(this);
			alphaList.setCellRenderer(new AlphaListRenderer(this));
			ToolTipManager.sharedInstance().registerComponent(alphaList);
			//the following only allows one item to be selected at a time
			alphaList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);		
		
		//now to fill the list
			shortenJavaBox.setSelected(shortJava);
			shortenOtherBox.setSelected(shortOther);
			fillList();
				
		//now to make the JScrolPane to put the JList on		
			JScrollPane listPane = new JScrollPane(alphaList);
			
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
							
		//now to add listeners to the components that will pop up the popup menu
			MouseListener popupListener = new PopupListener();
			alphaList.addMouseListener(popupListener);
			
		add(listPane);
	}
	
	private JToggleButton getImagedJToggleButton(String filename, String name)
	{
		ImageIcon icon = SystemsManager.getImageIconOrNull(filename);
		if (icon != null)
			return new JToggleButton(icon);
		else
			return new JToggleButton(name);
	}
	
	public JPanel getSearchPanel()
	{
		return searchPanel;
	}
	
	public void setSearchPanel(JPanel panel)
	{
		searchPanel = panel;
	}
	
	public JTextField getLabel()
	{
		return label;
	}
	
	public void setLabel(JTextField str)
	{
		label = str;
	}
	
	public JToolBar createToolBar()
	{
		JToolBar toolBar = new JToolBar();
			toolBar.add(new JLabel("Filters:  "));
			toolBar.add(viewInner);
			toolBar.add(viewOuter);
			toolBar.add(viewAbstract);
			toolBar.add(viewConcrete);
			toolBar.add(viewClasses);
			toolBar.add(viewInterfaces);
			toolBar.add(new JToolBar.Separator());
			toolBar.add(checkBox);
		toolBar.setFloatable(false);
		ToolTipManager.sharedInstance().registerComponent(toolBar);
		toolBar.setToolTipText("Filter which classes or interfaces are to be displayed");
		
		return toolBar;
	}
	
	/**
	 * Get the close menu item associated with this panel.  The class using this panel 
	 * has to handle the event that is to occur when the user selects the close menu item.
	 * @return The close menu item.
	 */
	public JMenuItem getCloseMenuItem()
	{
		JMenuItem closeButton = new JMenuItem("Close");
		return closeButton;
	}
	
	/**
	 * Get the edit menu associated with this panel.  The menu contains options for 
	 * the user to associate source code and javadocs files with the project.
	 * @return The edit menu.
	 */
	public JMenu getEditMenu()
	{
		JMenu editMenu = new JMenu("Edit");
			JMenuItem associateSourceCodeItem = new JMenuItem("Associate Source Code");
			associateSourceCodeItem.addActionListener(this);
			associateSourceCodeItem.setActionCommand("associate.source.code");
			editMenu.add(associateSourceCodeItem);

			JMenuItem associateJavadocsItem = new JMenuItem("Associate Javadocs");
			associateJavadocsItem.addActionListener(this);
			associateJavadocsItem.setActionCommand("associate.javadocs");
			editMenu.add(associateJavadocsItem);
			
			return editMenu;
	}
	
	/**
	 * Get the view menu associated with this panel.  The view menu contains options to view 
	 * an Interface's UML diagram, shortened source code, source code, and javadocs.
	 * @return Get the view menu.
	 */
	public JMenu getViewMenu()
	{
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
			
			return viewMenu;
	}
	
	/**
	 * Get the properties menu associated with this panel.  This menu contains JCheckBoxes 
	 * that shorten java or non-java names in the list of Interfaces.
	 * @param shortJava Set this to true if java names are supposed to be shortened by default.
	 * @param shortOther Set this to true if non-java names are supposed to be shortened by default.
	 * @return The properties menu.
	 */
	public JMenu getPropertiesMenu(boolean shortJava, boolean shortOther)
	{
		JMenu propertiesMenu = new JMenu("Properties");
			shortenJavaBox = new JCheckBox("Shorten Java Names");
			shortenJavaBox.addActionListener(this);
			shortenJavaBox.setActionCommand("properties.shorten");
			shortenJavaBox.setSelected(shortJava);
			propertiesMenu.add(shortenJavaBox);
					
			shortenOtherBox = new JCheckBox("Shorten Non-Java Names");
			shortenOtherBox.addActionListener(this);
			shortenOtherBox.setActionCommand("properties.shorten");
			shortenOtherBox.setSelected(shortOther);
			propertiesMenu.add(shortenOtherBox);
			
			return propertiesMenu;
	}
	
	/**
	 * Get the checkbox which the user selects if he/she wants java names to be shortened.
	 * @return The checkbox to shorten java names.
	 */
	public JCheckBox getShortenJavaCheckBox()
	{
		return shortenJavaBox;
	}
	
	/**
	 * Get the checkbox which the user selects if he/she wants non-java names to be shortened.
	 * @return The checkbox to shorten non-java names.
	 */
	public JCheckBox getShortenOtherCheckBox()
	{
		return shortenOtherBox;
	}
	
	/**
	 * Set the project to use.
	 * @param PRO The project to use.
	 */
	public void setProject(Project PRO)
	{
		project = PRO;
	}
	
	/**
	 * The project whose classes and interfaces are displayed.
	 * @return The project used.
	 */
	public Project getProject()
	{
		return project;
	}
	
	/**
	 * Get the Interface selected from the list.  If no interfaces are selected 
	 * the first interface in the list is returned.
	 * @return The selected interface.
	 */
	public Interface[] getSelectedInterfaces()
	{
		int[] indexArr = alphaList.getSelectedIndices();
		Vector tempVec = new Vector();
		for (int i=0; i<indexArr.length; i++)
		{
			if (indexArr[i] >= 0)
				tempVec.add( (Interface)alphaList.getModel().getElementAt(indexArr[i]) );
		}
		Interface[] intfArr = new Interface[tempVec.size()];
		for (int i=0; i<tempVec.size(); i++)
			intfArr[i] = (Interface)tempVec.elementAt(i);
					
		return intfArr;
	}
		
	/**
	 * Fill the list with the classes and interfaces from the Project object from the field project.
	 */
	public void fillList()//boolean shortJava, boolean shortOther)
	{
		shortenJavaBox.setEnabled(false);
		shortenOtherBox.setEnabled(false);
		shortenJavaBox.setToolTipText("The list is still being populated");
		shortenOtherBox.setToolTipText("The list is still being populated");
		
		Vector currentlyDisplayedIntf = null;
		
		if (!checkBox.isSelected())
			currentlyDisplayedIntf = project.getInterfaceVec();
		else
		{
			currentlyDisplayedIntf = new Vector();
			for (int i=0; i<alphaList.getModel().getSize(); i++)
				currentlyDisplayedIntf.add(alphaList.getModel().getElementAt(i));
		}

		InterfaceUtilities.alphabatizeVector(currentlyDisplayedIntf, shortenJavaBox.isSelected(), shortenOtherBox.isSelected());
		Vector tempVec = new Vector();
		InterfaceDefn intf = new InterfaceDefn();
		boolean viewAll = (!viewInner.isSelected() && !viewOuter.isSelected() && !viewAbstract.isSelected() && !viewConcrete.isSelected() && !viewClasses.isSelected() && !viewInterfaces.isSelected());
		for (int i=0; i<currentlyDisplayedIntf.size(); i++)
		{
			intf = ((Interface)currentlyDisplayedIntf.elementAt(i)).getPgmDefn();

			boolean show = true;
			
			if (!viewAll)
			{
				if (viewInner.isSelected())
					show = show && intf.isInner();
				else if (show && viewOuter.isSelected())
					show = show && intf.isOuter();
				
				if (show && viewAbstract.isSelected())
					show = show && intf.isAbstract();
				else if (show && viewConcrete.isSelected())
					show = show && intf.isConcrete();
					
				if (show && viewClasses.isSelected())
					show = show && intf.isClass();
				else if (show && viewInterfaces.isSelected())
					show = show && intf.isInterface();
			}

			if (show)
				tempVec.addElement((Interface)currentlyDisplayedIntf.elementAt(i));
		}
		alphaList.setListData(tempVec);
		
		shortenJavaBox.setEnabled(true);
		shortenOtherBox.setEnabled(true);
		shortenJavaBox.setToolTipText(null);
		shortenOtherBox.setToolTipText(null);
	}
	
	/**
	 * Looks at the JToggleButton, JCheckBoxes, and JTextField (holding the search querie) to determine 
	 * which Interfaces should be displayed in the list.  The Interfaces that are to be displayed are placed in 
	 * Vector returned.
	 */
	private Vector getAppropriateInterfaces()
	{
		Vector vec = new Vector();
		if (!checkBox.isSelected())
		{
			Vector initialVec = project.getInterfaceVec();
			
		}
		else
		{
		}
		
		return vec;
	}
	
	public void searchList()
	{
		this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		StringBuffer str = new StringBuffer();
		
		if (!checkBox.isSelected())
			str.append("all ");
		
		if (viewAbstract.isSelected())
			str.append("abstract ");
		else if (viewConcrete.isSelected())
			str.append("concrete ");
			
		if (viewInner.isSelected())
			str.append("inner ");
		else if (viewOuter.isSelected())
			str.append("outer ");
			
		if (!viewClasses.isSelected() && !viewInterfaces.isSelected())
			str.append("classes and interfaces");
		else if (viewClasses.isSelected())
			str.append("classes");
		else if (viewInterfaces.isSelected())
			str.append("interfaces");
			
		if (!checkBox.isSelected())
			str.append(".");
		else
			str.append(" within previous results.");
			
		label.setText("Searching "+str.toString());
		Vector tempVec = new Vector();
		if (!checkBox.isSelected())
		{
			for (int i=0; i<project.getInterfaceVec().size(); i++)
				if (searchField.getText().trim().equals("") || SearchUtilities.stringMatches(searchField.getText(),((Interface)project.getInterfaceVec().elementAt(i)).getPgmDefn().getInterface_name(shortenJavaBox.isSelected(),shortenOtherBox.isSelected()),true,false,false))
					tempVec.add((Interface)project.getInterfaceVec().elementAt(i));
		}
		else
		{
			for (int i=0; i<alphaList.getModel().getSize(); i++)
				if (searchField.getText().trim().equals("") || SearchUtilities.stringMatches(searchField.getText(),((Interface)alphaList.getModel().getElementAt(i)).getPgmDefn().getInterface_name(shortenJavaBox.isSelected(),shortenOtherBox.isSelected()),true,false,false))
					tempVec.add((Interface)alphaList.getModel().getElementAt(i));
		}
			
		alphaList.setListData(tempVec);
		label.setText("Searched "+str.toString());
		this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
		
	/**
	 * Handles ActionEvents.
	 */
	public void actionPerformed(ActionEvent event)
	{
		ActionPerformedThread thread = new ActionPerformedThread(event,this);
		thread.start();
	}
	
	private void setEveryButtonIsSelected(boolean bol)
	{
		viewInner.setSelected(bol);
		viewOuter.setSelected(bol);
		viewAbstract.setSelected(bol);
		viewConcrete.setSelected(bol);
		viewClasses.setSelected(bol);
		viewInterfaces.setSelected(bol);
	}
	
	private void setEveryButtonIsEnabled(boolean bol)
	{
		viewInner.setEnabled(bol);
		viewOuter.setEnabled(bol);
		viewAbstract.setEnabled(bol);
		viewConcrete.setEnabled(bol);
		viewClasses.setEnabled(bol);
		viewInterfaces.setEnabled(bol);
	}
	
	/**
	 * Called by actionPerformed to handle ActionEvents in a separate thread split apart from the AWT thread.
	 */		
	public void actionPerformedInSeparateThread( ActionEvent event)
	{
		if (event.getActionCommand().equals("popup.singleUML"))
			InternalFrameUtilities.showSingleUMLDiagrams(getSelectedInterfaces(),desktop);
		else if (event.getActionCommand().equals("popup.shortenedSource"))
			InternalFrameUtilities.showShortenedSourceCode(getSelectedInterfaces(),desktop);
		else if (event.getActionCommand().equals("popup.sourceCode"))
			InternalFrameUtilities.showSourceCode(getSelectedInterfaces(),desktop);
		else if (event.getActionCommand().equals("popup.javadocs"))
			InternalFrameUtilities.showJavadocs(getSelectedInterfaces(),desktop);
		else if (event.getActionCommand().equals("associate.source.code"))
			InternalFrameUtilities.showAssociateSourceCodeWindow(getSelectedInterfaces(),desktop);
		else if (event.getActionCommand().equals("associate.javadocs"))
			InternalFrameUtilities.showAssociateJavadocsWindow(getSelectedInterfaces(),desktop);
		else if (event.getActionCommand().equals("properties.shorten"))
			fillList();
		else if (event.getActionCommand().equals("search"))
			searchList();
		else if (event.getActionCommand().equals("reset"))
		{
			this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			label.setText("Clearing the search specifications.");
			alphaList.setListData(project.getInterfaceVec());
			searchField.setText("");
			setEveryButtonIsSelected(false);
			label.setText("All classes and interfaces are displayed");
			this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			System.out.println("setting the cursor to the default cursor in reset");
		}
		else if (event.getActionCommand().startsWith("toolbar."))
		{
			if (event.getActionCommand().endsWith(".abstract"))
			{
				if (viewConcrete.isSelected() && viewAbstract.isSelected())
					viewConcrete.setSelected(false);
			}
			else if (event.getActionCommand().endsWith(".concrete"))
			{
				if (viewConcrete.isSelected() && viewAbstract.isSelected())
					viewAbstract.setSelected(false);
			}
			else if (event.getActionCommand().endsWith(".inner"))
			{
				if (viewInner.isSelected() && viewOuter.isSelected())
					viewOuter.setSelected(false);
			}
			else if (event.getActionCommand().endsWith(".outer"))
			{
				if (viewInner.isSelected() && viewOuter.isSelected())
					viewInner.setSelected(false);
			}
			else if (event.getActionCommand().endsWith(".classes"))
			{
				if (viewClasses.isSelected() && viewInterfaces.isSelected())
					viewInterfaces.setSelected(false);
			}
			else if (event.getActionCommand().endsWith(".interfaces"))
			{
				if (viewClasses.isSelected() && viewInterfaces.isSelected())
					viewClasses.setSelected(false);
			}
			String frontFilterStr = "";
			String frontStr = "";
			StringBuffer str = new StringBuffer();

			if (!checkBox.isSelected())
			{
				frontStr = "All";
				frontFilterStr = "Filtering all";
			}
			else
				frontFilterStr = "Filtering";
			
			if (viewAbstract.isSelected())
				str.append(" abstract");
			else if (viewConcrete.isSelected())
				str.append(" concrete");
			
			if (viewInner.isSelected())
				str.append(" inner");
			else if (viewOuter.isSelected())
				str.append(" outer");
			
			if (!viewClasses.isSelected() && !viewInterfaces.isSelected())
				str.append(" classes and interfaces");
			else if (viewClasses.isSelected())
				str.append(" classes");
			else if (viewInterfaces.isSelected())
				str.append(" interfaces");
			
			if (checkBox.isSelected())
				str.append(" from the previous results");
			
			String end = "";
			if (searchField.getText().equals(""))
				end = ".";
			else
				end = " that meet the search requirements.";
			
			//String front = String.valueOf(str.toString().charAt(0));
			//	front.toUpperCase();
			
			this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			label.setText(frontFilterStr+str.toString()+end);
			fillList();
			label.setText(frontStr+str.toString()+" are displayed"+end);
			this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
		else if (event.getActionCommand().equals("checkBox"))
		{
			// Don't do anything.  The next option selected (search queried) will applied to the 
			// whole list or the currently displayed interfaces, depending on whether the checkbox
			// is selected or not.
			// fillList(shortenJavaBox.isSelected(),shortenOtherBox.isSelected());
			// searchList();
		}
	}
	
	/**
	 * Handles changes to the list.
	 * @param e The event that is caught.
	 */
	public void valueChanged(ListSelectionEvent e)
	{
	}
	
	/**
	* This class handles what to do when the user wants to open
	* a popup menu.
	*/
	class PopupListener extends MouseAdapter 
	{
		/**
		 * Handles a mouse being pressed.
		 * @param e The event caught.
		 */
		public void mousePressed(MouseEvent e)
		{
			maybeShowPopup(e);
		}
		
		/**
		 * Handles a mouse being released.
		 * @param e The event caught.
		 */
		public void mouseReleased(MouseEvent e)
		{
			maybeShowPopup(e);
		}

		/**
		 * Handles showing the popup menu.
		 * @param e The event caught.
		 */
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
	 * Class which handles ActionEvents the same way as the actionPerformedInSeparateThread(ActionEvent) 
	 * method would except, in a separate thread.
	 * @author Dominic Kramer
	 */
	class ActionPerformedThread extends Thread
	{
		/** The ActionEvent to handle. */
		private ActionEvent e;
		/** The panel from which the user selects to perform an action. */
		private JPanel panel;
		/** Make a new ActionPerformedThread object. */
		public ActionPerformedThread(ActionEvent ev, JPanel pan)
		{
			e = ev;
			panel = pan;
		}
		/** Defines what to do in the new thread. */
		public void run()
		{
			actionPerformedInSeparateThread(e);
		}
	}
	
	public class AlphaListRenderer extends DefaultListCellRenderer
	{
		protected AlphabeticalListJPanel aljp;
				
		public AlphaListRenderer(AlphabeticalListJPanel panel)
		{
			aljp = panel;
		}
		
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
		{
			super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
			
			InterfaceDefn intfD =  ((Interface)value).getPgmDefn();
			setText( intfD.getInterface_name(aljp.getShortenJavaCheckBox().isSelected(),aljp.getShortenOtherCheckBox().isSelected()));
			
			ImageIcon icon = null;
			if (intfD.isClass())
			{
				if (intfD.isAbstract())
				{
					setToolTipText("Abstract Class "+intfD.getInterface_name());
					icon = SystemsManager.getImageIconOrBlankIcon("abstract_class_icon.png");
				}
				else
				{
					setToolTipText("Class "+intfD.getInterface_name());
					icon =SystemsManager. getImageIconOrBlankIcon("class_icon.png");
				}
				setFont(new Font("Plain",Font.PLAIN,12));
			}
			else
			{
				icon = SystemsManager.getImageIconOrBlankIcon("interface_icon.png");
				setFont(new Font("Italic",Font.ITALIC,12));
				setToolTipText("Interface "+intfD.getInterface_name());
			}
			
			if (icon != null)
				setIcon(icon);
				
//			setEnabled(list.isEnabled());
//			setOpaque(true);
			
			return this;
		}		
	}
	
	/**
	 * The Components in the array returned from this method are the Components that should have the 
	 * mouse use the waiting animation when an operation is in progress.
	 */
	public Component[] determineWaitingComponents()
	{
		Component[] compArr = new Component[3];
		compArr[0] = alphaList;
		compArr[1] = popup;
		compArr[2] = this;
		return compArr;
	}
}
