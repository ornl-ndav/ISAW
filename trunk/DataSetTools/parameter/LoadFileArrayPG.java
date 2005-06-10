/*
 * File:  LoadFileArrayPG.java
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
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA and by
 * the National Science Foundation under grant number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 *
 * $Log$
 * Revision 1.16  2005/06/10 15:27:42  rmikk
 * Gave a more descriptive label for what is to be entered
 *
 * Revision 1.15  2005/06/07 15:05:48  rmikk
 * Made the initial button better  represent the data to be entered
 *
 * Revision 1.14  2004/05/11 18:23:53  bouzekc
 * Added/updated javadocs and reformatted for consistency.
 *
 * Revision 1.13  2003/12/16 00:06:00  bouzekc
 * Removed unused imports.
 *
 * Revision 1.12  2003/10/11 19:19:16  bouzekc
 * Removed clone() as the superclass now implements it using reflection.
 *
 * Revision 1.11  2003/09/09 23:06:28  bouzekc
 * Implemented validateSelf().
 *
 * Revision 1.10  2003/08/28 03:38:40  bouzekc
 * Changed innerParameter assignment to call to setParam().
 *
 * Revision 1.9  2003/08/28 02:32:36  bouzekc
 * Modified to work with new VectorPG.
 *
 * Revision 1.8  2003/08/15 23:50:05  bouzekc
 * Modified to work with new IParameterGUI and ParameterGUI
 * classes.  Commented out testbed main().
 *
 * Revision 1.7  2003/06/23 20:18:30  bouzekc
 * Added GPL info.
 *
 */
package DataSetTools.parameter;

import java.io.File;

import java.util.Vector;


/**
 * Subclass of VectorPG to deal with lists of file names to load.
 */
public class LoadFileArrayPG extends VectorPG {
  //~ Constructors *************************************************************

  /**
   * Creates a new LoadFileArrayPG object.
   *
   * @param name The name of this LoadFileArrayPG.
   * @param val The value of this LoadFileArrayPG.
   */
  public LoadFileArrayPG( String name, Object val ) {
    super( name, val );
    setParam( new LoadFilePG( "Enter File to Load", null ) );
  }

  /**
   * Creates a new LoadFileArrayPG object.
   *
   * @param name The name of this LoadFileArrayPG.
   * @param val The value of this LoadFileArrayPG.
   * @param valid True if this LoadFileArrayPG should be considered initially
   *        valid.
   */
  public LoadFileArrayPG( String name, Object val, boolean valid ) {
    super( name, val, valid );
    setParam( new LoadFilePG( "Enter File to Load", null ) );
  }

  //~ Methods ******************************************************************

  /*
   * Testbed.
   */
  /*public static void main( String args[] ){
     JFrame jf = new JFrame("Test");
     jf.getContentPane().setLayout( new GridLayout( 1,2));
     LoadFileArrayPG IaPg = new LoadFileArrayPG( "Enter File list", null);
     IaPg.initGUI(null);
     jf.getContentPane().add(IaPg.getGUIPanel());
     JButton  jb = new JButton("Result");
     jf.getContentPane().add(jb);
     jb.addActionListener( new PGActionListener( IaPg));
     jf.setSize( 500,100);
     jf.invalidate();
     jf.show();
     }*/

  /**
   * Validates this LoadFileArrayPG.  An LoadFileArrayPG is considered valid if
   * it contains all String elements that correspond to existing non-directory
   * readable files.
   */
  public void validateSelf(  ) {
    validateElements( new String(  ).getClass(  ) );

    //we need to be sure all the files exist and are readable real files
    if( getValid(  ) ) {
      //from the superclass method, we already know at this point that the
      //value is a non-null Vector.
      Vector fileElements = ( Vector )getValue(  );
      File temp           = null;
      boolean allLoadable = true;

      for( int i = 0; ( i < fileElements.size(  ) ) && allLoadable; i++ ) {
        temp = new File( fileElements.get( i ).toString(  ) );

        if( !temp.exists(  ) || temp.isDirectory(  ) || !temp.canRead(  ) ) {
          allLoadable = false;
        }
      }

      setValid( allLoadable );
    }
  }
}
