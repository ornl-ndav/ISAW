package DataSetTools.components.ParametersGUI;

import javax.swing.*;
import DataSetTools.operator.*;
import javax.swing.filechooser.*;
import java.awt.event.*;
import DataSetTools.util.*;

import java.io.*;
public class JOneFileChooserParameterGUI  extends JParameterGUI
{
  String filename;
  JPanel GUI;
  JTextField  tf;
  JButton  browse;
   javax.swing.filechooser.FileFilter file_filter;
public JOneFileChooserParameterGUI( Parameter  p)
  {super(p);
   this.filename = filename;
   GUI = new JPanel();
   BoxLayout bl = new BoxLayout(GUI, BoxLayout.X_AXIS);
   GUI.setLayout( bl);
   tf = new JTextField(( p.getValue().toString()),25);
   filename = (p.getValue()).toString();
   browse = new JButton( "Browse");
   GUI.add( new JLabel( p.getName()));
   GUI.add(tf);
   GUI.add( browse);
   browse.addActionListener( new myActionListener());
   file_filter = null;
   
  }
public Parameter getParameter()
  { Parameter p = super.getParameter();
    p.setValue( new DataDirectoryString(filename)); 
    return p;
   }
public void setFileFilter(  javax.swing.filechooser.FileFilter file_filter)
  {  this.file_filter = file_filter;
   }

    public void setEnabled(boolean en){
        tf.setEnabled(en);
        browse.setEnabled(en);
    }

public JPanel getGUISegment()
 { return GUI;
  }

class myActionListener implements ActionListener
  {
   public void actionPerformed( ActionEvent evt)
     { JFileChooser jf = new JFileChooser( filename);
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

public static void main( String args[] )
 {JFrame jf = new JFrame("Test");
  jf.setSize( 300,100);
  JOneFileChooserParameterGUI of = new JOneFileChooserParameterGUI( 
             new Parameter( "Hoe it works","C:\\SampleRuns\\gppd9899.run"));
  jf.getContentPane().add(of.getGUISegment());
  jf.show();
  jf.validate();
  System.out.println( of.getParameter().getName()+","+of.getParameter().getValue());
  try{
   char c=0;
   while( c!='x')
     { System.out.println( of.getParameter().getName()+","+of.getParameter().getValue());
       c=(char) System.in.read();


     }


  }
  catch(Exception s)
   {}
  }


}

