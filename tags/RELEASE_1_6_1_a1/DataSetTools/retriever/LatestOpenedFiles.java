/*
 * File:  LatestOpenedFiles.java
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
 * Contact :  Ruth Mikkelson <mikkelsonr@uwstout.edu>
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
 * $Log$
 * Revision 1.3  2003/12/14 18:22:50  rmikk
 * Added the new DataSets to the ISAW program completely. Now, the datablock attributes
 * should appear
 *
 * Revision 1.2  2003/12/11 19:27:15  rmikk
 * Printed a stack trace if an exception occurs while loading
 *    the file
 *
 * Revision 1.1  2003/11/30 19:09:55  rmikk
 * Initial Checkin
 *
 */

package DataSetTools.retriever;
import java.util.prefs.*;
import Command.*;
import DataSetTools.util.*;
import DataSetTools.dataset.*;
import java.awt.event.*;
import javax.swing.*;
import IsawGUI.*;

/**
  *  This class contains utilities to save, retrieve, act on, and add DataSet 
  *  files to menu bars. The file names are saved in the User preferences for 
  *  the class DataSetTools.retriever.Retriever with keys Filex, where 
  *  x = 0,1,2, or 3.
  */
public class LatestOpenedFiles{

  public static final int NSavedFiles = 4;
  private static final String NO_SUCH_FILE = "No Such File";


  /**
    * This method gets the last four Data Set files used by the current user 
    * and appends them to the end of the JMenu, Menuu.  Listeners are added 
    * to these JMenuItems and the Listener adds the data sets from this file 
    * to the tree and adds IOBs
    * @param Menuu  The menu to which these file names are appended
    * @param tree  the JDataTree to which the ActionListener adds the new data 
    *               sets
    * @param IOBs  the IObserver that is to be added to each new data set
    */
  public static void setUpMenuItems( JMenu Menuu , JDataTree tree , 
                                                           IObserver IOBs ){
     
     Preferences pref = null;
     try{
        pref = Preferences.userNodeForPackage( 
                   Class.forName( "DataSetTools.retriever.Retriever" ) );
     }catch( Exception s1 ){
       ( new JOptionPane() ).showMessageDialog( null , 
                                                "No Preferences " + s1 );
        return;
     }
 
     Menuu.addSeparator();
     
     for( int i = 0 ; i < NSavedFiles ; i++ ){

        String filname = pref.get( "File" + i , NO_SUCH_FILE );
        
        if( filname != NO_SUCH_FILE ){

           JMenuItem jmi = new JMenuItem( Mangle( filname ) );
           Menuu.add( jmi );
           MyActionListener actList =new MyActionListener( tree , filname , 
                                                                 IOBs ) ;
           jmi.addActionListener(actList );
        }
     }
    
  }//setUpMenuItems


  /**
    *  This method adds a new filename to the DataSetTools.retriever. 
    *  Retriever's user preferences with key Filex where x is 0,1,2,or 3.  
    *  Duplicates are eliminated
    * @param Filename  the new file name to be saved to the appropriate 
    *     preferences
    * @return true if this was a new file
    */
  public static boolean addNewOpenedFile( String Filename ){
    
     Preferences pref = null;
     try{
        pref = Preferences.userNodeForPackage( 
                   Class.forName( "DataSetTools.retriever.Retriever" ) );
     }catch( Exception s1 ){
        ( new JOptionPane() ).showMessageDialog( null , "No Preferences " +
                                       s1 );
          return false;
     }
    
     if( isInPrefs( Filename , pref ) )
        return false;

     //Move all the Preference key values up by "1"

     String filname;
     for( int i = NSavedFiles - 2 ; i >= 0 ; i-- ){

        filname = pref.get( "File" + i , NO_SUCH_FILE );

        if( filname != NO_SUCH_FILE )
           pref.put( "File" +  ( i + 1 ) , filname );
     }

     pref.put( "File0" , Filename );
     return true;
  }



  /**
    *  Attempts to replace large parts of the path by ... so final name
    *  name has a length 40 characters
    */
  public static String Mangle( String fName ){

     String fName1 = fName.replace( '\\' , '/' );
     int i = fName1.lastIndexOf( '/' );

     if( i < 0 )
        return fName;

     if( fName.length() < 40 )
        return fName;

     int L = 40-( fName.length() - i )-3;

     if( L < 0 )
        return fName.substring( i + 1 );
     
     String res = fName.substring( 0 , L/2 ) + "..." + 
                       fName.substring( i-L/2 , i + 1 ) + 
                                    fName.substring( i + 1 );
     return res;
  }


  // Returns true if the current FileName is already in pref's 
  private static boolean isInPrefs( String FileName , Preferences pref ){

     for( int i = 0 ; i< NSavedFiles ; i++ )

        if( pref.get( "File" + i , NO_SUCH_FILE ).equals( FileName ) )
           return true;

     return false;
  }

}

//Handles the necessary actions needed to service the selection of a given
//  filename in the menu
class MyActionListener implements ActionListener{

  JDataTree tree;
  IObserver IOBs;
  String filename;

  public MyActionListener( JDataTree tree , String filname , IObserver IOBs ){
     this.tree = tree;
     this.IOBs = IOBs;
     filename = filname;
  }
  public void actionPerformed( ActionEvent evt ){
    
     DataSet[] DSS = null;
     try{
        DSS = ScriptUtil.load( filename );
        filename = filename.replace( '\\' , '/' );
        int l = filename.lastIndexOf( '/' );
        String filename1=filename;
        if( l >= 0 ) 
           filename1 = filename.substring( l + 1 );
      
        //tree.addExperiment( DSS , filename1 );

        if( IOBs != null )
             IOBs.update( tree, DSS);
  

     }catch( Exception ss ){

        ( new JOptionPane() ).showMessageDialog( null , "Error :" + ss );
        ss.printStackTrace();
        SharedData.addmsg( "Error :" + ss ); 
     }
  }


 }//MyActionListener



