/*
 * Created on Feb 27, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package devTools.Hawk.classDescriptor.gui.panel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

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
			alphaModel.addElement((((((Interface)((project.getInterfaceVec()).elementAt(i))).getPgmDefn()).getInterface_name(shortJava, shortOther))) );
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
	public void actionPerformed( ActionEvent event)
	{
		if (event.getActionCommand().equals("popup.singleUML"))
		{
			Interface[] intfArr = getSelectedInterfaces();
			if (intfArr.length == 0)
			{
				JOptionPane opPane = new JOptionPane();
					JOptionPane.showMessageDialog(opPane,"You need to select a class or interface to view \nits UML diagram."
						,"Note"
						,JOptionPane.INFORMATION_MESSAGE);
			}
			else
			{
				for (int i=0; i<intfArr.length; i++)
				{
					SingleUMLGUI singleUML = null;
					singleUML = new SingleUMLGUI(intfArr[i], intfArr[i].getPgmDefn().getInterface_name(true,false), true, false, desktop);
						
					if (singleUML != null)
					{
						singleUML.setVisible(true);
						desktop.getSelectedDesktop().add(singleUML);
						singleUML.setAsSelected(true);
					}
				}
			}
		}
		else if (event.getActionCommand().equals("popup.shortenedSource"))
		{
			Interface[] intfArr = getSelectedInterfaces();
			if (intfArr.length == 0)
			{
				JOptionPane opPane = new JOptionPane();
					JOptionPane.showMessageDialog(opPane,"You need to select a class or interface to view \nits shortened source code."
						,"Note"
						,JOptionPane.INFORMATION_MESSAGE);
			}
			else
			{
				for (int i=0; i<intfArr.length; i++)
				{
					ShortenedSourceGUI popupSsg = new ShortenedSourceGUI(intfArr[i], intfArr[i].getPgmDefn().getInterface_name(true,false), true, false,desktop);
					popupSsg.setVisible(true);
					desktop.getSelectedDesktop().add(popupSsg);
					popupSsg.setAsSelected(true);
				}
			}
		}
		else if (event.getActionCommand().equals("popup.sourceCode"))
		{
			Interface[] intfArr = getSelectedInterfaces();
			if (intfArr.length == 0)
			{
				JOptionPane opPane = new JOptionPane();
					JOptionPane.showMessageDialog(opPane,"You need to select a class or interface to view \nits source code."
						,"Note"
						,JOptionPane.INFORMATION_MESSAGE);
			}
			else
			{
				for (int i=0; i<intfArr.length; i++)
				{
					SourceCodeGUI popupSource = new SourceCodeGUI(intfArr[i], intfArr[i].getPgmDefn().getInterface_name(), desktop);
					popupSource.setVisible(true);
					desktop.getSelectedDesktop().add(popupSource);
					popupSource.setAsSelected(true);
				}
			}
		}
		else if (event.getActionCommand().equals("popup.javadocs"))
		{
			Interface[] intfArr = getSelectedInterfaces();
			if (intfArr.length == 0)
			{
				JOptionPane opPane = new JOptionPane();
					JOptionPane.showMessageDialog(opPane,"You need to select a class or interface to view \nits UML diagram."
						,"Note"
						,JOptionPane.INFORMATION_MESSAGE);
			}
			else
			{
				for (int i=0; i<intfArr.length; i++)
				{
					JavadocsGUI javagui = new JavadocsGUI(intfArr[i], intfArr[i].getPgmDefn().getInterface_name(), desktop);
					javagui.setVisible(true);
					desktop.getSelectedDesktop().add(javagui);
					javagui.setAsSelected(true);
				}
			}
		}
		else if (event.getActionCommand().equals("properties.shorten"))
		{
			fillList(shortenJavaBox.isSelected(), shortenOtherBox.isSelected());
			if (component instanceof JFrame)
				((JFrame)component).pack();
			else if (component instanceof JInternalFrame)
				((JInternalFrame)component).pack();
		}
		else if (event.getActionCommand().equals("associate.source.code"))
		{
			Interface[] intfArr = getSelectedInterfaces();
			if (intfArr.length == 0)
			{
				JOptionPane opPane = new JOptionPane();
				JOptionPane.showMessageDialog(opPane,
					"You need to select an interface to associate a source code file with it",
					"Note",
					JOptionPane.INFORMATION_MESSAGE);
			}
			else
			{
				if (intfArr.length == 1)
				{
					FileAssociationGUI sourceGUI = new FileAssociationGUI(intfArr[0], FileAssociationManager.JAVASOURCE);
					sourceGUI.setVisible(true);
				}
				else
				{
					Vector intfVec = new Vector();
					for (int i=0; i<intfArr.length; i++)
						intfVec.add(intfArr[i]);
				
					FileAssociationGUI sourceGUI = new FileAssociationGUI(intfVec, FileAssociationManager.JAVASOURCE,"Associate Java Source Files for the Selected Classes and Interfaces");
					sourceGUI.setVisible(true);
				}
			}
		}
		else if (event.getActionCommand().equals("associate.javadocs"))
		{
			Interface[] intfArr = getSelectedInterfaces();
			if (intfArr.length == 0)
			{
				JOptionPane opPane = new JOptionPane();
				JOptionPane.showMessageDialog(opPane,
					"You need to select an interface to associate a javadoc file with it",
					"Note",
					JOptionPane.INFORMATION_MESSAGE);
			}
			else
			{
				if (intfArr.length == 1)
				{
					FileAssociationGUI sourceGUI = new FileAssociationGUI(intfArr[0], FileAssociationManager.JAVADOCS);
					sourceGUI.setVisible(true);
				}
				else
				{
					Vector intfVec = new Vector();
					for (int i=0; i<intfArr.length; i++)
						intfVec.add(intfArr[i]);
				
					FileAssociationGUI sourceGUI = new FileAssociationGUI(intfVec, FileAssociationManager.JAVADOCS,"Associate Javadoc Files for the Selected Classes and Interfaces");
					sourceGUI.setVisible(true);
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
}
