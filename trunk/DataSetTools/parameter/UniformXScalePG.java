/*
 * File:  UniformXScalePG.java
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
 * Revision 1.8  2003/11/19 04:06:54  bouzekc
 * This class is now a JavaBean.  Added code to clone() to copy all
 * PropertyChangeListeners.
 *
 * Revision 1.7  2003/10/11 19:11:53  bouzekc
 * Now implements clone using reflection.
 *
 * Revision 1.6  2003/09/16 22:46:55  bouzekc
 * Removed addition of this as a PropertyChangeListener.  This is already done
 * in ParameterGUI.  This should fix the excessive events being fired.
 *
 * Revision 1.5  2003/09/13 23:29:47  bouzekc
 * Moved calls from setValid(true) to validateSelf().
 *
 * Revision 1.4  2003/09/13 23:16:40  bouzekc
 * Removed calls to setEnabled in initGUI(Vector), since ParameterGUI.init()
 * already calls this.
 *
 * Revision 1.3  2003/09/09 23:33:17  bouzekc
 * Added definition of type to the constructors.
 *
 * Revision 1.2  2003/09/09 23:06:31  bouzekc
 * Implemented validateSelf().
 *
 * Revision 1.1  2003/09/09 00:31:56  bouzekc
 * Added to CVS.
 *
 */
package DataSetTools.parameter;

import DataSetTools.components.ParametersGUI.EntryWidget;
import DataSetTools.components.ParametersGUI.StringEntry;

import DataSetTools.dataset.UniformXScale;
import DataSetTools.dataset.XScale;

import DataSetTools.util.FloatFilter;
import DataSetTools.util.IntegerFilter;

import java.awt.*;
import java.awt.event.*;

import java.beans.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.Vector;

import javax.swing.*;


/**
 * This is the implementation of IXScalePG which will deal with uniform
 * XScales. As such, it can take an XScale in setValue() and its constructor.
 * It also has a constructor to creation using start, end, number of steps.
 * The "standard" constructors and setValue() can also take Vectors, although
 * the elements must be Float or Double objects, and the values must be
 * monotonic increasing and equally "spaced." Note also that getValue() will
 * return a Vector for general compatibility with Scripts and Wizards,
 * although there are two "fast-access" methods that directly return an XScale
 * and a Vector, respectively.
 */
public class UniformXScalePG extends ParameterGUI implements IXScalePG {
  //~ Static fields/initializers ***********************************************

  private static final String CREATE_LABEL = "Create";

  //~ Instance fields **********************************************************

  private final String TYPE     = "UniformXScale";
  private final float TOLERANCE = 0.001f;
  private StringEntry start;
  private StringEntry end;
  private StringEntry steps;

  //~ Constructors *************************************************************

  /**
   * First "standard" constructor for UniformXScalePG.  Used to set a name and
   * value for this UniformXScalePG.
   *
   * @param name The name to give to this UniformXScalePG.
   * @param val The value to give to this UniformXScalePG.
   */
  public UniformXScalePG( String name, Object val ) {
    super( name, val );
    this.setType( TYPE );
  }

  /**
   * Second "standard" constructor for UniformXScalePG.  Used to set a name and
   * value for this UniformXScalePG.  In addition, this will draw a checkbox
   * for the GUI and set the validity of this UniformXScale to the valid
   * parameter value.
   *
   * @param name The name to give to this UniformXScalePG.
   * @param val The value to give to this UniformXScalePG.
   * @param valid Whether this UniformXScale should be initially valid or not.
   */
  public UniformXScalePG( String name, Object val, boolean valid ) {
    super( name, val, valid );
    this.setType( TYPE );
  }

  /**
   * Constructor to allow setting the start, end, and number of steps values of
   * this UniformXScalePG.  Does not draw the valid checkbox.
   *
   * @param name The name to give to this UniformXScalePG.
   * @param start The start point.
   * @param end The end point.
   * @param steps The number of steps (X-values).
   */
  public UniformXScalePG( String name, float start, float end, int steps ) {
    this( name, null );

    UniformXScale scale = new UniformXScale( start, end, steps );
    setValue( scale );
    this.setType( TYPE );
  }

  /**
   * Constructor to allow setting the start, end, and number of steps values of
   * this UniformXScalePG.  Draws the valid checkbox.
   *
   * @param name The name to give to this UniformXScalePG.
   * @param start The start point.
   * @param end The end point.
   * @param steps The number of steps (X-values).
   * @param valid Whether this UniformXScale should be initially valid or not.
   */
  public UniformXScalePG( 
    String name, float start, float end, int steps, boolean valid ) {
    this( name, null, valid );

    UniformXScale scale = new UniformXScale( start, end, steps );
    setValue( scale );
    this.setType( TYPE );
  }

  //~ Methods ******************************************************************

  /**
   * Method to set the value of this UniformXScalePG.  The parameter must be a
   * non-null Vector or a non-null UniformXScale.  If not, nothing is done.   <br>
   * <br>
   * Note that this will simply try to determine if the Vector values meet a
   * certain tolerance for spacing, then it will put them into an XScalePG,
   * which may or may not directly map its elements to the original Vector
   * elements.  The only guarantee is that the first and last elements and the
   * number of elements will be the same.
   *
   * @param val The value to set this UniformXScale to.
   */
  public void setValue( Object val ) {
    if( 
      ( val == null ) ||
        ( !( val instanceof UniformXScale ) && !( val instanceof Vector ) ) ) {
      return;
    }

    UniformXScale scale = null;

    if( val instanceof UniformXScale ) {
      scale = ( UniformXScale )val;
    } else if( val instanceof Vector && ( ( ( Vector )val ).size(  ) > 0 ) ) {
      Vector temp       = ( Vector )val;
      Object element    = null;
      float[] elems     = new float[temp.size(  )];
      boolean allFloats = true;

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

      //toss the Vector memory back to the garbage collector
      temp = null;

      if( allFloats ) {
        boolean isMonotonicIncreasing = true;
        float oldSpacing              = 0.0f;
        float newSpacing              = 0.0f;

        //we made it through with all elements intact.  Now check to be sure
        //they are monotonic increasing.
        if( elems.length > 1 ) {
          oldSpacing = elems[1] - elems[0];
        }

        //now we want to check from element one onward
        for( int i = 1; ( i < elems.length ) && isMonotonicIncreasing; i++ ) {
          if( i < ( elems.length - 1 ) ) {
            //don't run off the end of the array
            newSpacing = elems[i + 1] - elems[i];

            //we'll consider them the same within a tolerance
            if( ( newSpacing - oldSpacing ) > TOLERANCE ) {
              //end the loop
              isMonotonicIncreasing = false;
            } else {
              //set up for the next round
              oldSpacing = newSpacing;
            }
          }
        }

        if( isMonotonicIncreasing ) {
          scale = new UniformXScale( 
              elems[0], elems[elems.length - 1], elems.length );
        }
      }
    }

    if( getInitialized(  ) && ( scale != null ) ) {
      start.setText( new Float( scale.getStart_x(  ) ).toString(  ) );
      end.setText( new Float( scale.getEnd_x(  ) ).toString(  ) );
      steps.setText( new Float( scale.getStep(  ) ).toString(  ) );
    }
    super.setValue( scale );
  }

  /**
   * Uses the internal XScalePGHelper to convert the inner UniformXScale to a
   * Vector and return it.  If the internal value is null and this is
   * getInitialized(), it will first trigger the creation of the inner
   * UniformXScale the same way that clicking on the "Create" button would.
   *
   * @return The Vector of values for this UniformXScalePG.
   */
  public Object getValue(  ) {
    Object scaleVal = super.getValue(  );

    if( getInitialized(  ) ) {
      scaleVal = createXScaleFromGUIValues(  );
    }

    return XScalePGHelper.convertXScaleToVector( ( XScale )scaleVal );
  }

  /**
   * Fast access method to return a typecast Vector.
   *
   * @return Vector-cast value of this UniformXScalePG.
   */
  public Vector getVectorValue(  ) {
    return ( Vector )getValue(  );
  }

  /**
   * Fast access method to return the internal UniformXScale.
   *
   * @return The internal UniformXScale.
   */
  public XScale getXScaleValue(  ) {
    return ( XScale )super.getValue(  );
  }

  /**
   * Creates the GUI for this UniformXScalePG.
   *
   * @param vals The Vector of initial values.  Note that this follows the same
   *        rules as setValue() does when determining whether or not the
   *        Vector is valid for this UniformXScalePG.
   */
  public void initGUI( Vector vals ) {
    if( this.getInitialized(  ) ) {
      return;
    }

    if( vals != null ) {
      setValue( vals );
    }
    setEntryWidget( new EntryWidget(  ) );

    EntryWidget wijit = getEntryWidget(  );
    wijit.setLayout( new BorderLayout(  ) );

    JPanel innerPanel = new JPanel(  );
    innerPanel.setLayout( new GridLayout( 0, 2 ) );
    innerPanel.add( new JLabel( "Start value" ) );
    start = new StringEntry( "", new FloatFilter(  ) );
    innerPanel.add( start );
    innerPanel.add( new JLabel( "End value" ) );
    end = new StringEntry( "", new FloatFilter(  ) );
    innerPanel.add( end );
    innerPanel.add( new JLabel( "Number of steps" ) );
    steps = new StringEntry( "", new IntegerFilter(  ) );
    innerPanel.add( steps );
    wijit.add( innerPanel, BorderLayout.CENTER );

    JButton createButton = new JButton( CREATE_LABEL );
    wijit.add( createButton, BorderLayout.SOUTH );
    createButton.addActionListener( new UniformXScalePGListener(  ) );
    super.initGUI(  );
  }

  /**
   * Testbed.
   */
  public static void main( String[] args ) {
    UniformXScalePG uxpg = new UniformXScalePG( "TestUniformXScalePG", null );
    Vector tester        = new Vector(  );
    tester.add( new Float( 1.6f ) );
    tester.add( new Float( 6.6f ) );
    tester.add( new Float( 11.6f ) );
    tester.add( new Float( 16.6f ) );
    uxpg.setValue( tester );
    System.out.println( "With a Vector value" );
    System.out.println( uxpg.getValue(  ) );
    System.out.println( uxpg.getXScaleValue(  ) );
    System.out.println( "With a UniformXScale value" );

    UniformXScale ux = new UniformXScale( 1.0f, 10.0f, 4 );
    uxpg.setValue( ux );
    System.out.println( uxpg.getValue(  ) );
    System.out.println( uxpg.getXScaleValue(  ) );
    uxpg.initGUI( null );
    uxpg.showGUIPanel(  );
  }

  /**
   * Definition of the clone method.  Overwritten because internally the value
   * is stored using a UniformXScale.
   */
  public Object clone(  ) {
    try {
      Class klass           = this.getClass(  );
      Constructor construct = klass.getConstructor( 
          new Class[]{ String.class, Object.class } );
      UniformXScalePG pg    = ( UniformXScalePG )construct.newInstance( 
          new Object[]{ null, null } );
      pg.setName( new String( this.getName(  ) ) );
      pg.setValue( pg.getXScaleValue(  ) );
      pg.setDrawValid( this.getDrawValid(  ) );
      pg.setValid( this.getValid(  ) );

      if( this.getInitialized(  ) ) {
        pg.initGUI( null );
      }

      if( getPropListeners(  ) != null ) {
        java.util.Enumeration e    = getPropListeners(  )
                                       .keys(  );
        PropertyChangeListener pcl = null;
        String propertyName        = null;

        while( e.hasMoreElements(  ) ) {
          pcl            = ( PropertyChangeListener )e.nextElement(  );
          propertyName   = ( String )getPropListeners(  )
                                       .get( pcl );
          pg.addPropertyChangeListener( propertyName, pcl );
        }
      }

      return pg;
    } catch( InstantiationException e ) {
      throw new InstantiationError( e.getMessage(  ) );
    } catch( IllegalAccessException e ) {
      throw new IllegalAccessError( e.getMessage(  ) );
    } catch( NoSuchMethodException e ) {
      throw new NoSuchMethodError( e.getMessage(  ) );
    } catch( InvocationTargetException e ) {
      throw new RuntimeException( e.getTargetException(  ).getMessage(  ) );
    }
  }

  /**
   * Validates this UniformXScalePG.  Due to the way getValue() works, a
   * UniformXScalePG is considered valid if getValue() returns a non-null
   * value.
   */
  public void validateSelf(  ) {
    setValid( ( getValue(  ) != null ) );
  }

  /**
   * Creates an XScale from the filled in GUI values.
   *
   * @return The new UniformXScale.
   */
  private UniformXScale createXScaleFromGUIValues(  ) {
    float startNum = Float.parseFloat( start.getText(  ) );
    float endNum   = Float.parseFloat( end.getText(  ) );
    int stepNum    = Integer.parseInt( steps.getText(  ) );

    return new UniformXScale( startNum, endNum, stepNum );
  }

  //~ Inner Classes ************************************************************

  /**
   * Inner class to deal with creating the UniformXScale Object based on the
   * "click" of the button.
   */
  class UniformXScalePGListener implements ActionListener {
    //~ Methods ****************************************************************

    /**
     * Creates the inner XScale if and only if the action command matches the
     * name of the button.
     */
    public void actionPerformed( ActionEvent ae ) {
      if( ae.getActionCommand(  ) == CREATE_LABEL ) {
        setValue( createXScaleFromGUIValues(  ) );
      }
    }
  }
}
