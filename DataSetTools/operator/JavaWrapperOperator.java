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
 * grant number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * $Log$
 * Revision 1.19  2004/05/07 17:44:55  dennis
 * Changed to use WrappedCrunch in the Operators/Examples
 * directory instead of Operators.
 *
 * Revision 1.18  2004/05/06 00:10:26  bouzekc
 * Replaced 3 method calls to get() with a local variable and one get() call.
 * No need for extra overhead.
 *
 * Revision 1.17  2004/05/06 00:06:32  bouzekc
 * FIxed an error that added a DataSetPG in where a DataDirPG should have gone.
 *
 * Revision 1.16  2004/05/04 19:04:35  dennis
 * Now clears DataSetPG after using value, to avoid memory leak.
 *
 * Revision 1.15  2004/04/19 14:00:27  rmikk
 * String parameter values are retained
 * MediaList parameters were added
 * SpecialString parameters do not create type mismatch errors
 *
 * Revision 1.14  2004/03/15 19:33:50  dennis
 * Removed unused imports after factoring out view components,
 * math and utilities.
 *
 * Revision 1.13  2004/03/15 03:28:22  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.12  2004/03/11 07:41:15  bouzekc
 * Now handles StringChoiceList correctly.
 *
 * Revision 1.11  2004/03/11 06:14:34  bouzekc
 * Removed dead code.
 *
 * Revision 1.10  2004/03/06 19:07:41  rmikk
 * It now handles the Vector Data Type
 *
 * Revision 1.9  2004/02/16 19:45:44  bouzekc
 * Now handles HiddenOperator implementation by the Wrappable Object.
 *
 * Revision 1.8  2004/02/03 23:45:48  bouzekc
 * Added clone test to main().
 *
 * Revision 1.7  2004/02/01 01:36:45  bouzekc
 * Added clone.  The JavaWrapperOperators can now be called from Scripts.
 *
 * Revision 1.6  2004/02/01 01:18:02  bouzekc
 * Changed the algorithm that determined category name.  It now looks at the
 * package name of the Wrappable Object and breaks it down, removing any
 * starting "DataSetTools" and replacing "Operators" with "operator.Generic".
 * This should now allow correct location of any Wrappable within the menus.
 *
 * Revision 1.5  2004/01/30 02:10:38  bouzekc
 * Now handles Fields and values that represent primitive array type
 * initializations (e.g. int num[] = {4,3}) as well as regular
 * primitive array initializations.
 *
 * Revision 1.4  2004/01/08 23:31:07  bouzekc
 * Added NSF grant number to header.
 *
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

import gov.anl.ipns.Util.SpecialStrings.*;

import java.lang.reflect.*;

import java.util.*;


/**
 * This class is designed to "wrap" pure Java code so that IPNS users can write
 * calculation routines and still have their routines show up in ISAW as
 * Operators.
 */
public class JavaWrapperOperator extends GenericOperator {
  //~ Static fields/initializers ***********************************************

  private static String DS_OP  = "DataSetTools";
  private static String BIG_OP = "Operators";

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
   * Testbed.
   */
  public static void main( String[] args ) {
    Operators.Example.WrappedCrunch op = new Operators.Example.WrappedCrunch();

    //Operators.StringChoiceOp op = new Operators.StringChoiceOp(  );
    //Operators.MyFortran crunch = new Operators.MyFortran(  );
    JavaWrapperOperator wrapper = new JavaWrapperOperator( op );

    /*DataSet temp = new DataSetTools.retriever.RunfileRetriever(
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
    System.out.println( wrapper.getResult(  ) );

    //this test is good only for WrappedCrunch
    wrapper.getParameter( 1 ).setValue( new Float( 5.0f ) );

    JavaWrapperOperator clonedOp = ( JavaWrapperOperator )wrapper.clone(  );

    System.out.print( "Original value: " );
    System.out.println( wrapper.getParameter( 1 ) );
    System.out.print( "New value: " );
    System.out.println( clonedOp.getParameter( 1 ) );
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

    String category = this.wrapped.getClass(  ).getPackage(  ).getName(  );

    if( category.startsWith( DS_OP ) ) {
      //we don't need the DataSetTools part of the package name
      category = category.substring( DS_OP.length(  ), category.length(  ) );

      //now remove the "." if it is there
      if( category.startsWith( "." ) ) {
        category = category.substring( 1, category.length(  ) );
      }
    } else if( category.startsWith( BIG_OP ) ) {
      //however, we have to catch the oddball fact that "Operators" 
      //is not recognized the same way as "operator" is, 
      //i.e. we want "operator.Generic" rather than
      //"Operators" for the start of the package name 
      category = category.replaceFirst( BIG_OP, "operator.Generic" );
    }

    String[] tempList = category.split( "\\." );
    String[] catList  = null;

    if( wrapped instanceof HiddenOperator ) {
      catList      = new String[tempList.length + 1];
      catList[0]   = "HIDDENOPERATOR";

      //copy the rest of the categories over
      for( int catIndex = 1; catIndex < catList.length; catIndex++ ) {
        catList[catIndex] = tempList[catIndex - 1];
      }
    } else {
      catList = tempList;
    }

    // split up into an array and return
    return catList;
  }

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
            addParameter( new StringPG( name, val ) );
          } else if( ( type.isArray(  ) ) || ( type == Vector.class ) ) {
            //ArrayPG
            addParameter( new ArrayPG( name, val ) );
          } else if( type == DataSet.class ) {
            addParameter( new DataSetPG( name, val ) );
          } else if( type == DataDirectoryString.class ) {
            addParameter( new DataDirPG( name, val ) );
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
          } else if( type == MediaList.class ) {
            MediaList ml   = new MediaList(  );
            Vector choices = new Vector(  );

            for( int iii = 0; iii < ml.num_strings(  ); iii++ ) {
              choices.addElement( ml.getString( iii ) );
            }

            ChoiceListPG Ch = new ChoiceListPG( name, val );

            Ch.addItems( choices );
            addParameter( Ch );
          } else if( type == StringChoiceList.class ) {
            addParameter( 
              new ChoiceListPG( 
                name, ( ( StringChoiceList )val ).getStrings(  ) ) );
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
      ParameterGUI pg;

      for( int i = 0; i < parameters.size(  ); i++ ) {
        pg          = ( ParameterGUI )parameters.get( i );
        values[i]   = pg.getValue(  );

        if( pg instanceof DataSetPG ) {
          ( ( DataSetPG )pg ).clear(  );  // clear DataSetPG to 

          // avoid memory leak !
        }
      }

      for( int k = 0; k < fieldParams.length; k++ ) {
        if( fieldParams[k].getType(  ).isArray(  ) ) {
          //this means that we have hit an ArrayPG
          //make sure we can go through the vector
          if( values[k] instanceof Vector ) {
            Vector myVect = ( Vector )values[k];

            //iterate through the vector of values and set each of the field
            //param's values to the element value
            for( int m = 0; m < myVect.size(  ); m++ ) {
              Array.set( fieldParams[k].get( wrapped ), 1, myVect.get( m ) );
            }
          }
        }  //else if( fieldParams[k].getType(  ) instanceof SpecialString ) {
        else if( 
          SpecialString.class.isAssignableFrom( fieldParams[k].getType(  ) ) ) {
          SpecialString ss = ( SpecialString )fieldParams[k].get( wrapped );

          ss.setString( values[k].toString(  ) );
        } else {
          fieldParams[k].set( wrapped, values[k] );
        }
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
   * Clone method for JavaWrapperOperators.  Works by cloning the Wrappable
   * inside.
   */
  public Object clone(  ) {
    try {
      Constructor konstruktor = this.getClass(  ).getConstructor( 
          new Class[]{ Wrappable.class } );
      JavaWrapperOperator op  = ( JavaWrapperOperator )konstruktor.newInstance( 
          new Object[]{ this.wrapped.getClass(  ).newInstance(  ) } );

      op.CopyParametersFrom( this );

      return op;
    } catch( InstantiationException ie ) {
      throw new InstantiationError( ie.getMessage(  ) );
    } catch( IllegalAccessException iae ) {
      throw new IllegalAccessError( iae.getMessage(  ) );
    } catch( NoSuchMethodException nsme ) {
      throw new NoSuchMethodError( nsme.getMessage(  ) );
    } catch( InvocationTargetException ite ) {
      throw new RuntimeException( ite.getMessage(  ) );
    }
  }
}
