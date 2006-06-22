/*
 * File: GetSelectedIndices.java
 *
 * Copyright (C) 2005, Dennis Mikkelson
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
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.1  2005/10/03 04:02:21  dennis
 * Initial version of operator to return list containing the positions
 * (i.e. indices) of all selected Data blocks in the DataSet.  The list
 * of selected positiosn is returned as a String.  It may be necessary to
 * implement another operator to convert such a string to a Vector, so
 * that the indices can be used from the ISAW scripting language.
 *
 *
 */

package DataSetTools.operator.DataSet.Attribute;

import DataSetTools.dataset.DataSet;
import gov.anl.ipns.Util.Numeric.*;

import java.io.Serializable;
import java.util.Vector;

import Command.*;

/**
 * Gets a String listing the indices of all currently selected Data blocks
 * in this DataSet.
 */

public class GetSelectedIndices extends    DS_Attribute
                                implements Serializable
{
  public static final String TITLE   = "Get Indicies of Selected Groups";


  /* ------------------------ default constructor ----------------------- */
  /**
   * Construct an operator with a default parameter list.  If this construtor
   * is used, meaningful values for the parameters should be set before 
   * calling getResult().
   */
  public GetSelectedIndices()
  {
    super( TITLE );
  }


  /* ---------------------------- constructor ---------------------------- */
  /**
   *  Construct this operator for the specified DataSet. 
   *
   *  @param  ds         The DataSet to which the operation is applied
   */
  public GetSelectedIndices( DataSet  ds )
  {
    this();
    setDataSet( ds );
    setDefaultParameters();
  }


  /* --------------------------- getDocumentation ------------------------ */
  /**
   *  Get the end user documentation for this operator.
   *
   *  @return   A multi-line string describing this operator.
   */
  public String getDocumentation()
  {
    StringBuffer Res = new StringBuffer();
    Res.append("@overview This operator gets a list of the positions ");
    Res.append(" of all currently selected Data blocks in this DataSet."); 

    Res.append("@param ds  The DataSet whose selection flags are to be ");
    Res.append("checked\n");

    Res.append("@return A String containing the indices in the DataSet "); 
    Res.append(" of all currently selected Data blocks.");
    
    return Res.toString();
  }


  /* ------------------------ setDefaultParameters ------------------------ */ 
  /**
   *  Set up the parameter list for this operator.  
   */
  public void setDefaultParameters()
  { 
    parameters = new Vector();       // Empty parameter list for this operator 
  }


  /* ----------------------------- getResult ---------------------------- */
  /**
   *  Gets the list of selected indices.
   *
   *  @return String with the selected indices. 
   */
  public Object getResult()
  { 
    DataSet ds        = getDataSet();
    
    int indices[] = ds.getSelectedIndices();
    String result = IntList.ToString( indices );

    return result;
  }


  /* ------------------------------ main -------------------------------- */
  /**
  *  Test program.
  *
  *   args[0] contains the filename to use
  */
  public static void main( String args[] )
  {
    System.out.println("START OF TEST");

    DataSet[] dss = null;
    try
    {
       dss = ScriptUtil.load( args[0] );
    }
    catch( Exception ss)
    { 
      System.exit(0);
    }

    DataSet ds = dss[ dss.length-1 ];
    System.out.println( "Loaded DataSet " + ds );

    SelectByIndex select_op = new SelectByIndex( ds, "5:10,20,30:35", 
                                                     "Set Selected" );
    select_op.getResult();

    GetSelectedIndices get_op = new GetSelectedIndices( ds );
 
    System.out.println("The selected indices are " + get_op.getResult() );
  }

}
