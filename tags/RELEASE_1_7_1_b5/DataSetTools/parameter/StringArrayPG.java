/*
 * File:  StringArrayPG.java
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
 * Revision 1.15  2004/05/11 18:23:55  bouzekc
 * Added/updated javadocs and reformatted for consistency.
 *
 * Revision 1.14  2003/12/16 00:06:00  bouzekc
 * Removed unused imports.
 *
 * Revision 1.13  2003/10/11 19:19:17  bouzekc
 * Removed clone() as the superclass now implements it using reflection.
 *
 * Revision 1.12  2003/09/09 23:06:31  bouzekc
 * Implemented validateSelf().
 *
 * Revision 1.11  2003/08/28 03:38:40  bouzekc
 * Changed innerParameter assignment to call to setParam().
 *
 * Revision 1.10  2003/08/28 02:32:36  bouzekc
 * Modified to work with new VectorPG.
 *
 * Revision 1.9  2003/08/15 23:50:06  bouzekc
 * Modified to work with new IParameterGUI and ParameterGUI
 * classes.  Commented out testbed main().
 *
 * Revision 1.8  2003/06/23 16:12:28  bouzekc
 * Reformatted for consistent indenting.
 *
 * Revision 1.7  2003/06/23 14:59:58  bouzekc
 * Fixed log message.  Now uses PGActionListener.
 *
 * Revision 1.6  2003/06/23 14:58:17  bouzekc
 * Removed duplicate inner ActionListener class.
 *
 * Revision 1.5  2003/06/18 20:36:41  pfpeterson
 * Changed calls for NxNodeUtils.Showw(Object) to
 * DataSetTools.util.StringUtil.toString(Object)
 *
 * Revision 1.4  2003/06/09 22:30:57  rmikk
 * Added Clone method
 *
 * Revision 1.3  2003/05/25 18:43:28  rmikk
 * Added GPL
 *
 */
package DataSetTools.parameter;

/**
 * Subclass of VectorPG to deal with one-dimensional String arrays.
 */
public class StringArrayPG extends VectorPG {
  //~ Constructors *************************************************************

  /**
   * Creates a new StringArrayPG object.
   *
   * @param name The name of this StringArrayPG.
   * @param val The value of this StringArrayPG.
   */
  public StringArrayPG( String name, Object val ) {
    super( name, val );
    setParam( new StringPG( "Enter String", "" ) );
  }

  /**
   * Creates a new StringArrayPG object.
   *
   * @param name The name of this StringArrayPG.
   * @param val The value of this StringArrayPG.
   * @param valid True if this StringArrayPG should be considered initially
   *        valid.
   */
  public StringArrayPG( String name, Object val, boolean valid ) {
    super( name, val, valid );
    setParam( new StringPG( "Enter String", "" ) );
  }

  //~ Methods ******************************************************************

  /*
   * Testbed.
   */
  /*public static void main( String args[] ){
     JFrame jf = new JFrame("Test");
     jf.getContentPane().setLayout( new GridLayout( 1,2));
     StringArrayPG IaPg = new StringArrayPG( "Enter String list", null);
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
   * Validates this StringArrayPG.  An StringArrayPG is considered valid if  it
   * contains all String elements.
   */
  public void validateSelf(  ) {
    validateElements( new String(  ).getClass(  ) );
  }
}
