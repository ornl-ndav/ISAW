
/*
 * File:  SaveDataSetActionListener.java 
 *             
 * Copyright (C) 2003, Ruth Mikkelson
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
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2003/08/12 14:30:08  rmikk
 * Initial Check in.
 *
 */
package DataSetTools.viewer;


import java.io.*;
import javax.swing.*;
import java.awt.event.*;
import Command.*;
import DataSetTools.util.*;
import java.awt.*;
import DataSetTools.dataset.*;
import IsawGUI.*;
/**
*    Creates a JMenuItem "Save DataSet to file" with a listener that pops
*    up a JFileChooser dialog box.  The extension on the file determines the
*    format of the save
*/
public class SaveDataSetActionListener extends JMenuItem implements ActionListener{

  DataSet DS;

  public SaveDataSetActionListener( DataSet DS){
    super("Save DataSet to file");
    this.DS = DS;
    addActionListener( this);
  }

  /**
  *   Utility to add this JMenu Item to the File Menu option of a MenuBar
  */
  public void setUpMenuItem(JMenuBar jmb){
    
    setUpMenuItem(jmb.getMenu( DataSetTools.viewer.DataSetViewer.FILE_MENU_ID ));
  }

  /**
  *   Utility to add this JMenuItem(SaveDataSetActionListener) to the menu JMenu
  */
  public void setUpMenuItem(JMenu jm){
    if( jm != null)
      jm.add(this);
  }

 /**
 *    Changes the DataSet that will be saved when this JMenuItem(SaveDataSetActionListener)
 *    is selected
 */
 public void setDataSet( DataSet DS){
    this.DS = DS;
 }
    
 private String filename = null;
  /**
  *   This method is invoked when the corresponding JMenuItem is selected. It
  *   pops up a JFileChooser Selection Dialog Box.  The DataSet is saved in the
  *   format specified by the extension of the file specified
  *  @see  Command.ScriptUtil.save( String, DataSet)
  */
  public void actionPerformed( ActionEvent evt){

try
          { JFileChooser fc = new JFileChooser();
            String title = new String( "Please choose the File to save" );
            if( filename == null)
               filename =SharedData.getProperty("user.home");
            fc.setCurrentDirectory(  new File( filename )  );
            fc.setMultiSelectionEnabled( false );
	    fc.resetChoosableFileFilters();
            fc.addChoosableFileFilter( new DataSetTools.gsastools.GsasFileFilter() );
            fc.addChoosableFileFilter(  new NeutronDataFileFilter( true )  ); 
            fc.addChoosableFileFilter(  new NexIO.NexusfileFilter()  );
           // fc.addChoosableFileFilter(  new IPNS.Runfile.RunfileFilter()  );
	    Dimension d = new Dimension(650,300);
	    fc.setPreferredSize(d);
            
            if(  (fc.showSaveDialog(null) != JFileChooser.APPROVE_OPTION ) ) 
                return;
            
                        
            File f =  fc.getSelectedFile();
            filename = f.toString();
           
           // if(   !DataSet_IO.SaveDataSet(  ds, f.toString()  )   )   
           // System.out.println("Could not save File");
             ScriptUtil.save( f.toString(), DS);
          
//String filename, DataSet ds, IDataSetListHandler lh)

          }
          catch( Exception e ) 
          {
            SharedData.addmsg( "Error "+e );
            return;
          }
  }

}
