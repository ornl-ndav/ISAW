/*
 * File:  InternalFrameUtilities.java
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
 * Revision 1.2  2004/05/26 19:46:46  kramer
 * Added Javadoc documentation.
 *
 * Revision 1.1  2004/03/12 19:47:40  bouzekc
 * Added to CVS.
 *
 */
 package devTools.Hawk.classDescriptor.gui.internalFrame;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import devTools.Hawk.classDescriptor.gui.MouseNotifiable;
import devTools.Hawk.classDescriptor.gui.frame.FileAssociationGUI;
import devTools.Hawk.classDescriptor.gui.frame.HawkDesktop;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.tools.FileAssociationManager;

/**
 * This class contains methods which display DesktopInternalFrame objects inside a HawkDesktop.  
 * This class exists so that code for displaying the DesktopInternalFrame objects would not have to 
 * be re-written multiple times throughout multiple classes.
 * @author Dominic Kramer
 */
public class InternalFrameUtilities
{
	/** The constructor is private because all of the methods are static. */
	private InternalFrameUtilities() {}
	
	/**
	 * Displays SingleUMLGUI windows in the HawkDesktop desktop for each Interface object given.
	 * @param intfArr The Interface objects used.
	 * @param desktop The HawkDesktop to place the windows in.
	 */
	public static void showSingleUMLDiagrams(Interface[] intfArr, HawkDesktop desktop)
	{
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
	
	/**
	 * Displays a SingleUMLGUI window in the HawkDesktop desktop for the Interface object given.
	 * @param intf The Interface object used.
	 * @param desktop The HawkDesktop to place the windows in.
	 */
	public static void showSingleUMLDiagram(Interface intf, HawkDesktop desktop)
	{
		showSingleUMLDiagrams(getInterfaceArrayFromInterface(intf),desktop);
	}
	
	/**
	 * Displays ShortenedSourceGUI windows in the HawkDesktop desktop for each Interface object given.
	 * @param intfArr The Interface objects used.
	 * @param desktop The HawkDesktop to place the windows in.
	 */
	public static void showShortenedSourceCode(Interface[] intfArr, HawkDesktop desktop)
	{
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
	
	/**
	 * Displays a ShortenedSourceGUI window in the HawkDesktop desktop for the Interface object given.
	 * @param intf The Interface object used.
	 * @param desktop The HawkDesktop to place the windows in.
	 */
	public static void showShortenedSourceCode(Interface intf, HawkDesktop desktop)
	{
		showShortenedSourceCode(getInterfaceArrayFromInterface(intf),desktop);
	}
	
	/**
	 * Displays SourceCodeGUI windows in the HawkDesktop desktop for each Interface object given.
	 * @param intfArr The Interface objects used.
	 * @param desktop The HawkDesktop to place the windows in.
	 */
	public static void showSourceCode(Interface[] intfArr, HawkDesktop desktop)
	{
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
	
	/**
	 * Displays a SourceCodeGUI window in the HawkDesktop desktop for the Interface object given.
	 * @param intf The Interface object used.
	 * @param desktop The HawkDesktop to place the windows in.
	 */
	public static void showSourceCode(Interface intf, HawkDesktop desktop)
	{
		showSourceCode(getInterfaceArrayFromInterface(intf),desktop);
	}
	
	/**
	 * Displays JavadocsGUI windows in the HawkDesktop desktop for each Interface object given.
	 * @param intfArr The Interface objects used.
	 * @param desktop The HawkDesktop to place the windows in.
	 */
	public static void showJavadocs(Interface[] intfArr, HawkDesktop desktop)
	{
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
	
	/**
	 * Displays a JavadocsGUI window in the HawkDesktop desktop for the Interface object given.
	 * @param intf The Interface object used.
	 * @param desktop The HawkDesktop to place the windows in.
	 */
	public static void showJavadocs(Interface intf, HawkDesktop desktop)
	{
		showJavadocs(getInterfaceArrayFromInterface(intf),desktop);
	}
	
	/**
	 * Displays FileAssociationGUI windows used for associating java source files in the HawkDesktop desktop for the Interface objects given.
	 * @param intfArr The Interface objects used.
	 * @param desktop The HawkDesktop to place the windows in.
	 */
	public static void showAssociateSourceCodeWindow(Interface[] intfArr, HawkDesktop desktop)
	{
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
	
	/**
	 * Displays a FileAssociationGUI window used for associating java source files in the HawkDesktop desktop for the Interface object given.
	 * @param intf The Interface object used.
	 * @param desktop The HawkDesktop to place the windows in.
	 */
	public static void showAssociateSourceCodeWindow(Interface intf, HawkDesktop desktop)
	{
		showAssociateSourceCodeWindow(getInterfaceArrayFromInterface(intf),desktop);
	}
	
	/**
	 * Displays FileAssociationGUI windows used for associating javadoc files in the HawkDesktop desktop for the Interface objects given.
	 * @param intfArr The Interface objects used.
	 * @param desktop The HawkDesktop to place the windows in.
	 */
	public static void showAssociateJavadocsWindow(Interface[] intfArr, HawkDesktop desktop)
	{
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
	
	/**
	 * Displays a FileAssociationGUI window used for associating javadocs files in the HawkDesktop desktop for the Interface object given.
	 * @param intf The Interface object used.
	 * @param desktop The HawkDesktop to place the windows in.
	 */
	public static void showAssociateJavadocsWindow(Interface intf, HawkDesktop desktop)
	{
		showAssociateJavadocsWindow(getInterfaceArrayFromInterface(intf),desktop);
	}
	
	/**
	 * Constructs a JMenu holding options to view a SingleUMLGUI, ShortenedSourceGUI, JavadocsGUI, or SourceCodeGUI for an Interface object.
	 * @param act The object that will be listening for ActionEvents.
	 * @param showUMLOption True if the option to view a SingleUMLGUI should be given.
	 * @param showShortenedSourceOption True if the option to view a ShortenedSourceGUI should be given.
	 * @param showJavadocsOption True if the option to view a JavadocsGUI should be given.
	 * @param showSourceOption True if the option to view a SourceCodeGUI should be given.
	 * @return The corresponding JMenu.
	 */
	public static JMenu constructViewMenu(ActionListener act, boolean showUMLOption, boolean showShortenedSourceOption, boolean showJavadocsOption, boolean showSourceOption)
	{
		JMenu viewMenu = new JMenu("View");
			if (showUMLOption)
			{
				JMenuItem viewUML = new JMenuItem("Single UML");
					viewUML.setActionCommand("view.uml");
					viewUML.addActionListener(act);
					viewMenu.add(viewUML);
			}
			if (showShortenedSourceOption)
			{
				JMenuItem viewShortenedSource = new JMenuItem("Shortened Source Code");
					viewShortenedSource.setActionCommand("view.shortenedSource");
					viewShortenedSource.addActionListener(act);
					viewMenu.add(viewShortenedSource);
			}
			if (showJavadocsOption)
			{
				JMenuItem viewJavadocs = new JMenuItem("Javadocs");
					viewJavadocs.setActionCommand("view.javadocs");
					viewJavadocs.addActionListener(act);
					viewMenu.add(viewJavadocs);
			}
			if (showSourceOption)
			{
				JMenuItem viewSource = new JMenuItem("Source Code");
					viewSource.setActionCommand("view.sourceCode");
					viewSource.addActionListener(act);
					viewMenu.add(viewSource);
			}
		return viewMenu;
	}
	
	/**
	 * Process the ActionEvent that would be thrown from one of the JMenuItem objects from the JMenu created by the method 
	 * constructViewMenu(ActionListener act, boolean showUMLOption, boolean showShortenedSourceOption, boolean showJavadocsOption, boolean showSourceOption).  
	 * This method will display the appropriate DesktopInternal from on the HawkDesktop desktop.  Here are the pairings for the result from 
	 * event.getActionCommand() and the corresponding action performed:<br>
	 * "view.uml"  Display a SingleUMLGUI window<br>
	 * "view.shortenedSource"  Display a ShortenedSourceGUI window<br>
	 * "view.javadocs"  Displays a JavadocsGUI window<br>
	 * "view.sourceCode"  Displays a SourceCodeGUI window<br>
	 * @param event The ActionEvent to process.
	 * @param intf The Interface object whose information is to be displayed.
	 * @param desktop The HawkDesktop onto which the window will be displayed.
	 */
	public static void processActionEventFromViewMenu(final ActionEvent event, final Interface intf, final HawkDesktop desktop, final MouseNotifiable mouseNot)
	{
		SwingUtilities.invokeLater(new Runnable()
		  {
		  	public void run()
		  	{
				InternalFrameUtilitiesThread thread = new InternalFrameUtilitiesThread(event,intf,desktop,mouseNot);
				thread.start();
		  	}
		  });
	}
	
	/**
	 * Used in this class to make an array of one Interface object from the Interface object given.
	 * @param intf The Interface object to use.
	 * @return intf as the only element in a one element array of Interface objects.
	 */
	private static Interface[] getInterfaceArrayFromInterface(Interface intf)
	{
		Interface[] intfArr = new Interface[1];
		intfArr[0] = intf;
		return intfArr;
	}
	
	private static class InternalFrameUtilitiesThread extends Thread
	{
		private ActionEvent event;
		private Interface intf;
		private HawkDesktop desktop;
		private MouseNotifiable mouseNot;
		public InternalFrameUtilitiesThread(ActionEvent actEvent, Interface intFace, HawkDesktop hDesktop, MouseNotifiable mouseN)
		{
			event = actEvent;
			intf = intFace;
			desktop = hDesktop;
			mouseNot = mouseN;
		}
		
		public void run()
		{
			for (int i=0; i<mouseNot.determineWaitingComponents().length; i++)
				mouseNot.determineWaitingComponents()[i].setCursor(new Cursor(Cursor.WAIT_CURSOR));
			if (event.getActionCommand().equals("view.uml"))
				showSingleUMLDiagram(intf,desktop);
			else if (event.getActionCommand().equals("view.shortenedSource"))
				showShortenedSourceCode(intf,desktop);
			else if (event.getActionCommand().equals("view.javadocs"))
				showJavadocs(intf,desktop);
			else if (event.getActionCommand().equals("view.sourceCode"))
				showSourceCode(intf,desktop);
			for (int i=0; i<mouseNot.determineWaitingComponents().length; i++)
				mouseNot.determineWaitingComponents()[i].setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}
}
