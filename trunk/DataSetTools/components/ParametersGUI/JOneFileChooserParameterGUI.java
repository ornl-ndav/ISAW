/*
 * File: JOneFileChooserParameterGUI.java
 *
 * Copyright (C) 2002, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.4  2002/08/05 19:04:54  pfpeterson
 *  Fixed problem with the value of the parameter not being updated. Also added documentation to the file.
 *
 *
 */
package DataSetTools.components.ParametersGUI;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import DataSetTools.operator.*;
import javax.swing.filechooser.*;
import java.awt.event.*;
import DataSetTools.util.*;
import java.io.*;

/**
 * This class provides the GUI for the DataDirectoryString parameter.
 */
public class JOneFileChooserParameterGUI  extends JParameterGUI{
    private String     filename;
    private JPanel     GUI;
    private JTextField tf;
    private JButton    browse;
    private FileFilter file_filter;
    
    /**
     * Constructor which builds the GUI initialized with the specified
     * parameter.
     */
    public JOneFileChooserParameterGUI( Parameter  p){
        super(p);
        this.filename = filename;
        this.GUI = new JPanel();
        BoxLayout bl = new BoxLayout(this.GUI, BoxLayout.X_AXIS);
        this.GUI.setLayout( bl);
        this.tf = new JTextField(( p.getValue().toString()),25);
        this.filename = (p.getValue()).toString();
        this.browse = new JButton( "Browse");
        this.GUI.add( new JLabel( p.getName()));
        this.GUI.add(tf);
        this.GUI.add( browse);
        this.browse.addActionListener( new myActionListener());
        this.file_filter = null;
    }

    /**
     * Returns the currently value of the parameter displayed.
     */
    public Parameter getParameter(){
        Parameter p = super.getParameter();
        filename=tf.getText();
        p.setValue( new DataDirectoryString(filename)); 
        return p;
    }

    /**
     * Set the file filter used by the JFileChooser.
     */
    public void setFileFilter( FileFilter file_filter ){
        this.file_filter = file_filter;
    }
    
    /**
     * Allow enabling and disabling of the GUI.
     */
    public void setEnabled(boolean en){
        tf.setEnabled(en);
        browse.setEnabled(en);
    }
    
    /**
     * Method that returns the GUI built by this class.
     */
    public JPanel getGUISegment(){
        return GUI;
    }
    
    /**
     * Inner class of JOneFileChooserParameterGUI for dealing with the
     * browse button being pressed. It brings up a JFileChooser to
     * select a directory.
     */
    class myActionListener implements ActionListener{
        public void actionPerformed( ActionEvent evt){
            JFileChooser jf = new JFileChooser( filename);
            if( file_filter != null)
                jf.addChoosableFileFilter( file_filter );
            jf.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if( jf.showOpenDialog( null )==( JFileChooser.APPROVE_OPTION))
                {  File ff= jf.getSelectedFile();
                if( ff.isDirectory()) filename = ff.toString()+File.separator;
                else filename = ff.getParent().toString()+File.separator;
                tf.setText( filename);
                }
        }
    }

    /**
     * Main method  exclusively for testing purposes.
     */
    public static void main( String args[] ){
        JFrame jf = new JFrame("Test");
        jf.setSize( 300,100);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JOneFileChooserParameterGUI of = 
            new JOneFileChooserParameterGUI( new Parameter( "How it works",
                                             "C:\\SampleRuns\\gppd9899.run"));
        jf.getContentPane().add(of.getGUISegment());
        jf.show();
        jf.validate();
        System.out.println( of.getParameter().getName()+","
                            +of.getParameter().getValue());
    }
}
