/*
 * File:  VariableXScalePG.java
 *
 * Copyright (C) 2003 Chris Bouzek
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
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Chris Bouzek <coldfusion78@yahoo.com>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 * This work was supported by the National Science Foundation under
 * grant number
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.6  2003/12/15 02:44:08  bouzekc
 * Removed unused imports.
 *
 * Revision 1.5  2003/11/19 04:13:23  bouzekc
 * Is now a JavaBean.
 *
 * Revision 1.4  2003/10/11 19:32:48  bouzekc
 * Really implemented validateSelf().
 *
 * Revision 1.3  2003/09/09 23:33:18  bouzekc
 * Added definition of type to the constructors.
 *
 * Revision 1.2  2003/09/09 22:59:57  bouzekc
 * Implemented validateSelf() and changed superclass to FloatArrayPG.
 *
 * Revision 1.1  2003/09/09 00:31:55  bouzekc
 * Added to CVS.
 *
 */
package DataSetTools.parameter;

import java.util.Vector;

import DataSetTools.components.ParametersGUI.ArrayEntryJFrame;
import DataSetTools.dataset.VariableXScale;
import DataSetTools.dataset.XScale;


/**
 * This is the implementation of XScalePG which will deal with arbitrary
 * XScales.  As such, it can take a Vector value or an XScale in setValue()
 * and its constructor.   If an XScale is sent, it will be converted to a
 * VariableXScale if it is not one already.  Note also that getValue() will
 * return a Vector for general compatibility with Scripts and Wizards,
 * although there are two "fast-access" methods that directly return an XScale
 * and a Vector, respectively.
 */
public class VariableXScalePG extends FloatArrayPG implements IXScalePG {
  //~ Instance fields **********************************************************

  private final String TYPE = "VariableXScale";

  //~ Constructors *************************************************************

  /**
   * Creates a new VariableXScalePG object without a drawn "valid" checkbox.
   *
   * @param name The name of this ParameterGUI
   * @param val Either the Vector or XScale to work with.
   */
  public VariableXScalePG( String name, Object val ) {
    super( name, val );
    setParam( new FloatPG( "Enter divisions", 0.0f ) );
    this.setType( TYPE );
  }

  /**
   * Creates a new XScalePG object with a "valid" checkbox in its GUI.
   *
   * @param name The name of this ParameterGUI.
   * @param val Either the Vector or XScale to work with.
   * @param valid Whether this ParameterGUI should be initially valid or not.
   */
  public VariableXScalePG( String name, Object val, boolean valid ) {
    super( name, val, valid );
    setParam( new FloatPG( "Enter divisions", 0.0f ) );
    this.setType( TYPE );
  }

  //~ Methods ******************************************************************

  /**
   * Sets the internal VariableXScale.  The value passed in must be an XScale
   * or a Vector.  The Vector must be composed of monotonic increasing Floats.
   * Note that for a Vector value, the elements in the Vector MUST be
   * monotonic  Floats or Doubles.  If they are not, nothing is done. In
   * addition, a Double is cast down to a Float.   Any XScale sent in which is
   * not a VariableXScale will be converted to one. If the parameter is not a
   * Vector or an XScale, nothing will be done.
   *
   * @param val The Vector or XScale to use.
   */
  public void setValue( Object val ) {
    if( 
      ( val == null ) ||
        ( !( val instanceof XScale ) && !( val instanceof Vector ) ) ) {
      return;
    }

    ArrayEntryJFrame GUI = getEntryFrame(  );
    VariableXScale scale = null;

    //we are dealing with some form of non-VariableXScale, so convert it to a
    //VariableXScale
    if( val instanceof XScale && !( val instanceof VariableXScale ) ) {
      scale = new VariableXScale( ( ( XScale )val ).getXs(  ) );
    } else if( val instanceof VariableXScale ) {
      scale = ( VariableXScale )val;
    } else if( val instanceof Vector ) {
      Vector temp       = ( Vector )val;
      Object element    = null;
      boolean allFloats = true;

      //allocate a float array since we need one for VariableXScale
      float[] elems = new float[temp.size(  )];

      for( int i = 0; ( i < temp.size(  ) ) && allFloats; i++ ) {
        element = temp.get( i );

        if( element instanceof Float ) {
          elems[i] = ( ( Float )element ).floatValue(  );
        } else if( element instanceof Double ) {
          elems[i] = ( ( Double )element ).floatValue(  );
        } else {
          allFloats = false;
        }
      }
      scale = new VariableXScale( elems );
    }
    super.setValue( scale );

    if( GUI != null ) {
      //ArrayEntryJFrame can't handle XScales
      GUI.setValue( XScalePGHelper.convertXScaleToVector( scale ) );
    }
  }

  /**
   * Uses the internal XScalePGHelper to convert the inner VariableXScale to a
   * Vector and return it.
   *
   * @return The Vector of values for this VariableXScalePG.
   */
  public Object getValue(  ) {
    return XScalePGHelper.convertXScaleToVector( ( XScale )super.getValue(  ) );
  }

  /**
   * Testbed.
   */

  /*public static void main( String[] args ) {
     JFrame jf = new JFrame( "Test" );
     jf.getContentPane(  )
       .setLayout( new GridLayout( 1, 2 ) );
     VariableXScalePG vxpg = new VariableXScalePG(
         "Test VariableXScalePG", null );
  
     Vector tester        = new Vector(  );
     tester.add( new Float( 1.9f ) );
     tester.add( new Float( 6.0f ) );
     tester.add( new Float( 11.4f ) );
     //this should generate an error from VariableXScale.java
     tester.add( new Float( 9.6f ) );
     vxpg.setValue( tester );
     System.out.println( "With a Vector value" );
     System.out.println( vxpg.getValue(  ) );
     System.out.println( vxpg.getXScaleValue(  ) );
     System.out.println( "With a VariableXScale value" );
     float[] nums = new float[]{ 1.0f, 5.5f, 8.8f, 9.9f};
     VariableXScale vx = new VariableXScale( nums );
     vxpg.setValue( vx );
     System.out.println( vxpg.getValue(  ) );
     System.out.println( vxpg.getXScaleValue(  ) );
     vxpg.initGUI( tester );
     jf.getContentPane(  )
       .add( vxpg.getGUIPanel(  ) );
     JButton jb = new JButton( "Result" );
     jf.getContentPane(  )
       .add( jb );
     jb.addActionListener( new DataSetTools.util.PGActionListener( vxpg ) );
     jf.setSize( 500, 100 );
     jf.invalidate(  );
     jf.show(  );
     }*/

  /**
   * Fast access method to return a typecast Vector.
   *
   * @return Vector-cast value of this VariableXScalePG.
   */
  public Vector getVectorValue(  ) {
    return ( Vector )getValue(  );
  }

  /**
   * Fast access method to return the internal VariableXScale.
   *
   * @return The internal VariableXScale.
   */
  public XScale getXScaleValue(  ) {
    return ( XScale )super.getValue(  );
  }

  /**
   * Validates this VariableXScalePG.  A VariableXScalePG is considered valid
   * if it contains all Float elements in monotonic increasing order.
   */
  public void validateSelf(  ) {
    super.validateSelf(  );

    Vector elems = getVectorValue(  );

    if( elems != null ) {
      boolean greater = true;
      float num1;
      float num2;

      for( int i = 0; ( i < ( elems.size(  ) - 1 ) ) && greater; i++ ) {
        num1   = ( ( Float )elems.get( i ) ).floatValue(  );
        num2   = ( ( Float )elems.get( i + 1 ) ).floatValue(  );

        if( num1 >= num2 ) {
          greater = false;
        }
      }
      setValid( greater );
    } else {
      setValid( false );
    }
  }
}
