/*
 * File:  IntArrayPG.java
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
 *  Revision 1.21  2005/06/14 21:38:51  rmikk
 *  Now takes [] and quotes
 *
 *  Revision 1.20  2005/06/14 18:45:32  rmikk
 *  Returned "" in place of null
 *
 *  Revision 1.19  2004/05/11 18:23:49  bouzekc
 *  Added/updated javadocs and reformatted for consistency.
 *
 *  Revision 1.18  2004/03/15 03:28:40  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.17  2004/03/12 21:13:48  bouzekc
 *  Added clear() method.
 *
 *  Revision 1.16  2004/03/12 20:55:41  bouzekc
 *  Code reformat and added javadocs.
 *
 *  Revision 1.15  2003/12/15 02:10:48  bouzekc
 *  Removed unused imports.
 *
 *  Revision 1.14  2003/11/19 04:13:22  bouzekc
 *  Is now a JavaBean.
 *
 *  Revision 1.13  2003/10/11 19:24:33  bouzekc
 *  Removed declaration of "ParamUsesString" as the superclass declares it
 *  already.  Removed clone() definition as the superclass implements it
 *  using reflection.
 *
 *  Revision 1.11  2003/10/07 18:38:51  bouzekc
 *  Removed declaration of "implements ParamUsesString" as the
 *  StringEntryPG superclass now declares it.
 *
 *  Revision 1.10  2003/09/13 23:29:47  bouzekc
 *  Moved calls from setValid(true) to validateSelf().
 *
 *  Revision 1.9  2003/08/15 23:50:05  bouzekc
 *  Modified to work with new IParameterGUI and ParameterGUI
 *  classes.  Commented out testbed main().
 *
 *  Revision 1.8  2003/06/06 18:53:37  pfpeterson
 *  Now extends StringEntryPG and implements ParamUsesString.
 *
 *  Revision 1.7  2003/04/14 20:57:14  pfpeterson
 *  Added method to get value as int[].
 *
 *  Revision 1.6  2003/03/03 16:32:06  pfpeterson
 *  Only creates GUI once init is called.
 *
 *  Revision 1.5  2002/11/27 23:22:42  pfpeterson
 *  standardized header
 *
 *  Revision 1.4  2002/10/23 19:03:03  pfpeterson
 *  Fixed a bug where the parameter did not work with a null value.
 *
 *  Revision 1.3  2002/10/07 15:27:40  pfpeterson
 *  Another attempt to fix the clone() bug.
 *
 *  Revision 1.2  2002/09/30 15:20:50  pfpeterson
 *  Update clone method to return an object of this class.
 *
 *  Revision 1.1  2002/06/06 16:14:33  pfpeterson
 *  Added to CVS.
 *
 *
 */
package DataSetTools.parameter;

import gov.anl.ipns.Util.Numeric.IntList;
import gov.anl.ipns.Util.StringFilter.IntArrayFilter;


/**
 * This is class is to deal with Integer Arrays. Its value is stored as a
 * String.
 */
public class IntArrayPG extends StringEntryPG {
  //~ Static fields/initializers ***********************************************

  private static final String TYPE = "IntArray";

  //~ Constructors *************************************************************

  /**
   * Creates a new IntArrayPG object.
   *
   * @param name The name of this PG.
   * @param value The initial value of this PG.
   */
  public IntArrayPG( String name, Object value ) {
    super( name, value );
    this.setType( TYPE );
    FILTER = new IntArrayFilter(  );
  }

  /**
   * Creates a new IntArrayPG object.
   *
   * @param name The name of this PG.
   * @param value The initial value of this PG.
   * @param valid Whether this PG should be considered initially valid.
   */
  public IntArrayPG( String name, Object value, boolean valid ) {
    super( name, value, valid );
    this.setType( TYPE );
    FILTER = new IntArrayFilter(  );
  }

  //~ Methods ******************************************************************

  /**
   * @return The array value of this PG.
   */
  public int[] getArrayValue(  ) {
    String svalue = ( String )getValue(  );

    if( ( svalue == null ) || ( svalue.length(  ) <= 0 ) ) {
      return null;
    } else {
      return IntList.ToArray( svalue );
    }
  }

  /**
   * Sets the value of this PG using a String.
   *
   * @param val The new value.
   */
  public void setStringValue( String val ) {
    this.setValue( val );
  }

  /**
   * @return The value of this PG in String format.
   */
  public String getStringValue(  ) {
    return ( String )super.getValue(  );
  }

  /**
   *   Returns the string representation of the intList(Set) or ""
   */
  public Object getValue(){
    String S = getStringValue().trim();
    
    if( S == null)
       return "";
    if( S.length() <1)
       return "";
    if( S.startsWith("["))
      S=S.substring(1);
    if( S.endsWith("]"))
      S = S.substring(0, S.length()-1);
    super.setValue(S);
    return S;
  }
  /**
   * Sets the value of this PG.
   *
   * @param val The new value.
   */
  public void setValue( Object val ) {
    if( this.getInitialized(  ) ) {
      super.setEntryValue( val );
    }

    super.setValue( val );
  }

  /**
   * Used to clear out the PG.  This sets the internal value to an empty
   * String.
   */
  public void clear(  ) {
    setStringValue( "" );
  }

  /*
   * Testbed.
   */
  public static void main( String[] args ) {
    IntArrayPG fpg;

    fpg = new IntArrayPG( "a", "0:1" );
    System.out.println( fpg );
    fpg.initGUI( null );
    fpg.showGUIPanel(  );
    fpg = new IntArrayPG( "b", "0:2" );
    System.out.println( fpg );
    fpg.setEnabled( false );
    fpg.initGUI( null );
    fpg.showGUIPanel(  );
    fpg = new IntArrayPG( "c", "0:3", false );
    System.out.println( fpg );
    fpg.setEnabled( false );
    fpg.initGUI( null );
    fpg.showGUIPanel(  );
    fpg = new IntArrayPG( "d", "0:4", true );
    fpg.clear(  );
    System.out.println( fpg );
    fpg.setDrawValid( true );
    fpg.initGUI( null );
    fpg.showGUIPanel(  );
  }
}
