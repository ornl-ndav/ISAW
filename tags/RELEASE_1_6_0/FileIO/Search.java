/*
 * File:  Search.java
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
 * Modified:
 *
 * $Log$
 * Revision 1.1  2003/12/10 19:10:27  bouzekc
 * Added to CVS.
 *
 */

package FileIO;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.logging.*;
import NexIO.NexusfileFilter;
import java.io.*;

/**
 * Class Search is used to find files containing a specified expression.  
 * Programmer Challenge from Core Java Technologies Tech Tip 12/17/2003 was 
 * used as a basis for this class.
 *@author Thomas Worlton
 */
public class Search extends JFrame {
  JPanel contentPane;
  JPanel topPanel = new JPanel();

  Insets insets0 = new Insets(0, 0, 0, 0);
  Insets insets10 = new Insets(0, 10, 0, 10);

  JLabel directoryLabel = new JLabel();
  JTextField searchDirectory = new JTextField();
  JButton browseButton = new JButton();

  JCheckBox subdirectories = new JCheckBox();

  JLabel expressionLabel = new JLabel();
  JTextField expression = new JTextField();
  JCheckBox caseBlind = new JCheckBox("Case Blind?", true);

  JLabel filterLabel = new JLabel();
  JButton submitButton = new JButton();
 
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  JTabbedPane results = new JTabbedPane();
  JFileChooser fileChooser = new JFileChooser();
  private static Logger logger = Logger.getLogger("global");
  FileFilter filter;
  String[] filterStrings = { "Source", "Scripts", "RunFiles", "NeXus"};
  File searchDef;

/**
 * Search default constructor lays out a GUI for finding files
 * containing a specified expression
 */
  public Search() {
    super("Search");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    contentPane = (JPanel) this.getContentPane();
    setSize(450, 300);
    topPanel.setLayout(gridBagLayout1);
//  get default directory for chooser
    try { 
//    Note that the following is forbidden for applets.
      String value = System.getProperty("user.dir"); 
      searchDef = new File(value);
//      System.out.println( "current working directory =" + value);
    } catch (SecurityException e) { 
      System.err.println("Could not read user.dir property" + e);
  } 
//
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    fileChooser.setCurrentDirectory( searchDef );
    fileChooser.setDialogTitle( "Choose a directory to search" );
    fileChooser.setSelectedFile( searchDef );
    searchDirectory.setText( searchDef.toString() );
    browseButton.setText("Browse");
    browseButton.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          browseButton_actionPerformed(e);
        }
      }
    );
    expressionLabel.setText("Expression");
    filterLabel.setText("File Filter");
    JComboBox filterList = new JComboBox(filterStrings);
//define a default filter-----
    filterList.setSelectedIndex(3);
    filter = new NexusfileFilter();
//--------------------------------
    filterList.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JComboBox cb = (JComboBox)e.getSource();
        String filterName = (String)cb.getSelectedItem();
//        System.out.println( " filter = " + filterName);
        if(filterName == "Source" )
           filter = new TextFileFilter();
        else if (filterName == "RunFiles")
           filter = new RunFileFilter();
        else if (filterName == "NeXus")
           filter = new NexusfileFilter();
        else if (filterName == "Scripts")
           filter = new ScriptFileFilter();
        else
          System.err.println( " Filter not found.");
      //System.out.println("New filter chosen " + filter);
      } // end of actionPerformed
    }); // end of addActionListener
    directoryLabel.setText("Search Directory");
    // Hitting enter in text field or on button submits
    ActionListener submitAction = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          submitButton_actionPerformed(e);
        }
    };
    expression.addActionListener(submitAction);
    searchDirectory.addActionListener(submitAction);
    submitButton.setText("Submit");
    submitButton.addActionListener(submitAction);
    subdirectories.setText("Search Subdirectories?");
    contentPane.add(topPanel,  BorderLayout.NORTH);
// Define top row
    topPanel.add(directoryLabel,
      new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
      GridBagConstraints.WEST, GridBagConstraints.NONE,
      insets10, 0, 0));
    topPanel.add(searchDirectory,
      new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
      GridBagConstraints.WEST,
      GridBagConstraints.HORIZONTAL,
      insets0, 0, 0));
    topPanel.add(browseButton,
      new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
      GridBagConstraints.CENTER, GridBagConstraints.NONE,
      insets10, 0, 0));

// for now subdirectories is by itself on the second line
    topPanel.add(subdirectories,
      new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
      GridBagConstraints.WEST, GridBagConstraints.NONE,
      insets10, 0, 0));

// Define row 2 (third)
    topPanel.add(expressionLabel,
      new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
      GridBagConstraints.WEST, GridBagConstraints.NONE,
      insets10, 0, 0));
    topPanel.add(expression,
      new GridBagConstraints(1, 2, 1, 1, 1.0, 1.0,
      GridBagConstraints.CENTER,
      GridBagConstraints.HORIZONTAL,
      insets0, 0, 0));
    topPanel.add(caseBlind,
      new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
      GridBagConstraints.CENTER, GridBagConstraints.NONE,
      insets10, 0, 0));

//Fourth line has submit button and filter type combo box
    topPanel.add(filterLabel,
      new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
      GridBagConstraints.WEST, GridBagConstraints.NONE,
      insets10, 0, 0));

    topPanel.add(filterList,
      new GridBagConstraints( 1, 3, 1, 1, 0.0, 0.0,
      GridBagConstraints.WEST, GridBagConstraints.NONE,
      insets10, 0, 0));

    topPanel.add(submitButton,
      new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0,
      GridBagConstraints.CENTER, GridBagConstraints.NONE,
      insets10, 0, 0));

    results.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    contentPane.add(results, BorderLayout.CENTER);
  }

  void browseButton_actionPerformed(ActionEvent e) {
    int returnValue = fileChooser.showOpenDialog(this);
    if (returnValue == JFileChooser.APPROVE_OPTION) {
      searchDirectory.setText(
        fileChooser.getSelectedFile().getAbsolutePath());
    }
  }
//
// This is where all the action occurs
//
  void submitButton_actionPerformed(ActionEvent e) {
// Start searching asynchronously (in separate thread)
    ResultsPanel resultsPanel = new ResultsPanel(
      expression.getText(),
      searchDirectory.getText(),
      subdirectories.isSelected(),
      caseBlind.isSelected(),
      filter);
// log the search pattern and directory
    logger.info("Pattern: " + expression.getText() +
      " / Directory: " + searchDirectory.getText());
// create a new tabbed JPanel to hold the search results.
    final JPanel panel = new JPanel(new BorderLayout());
    panel.add(resultsPanel, BorderLayout.CENTER);
    JButton closeButton = new JButton("Remove");
    panel.add(closeButton, BorderLayout.SOUTH);
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        results.remove(panel);
      }
    });
    results.add(panel, expression.getText());
    results.setSelectedComponent(panel);
  }

  public static void main(String args[]) {
    Search frame = new Search();
    // Center the window
    frame.setLocationRelativeTo(null);
    frame.show();
  }
}

//.  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .
//
//IMPORTANT: Please read our Terms of Use, Privacy, and Licensing 
//policies:
//http://www.sun.com/share/text/termsofuse.html
//http://www.sun.com/privacy/
//http://developer.java.sun.com/berkeley_license.html

//* FEEDBACK
//  Comments? Send your feedback on the Core Java Technologies 
//  Tech Tips to: 
//  jdc-webmaster@sun.com

//* SUBSCRIBE/UNSUBSCRIBE

//  Subscribe to other Java developer Tech Tips:
  
//  - Enterprise Java Technologies Tech Tips. Get tips on using
//    enterprise Java technologies and APIs, such as those in the
//    Java 2 Platform, Enterprise Edition (J2EE(tm)).    
//  - Wireless Developer Tech Tips. Get tips on using wireless
//    Java technologies and APIs, such as those in the Java 2 
//    Platform, Micro Edition (J2ME(tm)).
    
//  To subscribe to these and other JDC publications:
//  - Go to the JDC Newsletters and Publications page,
//    (http://developer.java.sun.com/subscription/), 
//    choose the newsletters you want to subscribe to and click 
//    "Update".
//  - To unsubscribe, go to the subscriptions page,
//    (http://developer.java.sun.com/subscription/), 
//    uncheck the appropriate checkbox, and click "Update".
//  - To use our one-click unsubscribe facility, see the link at 
//    the end of this email:
    
//- ARCHIVES
//You'll find the Core Java Technologies Tech Tips archives at:

//http://java.sun.com/jdc/TechTips/index.html


//- COPYRIGHT
//Copyright 2002 Sun Microsystems, Inc. All rights reserved.
//901 San Antonio Road, Palo Alto, California 94303 USA.

//This document is protected by copyright. For more information, see:

//http://java.sun.com/jdc/copyright.html


//Core Java Technologies Tech Tips 
//December 17, 2002

//Sun, Sun Microsystems, Java, Java Developer Connection, J2SE, 
//J2EE, and J2ME are trademarks or registered trademarks of Sun 
//Microsystems, Inc. in the United States and other countries.

