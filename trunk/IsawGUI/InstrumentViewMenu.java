/*
 * File: InstrumentViewMenu.java
 *
 * Copyright (C) 2002, Peter F. Peterson
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
 * $Log$
 * Revision 1.3  2003/02/13 21:45:13  pfpeterson
 * Removed calls to deprecated function fixSeparator.
 *
 * Revision 1.2  2002/12/11 15:57:20  pfpeterson
 * Fixed small bug where the tag used in the menu was not 'trim()'ed.
 *
 * Revision 1.1  2002/12/03 17:44:02  pfpeterson
 * Added to CVS
 *
 */
package IsawGUI;

import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import DataSetTools.util.*;

/**
 * This class is built to control the user configurable links menu in
 * the main ISAW window. It looks for the links to be specified in
 * ISAW_HOME/Databases/InstLinks.txt. Dealing with the ActionEvents
 * from menu items being selected is also dealt with by this class.
 *
 * The configuration file should have a menu item on each line with
 * the first set of non-whitespace characters being the URL and
 * everything after it being the menu item name. If a line has
 * 'SEPARATOR' first a separator will be added to the menu rather than
 * a menu item.
 */
class InstrumentViewMenu extends JMenu implements ActionListener{
  private static final String         SEPARATOR   = "SEPARATOR";
  private static final String         filename    = "Databases/InstLinks.txt";
  private static       boolean        DEBUG       = false;
  private static       boolean        initialized = false;
  private static       String[]       tags        = null;
  private static       Hashtable      urls        = null;
  private static       BrowserControl bc          = null;

  /**
   * The only available constructor. It tries to load list of links
   * from the configuration file and will throw an exception if
   * ANYTHING goes wrong so the caller can just skip the menu
   * altogether.
   *
   * @param title the name of the JMenu to be created
   *
   * @throws InstantiationException if anything goes wrong when
   * reading the file
   */
  InstrumentViewMenu( String title ) throws InstantiationException{
    // let the parent do most of the work
    super(title);
    
    // only read the file if the static information isn't already initialized
    if(!initialized){
      // get the filename
      String file=SharedData.getProperty("ISAW_HOME")+"/"+filename;
      file=FilenameUtil.setForwardSlash(file);    
      if(DEBUG) System.out.println("FILE:"+file);

      // read in the information from the file
      try{
        readInfo(file);
        initialized=true;
      }catch(IOException e){
        throw new InstantiationException("Error in InstLinks.txt:"
                                         +e.getMessage());
      }
    }

    // use the static information to set up the menu
    if( initialized ){
      if(tags==null)
        throw new InstantiationException("Error: menu labels not set");
      if(urls==null)
        throw new InstantiationException("Error: urls not set");

      JMenuItem tempMenu=null;
      for( int i=0 ; i<tags.length ; i++ ){
        if(DEBUG) System.out.println(tags[i]+" "+urls.get(tags[i]));
        if(tags[i].equals(SEPARATOR)){
          this.addSeparator();
        }else{
          tempMenu=new JMenuItem(tags[i]);
          tempMenu.addActionListener(this);
          this.add(tempMenu);
        }
      }
      tempMenu=null;
    }else{
      throw new InstantiationException("error in initialization");
    }
  }

  /**
   * Read in the information from the configuration file.
   *
   * @param file the name of the file to read the information from
   *
   * @throws IOException passed up from the TextFileReader used to
   * parse the file.
   */
  private void readInfo(String file) throws IOException{
    // set up some temporary variables
    TextFileReader tfr     = new TextFileReader(file);
    Vector         tagV    = new Vector();
    Vector         urlV    = new Vector();
    String         tempURL = null;
    String         tempTAG = null;

    // read the whole file
    while(!tfr.eof()){
      // read in the line
      tempURL=tfr.read_String();
      tempTAG=tfr.read_line();
      tempTAG=tempTAG.trim();

      // check if we are supposed to add a separator
      if(tempURL.equals(SEPARATOR)){
        urlV.add("");
        tagV.add(SEPARATOR);
      }else{
        urlV.add(tempURL);
        tagV.add(tempTAG);
      }
    }

    // clean up the file access
    tfr.close();
    tfr=null;

    // fill the tags array and urls hash
    if(tagV.size()==urlV.size()){
      tags=new String[tagV.size()];
      urls=new Hashtable(tagV.size());
      for( int i=0 ; i<tags.length ; i++ ){
        tags[i]=(String)tagV.elementAt(i);
        urls.put(tags[i],(String)urlV.elementAt(i));
      }
    }

    // free up some memory
    file=null;
    tagV=null;
    urlV=null;
    tempTAG=null;
    tempURL=null;
  }

  /**
   * This converts the menu item into a URL and displays it.
   */
  public void actionPerformed( ActionEvent ev ){
    // get the url
    String url=(String)urls.get(ev.getActionCommand());
    if(DEBUG) System.out.println("ACTION:"+ev.getActionCommand()+"->"+url);

    // create the BrowserControl if it doesn't already exist
    if(bc==null) bc=new BrowserControl();

    // display the URL
    bc.displayURL(url);
  }

  /**
   * Main method for testing purposes only. Tries to create an
   * instance with the debug flag on.
   */
  public static void main(String[] args){
    try{
      InstrumentViewMenu.DEBUG=true;
      InstrumentViewMenu ivm=new InstrumentViewMenu("TEST MENU");
    }catch(InstantiationException e){
      System.out.println(e.getMessage());
    }
  }

}
