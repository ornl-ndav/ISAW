/*
 * File:  GetDataAttribute.java 
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
 *  $Log$
 *  Revision 1.4  2003/02/07 13:46:37  dennis
 *  Added getDocumentation() method. (Mike Miller)
 *
 *  Revision 1.3  2002/11/27 23:16:41  pfpeterson
 *  standardized header
 *
 *  Revision 1.2  2002/09/19 16:00:04  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.1  2002/02/22 21:00:06  pfpeterson
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
  *  Allows the user to get attributes from the Data blocks in a DataSet
  *
  *  @see DS_Attribute 
  */

public class GetDataAttribute extends    DS_Attribute 
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

  public GetDataAttribute( )
  {
    super( "Get Data Attribute" );
  }

  
/* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied.
   *  @param  index       The index of the data block whose attribute is to 
   *                      be set.
   *  @param   Attrib     The Attribute to be set.
   * 
   */

  public GetDataAttribute  ( DataSet               ds,
                             Integer               index,  
                             AttributeNameString   Attrib
                           )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters
    IParameter parameter ;

    parameter = getParameter( 0 );
    parameter.setValue( index);

    parameter= getParameter( 1 );
    parameter.setValue( Attrib);   

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }

/* ---------------------------getDocumentation--------------------------- */
 /**
  *  Returns a string of the description/attributes of GetDataAttribute
  *   for a user activating the Help System
  */
  public String getDocumentation()
  {
    StringBuffer Res = new StringBuffer();
    Res.append("@overview This operator returns the attribute ");
    Res.append("value of the data.\n");
    Res.append("@algorithm Given a data set, an index, and the ");
    Res.append("attribute name, the attribute value for the data ");
    Res.append("at the index is found.\n");
    Res.append("@param ds\n");
    Res.append("@param index\n");
    Res.append("@param Attrib\n");
    Res.append("@return an Object containing the attribute value\n"); 
    Res.append("@error Improper index\n");  
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
    Parameter parameter;
    
    parameter = new Parameter( "Data block index?", new Integer( 0 ));
    addParameter( parameter);

    parameter = new Parameter( "Attribute?", new AttributeNameString("") );
    addParameter( parameter );
  }

  /* ---------------------------- getResult ------------------------------- */
  /**
   * @return An Object is returned containing the value of the attribute.
   * The data type of this value depends on the dataset. 
   */

  public Object getResult()
  { 
//     Attribute A;
     DataSet ds = getDataSet();

     int index = ((Integer) (getParameter(0).getValue())).intValue();
     String S  = ((AttributeNameString)(getParameter(1).getValue())).toString();

     Data D    = ds.getData_entry(index);
     if( D == null) 
       return new ErrorString(" Improper index ");
     
     Object O = D.getAttributeValue( S );
     if ( O == null)
       return new ErrorString(" Attribute "+ S + " is not in the List" );

     return O;
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current GetDataAttribute Operator.  The list of 
   * parameters and the reference to the DataSet to which it applies is copied.
   */
  public Object clone()
  {
    GetDataAttribute new_op = new GetDataAttribute( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }
  
 /* ------------------------------- main --------------------------------- */ 
 /** 
  * Test program to verify that this will compile and run ok.  
  *
  */
  
  public static void main( String args[] )
  {

     System.out.println("Test of GetDataAttribute starting...");
     DataSet ds = DataSetFactory.getTestDataSet();
     Data d = ds.getData_entry(1);
     Integer i = new Integer(1);
     AttributeNameString at_name = new
             AttributeNameString( d.getAttribute(1).getName() );
     
     GetDataAttribute test_group = new GetDataAttribute( ds, i, at_name );
     
     Object attrib_value = test_group.getResult(); 
     System.out.println( "Attribute value: " + attrib_value.toString() );
     
     System.out.println( test_group.getDocumentation() );
    
     System.out.println("Test of GetDataAttribute done.");
     
  } 

}
