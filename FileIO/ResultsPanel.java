/*
 * File:  ResultsPanel.java
 *
 * Copyright (C)  2003 Thomas G. Worlton
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
 * Contact : Thomas G. Worlton <tworlton@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * NOTE: This file was modified from an example in the Sun Core Java 
 *       Technologies Tech Tips.  Used with permission of Sun Microsystems.
 *        
 * Core Java Technologies Tech Tips, December 17, 2002
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * 901 San Antonio Road, Palo Alto, California 94303 USA.
 *
 * This document is protected by copyright. For more information, see:
 *
 * http://java.sun.com/jdc/copyright.html
 *
 * IMPORTANT: Please read our Terms of Use, Privacy, and Licensing
 * policies:
 * http://www.sun.com/share/text/termsofuse.html
 * http://www.sun.com/privacy/
 * http://developer.java.sun.com/berkeley_license.html
 *
 * - ARCHIVES
 *   You'll find the Core Java Technologies Tech Tips archives at:
 *   http://java.sun.com/jdc/TechTips/index.html
 *
 * Sun, Sun Microsystems, Java, Java Developer Connection, J2SE,
 * J2EE, and J2ME are trademarks or registered trademarks of Sun
 * Microsystems, Inc. in the United States and other countries.
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.3  2004/01/23 16:11:19  dennis
 * Move source citation information (Sun Java Tech Tips) to top of file
 * with other copyright and modification information.
 *
 * Revision 1.2  2004/01/23 16:00:38  dennis
 * Modified by tworlton:
 * Eliminated special characters from name of search results file.
 * Underscores now replace all non-letters/numbers in the search string.
 *
 * Revision 1.10  2003/12/10 19:10:27  bouzekc
 * Added to CVS.
 *
 */
package FileIO;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.regex.*;
import java.util.*;
import NexIO.*;
import DataSetTools.operator.Generic.Special.ViewASCII;
import IPNS.Runfile.*;

/**
 * Class ResultsPanel creates a panel to display the results of a search
 */
public class ResultsPanel extends JPanel {
  BorderLayout borderLayout1 = new BorderLayout();
  DefaultListModel model = new DefaultListModel();
  JList fileList = new JList(model);
  private String outname;
  private String path1 = "null";
  private String pathnow = "null";
  private String pathold = "null";
  private JProgressBar progressBar;
  private static CharBuffer charBuffer = CharBuffer.allocate(80);
  private File top;
  private JDialog progress;
  int nfiles;
/**
 * ResultsPanel constructor
 *@param pattern search pattern
 *@param directory top directory of search
 *@param boolean search sub-directories
 *@param boolean case blind compare
 *@param filter filter for files
 */
  public ResultsPanel(final String searchPattern,
      final String searchDir, final boolean subdirs,
      final boolean caseblind,
      final FileFilter filter) {
    top = new File(searchDir);
// Temporarily create files list in order to count
    path1 = top.getAbsolutePath();
    if(path1 != null) {  // restrict scope of files1[] to conserve memory
      File files1[] = top.listFiles(filter); // Get list of files in top search directory
      nfiles = files1.length + 1; // I just want to find the number of files
      }
    progressBar = new JProgressBar(0, nfiles);
    progressBar.setValue(0);
    progressBar.setStringPainted(true);
    progress = new JDialog();
    Container contentPane = progress.getContentPane();
    contentPane.add(progressBar);
    progress.setTitle("Search progress");
    progress.setSize(350,50);
    progress.show();
// compose an output file name using legal file name characters
    char[] chary = searchPattern.toCharArray();
    char underscore = "_".charAt(0);
    for (int i = 0; i < searchPattern.length(); i++)
      if( !Character.isLetterOrDigit( chary[i] ))
         chary[i] = underscore;
    outname = new String(chary) + "_files.txt";
    System.out.println("Search results stored in "+outname);
    File fout = new File(outname);
    if(fout.exists())
      fout.delete();
    this.setLayout(borderLayout1);
    JScrollPane jScrollPane1 = new JScrollPane(fileList);
    this.add(jScrollPane1, BorderLayout.CENTER);
    fileList.addListSelectionListener(
      new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          if (!e.getValueIsAdjusting()) {
            String filename = (String) fileList.getSelectedValue();
            if (filename != null && filter instanceof TextFileFilter) {
              showHighlightedAreas(filename, searchPattern, caseblind);
//              showHighlightedAreas(filename, searchPattern);
            }
          }
        }
      }
    );
// Don't block the event thread. Add them as they are found
// directories are searched recursively
    new Thread() {
      public void run() {
        File file = new File(searchDir);
        addFile(searchPattern, file, subdirs, true, filter, caseblind);
        progressBar.setValue(nfiles);
        progressBar.setString(nfiles + "/" + nfiles);
        progressBar.setIndeterminate(false);
        progress.setTitle("Search done");
        progress.dispose();
        File fout = new File( outname );
        if(fout.exists() && fout.length() != 0) {
//          System.out.println( "out file length =" + fout.length() );
          ViewASCII summaries = new ViewASCII( outname );
          summaries.getResult();
          }
        }
      }
    .start();
    } //end ResultsPanel constructor

  // Add files to model that match the search pattern
/**
 * method addFile adds files which match the search pattern to
 * the list displayed on the tabbed pane
 * @param pattern the search pattern
 * @param file the file name
 * @param boolean subdirectory search?
 * @param boolean first subdirectory?
 * @param filter FileFilter
 */  
  private void addFile(String searchPattern,
      final File file, boolean subdirs, boolean first,
      FileFilter filter, boolean nocase)
    {
    String fname = file.getName();
    if (fname.startsWith( "."))	//Ignore fdl files
      {
      }
    else if (file.isFile()) {
      pathnow = file.getParent();
      if(pathnow.equals(pathold) == false) {  //directory has changed
        if(pathnow.equals(path1) == false) {
          progressBar.setIndeterminate(true);  // not in top directory
          progress.setTitle("Searching " + pathnow);
          }
        else {
          progressBar.setIndeterminate(false);
          progress.setTitle("Searching " + path1);
          }
      }
      if (filter instanceof TextFileFilter) 
        {
        if (containsPattern(file, searchPattern, nocase)) 
          {
          // Need to add to model in event thread
          EventQueue.invokeLater(new Runnable() 
            {
            public void run() {
              model.addElement(file.getAbsolutePath());
              }
            });
          }  // end containsPattern block
        }  // end TextFileFiler block
      else if (filter instanceof RunFileFilter) {
        try {
          RunFileSummary fileSum = new RunFileSummary( file.toString() );
          if(containsPattern(fileSum.getSummary(), searchPattern, nocase)) {
            try {
              File fout = new File(outname);
              BufferedWriter out = 
                     new BufferedWriter(new FileWriter(fout, true));
              if(pathnow.equals(pathold)==false){
                out.write("Directory="+pathnow+"\n");
                progress.setTitle("Searching " + pathnow);
                pathold = pathnow;
                }
              out.write(fileSum.getSummary()+" \n");
              out.close();
              } catch (IOException e) {
              } //end try/catch
            EventQueue.invokeLater(new Runnable() {
              public void run() {
                model.addElement(file.getAbsolutePath());
                }
              });  //end EventQueue.invokeLater
            }      //end if containsPattern
          }        //end try for runfile
        catch (Exception e) {
          System.err.println(" Exception from RunFileSummary "+ e.toString());
          }
        } //end of RunFileFilter block
      else if (filter instanceof NexusfileFilter)
        {
        try {
          if(containsPattern(NXutil.getNXfileSummary( file.toString() ), searchPattern, nocase)) 
            {
              try {
                File fout = new File(outname);
                BufferedWriter out = 
                     new BufferedWriter(new FileWriter(fout, true));
                if(pathnow.equals(pathold)==false){
                  out.write("Directory="+pathnow+"\n");
                  progress.setTitle("Searching " + pathnow);
                  pathold = pathnow;
                  }
                out.write(NXutil.getNXfileSummary(file.toString())+" \n");
                out.close();
              } catch (IOException e) {
              } //end try/catch
            EventQueue.invokeLater(new Runnable() 
              {
              public void run() {
                model.addElement(file.getAbsolutePath());
                }
              });  //end EventQueue.invokeLater
            }      //end if
          }        //end try
        catch (Exception e) {
          System.err.println(" Exception from NeXusFileSummary "+ e.toString());
          }
        } //end of NeXusFileFilter block
      }   //end of file.isFile block
      else if (file.isDirectory()) 
        {
        if (first || subdirs) 
          { // need to let first pass through
          File files[] = file.listFiles(filter); // T. Worlton added filter
          ArrayList theList =
              new ArrayList( Arrays.asList( files ));
          Collections.sort( theList );
          for (int i=0; i < files.length; i++) {
            files[i] = (File) theList.get(i);
            }
            if (filter instanceof RunFileFilter || filter instanceof NexusfileFilter) {
            try {
              File fout = new File(outname);
              BufferedWriter out = 
                   new BufferedWriter(new FileWriter(fout, true) );
              out.flush();
              out.close();
              } catch (IOException e) {
              } //end try/catch
            } // end output file creation option
//          System.out.print(".");
          for (int i = 0; i < files.length; i++) 
            {  //false means it is not the first directory
            addFile(searchPattern, files[i], subdirs, false, filter, nocase);
// only update progress bar when we are in the top level directory
            String fis=null;
            if(files[i].isFile())
              fis = files[i].getParent();
            else if (files[i].isDirectory() ) {
              fis = files[i].getAbsolutePath();
              progressBar.setString(i + "/" + nfiles);
              }
            else
              System.err.println("unknown file type =" + files[i]);
            if(fis.equals(path1)) {
              progressBar.setString(i + "/" + nfiles);
              progressBar.setValue(i);
              progress.setTitle("Searching " + fis );
              }
            }
//          System.out.println("Searched " + nfiles + " files in " + file.toString());
          } //end of if (first)
        } //end of else if (file.isDirectory())
    } //end of addFile
/**
 * method to check for a pattern within a file
 * @param file the file to search
 * @param pattern the pattern to look for
 * @throws exception if file is invalid
 */
  private boolean containsPattern(File file, String pattern, boolean nocase) {
    // Assume file small enough to read entirely into memory
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(file);
      FileChannel channel = fis.getChannel();
      int fileLength = (int) channel.size();
      MappedByteBuffer buffer = channel.map(
          FileChannel.MapMode.READ_ONLY, 0, fileLength);
      // Convert to character buffer
      Charset charset = Charset.forName("ISO-8859-1");
      CharsetDecoder decoder = charset.newDecoder();
      CharBuffer charBuffer = decoder.decode(buffer);
      Pattern thePattern;
      if(nocase)
        thePattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
      else
        thePattern = Pattern.compile(pattern);
      Matcher matcher = thePattern.matcher(charBuffer);
      return matcher.find();
    } catch (IOException ex) {
      return false;
    }
  }
/**
 * method to check for a pattern within a String
 * @param text the string to search
 * @param pattern the pattern to look for
 * @param nocase if true, ignore case
 */
  private boolean containsPattern(String text, String pattern, boolean nocase) {
    charBuffer.clear();
    charBuffer.put(text);
    charBuffer.position(0);
    Charset charset = Charset.forName("ISO-8859-1");
    CharsetDecoder decoder = charset.newDecoder();
    Pattern thePattern;
    if(nocase)
      thePattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    else
      thePattern = Pattern.compile(pattern);
    Matcher matcher = thePattern.matcher(charBuffer);
    return matcher.find();
  }
/**
 * method to highlight areas of text which match the search pattern
 * @param filename the file name
 * @param pattern the pattern to match
 * @param nocase if nocase, modify the search to be CASE_INSENSITIVE
 */
  private void showHighlightedAreas(
      final String filename, final String pattern, final boolean nocase) {
        JDialog dialog = new JDialog();
        Container contentPane = dialog.getContentPane();
        try {
          JTextPane textPane = new JTextPane();
          textPane.setEditable(false);
          JScrollPane scrollPane = new JScrollPane(textPane);
          contentPane.add(scrollPane);
          // Read in file
          FileReader reader = new FileReader(filename);
          textPane.read(reader, null);
          // Make highlight attribute
          SimpleAttributeSet set = new SimpleAttributeSet();
          set.addAttribute(
            StyleConstants.CharacterConstants.Bold,
            Boolean.TRUE);
          // Get Document for text pane
          DefaultStyledDocument document =
            (DefaultStyledDocument)textPane.getDocument();
          // Handle newlines across platforms
          document.putProperty(
            DefaultEditorKit.EndOfLineStringProperty, "\n");
          // highlight areas
          String text = textPane.getText();
//          System.out.println(text);
          Pattern thePattern;
          if(nocase) 
            thePattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
          else
            thePattern = Pattern.compile(pattern);
//          System.out.println("Total text length =" + text.length());
          Matcher matcher = thePattern.matcher(text);
          while (matcher.find()) {
            int start = matcher.start();
            int length = matcher.end() - start;
//            System.out.println("start=" + start + ", length =" + length);
            document.setCharacterAttributes(
              start, length, set, false);
          }
          // show results
//          System.out.println("creating new dialog window");
          dialog.setSize(450, 300);
//          System.out.println("showing dialog");
          dialog.show();
        } catch (IOException ex) {
          System.err.println("Unable to show highlights");
        }
      }
}
