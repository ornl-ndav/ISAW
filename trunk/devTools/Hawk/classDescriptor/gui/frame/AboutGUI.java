/*
 * File:  AboutGUI.java
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
 * Revision 1.6  2004/05/26 19:27:01  kramer
 * I made the gui display information using a JTabbedPane.  It can now also
 * read the license from inside a jar file.
 *
 * Revision 1.5  2004/03/12 19:46:14  bouzekc
 * Changes since 03/10.
 *
 * Revision 1.3  2004/02/07 05:30:39  bouzekc
 * Made the path to the GPL relative to ISAW_HOME.
 *
 * Revision 1.2  2004/02/07 05:24:32  bouzekc
 * Changed all instances of "liscense" to "license".
 *
 * Revision 1.1  2004/02/07 05:08:48  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.gui.frame;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import devTools.Hawk.classDescriptor.tools.SystemsManager;

/**
 * This class is used to open a window displaying information about Hawk.  It contains
 * the author's name along with a button giving access to the liscense information.
 * @author Dominic Kramer
 */ 
public class AboutGUI extends JFrame implements ActionListener
{
	/**
	 * This is the window that holds the liscense information.  This window 
	 * is displayed when the user selects the button specifying to view the 
	 * liscense.
	 */
	private JFrame licenseFrame;
	
	/**
	 * General constructor which creates the window and everything in it.
	 */
	public AboutGUI()
	{
		setTitle("About Hawk");
		setSize(200,200);
		addWindowListener(new WindowDestroyer("main"));
		Container pane = getContentPane();
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
			JTextArea descArea = new JTextArea();
				descArea.setBackground(mainPanel.getBackground());
				descArea.setEditable(false);
				descArea.setLineWrap(false);
				descArea.setText("    Hawk is a developer's tool which assists in the analysis of the\n" +
										   "construction and implementation of java applications.  From an\n" +
										   "application's .class files, Hawk can extract the classes,\n" +
										   "interfaces, and abstract classes that compose the application.\n" +
										   "    Hawk then allows you to view data about each class or\n" +
										   "interface by viewing the class or interface's UML diagram,\n" +
										   "source code, javadocs, or a shortened version of the source\n" +
										   "code.\n" +
										   "    In addition, Hawk can print information about about classes\n" +
										   "and interfaces in a concise, compact document to a file.  Then,\n" +
										   "you can decide if you want to, for example, print the document\n" +
										   "or include it in a devolper's manual.");
			JScrollPane descScrollPane = new JScrollPane(descArea);
				
			JTextArea infoArea = new JTextArea();
				infoArea.setBackground(mainPanel.getBackground());
				infoArea.setEditable(false);
				infoArea.setLineWrap(false);
				
				StringBuffer infoBuffer = new StringBuffer();
				infoBuffer.append("Version:  "+SystemsManager.getVersion()+"\n");
				infoBuffer.append("Build Date:  "+SystemsManager.getBuildDate()+"\n");
				infoBuffer.append("\n");
				infoBuffer.append("Hawk is licensed under the GNU General Public Licensing Agreement\n");
				infoBuffer.append("Copyright \251 2003-2004 Dominic D. Kramer\n");
			infoArea.setText(infoBuffer.toString());
		JScrollPane infoScrollPane = new JScrollPane(infoArea);
		
			JTextArea creditsArea = new JTextArea();
				creditsArea.setBackground(mainPanel.getBackground());
				creditsArea.setEditable(false);
				creditsArea.setLineWrap(false);
				
				creditsArea.setText("Main Programmer:  "+SystemsManager.getAuthor()+"\n\n" +
											 "A special thanks to:\n" +
											 "  Dr. Dennis Mikkelson\n" +
											 "  Dr. Ruth Mikkelson\n" +
											 "  Chris Bouzek\n" +
											 "  Mike Miller and\n" +
											 "  Brent Serum\n\n" +
											 "You can contact the author of this program at\n"+SystemsManager.getAuthorsEmailAddress());
			JScrollPane creditsScrollPane = new JScrollPane(creditsArea);
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("General",infoScrollPane);
		tabbedPane.addTab("Description",descScrollPane);
		tabbedPane.addTab("Credits",creditsScrollPane);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			JButton viewButton = new JButton("View License");
			JButton closeButton = new JButton("Close"); 
			
			viewButton.addActionListener(this);
			closeButton.addActionListener(this);
			
			buttonPanel.add(viewButton);
			buttonPanel.add(closeButton);

		ImageIcon hawkIcon = new ImageIcon();
		URL imageURL = ClassLoader.getSystemClassLoader().getResource("devTools/Hawk/classDescriptor/pixmaps/about_hawk.png");		
		if (imageURL != null)
			hawkIcon = new ImageIcon(imageURL);
		
		JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			JLabel hawkLabel = new JLabel("Hawk");
			hawkLabel.setFont(new Font("Serif",Font.BOLD|Font.ITALIC,20));
			titlePanel.add(hawkLabel);
		
		mainPanel.add(titlePanel, BorderLayout.NORTH);
		mainPanel.add(new JLabel(hawkIcon), BorderLayout.WEST);
		mainPanel.add(tabbedPane, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		pane.add(mainPanel);
		setSize(475,321);
	}
	
	/**
	 * Handles ActionEvents
	 */
	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals("Close"))
		{
			dispose();
			if (licenseFrame != null)
				licenseFrame.dispose();
		}
		else if (event.getActionCommand().equals("View License"))
		{
			licenseFrame = new JFrame();
			
			licenseFrame.setTitle("GNU General Public Licensing Agreement");
			licenseFrame.setSize(550,550);
			licenseFrame.addWindowListener(new WindowDestroyer("license"));
			
			JTextArea license = new JTextArea();
			license.setEditable(false);
			String line = "";
			try
			{
				URL licenseURL = ClassLoader.getSystemClassLoader().getResource("devTools/Hawk/classDescriptor/gui/license/LICENSE.txt");
				InputStream inputStream = licenseURL.openStream();
		  /*
		  FilenameUtil.setForwardSlash( 
			SharedData.getProperty( "ISAW_HOME" ) +
			"/devTools/Hawk/classDescriptor/gui/license/LICENSE.txt") );
			*/
				int num = inputStream.read();
				StringBuffer buffer = new StringBuffer();
				while (num != -1)
				{
					buffer.append(String.valueOf((char)num));
					num = inputStream.read();
				}
				license.setText(buffer.toString());
			}
			catch (FileNotFoundException e)
			{
				JOptionPane opPane = new JOptionPane();
				JOptionPane.showMessageDialog(opPane,"The liscense agreement could not be found." 
					,"Error"
					,JOptionPane.ERROR_MESSAGE);
			}
			catch(Exception e)
			{
				SystemsManager.printStackTrace(e);
			}
			
			JScrollPane scrollpane = new JScrollPane(license);
			JPanel mainpanel = new JPanel();
			mainpanel.setLayout(new BorderLayout());
			
			JPanel buttonpanel = new JPanel();
			buttonpanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			
			JButton closebutton = new JButton("Close");
			closebutton.setActionCommand("licenseFrame.close");
			closebutton.addActionListener(this);
			buttonpanel.add(closebutton);
			
			mainpanel.add(scrollpane, BorderLayout.CENTER);
			mainpanel.add(buttonpanel, BorderLayout.SOUTH);
			
			licenseFrame.getContentPane().add(mainpanel);
			licenseFrame.setVisible(true);			
		}
		else if (event.getActionCommand().equals("licenseFrame.close"))
		{
			licenseFrame.dispose();
		}
	}
	
	/**
	 * This class is used to extend the actions that occur when the user 
	 * selects to close the AboutGUI window.
	 * @author Dominic Kramer
	 */
	private class WindowDestroyer extends WindowAdapter
	{
			/**
			 * Used to allow multiple windows use the same WindowDestroyer class.  
			 * Each window has its own name.
			 */
			private String name;
			
			/**
			 * Constructor allowing the window's name to be specified
			 * @param str The windows name
			 */
			public WindowDestroyer(String str)
			{
				name = str;
			}
			
			/**
			 * Handles WindowEvents that would be called when the window is closing.
			 */
			public void windowClosing(WindowEvent event)
			{
				if (name.equals("main"))	
					dispose();
				else if (name.equals("license"))
					licenseFrame.dispose();
			}
	}
}
