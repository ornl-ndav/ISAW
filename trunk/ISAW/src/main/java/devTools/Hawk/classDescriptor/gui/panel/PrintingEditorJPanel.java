/*
 * File:  PrintingEditorJPanel.java
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
 */
 package devTools.Hawk.classDescriptor.gui.panel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

import devTools.Hawk.classDescriptor.tools.SystemsManager;
import devTools.Hawk.classDescriptor.tools.printing.PrintableDocumentConstants;

/**
 * @author kramer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class PrintingEditorJPanel extends JPanel implements ActionListener, CaretListener
{
	protected JTextPane textPane;
	protected JToggleButton boldButton;
	protected JToggleButton italicButton;
	protected JToggleButton underlineButton;
	
	protected SimpleAttributeSet plain;
	protected SimpleAttributeSet bold;
	
	public PrintingEditorJPanel()
	{
		textPane = new JTextPane();
			textPane.addCaretListener(this);
		JScrollPane scrollPane = new JScrollPane(textPane);
			//	this is the default style which is the root of the style hierarchy
			/*
				Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
				StyleContext.NamedStyle bold = (StyleContext.NamedStyle)(textPane.getStyledDocument().addStyle(PrintableDocumentConstants.BOLD,def));
					bold.setName("bold");
					StyleConstants.setBold(bold,true);
				Style italic = textPane.getStyledDocument().addStyle(PrintableDocumentConstants.ITALIC,def);
					StyleConstants.setItalic(italic,true);
				Style underline = textPane.getStyledDocument().addStyle(PrintableDocumentConstants.UNDERLINE,def);
					StyleConstants.setUnderline(underline,true);
				Style plain = textPane.getStyledDocument().addStyle(PrintableDocumentConstants.PLAIN,def);
					StyleConstants.setBold(plain,false);
					StyleConstants.setItalic(plain,false);
					StyleConstants.setUnderline(plain,false);
			*/
			plain = new SimpleAttributeSet();
			bold = new SimpleAttributeSet();
				StyleConstants.setBold(bold,true);
			
		
		JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
		toolbar.setFloatable(false);
			JButton openButton = getIconedButton("stock-open.png","Open");
				openButton.setActionCommand("open");
				openButton.addActionListener(this);
			JButton saveButton = getIconedButton("filesave.png","Save");
				saveButton.setActionCommand("save");
				saveButton.addActionListener(this);
				
				toolbar.add(openButton);
				toolbar.add(saveButton);
				toolbar.add(new JToolBar.Separator());
				
			JButton cutButton = getIconedButton("cut.png","Cut");
				cutButton.setActionCommand("cut");
				cutButton.addActionListener(this);
			JButton copyButton = getIconedButton("copy.png","Copy");
				copyButton.setActionCommand("copy");
				copyButton.addActionListener(this);
			JButton pasteButton = getIconedButton("paste.png","Paste");
				pasteButton.setActionCommand("paste");
				pasteButton.addActionListener(this);
				
				toolbar.add(cutButton);
				toolbar.add(copyButton);
				toolbar.add(pasteButton);
				toolbar.add(new JToolBar.Separator());
			
			boldButton = getIconedToggleButton("bold.png","Bold");
				boldButton.setActionCommand("bold");
				boldButton.addActionListener(this);
			italicButton = getIconedToggleButton("italics.png","Italic");
				italicButton.setActionCommand("italic");
				italicButton.addActionListener(this);
			underlineButton = getIconedToggleButton("underline.png","Underline");
				underlineButton.setActionCommand("underline");
				underlineButton.addActionListener(this);
				
				toolbar.add(boldButton);
				toolbar.add(italicButton);
				toolbar.add(underlineButton);
		
		setLayout(new BorderLayout());
			add(toolbar,BorderLayout.NORTH);
			add(scrollPane,BorderLayout.CENTER);
			
			
	}
	
	private static JButton getIconedButton(String filename, String name)
	{
		ImageIcon icon = SystemsManager.getImageIconOrNull(filename);
		if (icon != null)
			return (new JButton(icon));
		else
			return (new JButton(name));
	}
	
	private static JToggleButton getIconedToggleButton(String filename, String name)
	{
		ImageIcon icon = SystemsManager.getImageIconOrNull(filename);
		if (icon != null)
			return (new JToggleButton(icon));
		else
			return (new JToggleButton(name));
	}
	
	private static JMenuItem getIconedMenuItem(String filename, String name)
	{
		ImageIcon icon = SystemsManager.getImageIconOrBlankIcon(filename);
		return (new JMenuItem(name,icon));
	}
	
	public JMenuBar getMenuBar()
	{
		JMenuBar menuBar = new JMenuBar();
			JMenu fileMenu = new JMenu("File");
				JMenuItem newItem = getIconedMenuItem("new.png","New");
					newItem.setActionCommand("new");
					newItem.addActionListener(this);
					fileMenu.add(newItem);
				JMenuItem openItem = getIconedMenuItem("stock-open.png","Open");
					openItem.setActionCommand("open");
					openItem.addActionListener(this);
					fileMenu.add(openItem);
				JMenuItem saveItem = getIconedMenuItem("filesave.png","Save");
					saveItem.setActionCommand("save");
					saveItem.addActionListener(this);
					fileMenu.add(saveItem);
				JMenuItem printItem = getIconedMenuItem("fileprint.png","Print");
					printItem.setActionCommand("print");
					printItem.addActionListener(this);
					fileMenu.add(printItem);
				JMenuItem exitItem = getIconedMenuItem("exit.png","Exit");
					exitItem.setActionCommand("exit");
					exitItem.addActionListener(this);
					fileMenu.add(new JSeparator());
					fileMenu.add(exitItem);
			menuBar.add(fileMenu);
			
			return menuBar;
	}
		
	public void actionPerformed(ActionEvent event)
	{
///		System.out.println("event.getActionCommand()="+event.getActionCommand());
		
		if (event.getActionCommand().equals("cut"))
			textPane.cut();
		else if (event.getActionCommand().equals("copy"))
			textPane.copy();
		else if (event.getActionCommand().equals("paste"))
			textPane.paste();
		else if (event.getActionCommand().equals("bold"))
		{
			String text = textPane.getSelectedText();
			textPane.setCharacterAttributes(bold,true);
			try
			{
					textPane.replaceSelection("");
					textPane.getStyledDocument().insertString(textPane.getCaretPosition(),text,bold);
			}
			catch (Throwable t)
			{
				SystemsManager.printStackTrace(t);
			}
			
			//if (boldButton.isSelected())
			//	processBoldItalicOrUnderlineButton(PrintableDocumentConstants.BOLD);
			//else
			//	processBoldItalicOrUnderlineButton(PrintableDocumentConstants.PLAIN);
		}
		else if (event.getActionCommand().equals("italic"))
		{
			if (italicButton.isSelected())
				processBoldItalicOrUnderlineButton(PrintableDocumentConstants.ITALIC);
			else
				processBoldItalicOrUnderlineButton(PrintableDocumentConstants.PLAIN);
		}
		else if (event.getActionCommand().equals("underline"))
		{
			if (underlineButton.isSelected())
				processBoldItalicOrUnderlineButton(PrintableDocumentConstants.UNDERLINE);
			else
				processBoldItalicOrUnderlineButton(PrintableDocumentConstants.PLAIN);
		}
	}
	
	private void processBoldItalicOrUnderlineButton(String str)
	{
		String text = textPane.getSelectedText();
		textPane.setCharacterAttributes(textPane.getStyledDocument().getStyle(str),true);
		try
		{
				textPane.replaceSelection("");
				textPane.getStyledDocument().insertString(textPane.getCaretPosition(),text,textPane.getStyledDocument().getStyle(str));
		}
		catch (Throwable t)
		{
			SystemsManager.printStackTrace(t);
		}
	}
	
	public void caretUpdate(CaretEvent event)
	{
		Style set = textPane.getLogicalStyle();
		System.out.println(set.equals(bold));
	}
	
	public static void main(String args[])
	{
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		PrintingEditorJPanel panel = new PrintingEditorJPanel();
		frame.setJMenuBar(panel.getMenuBar());
		frame.getContentPane().add(panel);
		frame.pack();
		frame.setVisible(true);
	}
}
