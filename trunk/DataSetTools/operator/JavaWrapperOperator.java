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
 * Revision 1.3  2004/01/08 22:29:30  bouzekc
 * Now extends GenericOperator.  Changed createCategoryList() to
 * getCategoryList() due to reflection problems.  Modified to allow
 * instantiation of ParameterGUIs from SpecialStrings.  Changed
 * implementation of getCategoryList().
 *
 * Revision 1.2  2003/10/30 18:38:33  bouzekc
 * Changed Operator name in main() to WrappedCrunch.
 *
 * Revision 1.1  2003/10/29 01:13:06  bouzekc
 * Added to CVS.
 *
 */
package DataSetTools.operator;

import DataSetTools.dataset.*;

import DataSetTools.operator.Generic.*;

import DataSetTools.parameter.*;

import DataSetTools.util.*;

import java.lang.reflect.*;

import java.util.*;


/**
 * This class is designed to "wrap" pure Java code so that IPNS users can write
 * calculation routines and still have their routines show up in ISAW as
 * Operators.
 */
public class JavaWrapperOperator extends GenericOperator {
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
    fieldParams   = wrapped.getClass(  ).getFields(  );
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
            addParameter( new ArrayPG( name, val ) );
          } else if( 
            ( type == DataSet.class ) || ( type == DataDirectoryString.class ) ) {
            addParameter( new DataSetPG( name, val ) );
          } else if( type == UniformXScale.class ) {
            addParameter( new UniformXScalePG( name, val ) );
          } else if( type == VariableXScale.class ) {
            addParameter( new VariableXScalePG( name, val ) );
          } else if( type == InstrumentNameString.class ) {
            addParameter( new InstNamePG( name, val ) );
          } else if( type == IntListString.class ) {
            addParameter( new IntArrayPG( name, val ) );
          } else if( type == LoadFileString.class ) {
            addParameter( new LoadFilePG( name, val ) );
          } else if( type == SaveFileString.class ) {
            addParameter( new SaveFilePG( name, val ) );
          } else if( type == StringChoiceList.class ) {
            addParameter( new ChoiceListPG( name, val ) );
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
   * Accessor method for the title of this Operator.  Uses the file name  of
   * the Wrappable internal instance.
   *
   * @return The title of this JavaWrapperOperator.
   */
  public String getTitle(  ) {
    String[] packageNames = super.getTitle(  ).split( "\\." );

    return packageNames[packageNames.length - 1];
  }

  /**
   * Testbed.
   */
  public static void main( String[] args ) {
    Operators.TOF_SCD.WrappedCrunch2 crunch = new Operators.TOF_SCD.WrappedCrunch2(  );
    JavaWrapperOperator wrapper    = new JavaWrapperOperator( crunch );
    /*DataSet temp                   = new DataSetTools.retriever.RunfileRetriever(
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
         newDS, DataSetTools.viewer.IViewManager.IMAGE );*/
    String[] catList = wrapper.getCategoryList(  );

    for( int i = 0; i < catList.length; i++ ) {
      System.out.println( catList[i] );
    }
    System.out.println( wrapper.getCommand(  ) );
  }

  /**
   * Method to create a category list from this classes nearest abstract
   * parent's package name.  Overridden to used the Wrappable's hierarchy.
   *
   * @return A String array of the category list.
   */
  public String[] getCategoryList(  ) {
    if( ( wrapped == null ) || !( wrapped instanceof Wrappable ) ) {
      return null;
    }

    // determine the correct abstract class
    Class wrappedKlass = wrapped.getClass(  );
    Class wrapperKlass = this.getClass(  );

    while( !Operator.isAbstract( wrapperKlass ) ) {
      wrapperKlass = wrapperKlass.getSuperclass(  );
    }

    // get the category name and shorten it
    String category = wrapperKlass.getPackage(  ).getName(  );
    category = category.substring( 
        category.indexOf( "." ) + 1, category.length(  ) );

    String wrappedCat = wrappedKlass.getPackage(  ).getName(  );
    int dotIndex      = wrappedCat.indexOf( "." );

    if( dotIndex >= 0 ) {
      wrappedCat   = wrappedCat.substring( dotIndex + 1, wrappedCat.length(  ) );
      category     = category + "." + wrappedCat;
    }

    // split up into an array and return
    return category.split( "\\." );
  }
}
