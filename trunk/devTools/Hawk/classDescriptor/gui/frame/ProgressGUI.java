/*
 * File:  ProgressGUI.java
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
 * Revision 1.3  2004/03/12 19:46:15  bouzekc
 * Changes since 03/10.
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * This class makes a window that displays the progress of a working process.
 * @author Dominic Kramer
 */
public class ProgressGUI extends JFrame implements ActionListener
{
	/**
	 * The window's title.
	 */
	protected String title;
	/**
	 * The progress bar which displays how far the progress has gone.
	 */
	protected JProgressBar bar;
	/**
	 * The button that can cancel the process or close the window depending on if the 
	 * process is done or not.
	 */
	protected JButton button;
	/**
	 * The text area that displays what the process is currently doing or other information about 
	 * the process.
	 */
	protected JTextArea textArea;
	/**
	 * The JProgressBar's current value.
	 */
	protected int value;
	/**
	 * The JProgressBar's minimum value.
	 */
	protected int min;
	/**
	 * The JProgressBar's maximum value.
	 */
	protected int max;
	/**
	 * Set to true if the process is supposed to be cancelled.
	 */
	protected boolean isCancelled;
	
	/**
	 * Create a new ProgressGUI object.
	 * @param MIN The JProgressBar's minimum value.
	 * @param MAX The JProgressBar's maximum value.
	 * @param TITLE The window's title.
	 */
	public ProgressGUI(int MIN, int MAX, String TITLE)
	{
		isCancelled = false;
		
		value = MIN;
		min = MIN;
		max = MAX;
		title = TITLE;
		
		setTitle(TITLE);
		setSize(200,200);
		
		addWindowListener(new WindowDestroyer());
		
		Container pane = getContentPane();
		
		//now to make the panels
			JPanel mainPanel = new JPanel();
				mainPanel.setLayout(new BorderLayout());
			JPanel barPanel = new JPanel();
				barPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			JPanel textPanel = new JPanel();
				textPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			JPanel buttonPanel = new JPanel();
				buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		//now to work on the barPanel
			bar = new JProgressBar(min, max);
			bar.setValue(0);
			bar.setStringPainted(true);
			barPanel.add(bar);
		
		//now to work on the textPanel
			textArea = new JTextArea(5,30);
			JScrollPane scrollPane = new JScrollPane(textArea);
			textPanel.add(scrollPane);
		
		//now to work on the buttonPanel
			button = new JButton("Cancel");
			button.setActionCommand("cancelled");
			button.addActionListener(this);
			buttonPanel.add(button);
		
		//now to add all of the panels to the mainPanel
			mainPanel.add(barPanel, BorderLayout.NORTH);
			mainPanel.add(textPanel, BorderLayout.CENTER);
			mainPanel.add(buttonPanel, BorderLayout.SOUTH);
			pane.add(mainPanel);
			pack();
	}
	
	/**
	 * Set the text in the JProgressBar.
	 * @param str The text.
	 */
	public void setProgressBarString(String str)
	{
		bar.setString(str);
	}
	
	/**
	 * Set the JProgressBar state as an indeterminant state if bol is equal to true.  
	 * The JProgressBar is in a normal state if bol is equal to false.
	 * @param bol Either true or false.
	 */	
	public void setIndeterminante(boolean bol)
	{
		bar.setIndeterminate(bol);
	}
	
	/**
	 * Get the JProgressBar's maximum value.
	 * @return The maximum value.
	 */
	public int getMaximum()
	{
		return bar.getMaximum();
	}
	
	/**
	 * Set the JProgressBar's maximum value.
	 * @param num The maximum value.
	 */
	public void setMaximum(int num)
	{
		bar.setMaximum(num);
	}
	
	/**
	 * Determine if the process is cancelled.
	 * @return True if the process is cancelled.
	 */
	public boolean isCancelled()
	{
		return isCancelled;
	}
	
	/**
	 * Append the String str to the JTextArea.
	 * @param str The text.
	 */
	public void appendMessage(String str)
	{
		textArea.append(str+"\n");
		pack();
	}
	
	/**
	 * Set the JTextArea's text.
	 * @param str The text.
	 */
	public void setText(String str)
	{
		textArea.setText(str);
		pack();
	}
	
	/**
	 * Get the JTextArea's text.
	 * @return The text.
	 */
	public String getText()
	{
		return textArea.getText();
	}
	
	/**
	 * Get the JProgressBar's current value.
	 * @return The current value.
	 */
	public int getValue()
	{
		return value;
	}
	
	/**
	 * Set the JProgressBar's current value.
	 * @param num The current value.
	 */
	public void setValue(int num)
	{
		value = num;
		bar.setValue(num);
				
		pack();
	}
	
	/**
	 * Used to signal that the process is completed.
	 */
	public void isCompleted()
	{
		button.setText("Done");
		button.setActionCommand("done");
	}
	
	/**
	 * Handle ActionEvents.
	 */
	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals("cancelled"))
		{
			appendMessage("Cancelled");
			isCancelled = true;
			button.setText("Done");
			button.setActionCommand("done");
		}
		else if (event.getActionCommand().equals("done"))
		{
			dispose();
		}
	}
	
	/**
	 * Class which handles closing the window.
	 * @author Dominic Kramer
	 */
	protected class WindowDestroyer extends WindowAdapter
	{
		/**
		 * Handles closing the window.
		 * @param event The event that is sent to close the window.
		 */
		public void windowClosing(WindowEvent event)
		{
			dispose();
		}
	}
}
