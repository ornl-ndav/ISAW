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

import DataSetTools.util.StringUtil;
import javax.swing.*;
import java.awt.*;
import java.util.Vector;
import java.io.File;
import DataSetTools.util.PGActionListener;

public class LoadFileArrayPG extends VectorPG{

  public LoadFileArrayPG( String name, Object val){ 
    super( name, val );
    setParam( new LoadFilePG("Enter File to Load", null) );
  }

  public LoadFileArrayPG( String name, Object val, boolean valid ) {
    super( name, val, valid );
    setParam( new LoadFilePG("Enter File to Load", null) );
  }

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
  public Object clone(){
    LoadFilePG faap = new LoadFilePG( getName(), getValue());
    return (Object)faap;
  }       

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
      File temp = null;
      boolean allLoadable = true;

      for( int i = 0; i < fileElements.size(  ) && allLoadable; i++ ) {
        temp = new File( fileElements.get( i ).toString(  ) );
        if( !temp.exists(  ) || temp.isDirectory(  )
                             || !temp.canRead(  ) ) {
          allLoadable = false;
        }
      }
      setValid( allLoadable );
    }
  }
}
