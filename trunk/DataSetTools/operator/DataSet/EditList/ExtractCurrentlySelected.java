/*
 * File:  ExtractCurrentlySelected.java 
 *
 * Copyright (C) 2001, Ruth Mikkelson
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
 * Modified:
 *             
 * $Log$
 * Revision 1.3  2003/02/07 13:48:19  dennis
 * Added getDocumentation() method. (Mike Miller)
 *
 * Revision 1.2  2002/11/27 23:17:40  pfpeterson
 * standardized header
 *
 * Revision 1.1  2002/02/22 21:01:56  pfpeterson
 * Operator reorganization.
 *
*/


package DataSetTools.operator.DataSet.EditList;

import DataSetTools.dataset.*;
import java.util.*;
import DataSetTools.util.*;
import DataSetTools.operator.Parameter;

/** Extracts the currently selected or non-selected groups. <P>
* The Title of this operator is <B>Extract Currently Selected Data blocks</b>.
*<BR>
* The Command is <B>ExtSel</b>. <P>
*This Title refers to this operator in menu bars.  The Command refers to this
* operator in Scripts.
*/ 
public class ExtractCurrentlySelected extends DS_EditList
  {
    public ExtractCurrentlySelected()
      { super( "Extract Currently Selected Data blocks" );       
        
      }


   /** Extracts currently selected or non-selected groups
   *@param ds  the data set that this operation is to be applied
   *@param  status <UL>
   *                <LI>if true, the selected items are used 
   *                <LI> if false, the unselected items are used
   *               </ul> 
   *@param make_new_ds  if true a new data set is created
   */
    public ExtractCurrentlySelected( DataSet ds,
                               boolean status,
                               boolean make_new_ds)

       {super( "Extract Currently Selected Data blocks" );
        parameters = new Vector();
        setDataSet( ds );
        addParameter( new Parameter( "Use selected vs unselected", 
                                new Boolean( status)));
        addParameter( new Parameter( "make new Data Set", 
                                 new Boolean( make_new_ds)));
       }
  

    /** Sets a set of default parameters
   */
    public void setDefaultParameters()
      {
       parameters = new Vector();
       addParameter( new Parameter( "Use selected vs unselected", 
                                new Boolean( true )));        
        addParameter( new Parameter( "make new Data Set", 
                                 new Boolean( true )));
     
     
      }
      
 /* ---------------------------getDocumentation--------------------------- */
 /**
  *  Returns a string of the description/attributes of ExtractCurrentlySelected
  *   for a user activating the Help System
  */
  public String getDocumentation()
  { 
    StringBuffer Res = new StringBuffer();
    Res.append("@overview This operator extracts the selected or ");
    Res.append("non-selected groups from the given dataset.");
    Res.append("@algorithm Given a data set, a status, and whether to ");
    Res.append("make a new dataset, the selected (status = true) or ");
    Res.append("unselected (status = false) groups are extracted from ");
    Res.append("the dataset.\n");
    Res.append("@param ds\n");
    Res.append("@param status\n");
    Res.append("@param make_new_ds\n");
    Res.append("@return a reference to the dataset\n"); 
    Res.append("@error There is no Data Set\n");  
    Res.append("@error Result has no entries\n");   
    
    return Res.toString();
    
  }
    /** Returns <B>ExtSel</b>, the command used by Scripts to refer to this
   * operator
   */
    public String getCommand()
     { return "ExtSel";
      }
 

   /**  This Method executes the operator specific code to extract the
   *    selected or non-selected groups from a given dataset. The result
   *    can be the given data set or the given data set stays fixed and a
   *    new data set with the extracted groups is created.
   *@return <ul>  A reference to the data set with the extracted groups or
   *              an ErrorString describing the error</ul>
   *<ul>Error coditions:
   *    <li> <B>There is no Data Set</b> if the data set is null
   *    <LI><B>Result has no entries</b> if the resultant data set has no
   *         data blocks.
   */
    public Object getResult()
     {  boolean useSelected = ((Boolean)(getParameter(0).getValue())).
                                booleanValue();
        boolean NewDataSet =((Boolean)(getParameter(1).getValue())).
                                booleanValue();
        DataSet ds = getDataSet();
   
        if( ds == null )
          return new ErrorString( "There is no Data Set");
        DataSet Res = new DataSet( "" , "" );       
        Res.copy( ds);
       
        Res.removeSelected(  !useSelected );
        if( Res.getNum_entries() <= 0 )
           return new ErrorString( "Result has no entries" );
        if( !NewDataSet )
          { ds.copy( Res );
            Res = ds;
          }
        String logMessage = "Extracted the ";
        if( useSelected )
            logMessage += "Selected ";
        else
            logMessage += "Unselected ";
        logMessage += "Groups"; 
        Res.addLog_entry( logMessage );  

        return Res;

     }


  /** Creates a clone of this object
  */
  public Object clone()
   {ExtractCurrentlySelected new_op = new ExtractCurrentlySelected( );
                                               // copy the data set associated
                                               // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
   }

 /** A Test program to see if the class  paths are set up correctly
 */
 public static void main( String args[] )
   {
    System.out.println( "Test for ExtractCurrentlySelected started..." );
    
    DataSet ds = DataSetFactory.getTestDataSet();
    
    ExtractCurrentlySelected CS = new ExtractCurrentlySelected(ds, false, false);
    System.out.println( CS.getResult().toString() );
    System.out.println( CS.getDocumentation() );
    System.out.println( "Test for ExtractCurrentlySelected finished..." );
   }


  }//End Extract Currently Selected class










