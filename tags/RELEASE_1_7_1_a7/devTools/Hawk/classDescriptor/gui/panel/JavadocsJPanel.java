/*
 * File:  JavadocsJPanel.java
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
 * Revision 1.2  2004/05/26 20:06:06  kramer
 * Added forward, back, refresh, home, etc. buttons to the gui.
 * Also added a field to type the URL to visit.
 * Also added a hyperlink listener.
 *
 * Revision 1.1  2004/03/12 19:48:22  bouzekc
 * Added to CVS.
 *
 */
 package devTools.Hawk.classDescriptor.gui.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import devTools.Hawk.classDescriptor.gui.ExternallyControlledFrame;
import devTools.Hawk.classDescriptor.gui.MouseNotifiable;
import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.tools.SystemsManager;

/**
 * This is a specialized JPanel that displays the javadoc information for a class or interface.  
 * This class is under construction.
 * @author Dominic Kramer
 */
public class JavadocsJPanel extends JPanel implements ActionListener, MouseNotifiable
{
	/**
	 * The pane to add the javadocs file to.
	 */
	protected JEditorPane htmlPane;
	/**
	 * The Interface whose javadocs file is to be displays.
	 */
	protected Interface selectedInterface;
	/**
	 * A Vector of the URLs visited.
	 */
	protected Vector urlVec;
	/**
	 * The index of the URL currently being viewed.
	 */
	protected int currentIndex;
	/**
	 * The first URL that was loaded on the page.
	 */
	protected URL home;
	/**
	 * The button that goes to the next URL.
	 */
	protected JButton nextButton;
	/**
	 * The button that goes to the previous URL.
	 */
	protected JButton previousButton;
	/**
	 * The button that goes to the home URL.
	 */
	protected JButton homeButton;
	/**
	 * The button that refreshes the screen.
	 */
	protected JButton refreshButton;
	/**
	 * The frame that contains this panel.  By being an ExternallyControlledFrame (implementing 
	 * the ExternallyControlled interface), this panel can handle window actions such as closing 
	 * or resizing the window.
	 */
	protected ExternallyControlledFrame frame;
	/**
	 * This contains the location of the next page to load.
	 */
	protected JTextField locationField;
	
	/**
	 * Create a new JavadocsGUI.
	 * @param INT The Interface whose javadocs file is to be displayed.
	 * @param title The title of the window.
	 * @param desk The HawkDesktop that this window is on.
	 */
	public JavadocsJPanel(Interface INT,ExternallyControlledFrame conFrame)
	{
		setLayout(new GridLayout(1,1));
		
		//now to instantiate selectedInterface
			selectedInterface = INT;
			frame = conFrame;
		
		//now to make the toolbar for the buttons
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
			previousButton = getIconedJButton("previous.png","Back");
				previousButton.addActionListener(this);
				previousButton.setActionCommand("previous");
				toolbar.add(previousButton);
			nextButton = getIconedJButton("next.png","Next");
				nextButton.addActionListener(this);
				nextButton.setActionCommand("next");
				toolbar.add(nextButton);
			refreshButton = getIconedJButton("refresh.png","Refresh");
				refreshButton.addActionListener(this);
				refreshButton.setActionCommand("refresh");
				toolbar.add(refreshButton);
			homeButton = getIconedJButton("home.png","Home");
				homeButton.addActionListener(this);
				homeButton.setActionCommand("home");
				toolbar.add(homeButton);
			locationField = new JTextField("",20);
				toolbar.add(locationField);
			JButton goButton = getIconedJButton("go.png","Go");
				goButton.addActionListener(this);
				goButton.setActionCommand("go");
				toolbar.add(goButton);
				
		//now to make the list which contains the previously visited URLs
			urlVec = new Vector();
			currentIndex = -1;  //then when a URL is added currentIndex is incremented to 0
		//now to create the area for placing the javadocs in html format
			htmlPane = new JEditorPane();     //"text/html", INT.getJavadocAsString());
			htmlPane.setEditable(false);
			try
			{
				home = new URL("file://"+INT.getJavadocsFileName());
				setPage(home);
			}
			catch (MalformedURLException e)
			{
				htmlPane.setText("The URL you specified has a syntax error.  Make sure the URL starts with http:// " +
					"when specifying pages on the world wide web.  Alternately, the URL should start with file:// when " +
					"specifying a local file.");
			}
			catch (NullPointerException e)
			{
				htmlPane.setText("The page "+home.toString()+" could not be displayed.");
			}
			catch (Throwable t)
			{
				SystemsManager.printStackTrace(t);
			}
			htmlPane.addHyperlinkListener(new JavadocsJPanelHyperlinkListener());
		//now to create the JScrollPane to put the JEditorPane on
			JScrollPane scrollPane = new JScrollPane(htmlPane);
			
		JPanel wrapperPanel = new JPanel(new BorderLayout());
			wrapperPanel.add(toolbar, BorderLayout.NORTH);
			wrapperPanel.add(scrollPane, BorderLayout.CENTER);
		add(wrapperPanel);
	}
	
	/**
	 * Creates a JButton that has the ImageIcon on it named 'name' from the pixmap directory.  If 
	 * the file does not exist, a JButton with the text 'text' is returned.
	 * Note:  the pixmap directory is specified by SystemsManager.getPixmapDirectory()
	 */
	private JButton getIconedJButton(String name, String text)
	{
		ImageIcon icon = SystemsManager.getImageIconOrNull(name);
		if (icon != null)
			return new JButton(icon);
		else
			return new JButton(text);
	}
	
	/**
	 * Get the basic menubar associated with this panel.
	 */
	public JMenuBar createMenuBar()
	{
		//Now to make the JMenuBar
			JMenuBar javadocsMenuBar = new JMenuBar();
				JMenu fileMenu = new JMenu("File");
					JMenuItem closeItem = new JMenuItem("Close");
					closeItem.setActionCommand("Close");
					closeItem.addActionListener(this);
				fileMenu.add(closeItem);
			javadocsMenuBar.add(fileMenu);
			
		return javadocsMenuBar;
	}
	
	private boolean previousURLExists()
	{
		return (currentIndex>0);
	}
	
	private boolean nextURLExists()
	{
		return ((currentIndex<(urlVec.size()-1))&&(currentIndex>=0));	
	}
	
	private void refreshButtons()
	{
		System.out.println("Refreshing the buttons.");
		System.out.println("  currentIndex="+currentIndex);
		previousButton.setEnabled(previousURLExists());
		nextButton.setEnabled(nextURLExists());
	}

	
	private URL getPreviousURL()
	{
		if (previousURLExists())
			return (URL)urlVec.elementAt(currentIndex-1);
		else
			return null;
	}
	
	private URL getNextURL()
	{
		if (nextURLExists())
			return (URL)urlVec.elementAt(currentIndex+1);
		else
			return null;	
	}
	
//	private URL getCurrentURL()
//	{
//		if ((currentIndex<0) || (currentIndex>=urlVec.size()))
//			return null;
//		else
//			return (URL)urlVec.elementAt(currentIndex);	
//	}
	
	public void setPage(URL urlLocation)
	{
		setPage(urlLocation.toString());
	}
	
	public void setPage(String location)
	{
		setPage(htmlPane,location);
	}
	
	public void setPage(JEditorPane pane, String location)
	{
		try
		{
			pane.setPage(location);
			urlVec.add(new URL(location));
			currentIndex++;
		}
		catch (MalformedURLException e)
		{
			pane.setText("The URL you specified has a syntax error.  Make sure the URL starts with http:// " +
				"when specifying pages on the world wide web.  Alternately, the URL should start with file:// when " +
				"specifying a local file.");
		}
		catch (NullPointerException e)
		{
			pane.setText("The page "+locationField.getText()+" could not be displayed.");
		}
		catch (FileNotFoundException e)
		{
			pane.setText("The location "+location+" could not be found.");
		}
		catch (UnknownHostException e)
		{
			pane.setText("Could not connect to "+location);
		}
		catch (Throwable t)
		{
			SystemsManager.printStackTrace(t);
		}
		finally
		{
			locationField.setText(location);
			refreshButtons();
		}
	}
	
//	public void addURL(URL url)
//	{
//		urlVec.add(url);
//		currentIndex++;
//	}
	
	/**
	 * Handles ActionEvents.
	 */
	public void actionPerformed(ActionEvent event)
	{
		System.out.println("In actionPerformed: event="+event.getActionCommand());
		try
		{
			if (event.getActionCommand().equals("Close"))
				frame.dispose();
			else if (event.getActionCommand().equals("previous"))
				setPage(getPreviousURL());
			else if (event.getActionCommand().equals("next"))
				setPage(getNextURL());
			else if (event.getActionCommand().equals("refresh"))
				setPage(htmlPane.getPage());
			else if (event.getActionCommand().equals("home"))
				setPage(home);
			else if (event.getActionCommand().equals("go"))
				setPage(locationField.getText());
		}
		catch (Throwable t)
		{
			SystemsManager.printStackTrace(t);
		}
		refreshButtons();
	}
		
	/**
	 * The Components in the array returned from this method are the Components that should have the 
	 * mouse use the waiting animation when an operation is in progress.
	 */
	public Component[] determineWaitingComponents()
	{
		Component[] compArr = new Component[3];
		compArr[0] = htmlPane;
		compArr[1] = frame.getControlledComponent();
		compArr[2] = this;
		return compArr;
	}
	
  class JavadocsJPanelHyperlinkListener implements HyperlinkListener
  {
	 public void hyperlinkUpdate(HyperlinkEvent e)
	 {
		  if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
		  {
			  JEditorPane pane = (JEditorPane) e.getSource();
			  if (e instanceof HTMLFrameHyperlinkEvent)
			  {
			  	HTMLFrameHyperlinkEvent event = (HTMLFrameHyperlinkEvent)e;
			  	HTMLDocument document = (HTMLDocument)pane.getDocument();
			  	document.processHTMLFrameHyperlinkEvent(event);
			  	setPage(e.getURL().toString());
			  }
			  else
			  {
				  try
				  {
				  	setPage(pane,e.getURL().toString());
				  }
			  	  catch (Throwable t)
			  	  {
		  			SystemsManager.printStackTrace(t);
		  	  	  }
			  }
		  }
	  }
	}
}
