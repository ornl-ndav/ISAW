/*
 * File: BrowseButtonListener.java
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
 *  Revision 1.8  2003/12/14 19:20:40  bouzekc
 *  Removed unused imports.
 *
 *  Revision 1.7  2003/06/16 14:51:52  bouzekc
 *  Added code so that if the FileFilter for BrowsePG (when
 *  masquerading as SaveFilePG) is a RobustFileFilter, the
 *  default extension is added if no extension is specified.
 *
 *  Revision 1.6  2003/05/29 21:46:50  bouzekc
 *  Added a constructor which takes a Vector of FileFilters.
 *  Added the capability to select a default FileFilter.
 *
 *  Revision 1.5  2003/02/07 15:35:50  pfpeterson
 *  Fixed a bug when the list of FileFilters is null.
 *
 *  Revision 1.4  2003/02/03 21:36:39  pfpeterson
 *  Made it possible for the JParameterDialog the is produced by this
 *  class to have more than one FileFilter.
 *
 *  Revision 1.3  2002/11/27 23:12:34  pfpeterson
 *  standardized header
 *
 *  Revision 1.2  2002/10/23 18:51:56  pfpeterson
 *  Now supports a javax.swing.filechooser.FileFilter to be specified
 *  for browsing options. Also fixed bug when the JFileChooser was
 *  started and did not go to the proper location.
 *
 *  Revision 1.1  2002/07/15 21:24:03  pfpeterson
 *  Added to CVS.
 *
 *
 */
package DataSetTools.components.ParametersGUI;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;
import java.io.*;
import java.util.Vector;
import javax.swing.filechooser.FileFilter;
import DataSetTools.util.RobustFileFilter;
    
public class BrowseButtonListener implements ActionListener{
    public static int SAVE_FILE  = 1;
    public static int LOAD_FILE  = 2;
    public static int LOAD_MULTI = 3;
    public static int DIR_ONLY   = 4;

    private static final boolean DEBUG=false;

    private JFileChooser   jfc;
    private JTextComponent textbox;
    private String         def_filename;
    private int            type;
    private Vector         filters;
    private FileFilter     defaultfilter;

    public BrowseButtonListener( JTextComponent entry, int type ){
        //create a new Vector and basically toss it-we need to 
        //be able to differentiate between the constructor
        //that takes a FileFilter and the one that takes a Vector
        //I picked an empty Vector rather than a temporary FileFilter
        this(entry,type,new Vector());
    }

    public BrowseButtonListener( JTextComponent entry, int type,
                                 FileFilter filter ){
        this.textbox=entry;
        this.type=type;
        this.jfc=null;
        this.def_filename=null;

        if(this.type==LOAD_MULTI){
            System.err.println("LOAD_MULTI not instantiated yet "
                               +"-- using LOAD_FILE instead");
            this.type=LOAD_FILE;
        }
        this.filters=null;
        this.addFileFilter(filter);
        defaultfilter = null;
    }
    
    public BrowseButtonListener( JTextComponent entry, int type,
                                 Vector file_filters ){
        this.textbox=entry;
        this.type=type;
        this.jfc=null;
        this.def_filename=null;
        if(this.type==LOAD_MULTI){
            System.err.println("LOAD_MULTI not instantiated yet "
                               +"-- using LOAD_FILE instead");
            this.type=LOAD_FILE;
        }

        //now we will KNOW that all the items in the Vector are 
        //FileFilters
        for( int i = 0; i < file_filters.size(); i ++ )
        {
          if( !(file_filters.elementAt(i) instanceof FileFilter) )
            file_filters.remove(i);
        }
        this.filters = file_filters;

        defaultfilter = null;
    }

    /**
     * Sets a FileFilter to be the current FileFilter.
     * 
     * @param filterindex          The index of the FileFilter
     *                             to use.
     */
    public void setFileFilter( int filterindex ){
      if(filterindex < 0) 
        return;
        
      defaultfilter = (FileFilter)
                        (filters.elementAt(filterindex));
    }
    
    /**
     * Adds a FileFilter to this BrowseButtonListener.
     * 
     * @param filter          The FileFilter to add.
     */
    public void addFileFilter( FileFilter filter ){
      if(filter==null) return; // can't add nothing

      if( this.filters==null ) // initialize the vector if necessary
        this.filters=new Vector();

      boolean has=false;
      for( int i=0 ; i<this.filters.size() ; i++ ){
        if( this.filters.elementAt(i).getClass().isInstance(filter) ){
          has=true;
          break;
        }
      }
      
      if( !has) this.filters.add(filter); // add if it isn't already there
    }

    /**
     * Removes a FileFilter from this BrowseButtonListener.
     * 
     * @param filter          The FileFilter to remove.
     */
    public void removeFileFilter( FileFilter filter ){
      if(filter==null) return; // can't remove nothing

      if( this.filters==null ) return; // can't remove from nothing

      for( int i=0 ; i<this.filters.size() ; i++ ){
        if( this.filters.elementAt(i).getClass().isInstance(filter) ){
          this.filters.remove(i);
          return;
        }
      }
    }

    /**
     * Implementation of ActionEvent for BrowseButtonListener.
     * If the JFileChooser is not yet configured, this will 
     * configure it.  Once the JFileChooser is configured, 
     * this method will display it according to the specified
     * configuration.
     *
     * @param evt            Unused parameter.  There is only one
     *                       button, thus only one event.
     */
    public void actionPerformed( ActionEvent evt){ 
        if(DEBUG)System.out.println("type="+this.type);
        String filename=null;
        // configure the filechooser if we are on the first time
        if(jfc==null){
            jfc=new JFileChooser(def_filename);
            if(def_filename!=null){
                jfc.setCurrentDirectory(new File(def_filename));
            }
            this.configureFileChooser();
            if( this.filters!=null && this.filters.size()>0 ){
              for( int i=0 ; i<this.filters.size() ; i++ )
                jfc.addChoosableFileFilter( (FileFilter)filters.elementAt(i) );
              if(defaultfilter != null)
                jfc.setFileFilter(defaultfilter);
            }
        }
        
        // set the text from the textbox
        filename=textbox.getText();
        if(filename!=null && filename.length()>0){
            File file=new File(filename);
            jfc.setCurrentDirectory(file);
            if( this.type==DIR_ONLY){
                jfc.setSelectedFile(file);
            }else if( !file.isDirectory() ){
                jfc.setSelectedFile(file);
            }
            filename=null;
        }

        // display the filechooser
        int returnVal=JFileChooser.CANCEL_OPTION;
        if( this.type==LOAD_FILE || this.type==LOAD_MULTI ){
            returnVal=jfc.showOpenDialog(null);
        }else if( this.type==DIR_ONLY ){
            returnVal=jfc.showDialog(null,"Select");
        }else if( this.type==SAVE_FILE ){
            returnVal=jfc.showSaveDialog(null);
        }

        // deal with the return value
        if( returnVal==( JFileChooser.APPROVE_OPTION) ){
            if(this.type==LOAD_MULTI){
                // do something special
            }else{
                File file= jfc.getSelectedFile();
                if(this.type==SAVE_FILE){
                  //make sure the extension is correct - if one is not given, 
                  //use the default
                    filename=file.getAbsolutePath();
                    FileFilter filter = jfc.getFileFilter();
                    
                    //only the RobustFileFilters have the built-in capability 
                    //of appending the right extension onto the file name 
                    if(filter instanceof RobustFileFilter)
                    {
                      RobustFileFilter robustFilter = (RobustFileFilter)filter;
                      //if filename is no good
                      if(!(robustFilter.acceptFileName(filename))) 
                        filename = robustFilter.appendExtension(filename);
                    }
                    
                    textbox.setText( filename );
                }else if(this.type==LOAD_FILE || this.type==DIR_ONLY){
                    if(file.exists()){
                        filename=file.getAbsolutePath();
                        textbox.setText( filename );
                    }
                }
            }
        }
    }

    private void configureFileChooser(){
        // set the selection mode appropriately
        if( this.type==DIR_ONLY ){
            if(DEBUG)System.out.print("DIR");
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }else{ // all others are file only
            if(DEBUG)System.out.print("FILES");
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        }
        if(DEBUG)System.out.print("_ONLY("+JFileChooser.DIRECTORIES_ONLY+","
                                  +JFileChooser.FILES_ONLY+":"
                                  +jfc.getFileSelectionMode()+") ");

        // set the type of dialog properly
        if( this.type==SAVE_FILE ){
            if(DEBUG)System.out.print("SAVE");
            jfc.setDialogType(JFileChooser.SAVE_DIALOG);
        }else{ // all other are loading or selecting directories
            if(DEBUG)System.out.print("OPEN");
            jfc.setDialogType(JFileChooser.OPEN_DIALOG);
        }
        if(DEBUG)System.out.print("_DIALOG("+JFileChooser.SAVE_DIALOG+","
                                  +JFileChooser.OPEN_DIALOG+":"
                                  +jfc.getDialogType()+") ");

        // turn multi-selection on/off
        if(DEBUG)System.out.print("LOAD_MULTI="+(this.type==LOAD_MULTI)+" ");
        jfc.setMultiSelectionEnabled(this.type==LOAD_MULTI);
    }
}
