/*
 * File:  JavaWrapperOperator.java
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
 * $Log$
 * Revision 1.2  2003/10/30 18:38:33  bouzekc
 * Changed Operator name in main() to WrappedCrunch.
 *
 * Revision 1.1  2003/10/29 01:13:06  bouzekc
 * Added to CVS.
 *
 */
package DataSetTools.operator;

import DataSetTools.dataset.*;

import DataSetTools.parameter.*;

import DataSetTools.util.*;

import java.lang.reflect.*;

import java.util.*;


/**
 * This class is designed to "wrap" pure Java code so that IPNS users can write
 * calculation routines and still have their routines show up in ISAW as
 * Operators.
 */
public class JavaWrapperOperator extends Operator {
  //~ Instance fields **********************************************************

  private Field[] fieldParams;
  private Wrappable wrapped;

  //~ Constructors *************************************************************

  /**
   * Constructor for creating a JavaWrapperOperator based on the given
   * Wrappable sent in.
   *
   * @param temp The Wrappable code sent in.
   */
  public JavaWrapperOperator( Wrappable temp ) throws ClassCastException {
    super( temp.getClass(  ).getName(  ) );
    wrapped       = temp;
    fieldParams   = wrapped.getClass(  )
                           .getFields(  );
    setDefaultParameters(  );
  }

  //~ Methods ******************************************************************

  /**
   * @return The ISS command name for this Operator.
   */
  public String getCommand(  ) {
    if( wrapped.getCommand(  ) == null ) {
      return super.getCommand(  );
    }

    return wrapped.getCommand(  );
  }

  /**
   * Sets the default parameters.
   */
  public void setDefaultParameters(  ) {
    Class type  = null;
    Object val  = null;
    String name = null;

    if( fieldParams != null ) {
      parameters = new Vector(  );

      try {
        for( int i = 0; i < fieldParams.length; i++ ) {
          type   = fieldParams[i].getType(  );

          //get the value of the field for the wrapped Object
          val    = fieldParams[i].get( wrapped );

          //get the name
          name   = fieldParams[i].getName(  );

          //BooleanPG
          if( ( type == Boolean.TYPE ) || ( type == Boolean.class ) ) {
            addParameter( new BooleanPG( name, val ) );
            //FloatPG
          } else if( 
            ( type == Float.TYPE ) || ( type == Double.TYPE ) ||
              ( type == Float.class ) || ( type == Double.class ) ) {
            addParameter( new FloatPG( name, val ) );
            //IntegerPG
          } else if( 
            ( type == Integer.TYPE ) || ( type == Long.TYPE ) ||
              ( type == Integer.class ) || ( type == Long.class ) ) {
            addParameter( new IntegerPG( name, val ) );
            //StringPG
          } else if( ( type == Character.TYPE ) || ( type == String.class ) ) {
            addParameter( new StringPG( name, null ) );
          } else if( type.isArray(  ) ) {
            //ArrayPG
            addParameter( new ArrayPG( name, null ) );
          } else if( type == DataSet.class ) {
            addParameter( new DataSetPG( name, null ) );
          } else if( type == UniformXScale.class ) {
            addParameter( new UniformXScalePG( name, null ) );
          } else if( type == VariableXScale.class ) {
            addParameter( new VariableXScalePG( name, null ) );
          }
        }
      } catch( IllegalAccessException iae ) {
        iae.printStackTrace(  );
      }
    }
  }

  /**
   * @return The javadoc formatted documentation for this Operator.
   */
  public String getDocumentation(  ) {
    if( wrapped.getDocumentation(  ) == null ) {
      return super.getDocumentation(  );
    }

    return wrapped.getDocumentation(  );
  }

  /**
   * Returns the result of executing the Wrappable's calculate() method.
   */
  public Object getResult(  ) {
    try {
      Object[] values = new Object[parameters.size(  )];

      //go through the parameter list, getting Objects for the calculate(...)
      //method.
      for( int i = 0; i < parameters.size(  ); i++ ) {
        values[i] = ( ( ParameterGUI )parameters.get( i ) ).getValue(  );
      }

      for( int k = 0; k < fieldParams.length; k++ ) {
        fieldParams[k].set( wrapped, values[k] );
      }

      //send the values through calculate(...)
      return wrapped.calculate(  );
    } catch( IllegalAccessException iae ) {
      iae.printStackTrace(  );

      return null;
    }
  }

  /**
   * Testbed.
   */
  public static void main( String[] args ) {
    Operators.WrappedCrunch crunch = new Operators.WrappedCrunch(  );
    JavaWrapperOperator wrapper    = new JavaWrapperOperator( crunch );
    DataSet temp                   = new DataSetTools.retriever.RunfileRetriever( 
        "/home/students/bouzekc/ISAW/SampleRuns/SCD06530.RUN" ).getDataSet( 1 );
    new DataSetTools.viewer.ViewManager( 
      temp, DataSetTools.viewer.IViewManager.IMAGE );
    wrapper.getParameter( 0 )
           .setValue( temp );
    wrapper.getParameter( 1 )
           .setValue( new Float( 0.0f ) );
    wrapper.getParameter( 2 )
           .setValue( new Float( 2.0f ) );
    wrapper.getParameter( 3 )
           .setValue( new Boolean( true ) );

    DataSet newDS = ( DataSet )wrapper.getResult(  );
    new DataSetTools.viewer.ViewManager( 
      newDS, DataSetTools.viewer.IViewManager.IMAGE );
  }

  /**
   * Method to create a category list from this classes nearest abstract
   * parent's package name.  Overridden to used the Wrappable's hierarchy.
   *
   * @return A String array of the category list.
   */
  protected String[] createCategoryList(  ) {
    if( wrapped == null ) {
      return null;
    }

    // determine the correct abstract class
    Class klass = wrapped.getClass(  );

    while( !Operator.isAbstract( klass ) ) {
      klass = klass.getSuperclass(  );
    }

    // get the category name and shorten it
    String category = klass.getPackage(  )
                           .getName(  )
                           .substring( dstools_length );

    // split up into an array and return
    return StringUtil.split( category, "." );
  }
}
