/*
 * File:  IntegerPG.java
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
 *  Revision 1.17  2004/03/12 20:27:29  bouzekc
 *  Code reformat and added javadocs.
 *
 *  Revision 1.16  2003/12/15 02:10:49  bouzekc
 *  Removed unused imports.
 *
 *  Revision 1.15  2003/11/19 04:13:22  bouzekc
 *  Is now a JavaBean.
 *
 *  Revision 1.14  2003/10/11 19:24:33  bouzekc
 *  Removed declaration of "ParamUsesString" as the superclass declares it
 *  already.  Removed clone() definition as the superclass implements it
 *  using reflection.
 *
 *  Revision 1.12  2003/10/07 18:38:51  bouzekc
 *  Removed declaration of "implements ParamUsesString" as the
 *  StringEntryPG superclass now declares it.
 *
 *  Revision 1.11  2003/09/13 23:36:34  bouzekc
 *  Added call to validateSelf() in setValue().
 *
 *  Revision 1.10  2003/08/28 02:09:50  bouzekc
 *  Added a try...catch block to trap a NumberFormatException which occurred
 *  when a minus sign was typed in.  This occurred because of the large
 *  number of PropertyChangeEvents being fired.
 *
 *  Revision 1.9  2003/08/15 23:50:05  bouzekc
 *  Modified to work with new IParameterGUI and ParameterGUI
 *  classes.  Commented out testbed main().
 *
 *  Revision 1.8  2003/06/12 18:58:28  bouzekc
 *  Fixed bug with setting value.
 *
 *  Revision 1.7  2003/06/06 18:50:59  pfpeterson
 *  Now extends StringEntryPG and implements ParamUsesString.
 *
 *  Revision 1.6  2003/03/03 16:32:06  pfpeterson
 *  Only creates GUI once init is called.
 *
 *  Revision 1.5  2002/11/27 23:22:42  pfpeterson
 *  standardized header
 *
 *  Revision 1.4  2002/10/07 15:27:41  pfpeterson
 *  Another attempt to fix the clone() bug.
 *
 *  Revision 1.3  2002/09/30 15:20:51  pfpeterson
 *  Update clone method to return an object of this class.
 *
 *  Revision 1.2  2002/06/12 14:20:20  pfpeterson
 *  Added two convenience constructors to create the parameter
 *  with an int.
 *
 *  Revision 1.1  2002/06/06 16:14:34  pfpeterson
 *  Added to CVS.
 *
 *
 */
package DataSetTools.parameter;

import DataSetTools.util.IntegerFilter;


/**
 * This is a superclass to take care of many of the common details of
 * IntegerPGs.
 */
public class IntegerPG extends StringEntryPG {
  //~ Static fields/initializers ***********************************************

  private static final String TYPE = "Integer";

  //~ Constructors *************************************************************

  /**
   * Creates a new IntegerPG object.
   *
   * @param name The name of this PG.
   * @param value The initial value of this PG.
   */
  public IntegerPG( String name, Object value ) {
    super( name, value );
    FILTER = new IntegerFilter(  );
    this.setType( TYPE );
  }

  /**
   * Creates a new IntegerPG object.
   *
   * @param name The name of this PG.
   * @param value The initial value of this PG.
   * @param valid Whether this PG should be considered initially valid.
   */
  public IntegerPG( String name, Object value, boolean valid ) {
    super( name, value, valid );
    FILTER = new IntegerFilter(  );
    this.setType( TYPE );
  }

  /**
   * Creates a new IntegerPG object.
   *
   * @param name The name of this PG.
   * @param value The initial value of this PG.
   */
  public IntegerPG( String name, int value ) {
    this( name, new Integer( value ) );
  }

  /**
   * Creates a new IntegerPG object.
   *
   * @param name The name of this PG.
   * @param value The initial value of this PG.
   * @param valid Whether this PG should be considered initially valid.
   */
  public IntegerPG( String name, int value, boolean valid ) {
    this( name, new Integer( value ), valid );
  }

  //~ Methods ******************************************************************

  /**
   * Sets the value of this PG using a String consisting of an integer number.
   *
   * @param val The new value.
   *
   * @throws NumberFormatException If there are non-integer characters in the
   *         String.
   */
  public void setStringValue( String val ) throws NumberFormatException {
    if( getInitialized(  ) ) {
      super.setEntryValue( val );
    } else {
      this.setValue( new Integer( val.trim(  ) ) );
    }
  }

  /**
   * @return The String value.
   */
  public String getStringValue(  ) {
    return this.getValue(  ).toString(  );
  }

  /**
   * Overrides the default behavior. If passed null this sets the value to
   * Integer.MIN_VALUE.
   */
  public void setValue( Object val ) {
    Integer intval = null;

    if( val == null ) {
      intval = new Integer( Integer.MIN_VALUE );
    } else if( val instanceof Integer ) {
      intval = ( Integer )val;
    } else if( val instanceof Float ) {
      intval = new Integer( Math.round( ( ( Float )val ).floatValue(  ) ) );
    } else if( val instanceof Double ) {
      intval = new Integer( 
          Math.round( ( int )( ( Double )val ).doubleValue(  ) ) );
    } else if( val instanceof String ) {
      this.setStringValue( ( String )val );

      return;
    } else {
      throw new ClassCastException( 
        "Could not coerce " + val.getClass(  ).getName(  ) +
        " into an Integer" );
    }

    if( this.getInitialized(  ) ) {
      super.setEntryValue( intval );
    }

    super.setValue( intval );
  }

  /**
   * Override the default method.
   */
  public Object getValue(  ) {
    Object val        = super.getValue(  );
    Integer bigIntVal = null;

    if( val instanceof String ) {
      try {
        bigIntVal = new Integer( ( String )val );
      } catch( NumberFormatException nfe ) {
        //probably just a minus sign.  We will return a 0.
        bigIntVal = new Integer( 0 );
      }
    } else if( val instanceof Integer ) {
      bigIntVal = ( Integer )val;
    } else {
      throw new ClassCastException( 
        "Could not coerce " + val.getClass(  ).getName(  ) +
        " into an Integer" );
    }

    return bigIntVal;
  }

  /**
   * Returns a primitive integer version of the value.
   */
  public int getintValue(  ) {
    return ( ( Integer )this.getValue(  ) ).intValue(  );
  }

  /**
   * Convenience method to allow for passing a primitive int.
   */
  public void setintValue( int value ) {
    this.setValue( new Integer( value ) );
  }

  /*
   * Testbed.
   */
  /*public static void main(String args[]){
     IntegerPG fpg;
     fpg=new IntegerPG("a",new Integer(1));
     System.out.println(fpg);
     fpg.initGUI(null);
     fpg.showGUIPanel();
     fpg=new IntegerPG("b",new Integer(10));
     System.out.println(fpg);
     fpg.setEnabled(false);
     fpg.initGUI(null);
     fpg.showGUIPanel();
     fpg=new IntegerPG("c",new Integer(100),false);
     System.out.println(fpg);
     fpg.setEnabled(false);
     fpg.initGUI(null);
     fpg.showGUIPanel();
     fpg=new IntegerPG("d",new Integer(1000),true);
     System.out.println(fpg);
     fpg.setDrawValid(true);
     fpg.initGUI(null);
     fpg.showGUIPanel();
     }*/
}
