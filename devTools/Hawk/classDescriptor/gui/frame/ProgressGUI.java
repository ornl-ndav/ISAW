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
 * @author kramer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ProgressGUI extends JFrame implements ActionListener
{
	protected String title;
	protected JProgressBar bar;
	protected JButton button;
	protected JTextArea textArea;
	protected int value;
	protected int min;
	protected int max;
	protected boolean isCancelled;
	
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
	
	public void setString(String str)
	{
		bar.setString(str);
	}
	
	public void setProgressBarString(String str)
	{
		bar.setString(str);
	}
	
	public void setIndeterminante(boolean bol)
	{
		bar.setIndeterminate(bol);
	}
	
	public int getMaximum()
	{
		return bar.getMaximum();
	}
		
	public void setMaximum(int num)
	{
		bar.setMaximum(num);
	}
	
	public boolean isCancelled()
	{
		return isCancelled;
	}
	
	public void appendMessage(String str)
	{
		textArea.append(str+"\n");
		pack();
	}
	
	public void setText(String str)
	{
		textArea.setText(str);
		pack();
	}
	
	public String getText()
	{
		return textArea.getText();
	}
	
	public int getValue()
	{
		return value;
	}
	
	public void setValue(int num)
	{
		value = num;
		bar.setValue(num);
				
		pack();
	}
	
	public void isCompleted()
	{
		button.setText("Done");
		button.setActionCommand("done");
	}
	
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
	
	protected class WindowDestroyer extends WindowAdapter
	{
		public void windowClosing(WindowEvent event)
		{
			dispose();
		}
	}
}
