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
 * Revision 1.2  2004/03/12 19:46:16  bouzekc
 * Changes since 03/10.
 *
 */
 package devTools.Hawk.classDescriptor.gui.panel;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import devTools.Hawk.classDescriptor.gui.frame.HawkDesktop;
import devTools.Hawk.classDescriptor.gui.internalFrame.InternalFrameUtilities;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.modeledObjects.InterfaceDefn;
import devTools.Hawk.classDescriptor.modeledObjects.Project;
import devTools.Hawk.classDescriptor.tools.InterfaceUtilities;
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
public class AlphabeticalListJPanel extends JPanel implements ActionListener, ListSelectionListener
{
	//the following are declared here to allow actionPerformed() to find them
	//they are components placed on the GUI
		/** The Jlist that contains all of the interfacees and/or classes
		* in a project in alphabetical order
		*/
		protected JList alphaList;
		
		/**
		* The mode that describes list
		*/
		protected DefaultListModel alphaModel;
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
		protected Object component;

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
	public AlphabeticalListJPanel(Project PRO, boolean shortJava, boolean shortOther, HawkDesktop desk, Object comp)
	{
		//Now to define the Project that the information is obtained from
		//to fill in data in the GUI
			project = PRO;
			desktop = desk;
			component = comp;
			shortenJavaBox = new JCheckBox();
				shortenJavaBox.setSelected(shortJava);
			shortenOtherBox = new JCheckBox();
				shortenOtherBox.setSelected(shortOther);
			
		//Now to make the JList
			//now to make the list for the gui		
			//this model allows you to modify the list
			alphaModel = new DefaultListModel();
			alphaList = new JList(alphaModel);
			alphaList.addListSelectionListener(this);
			alphaList.setCellRenderer(new AlphaListRenderer());
			ToolTipManager.sharedInstance().registerComponent(alphaList);
			//the following only allows one item to be selected at a time
			alphaList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);		
		
		//now to fill the list
			fillList(shortJava, shortOther);
				
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
/*
		int[] indexArr = alphaList.getSelectedIndices();
		Vector tempVec = new Vector();
		for (int i=0; i<indexArr.length; i++)
		{
			if (indexArr[i] >= 0)
				tempVec.add((Interface)project.getInterfaceVec().elementAt(indexArr[i]));
		}
		Interface[] intfArr = new Interface[tempVec.size()];
		for (int i=0; i<tempVec.size(); i++)
			intfArr[i] = (Interface)tempVec.elementAt(i);
			
		return intfArr;
*/
		int[] indexArr = alphaList.getSelectedIndices();
		Vector tempVec = new Vector();
		for (int i=0; i<indexArr.length; i++)
		{
			if (indexArr[i] >= 0)
				tempVec.add(((InterfaceListItem)alphaModel.elementAt(indexArr[i])).getInterface());
		}
		Interface[] intfArr = new Interface[tempVec.size()];
		for (int i=0; i<tempVec.size(); i++)
			intfArr[i] = (Interface)tempVec.elementAt(i);
					
		return intfArr;
	}
	
	/**
	 * Fill the list with the classes and interfaces from the Project object from the field project.
	 * @param shortJava True if you want a name to be shortened if it is a java name.  For 
	 * example, java.lang.String would be shortened to String.
	 * @param shortOther True if you want a name to be shortened if it is a non-java name.
	 */
	public void fillList(boolean shortJava, boolean shortOther)
	{
		alphaModel.removeAllElements();
		//first check if the Vector of Interfaces needs to be alphabatized
			InterfaceUtilities.alphabatizeVector(project.getInterfaceVec(), shortJava, shortOther);
		
		for (int i = 0; i < (project.getInterfaceVec()).size(); i++)
		{
			//alphaModel.addElement((((((Interface)((project.getInterfaceVec()).elementAt(i))).getPgmDefn()).getInterface_name(shortJava, shortOther))) );
			alphaModel.addElement(new InterfaceListItem((Interface)project.getInterfaceVec().elementAt(i), shortJava, shortOther));
		}
	}

	/**
	 * Fill the list with the classes and interfaces from the Project object pro.
	 * @param pro The Project object whose classes and interfaces are displayed.
	 * @param shortJava True if you want a name to be shortened if it is a java name.  For 
	 * example, java.lang.String would be shortened to String.
	 * @param shortOther True if you want a name to be shortened if it is a non-java name.
	 */
	public void fillList(Project pro, boolean shortJava, boolean shortOther)
	{
		alphaModel.removeAllElements();
		//first check if the Vector of Interfaces needs to be alphabatized
			InterfaceUtilities.alphabatizeVector(pro.getInterfaceVec(), shortJava, shortOther);
		for (int i = 0; i < (pro.getInterfaceVec()).size(); i++)
		{
			alphaModel.addElement( (((((Interface)((pro.getInterfaceVec()).elementAt(i))).getPgmDefn()).getInterface_name(shortJava, shortOther))) );
		}
	}
	
	/**
	 * Handles ActionEvents.
	 */
	public void actionPerformed(ActionEvent event)
	{
		ActionPerformedThread thread = new ActionPerformedThread(event,this);
		thread.start();
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
		{
			fillList(shortenJavaBox.isSelected(), shortenOtherBox.isSelected());
			try
			{
				if (component instanceof JFrame)
					((JFrame)component).pack();
				else if (component instanceof JInternalFrame)
					((JInternalFrame)component).pack();
			}
			catch (Throwable e)
			{
				SystemsManager.printStackTraceToStandardOutput(e);
				try
				{
					fillList(shortenJavaBox.isSelected(), shortenOtherBox.isSelected());
				}
				catch (Throwable e2)
				{
					SystemsManager.printStackTraceToStandardOutput(e2);
				}
			}
			
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
			panel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			actionPerformedInSeparateThread(e);
			panel.setCursor(Cursor.getDefaultCursor());
		}
	}
	
	public class AlphaListRenderer extends DefaultListCellRenderer
	{
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
		{
			super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
			
			InterfaceDefn intfD = ((InterfaceListItem)value).getInterface().getPgmDefn();

/*			
			if (isSelected)
			{
				list.setForeground(list.getSelectionForeground());
				list.setBackground(list.getSelectionBackground());
			}
			else
			{
				list.setForeground(list.getForeground());
				list.setBackground(list.getBackground());
			}
*/			
			ImageIcon icon = null;
			
			if (intfD.isClass())
			{
				if (intfD.isAbstract())
				{
					setToolTipText("Abstract Class "+intfD.getInterface_name());
					icon = getImageIcon("devTools/Hawk/classDescriptor/pixmaps/abstract_class_icon.png");
				}
				else
				{
					setToolTipText("Class "+intfD.getInterface_name());
					icon = getImageIcon("devTools/Hawk/classDescriptor/pixmaps/class_icon.png");
				}
				setFont(new Font("Plain",Font.PLAIN,12));
			}
			else
			{
				icon = getImageIcon("devTools/Hawk/classDescriptor/pixmaps/interface_icon.png");
				setFont(new Font("Italic",Font.ITALIC,12));
				setToolTipText("Interface "+intfD.getInterface_name());
			}
			
			if (icon != null)
				setIcon(icon);
				
//			setEnabled(list.isEnabled());
//			setOpaque(true);
			
			return this;
		}
		
		private ImageIcon getImageIcon(String location)
		{
			URL imageURL = ClassLoader.getSystemClassLoader().getResource(location);
			ImageIcon icon = null;
			if (imageURL != null)
				icon = new ImageIcon(imageURL);
		
			return icon;
		}
	}
	
	public class InterfaceListItem
	{
		protected Interface intf;
		protected boolean shortenJava;
		protected boolean shortenOther;
		
		public InterfaceListItem(Interface intF, boolean shortJava, boolean shortOther)
		{
			intf = intF;
			shortenJava = shortJava;
			shortenOther = shortOther;
		}
		
		public Interface getInterface()
		{
			return intf;
		}
		
		public void setShorteningParameters(boolean shortJava, boolean shortOther)
		{
			shortenJava = shortJava;
			shortenOther = shortOther;
		}
		
		public String toString()
		{
			return intf.getPgmDefn().getInterface_name(shortenJava,shortenOther);
		}
	}
}
