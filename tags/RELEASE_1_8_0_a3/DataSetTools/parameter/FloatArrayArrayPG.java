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
 * Revision 1.16  2005/06/14 18:45:33  rmikk
 * Returned "" in place of null
 *
 * Revision 1.15  2005/06/10 15:27:42  rmikk
 * Gave a more descriptive label for what is to be entered
 *
 * Revision 1.14  2005/06/07 15:05:47  rmikk
 * Made the initial button better  represent the data to be entered
 *
 * Revision 1.13  2004/05/11 18:23:48  bouzekc
 * Added/updated javadocs and reformatted for consistency.
 *
 * Revision 1.12  2003/12/16 00:05:59  bouzekc
 * Removed unused imports.
 *
 * Revision 1.11  2003/10/11 19:19:16  bouzekc
 * Removed clone() as the superclass now implements it using reflection.
 *
 * Revision 1.10  2003/09/09 23:06:28  bouzekc
 * Implemented validateSelf().
 *
 * Revision 1.9  2003/08/28 03:38:40  bouzekc
 * Changed innerParameter assignment to call to setParam().
 *
 * Revision 1.8  2003/08/28 02:32:36  bouzekc
 * Modified to work with new VectorPG.
 *
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

/**
 * Subclass of VectorPG to deal with two-dimensional float arrays.
 */
public class FloatArrayArrayPG extends VectorPG {
  //~ Constructors *************************************************************

  /**
   * Creates a new FloatArrayArrayPG object.
   *
   * @param name The name of this FloatArrayArrayPG.
   * @param val The initial value of this FloatArrayArrayPG.
   */
  public FloatArrayArrayPG( String name, Object val ) {
    super( name, val );
    setParam( new FloatArrayPG( "Enter a Float Array", null ) );
  }

  /**
   * Creates a new FloatArrayArrayPG object.
   *
   * @param name The name of this FloatArrayArrayPG.
   * @param val The initial value of this FloatArrayArrayPG.
   * @param valid True if this FloatArrayArrayPG should initially be considered
   *        valid.
   */
  public FloatArrayArrayPG( String name, Object val, boolean valid ) {
    super( name, val, valid );
    setParam( new FloatArrayPG( "Enter Float Array", null ) );
  }

  //~ Methods ******************************************************************

  /*
   * Testbed
   */
  /*public static void main( String args[] ){
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
     } */

  /**
   * Validates this FloatArrayArrayPG.  A FloatArrayArrayPG is considered valid
   * if it contains a FloatArrayPG with all Float elements.
   */
  public void validateSelf(  ) {
    FloatArrayPG fpg = ( FloatArrayPG )getParam(  );

    fpg.validateElements( new Float( 0.0f ).getClass(  ) );
    setValid( fpg.getValid(  ) );
  }
  
  public Object getValue(){
    java.util.Vector S =(java.util.Vector)super.getValue();
    if( S == null)
       return new java.util.Vector();
    return S; 
  }
}
