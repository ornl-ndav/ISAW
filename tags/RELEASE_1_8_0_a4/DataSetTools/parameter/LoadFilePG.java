/*
 * File:  LoadFilePG.java
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
 * Modified:
 *
 *  $Log$
 *  Revision 1.13  2004/05/11 18:23:53  bouzekc
 *  Added/updated javadocs and reformatted for consistency.
 *
 *  Revision 1.12  2003/12/15 02:20:38  bouzekc
 *  Removed unused imports.
 *
 *  Revision 1.11  2003/11/19 04:13:22  bouzekc
 *  Is now a JavaBean.
 *
 *  Revision 1.10  2003/10/11 19:19:16  bouzekc
 *  Removed clone() as the superclass now implements it using reflection.
 *
 *  Revision 1.9  2003/09/09 23:06:28  bouzekc
 *  Implemented validateSelf().
 *
 *  Revision 1.8  2003/08/15 23:50:05  bouzekc
 *  Modified to work with new IParameterGUI and ParameterGUI
 *  classes.  Commented out testbed main().
 *
 *  Revision 1.7  2003/05/29 21:40:24  bouzekc
 *  Removed the init(Vector init_values) method.  Now uses
 *  BrowsePG's init method, and sets the file selection type
 *  in the constructor.
 *
 *  Revision 1.6  2003/03/03 16:32:06  pfpeterson
 *  Only creates GUI once init is called.
 *
 *  Revision 1.5  2002/11/27 23:22:42  pfpeterson
 *  standardized header
 *
 *  Revision 1.4  2002/10/23 18:50:44  pfpeterson
 *  Now supports a javax.swing.filechooser.FileFilter to be specified
 *  for browsing options. Also fixed bug where it did not automatically
 *  switch to the data directory if no value was specified.
 *
 *  Revision 1.3  2002/10/07 15:27:42  pfpeterson
 *  Another attempt to fix the clone() bug.
 *
 *  Revision 1.2  2002/09/30 15:20:52  pfpeterson
 *  Update clone method to return an object of this class.
 *
 *  Revision 1.1  2002/07/15 21:26:08  pfpeterson
 *  Added to CVS.
 *
 *
 */
package DataSetTools.parameter;

import DataSetTools.components.ParametersGUI.BrowseButtonListener;

import java.io.File;


/**
 * This is a particular case of the BrowsePG used for loading a single file.
 * The value is a string.
 */
public class LoadFilePG extends BrowsePG {
  //~ Static fields/initializers ***********************************************

  private static String TYPE = "LoadFile";

  //~ Constructors *************************************************************

  /**
   * Creates a new LoadFilePG object.
   *
   * @param name The name of this LoadFilePG.
   * @param value The value of this LoadFilePG.
   */
  public LoadFilePG( String name, Object value ) {
    this( name, value, false );
    this.setDrawValid( false );
  }

  /**
   * Creates a new LoadFilePG object.
   *
   * @param name The name of this LoadFilePG.
   * @param value The value of this LoadFilePG.
   * @param valid True if this LoadFilePG should be considered initially valid.
   */
  public LoadFilePG( String name, Object value, boolean valid ) {
    super( name, value, valid );
    this.setType( TYPE );
    super.choosertype = BrowseButtonListener.LOAD_FILE;
  }

  //~ Methods ******************************************************************

  /*
   * Testbed.
   */
  /*public static void main(String args[]){
     LoadFilePG fpg;
     //y position and delta y, so that multiple windows can
     //be displayed without too much overlap
     int y=0, dy=70;
  
     String defString="/IPNShome/bouzekc/IsawProps.dat";
     fpg=new LoadFilePG ("Enabled, not valid, no filters",defString);
     System.out.println(fpg);
     fpg.initGUI(null);
     fpg.showGUIPanel(0,y);
     y+=dy;
  
     //disabled browse button GUI
     fpg=new LoadFilePG ("Disabled, not valid, no filters",defString);
     System.out.println(fpg);
     fpg.setEnabled(false);
     fpg.initGUI(null);
     fpg.showGUIPanel(0,y);
     y+=dy;
     fpg=new LoadFilePG ("Disabled, not valid, no filters",defString,false);
     System.out.println(fpg);
     fpg.setEnabled(false);
     fpg.initGUI(null);
     fpg.showGUIPanel(0,y);
     y+=dy;
     fpg=new LoadFilePG ("Valid, enabled, no filters",defString,true);
     System.out.println(fpg);
     fpg.setDrawValid(true);
     fpg.initGUI(null);
     fpg.showGUIPanel(0,y);
     fpg=new LoadFilePG ("Enabled, not valid, multiple filters",defString);
     System.out.println(fpg);
     //add some FileFilters
     fpg.addFilter(new ExpFilter());
     fpg.addFilter(new IntegrateFilter());
     fpg.addFilter(new MatrixFilter());
     fpg.initGUI(null);
     fpg.showGUIPanel(0,y);
     y+=dy;
     fpg=new LoadFilePG("Enabled, not valid, one filter",defString);
     System.out.println(fpg);
     //add some FileFilters
     fpg.addFilter(new IntegrateFilter());
     fpg.initGUI(null);
     fpg.showGUIPanel(0,y);
     y+=dy;
     }*/

  /**
   * Validates this LoadFilePG.  A LoadFilePG is considered valid if getValue()
   * returns a non-null String and the String references  a non-directory,
   * existing, and readable File
   */
  public void validateSelf(  ) {
    Object val = getValue(  );

    if( val != null ) {
      File file = new File( val.toString(  ) );

      if( file.exists(  ) && !file.isDirectory(  ) && file.canRead(  ) ) {
        setValid( true );
      } else {
        setValid( false );
      }
    } else {
      setValid( false );
    }
  }
}
