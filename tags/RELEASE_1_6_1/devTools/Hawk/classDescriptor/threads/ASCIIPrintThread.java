/*
 * File:  ASCIIPrintThread.java
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
 * Revision 1.1  2004/02/07 05:10:26  bouzekc
 * Added to CVS.  Changed package name.  Uses RobustFileFilter
 * rather than ExampleFileFilter.  Added copyright header for
 * Dominic.
 *
 */
package devTools.Hawk.classDescriptor.threads;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JOptionPane;

import devTools.Hawk.classDescriptor.gui.frame.PrintGUI;
import devTools.Hawk.classDescriptor.gui.frame.ProgressGUI;
import devTools.Hawk.classDescriptor.tools.ASCIIPrintFileManager;
import devTools.Hawk.classDescriptor.tools.InterfaceUtilities;
import devTools.Hawk.classDescriptor.tools.SystemsManager;

/**
 * @author kramer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ASCIIPrintThread extends Thread
{
	protected PrintGUI printGUI;
	protected ProgressGUI progress;
	protected int maxSize;
	
	public ASCIIPrintThread(PrintGUI PRINTGUI)
	{
		super();
		printGUI = PRINTGUI;
		maxSize = 0;
		if (printGUI.getUseIntroCheckBox().isSelected())
			maxSize++;
		if (printGUI.getUseContentsCheckBox().isSelected())
			maxSize++;
		if (printGUI.getUsePackageListCheckBox().isSelected())
			maxSize++;
		if (printGUI.getUseAlphaUMLCheckBox().isSelected())
			maxSize = maxSize + printGUI.getModel().size();
		if (printGUI.getUseShortenedSourceCheckBox().isSelected())
			maxSize = maxSize + printGUI.getModel().size();
		
		maxSize += InterfaceUtilities.getPackageListVector(printGUI.getVectorOfSelectedInterfaces()).size();
		
		progress = new ProgressGUI(0, maxSize, "Printing In Progress");
		progress.setVisible(true);
	}
	
	public void run()
	{
		String fileName =  printGUI.getFileField().getText().trim();
			
		if(!fileName.equals(""))
		{
			try
			{					
				int currentVal = 0;
					
				ASCIIPrintFileManager printer = new ASCIIPrintFileManager(fileName, "rw");
				
				int TABSIZE = 7;
				int LENGTH = 80;
				int largestNum = 1;
					
				long time1 = System.currentTimeMillis();
				
				if (printGUI.getUseIntroCheckBox().isSelected() && !progress.isCancelled())
				{
					progress.setText("Printing the introduction\n");
					printer.printIntroduction(printGUI.getTitleField().getText(), printGUI.getDateField().getText(), printGUI.getAuthorField().getText(), printGUI.getDescTextArea().getText());
					printer.writeBytes("\n");
					currentVal = progress.getValue()+1;
					progress.setValue(currentVal);
				}
					
				if (printGUI.getUseContentsCheckBox().isSelected() && !progress.isCancelled())
				{
					progress.setText("Printing the table of contents\n");
					Vector titleVec = new Vector();
						
					if (printGUI.getUsePackageListCheckBox().isSelected())
						titleVec.add("Package List");
					if (printGUI.getUseAlphaUMLCheckBox().isSelected())
						titleVec.add("UML Diagrams Listed Alphabetically");
					if (printGUI.getUseShortenedSourceCheckBox().isSelected())
						titleVec.add("Shortened Source Code Listed Alphabetically");
							
					int[] numArray = new int[titleVec.size()];
					for (int i = 0; i<numArray.length; i++)
						numArray[i]=i+1;
						
					printer.printSectionHeading(-1, LENGTH, "Table of Contents");
					printer.writeBytes("\n");
					printer.printTableOfContents(TABSIZE, numArray, titleVec);
					printer.writeBytes("\n");
					currentVal = progress.getValue()+1;
					progress.setValue(currentVal);
				}
				
				Vector selectedIntFVec = printGUI.getVectorOfSelectedInterfaces();
				
				if (printGUI.getUsePackageListCheckBox().isSelected() && !progress.isCancelled())
				{
					progress.setText("Printing the package list\n");
						printer.printSectionHeading(largestNum++, LENGTH, "Package List");
						printer.writeBytes("\n");
						printer.printPackageList(TABSIZE, selectedIntFVec,progress);
						printer.writeBytes("\n");
						currentVal = progress.getValue()+1;
						progress.setValue(currentVal);
				}
					
				if (printGUI.getUseAlphaUMLCheckBox().isSelected() && !progress.isCancelled())
				{
					printer.printSectionHeading(largestNum++, LENGTH, "UML Diagrams Organized Alphabetically");
					printer.writeBytes("\n");
					//Currently the two falses tell the method to print the printout without shortening the java names or other names
						//for example if a method returns a String it is printed as java.lang.String not just String
					
					printer.printUMLDiagramsAlphabetically(selectedIntFVec, LENGTH, TABSIZE, printGUI.getSingleUMLShortenJavaCheckBox().isSelected(), printGUI.getSingleUMLShortenNonJavaCheckBox().isSelected(), progress, 1);
					printer.writeBytes("\n");
				}
				
				if (printGUI.getUseShortenedSourceCheckBox().isSelected() && !progress.isCancelled())
				{
					printer.printSectionHeading(largestNum++, LENGTH, "Shortened Source Code Organized Alphabetically");
					printer.writeBytes("\n");
					//Currently this prints out the entire type names
						//for example java.lang.String instead of String
					printer.printShortenedSourceAlphabetically(TABSIZE, selectedIntFVec, printGUI.getShortenedSourceShortenJavaCheckBox().isSelected(), printGUI.getShortenedSourceShortenNonJavaCheckBox().isSelected(), progress, 1);
				}
					
				printer.printEndDivider(LENGTH);
					
				long time2 = System.currentTimeMillis();
				progress.appendMessage("The document was generated in "+(time2-time1)+" milliseconds.");
				progress.isCompleted();
				progress.setValue(progress.getMaximum());
				printer.close();
				printGUI.getFrame().dispose();
			}
			catch(FileNotFoundException e)
			{
				JOptionPane opPane = new JOptionPane();
				JOptionPane.showMessageDialog(opPane,
					"The file you selected could not be used for printing because it\n" +
						"does not exist.  Please select another file.",
					"File Error",
					JOptionPane.ERROR_MESSAGE);
			}
			catch(IOException e)
			{
				SystemsManager.printStackTrace(e);
			}
		}
		else
		{
			JOptionPane opPane = new JOptionPane();
			JOptionPane.showMessageDialog(opPane,
				"Please select a file to save the printout to.",
				"Filename Not Given",
			JOptionPane.INFORMATION_MESSAGE);
		}

		super.run();
	}
}
