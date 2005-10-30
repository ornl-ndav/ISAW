/*
 * File: ClearSelect.java
 *
 * Copyright (C) 2003, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.3  2005/10/03 01:35:05  dennis
 * Made title more explanatory: "Clear All Selection Flags", instead of
 * "Clear Groups".  Cleaned up some of the javadocs.
 *
 * Revision 1.2  2004/01/22 02:39:54  bouzekc
 * Removed/commented out unused imports/variables.
 *
 * Revision 1.1  2003/07/05 18:09:45  rmikk
 * Initial Checkin
 *
 */

package DataSetTools.operator.DataSet.Attribute;

import DataSetTools.dataset.DataSet;
import java.io.Serializable;
import java.lang.String;
import java.lang.Object;
import java.util.Vector;

/**
 *  Sets all selection flags to "false" for all Data blocks in this
 *  DataSet.
 */

public class ClearSelect
  extends    DS_Attribute
  implements Serializable
{

  public static final String TITLE = "Clear All Selection Flags";
 

  /**
   * Default constructor.
   */
  public ClearSelect()
  {
    super( TITLE );
  }


  /**
   *  Construct an operator for a specified DataSet.  When the getResult
   *  method is called, all the select flags on all the Data blocks of this 
   *  DataSet will be set to false.
   *
   *  @param  ds  The DataSet whose select flags will be clearred
   */
  public ClearSelect( DataSet ds )
  {
    this();
    setDataSet( ds );
    setDefaultParameters();
  }


  /**
   *  Get the end user documentation for this operator.
   *
   *  @return   A multi-line string describing this operator.
   */
  public String getDocumentation()
    {
       StringBuffer Res = new StringBuffer();
       Res.append("@overview  This operator clears all the select flags " );
       Res.append("on all of the Data blocks of the DataSet.");

       Res.append("@param ds\n");
       
       Res.append("@return Success"); 
      
       return Res.toString();
    }


  /**
   *  Set up the parameter list for this operator.  For this operator
   *  the list is empty.
   */
  public void setDefaultParameters()
  {
    parameters = new Vector();             // Set empty list of parameters
  }


  /**
   *  Clears all the select flags on all the groups of the DataSet 
   */
  public Object getResult()
  { 
    DataSet DS = getDataSet();
    DS.clearSelections();
    
    return "Success";
  }
 
  
}
