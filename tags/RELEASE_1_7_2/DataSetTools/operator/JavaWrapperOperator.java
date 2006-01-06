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
 * Revision 1.29  2005/06/13 14:25:51  rmikk
 * If Object has the same class as the given class, the object is returned 
 *    quickly in cvrt routine with Class argument
 *
 * Revision 1.28  2005/06/07 22:49:19  rmikk
 * Removed a return that caused some orphan code in the main program
 *
 * Revision 1.27  2005/06/07 21:34:11  rmikk
 * Added a public static method to convert a "multidimensioned" Vector
 *   to a multi(or 1D) dimensioned array of int, float, double, or String if
 *   possible.
 *
 * Revision 1.26  2005/01/07 16:34:04  rmikk
 * Used the new IWrappableWithCategoryList to calculate the category
 *    list when possible
 *
 * Revision 1.25  2005/01/04 19:55:34  rmikk
 * Fixed HiddenOperator problem.  The category list must have length 1
 *    in this case
 *
 * Revision 1.24  2005/01/02 17:55:59  rmikk
 * Added a method go getWrappable for documenting the location of the
 *    relevant code.
 *
 * Revision 1.23  2004/06/16 21:59:53  rmikk
 * The ParameterGUI for n dimension arrays of int, float, double, short, 
 *    and long is now the RealArrayPG.  These arrays are now passed
 *   by reference.
 *
 * Revision 1.22  2004/06/15 18:54:08  robertsonj
 * Added PrinterNameString which is a special string so the JavaWrapperOperator
 * can use the PrinterNamePG
 *
 * Revision 1.21  2004/06/03 16:39:56  rmikk
 * Setting Special String and Array parameters of a Wrappable that were not
 *   assigned an original value now no longer yields a null pointer exception
 *
 * Revision 1.20  2004/05/09 21:41:28  bouzekc
 * Added code to clear DataSet values before returning the wrapped result.
 *
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
   
       Vector V = new Vector();
       Vector Z = new Vector();
       Vector W = new Vector();
       float k=2.3f;
       for( int u=0; u<2;u++){
        V = new Vector();
       for( int m=0; m<4; m++){
          W = new Vector();
       for(int i=0; i<3; i++){
         
         W.add(new Float(k));
         k +=1;
       }
       V.add(W);
       }
       Z.add(V);
       }
      
       Object Res= JavaWrapperOperator.cvrt2MultiArray(W);
       Command.ScriptUtil.display( Res);
       System.out.println("Class = "+ Res.getClass());
    

   /* Operators.Example.WrappedCrunch op = new Operators.Example.WrappedCrunch(  );

    //Operators.StringChoiceOp op = new Operators.StringChoiceOp(  );
    //Operators.MyFortran crunch = new Operators.MyFortran(  );
    JavaWrapperOperator wrapper = new JavaWrapperOperator( op );
   */
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
    /*String[] catList = wrapper.getCategoryList(  );

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
    */
   
    Vector Vmain = new Vector();
    V = new Vector();
      Vector V1 = new Vector();
      V1.add( new Integer(1));
      V1.add( new Integer(2));
      V1.add( new Integer(3));
    V.add(V1);
    V1 = new Vector();
          V1.add( new Integer(4));
          V1.add( new Integer(5));
          V1.add( new Integer(6));
        V.add(V1);
    V1 = new Vector();
          V1.add( new Integer(7));
          V1.add( new Integer(8));
          V1.add( new Integer(9));
    V.add(V1);
    Vmain.add(V);
    V = new Vector();
         V1 = new Vector();
         V1.add( new Integer(11));
         V1.add( new Integer(12));
         V1.add( new Integer(13));
       V.add(V1);
       V1 = new Vector();
             V1.add( new Integer(14));
             V1.add( new Integer(15));
             V1.add( new Integer(16));
           V.add(V1);
       V1 = new Vector();
             V1.add( new Integer(17));
             V1.add( new Integer(18));
             V1.add( new Integer(19));
       V.add(V1);
     Vmain.add(V);
    
    try{
    
    Object O = JavaWrapperOperator.cvrt((new Vector[0][0].getClass()),Vmain);
    System.out.println("Res class="+O.getClass());
    System.out.println("Res="+ gov.anl.ipns.Util.
               Sys.StringUtil.toString(O));
    }catch(Exception sss){
      System.out.println("Exception occurred"+sss);
    }
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
    if( wrapped instanceof IWrappableWithCategoryList)
       return ((IWrappableWithCategoryList)wrapped).getCategoryList();
       
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
       category = category.replaceFirst( BIG_OP,"Operator.Generic");
                  //  DataSetTools.operator.Operator.OPERATOR );
    }else if( category.startsWith("DataSetTools.operator.Generic"))

     category = category.replaceFirst( "DataSetTools.operator.Generic",
                "Operator.Generic");

    String[] tempList = category.split( "\\." );
    String[] catList  = null;

    if( wrapped instanceof HiddenOperator ) {
      catList      = new String[1];
      catList[0]   = "HIDDENOPERATOR";
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
          } else if( ( type.isArray(  ) ) ) {
            //ArrayPG
            Object multiArray = RealArrayPG.getZeroLengthedArray(type);
            if((multiArray == null))
               addParameter( new ArrayPG( name, val ) );
            else if(val != null)
               addParameter( new RealArrayPG(name , val));
            else
               addParameter( new RealArrayPG(name , multiArray));  
          }else if( type == Vector.class ) {
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
          } else if( type == PrinterNameString.class ){
          	addParameter( new PrinterNamePG(name, val));
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
  
  public Wrappable getWrappable(){
    return wrapped;
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

        //at this point, we have the value, so we don't want excessive references to the 
        //DataSet, since they seem reluctant to release it
        if( pg instanceof DataSetPG ) {
          ( ( DataSetPG )pg ).clear(  );
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
            /*Object rr = fieldParams[k].get( wrapped);//Could be null
            System.out.println("rr="+rr+"::"+rr.getClass());
            for( int m = 0; m < myVect.size(  ); m++ ) {
              Array.set( fieldParams[k].get( wrapped ), 1, myVect.get( m ) );
            }
           */
           try{
              fieldParams[k].set( wrapped, cvrt( fieldParams[k].getType(), myVect));
           }catch(Exception ss){
               return new ErrorString( "improp params "+ss.toString());
           }
          }else
          fieldParams[k].set(wrapped, values[k]);
        } else if( 
          SpecialString.class.isAssignableFrom( fieldParams[k].getType(  ) ) ) {
          try{
          
          SpecialString spStr = (SpecialString)(fieldParams[k].getType().newInstance());
          spStr.setString( values[k].toString());
          fieldParams[k].set( wrapped,spStr);
          }catch(Exception ss8){
            return new ErrorString("Internal Error xxx389"+ss8);
          }
        } else {
          fieldParams[k].set( wrapped, values[k] );
        }
      }

      //send the values through calculate(...)
      Object result = wrapped.calculate(  );

      //we have our result, and it seems that the out of memory error occurs when we try to 
      //iterate over this class again, so drop the old values
      for( int k = 0; k < fieldParams.length; k++ ) {
        if( values[k] instanceof DataSet ) {
          values[k] = null;
          fieldParams[k].set( wrapped, DataSet.EMPTY_DATA_SET );
        }
      }

      return result;
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
 
 /**
  * 
  * @param C  The desired Class to put parts of orig into
  * @param orig  The variable with the data which is essentially
  *             of the correct class except that it can have Vector
  *             components or array components.
  * @return   The arrayified Vector
  * @throws Exception  if this process is not possible
  */
 public static Object cvrt( Class C, Object orig) throws Exception{
   if( C == null)
     return null;
   if( orig == null)
     return null;
   if( orig.getClass().equals( C))
       return orig;  
   if( C.isArray()){
      Class C1 = C.getComponentType();
      int n = Size(orig);
      if( n < 0)
         throw new IllegalArgumentException("# of Dimension do not correspond");
      Object u = Array.newInstance( C1,Size( orig ));
      for( int i = 0; i<Size(orig); i++)
        Array.set(u,i, cvrt(C1,Elt(orig,i)));    
     return u;
   }
  Class[] interfaces = C.getInterfaces();
  boolean isCollection = false;
  if( interfaces != null){
    for( int i = 0; (i< interfaces.length) && !isCollection; i++)
        if( interfaces[i] == Collection.class)
          isCollection = true;
  }
  
  if( isCollection){
    Collection CC = (Collection)C.newInstance();
    for( int i=0; i<Size(orig); i++)
       CC.add( Elt(orig,i));
     return CC;
  }
  if( !C.isPrimitive())
  if(!C.isAssignableFrom( orig.getClass()))
     throw new IllegalArgumentException( C.toString()+" and "+
                     orig.getClass().toString()+" are incompatible");
  if( C == orig.getClass())
    return orig;
  if( C == double.class)
    return  new Double(((Number)orig).doubleValue());
  if( C == float.class)
    return new Float(((Number)orig).floatValue());
  if( C == int.class)
    return new Integer(((Number)orig).intValue());
  return orig;   
 }
 
  /**
   * 
   * @param V
   * @return
   * TODO : Start with an Object
   */
  public static Object cvrt2MultiArray( Vector V){
     if( V== null)
        return null;
     if( V.size()<=1)
       return null;
    Vector info= getInfo(V.elementAt(0));
    if( info == null)
       return null;
    Object Res = Array.newInstance((Class)info.firstElement(), V.size() );
    Array.set(Res,0,info.lastElement());
    for(int i=0; i<V.size(); i++){
      Vector info1= getInfo(V.elementAt(i));
      if( !info1.firstElement().equals(info.firstElement()))
         return null;
      if( !info.lastElement().getClass().isArray()){
          if( info1.lastElement().getClass().isArray())
           return null;
      }else if( Array.getLength(info.lastElement())!=Array.getLength(info1.lastElement()))
         return null;
      Array.set(Res,i,info1.lastElement());
    }      
    return Res;
  }
  
  private static Vector getInfo( Object Obj){
     if( Obj== null)
        return null;
     
    
     int size =-1;
    
     if( Obj instanceof Vector){
        size=((Vector)Obj).size();
       
     }else if( Obj.getClass().isArray()){
       size = Array.getLength(Obj);
      
     }else  //At the primitive level
       if( (Obj instanceof Number) ||(Obj instanceof String)||
           (Obj.getClass().isPrimitive())){
       Vector Res = new Vector();
       Class C = Obj.getClass();
       if( C.equals(Float.class)) C =  float.class;
       else if( C.equals(Integer.class)) C =  int.class;   
       else if( C.equals(Long.class)) C =  long.class;  
       else if( C.equals(Byte.class)) C =  byte.class;  
       else if( C.equals(Short.class)) C =  short.class; 
       else if( C.equals(Double.class)) C =  double.class; 
       else if( !C.equals(String.class))
          return null; 
       Res.add(C);
       Res.add(Obj);
       return Res;     
     }else
       return null;
     if( size <1)//May at some time create 0 lengthed arrays
       return null; 
     
         
     {
       Vector V = new Vector();
       Vector Res = new Vector();
       if(Obj instanceof Vector)
          V = getInfo( ((Vector)Obj).elementAt(0));
       else
          V= getInfo( Array.get(Obj,0));
       if( V == null)
          return null;
       Object ArrayRes = Array.newInstance((Class)V.firstElement(), size);
       Array.set(ArrayRes,0,V.lastElement());
       Class compClass = (Class)V.firstElement();
       int compSize = 1;
       if( V.lastElement().getClass().isArray())
          compSize=Array.getLength( V.lastElement());
       for( int i=1; i< size; i++){

         if(Obj instanceof Vector)
            V = getInfo(((Vector) Obj).elementAt(i));
         else
            V= getInfo( Array.get(Obj,i));
         if( V== null)
            return null;
         if(!V.firstElement().equals(compClass))
            return null;
         if( V.lastElement().getClass().isArray()){
            if( Array.getLength(V.lastElement())!=compSize)
               return null;
         }else if( compSize !=1)
             return null;
         Array.set(ArrayRes,i,V.lastElement());  
                    
       }
       Res.add( ArrayRes.getClass());
       Res.add(ArrayRes);
       return Res;
     }
     
    
  }
 /**
  * Returns the number of subcomponents of the given Object
  * @param O  The Object whose length is desired.
  * @return  Returns the number of components in the Array or Collection. If
  *       of neither type -a is returned
  */
 public static int Size( Object O ){
   if( O == null)
     return -1;
   if( O.getClass().isArray())
     return Array.getLength(O);
   if( O instanceof Collection )
     return ((Collection)O).size();
   return -1;
 }
 
 /**
  *   Returns the ith component in the Object O
  * @param O  The Object that may be made up of subcomponents
  * @param i  The ith subcomponent
  * @return   The ith component, if there is one, otherwise null is returned
  */
 public static Object Elt( Object O, int i){
   if( i< 0)
     return null;
    if( i >= Size(O))
      return null;
    if( O.getClass().isArray())
      return Array.get(O,i);
    return ((Collection)O).toArray()[i];
 }
 
 
}
