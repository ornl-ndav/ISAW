/*
 * File:   GetDSAttribute.java 
 *             
 * Copyright (C) 2000, Ruth Mikkelson, Dennis Mikkelson
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
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * This operator gets a DataSet Attribute
 *
 *  $Log$
 *  Revision 1.4  2003/02/07 13:46:37  dennis
 *  Added getDocumentation() method. (Mike Miller)
 *
 *  Revision 1.3  2002/11/27 23:16:41  pfpeterson
 *  standardized header
 *
 *  Revision 1.2  2002/09/19 16:00:02  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.1  2002/02/22 21:00:05  pfpeterson
 *  Operator reorganization.
 *
 */

package DataSetTools.operator.DataSet.Attribute;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.parameter.*;

/**
  *  Allows the user to get attributes on a DataSet
  *
  *  @see DS_Attribute
  */

public class GetDSAttribute extends  DS_Attribute 
                                     implements Serializable
{
  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct an operator with a default parameter list.  If this
   * constructor is used, the operator must be subsequently added to the
   * list of operators of a particular DataSet.  Also, meaningful values for
   * the parameters should be set ( using a GUI ) before calling getResult()
   * to apply the operator to the DataSet this operator was added to.
   */

  public GetDSAttribute( )
  {
    super( "Get DataSet Attribute" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds       The DataSet to which the operation is applied
   *  @param  Attrib   The Attribute to be set.
   *
   */

  public GetDSAttribute  ( DataSet              ds,
                           AttributeNameString  Attrib )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    IParameter parameter = getParameter( 0 );
    parameter.setValue( Attrib);

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }

/* ---------------------------getDocumentation--------------------------- */
 /**
  *  Returns a string of the description/attributes of GetDSAttribute
  *   for a user activating the Help System
  */
  public String getDocumentation()
  {
    StringBuffer Res = new StringBuffer();
    Res.append("@overview This operator returns the attribute ");
    Res.append("value of the data set.\n");
    Res.append("@algorithm Given a data set and the attribute ");
    Res.append("name, the attribute value for the data set ");
    Res.append("is found.\n");
    Res.append("@param ds\n");
    Res.append("@param Attrib\n");
    Res.append("@return an Object containing the attribute value\n"); 
    Res.append("@error Attribute the_attribute is not in the list\n");   
    
    return Res.toString();
    
  }

  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processor: in this case, GetAttr
   */
   public String getCommand()
   {
     return "GetAttr";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters


    Parameter parameter = new Parameter( "Attribute?", 
                                          new AttributeNameString("") );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */
  /**
   * @return An Object is returned containing the value of the attribute.
   * The data type of this value depends on the dataset. 
   */
  public Object getResult()
    { //Attribute A;
     DataSet ds = getDataSet();
     String S = ((AttributeNameString)(getParameter(0).getValue())).toString();
    
     Object O = ds.getAttributeValue( S );
     if ( O == null)
	 return new ErrorString(" Attribute "+ S+ " not in List");
     return O;
  }  


  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current GetDSAttribute Operator.  The list of parameters 
   * and the reference to the DataSet to which it applies is copied.
   */
  public Object clone()
  {
    
   GetDSAttribute new_op = new GetDSAttribute( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

  /* --------------------------- main ----------------------------------- */
  /*
   *  Main program for testing purposes
   */
  public static void main( String[] args )
  {
    System.out.println("Test of GetDSAttribute starting...");
    
    GetDSAttribute op = new GetDSAttribute();

    String list[] = op.getCategoryList();
    System.out.println( "Categories are: " );
    for ( int i = 0; i < list.length; i++ )
      System.out.println( list[i] );
    System.out.println();
    System.out.println( op.getDocumentation() );
    
    System.out.println("Test of GetDSAttribute done.");
  }

}
