/*
 * File:  SetAttributeLevel.java 
 *
 * Copyright (C) 2004, Dennis Mikkelson
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
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2004/04/09 19:28:11  dennis
 * Initial version of operator to set retriever attribute level.
 *
 *
 */
package Operators.Generic.Load;

import DataSetTools.operator.*;
import DataSetTools.operator.Generic.Load.*;
import DataSetTools.retriever.*;
import DataSetTools.parameter.*;

import java.util.*;

/** 
 *    This operator controls the number of attributes loaded by 
 *  DataSet retrievers.
 */

public class SetAttributeLevel extends GenericLoad
{
  private static final String TITLE = "Set Attribute Level";
  private static Vector choices = null;

  private ChoiceListPG choice_pg = null;

 /* ------------------------- DefaultConstructor -------------------------- */
 /** 
  *  Creates operator with title "Set Attribute Level" and a default list of
  *  parameters.
  */  
  public SetAttributeLevel()
  {
    super( TITLE );
  }

 /* ----------------------------- Constructor ----------------------------- */
 /** 
  *  Creates operator with title "Set Attribute Level" and the specified 
  *  level.  The getResult method must still be used to execute
  *  the operator.
  *
  *  @param level  Specified the number of attributes to be added to DataSets
  *  by retrievers, by specifying on e of the levels
  *  "NONE", "MINIMAL VISUALIZATION",
  *  "MINIMAL ANALYSIS", "TOFNSAS ANALYSIS", "TOFNPD  ANALYSIS",
  *  "TOFNSCD ANALYSIS", "TOFNDGS ANALYSIS", "TOFNIGS ANALYSIS",
  *  "ANALYSIS", "DIAGNOSTIC"      
  */
  public SetAttributeLevel( String level )
  {
    this();
    choice_pg.setValue( level );
  }

 /* ------------------------------ getCommand ----------------------------- */
 /** 
  * Get the name of this operator to use in scripts
  * 
  * @return  "SetAttrLevel", the command used to invoke this operator in Scripts
  */
  public String getCommand()
  {
    return "SetAttrLevel";
  }

 /* ------------------------ setDefaultParameters ------------------------- */
 /** 
  * Sets default values for the parameters.  The parameters set must match the 
  * data types of the parameters used in the constructor.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();

    if ( choices == null )           // build the choice list once
    {
      choices = new Vector();
      choices.addElement( Retriever.NONE );
      choices.addElement( Retriever.MINIMAL_VISUALIZATION );
      choices.addElement( Retriever.TOFNSAS_ANALYSIS );
      choices.addElement( Retriever.TOFNPD_ANALYSIS );
      choices.addElement( Retriever.TOFNSCD_ANALYSIS );
      choices.addElement( Retriever.TOFNDGS_ANALYSIS );
      choices.addElement( Retriever.TOFNIGS_ANALYSIS );
      choices.addElement( Retriever.ANALYSIS );
      choices.addElement( Retriever.DIAGNOSTIC );
    }

    choice_pg = new ChoiceListPG( "Attribute Level", choices.elementAt(7) );
    choice_pg.addItems(choices);
    addParameter( choice_pg ); 
  }
  
 /* ---------------------- getDocumentation --------------------------- */
  /** 
   *  Returns the documentation for this method as a String.  The format 
   *  follows standard JavaDoc conventions.  
   */                                                                                    
  public String getDocumentation()
  {
    StringBuffer s = new StringBuffer("");                                                 
    s.append("@overview This operator set the level of attributes that ");
    s.append( "are returned by a data retriever");

    return s.toString();
  }    

 /* ------------------------------ getResult ------------------------------- */
 /** 
  *  Executes this operator using the current values of the parameters.
  *
  *  @return  If successful, this returns a new DataSet with the data 
  *           given the expression.
  */
  public Object getResult()
  {
    String level = getParameter(0).getValue().toString();
    
    boolean ok = Retriever.SetAttrLevel( level );
    
    if ( ok )
      return "Level set to " + level; 
    else
      return "FAILED to set level to " + level; 
  }

 /* --------------------------------- clone -------------------------------- */
 /** 
  *  Creates a clone of this operator.  Operators need a clone method, so 
  *  that Isaw can make copies of them when needed.
  */
  public Object clone()
  { 
    Operator op = new SetAttributeLevel();
    op.CopyParametersFrom( this );
    return op;
  }

 /* ------------------------------- main ----------------------------------- */
 /** 
  * Test program to verify that this will complile and run ok.  
  *
  */
  public static void main( String args[] )
  {
    Operator op  = new LoadExpression();
    Object   obj = op.getResult();
                                                 // display any message string
                                                 // that might be returned
    System.out.println("Operator returned: " + obj );

    System.out.println("Test of SetAttributeLevel done.");
  }
}
