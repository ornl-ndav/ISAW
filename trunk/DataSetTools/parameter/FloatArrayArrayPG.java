/*
 * File:  FloatArrayArrayPG.java 
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
 * Modified:
 *
 * $Log$
 * Revision 1.7  2003/08/15 23:50:04  bouzekc
 * Modified to work with new IParameterGUI and ParameterGUI
 * classes.  Commented out testbed main().
 *
 * Revision 1.6  2003/06/23 20:17:52  bouzekc
 * Fixed GPL file name.
 *
 * Revision 1.5  2003/06/23 16:12:24  bouzekc
 * Reformatted for consistent indenting.
 *
 * Revision 1.4  2003/06/23 14:50:31  bouzekc
 * Removed duplicate inner ActionListener class.  Now uses
 * PGActionListener.
 *
 * Revision 1.3  2003/06/18 20:36:40  pfpeterson
 * Changed calls for NxNodeUtils.Showw(Object) to
 * DataSetTools.util.StringUtil.toString(Object)
 *
 * Revision 1.2  2003/06/09 22:29:18  rmikk
 * Added a clone method
 *
 * Revision 1.1  2003/05/25 19:10:28  rmikk
 * Initial checkin.  This ParameterGUI allows for entering
 *   a Vector of Vector of floats
 *
 */
package DataSetTools.parameter;

import DataSetTools.util.StringUtil;
import javax.swing.*;
import java.awt.*;
import DataSetTools.util.PGActionListener;

/**
*   This ParameterGUI allows for users to enter a Vector whose elements are Vectors
*   of Float values
*/
public class FloatArrayArrayPG extends VectorPG{

  public FloatArrayArrayPG( String Prompt, Object value){ 
    super( new FloatArrayPG("Enter FloatArray",null),"Enter FloatArray List");
    setValue( value);
  }

  /*
   * Testbed
   */
  public static void main( String args[] ){
    JFrame jf = new JFrame("Test");
    jf.getContentPane().setLayout( new GridLayout( 1,2));
    FloatArrayArrayPG IaPg = new FloatArrayArrayPG( "Enter FloatArray list", null);
    IaPg.initGUI(null);
    jf.getContentPane().add(IaPg.getGUIPanel());
    JButton  jb = new JButton("Result");
    jf.getContentPane().add(jb);
    jb.addActionListener( new PGActionListener( IaPg));
    jf.setSize( 600,100);
    jf.invalidate();
    jf.show();
  } 
  public Object clone(){
    FloatArrayArrayPG faap = new FloatArrayArrayPG( getName(), getValue());
    return (Object)faap;
  }     
}
