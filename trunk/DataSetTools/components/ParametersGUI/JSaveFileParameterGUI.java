/*
 * File: JSaveFileParameterGUI.java
 *
 * Copyright (C) 2002, Peter Peterson
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
 * Contact : Peter F. Peterson <pfpeterson@anl.gov>
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
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.6  2004/01/22 01:41:28  bouzekc
 *  Removed unused variables and unused imports.
 *
 *  Revision 1.5  2003/12/15 02:17:29  bouzekc
 *  Removed unused imports.
 *
 *  Revision 1.4  2002/11/27 23:12:35  pfpeterson
 *  standardized header
 *
 *  Revision 1.3  2002/08/22 15:07:03  pfpeterson
 *  Fixed a bug with setting the parameter value and the return type.
 *
 *  Revision 1.2  2002/04/04 20:41:48  pfpeterson
 *  getParameter returns a String.
 *
 *  Revision 1.1  2002/04/02 22:50:29  pfpeterson
 *  Added to CVS.
 *
 *
 */
package DataSetTools.components.ParametersGUI;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import DataSetTools.operator.Parameter;
import DataSetTools.util.SaveFileString;

/**
 * This class is intended to be used for selecting a filename for
 * saving. Its creation was for writing out files using an operator.
 */
public class JSaveFileParameterGUI  extends JParameterGUI{
    String filename;
    JPanel GUI;
    JTextField  tf;
    JButton  browse;
    FileFilter file_filter;

    public JSaveFileParameterGUI( Parameter  parameter){
        super(parameter);
        GUI = new JPanel();
        //BoxLayout bl = new BoxLayout(GUI, BoxLayout.X_AXIS);
        GUI.setLayout( new GridLayout(1,2));

        tf = new JTextField(( parameter.getValue().toString()),20);
        filename = (parameter.getValue()).toString();
        browse = new JButton( "Browse");
        GUI.add( new JLabel( "  "+parameter.getName()));

        JPanel inner=new JPanel();
        GUI.add(inner);
        //inner.setLayout(new GridLayout(1,2));
        inner.setLayout(new BoxLayout(inner,BoxLayout.X_AXIS));
        inner.add(tf);
        inner.add( browse);

        browse.addActionListener( new myActionListener());
        file_filter = null;
        
    }

    public Parameter getParameter(){
        Parameter p = super.getParameter();
        filename=tf.getText();
        p.setValue(new SaveFileString(filename));
        return p;
    }

    public void setFileFilter(  FileFilter file_filter){
        this.file_filter = file_filter;
    }

    public FileFilter getFileFilter( ){
        return this.file_filter;
    }

    public void setEnabled(boolean en){
        tf.setEnabled(en);
        browse.setVisible(en);
    }
    
    public JPanel getGUISegment(){
        return GUI;
    }
    
    class myActionListener implements ActionListener{
        public void actionPerformed( ActionEvent evt){ 
            JFileChooser jf = new JFileChooser( filename);
            if( file_filter != null)
                jf.addChoosableFileFilter( file_filter );
            jf.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jf.setDialogType(JFileChooser.SAVE_DIALOG);
            if( jf.showSaveDialog( null )==( JFileChooser.APPROVE_OPTION)){
                File ff= jf.getSelectedFile();
                filename=ff.getAbsolutePath();
                tf.setText( filename );
            }
        }
    }

    public static void main( String args[] ){
        
        JFrame jf = new JFrame("Test");
        jf.setSize( 300,100);
        JSaveFileParameterGUI of = new JSaveFileParameterGUI( 
                new Parameter( "How it works","C:\\SampleRuns\\gppd9899.run"));
        jf.getContentPane().add(of.getGUISegment());
        jf.show();
        jf.validate();
        System.out.println( of.getParameter().getName()+","
                            +of.getParameter().getValue());
        try{
            char c=0;
            while( c!='x'){ 
                System.out.println( of.getParameter().getName()+","
                                    +of.getParameter().getValue());
                c=(char) System.in.read();
                
                
                }
            
            
        }
        catch(Exception s){
        }
    }
}
