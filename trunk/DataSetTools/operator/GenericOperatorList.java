/*
 * File:  GenericOperatorList.java 
 *             
 * Copyright (C) 2000, Dennis Mikkelson
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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.16  2001/08/14 20:24:36  dennis
 *  Added SaveGSAS operator
 *
 *  Revision 1.15  2001/08/07 15:59:15  dennis
 *  Added LoadRemoteData operator.
 *
 *  Revision 1.14  2001/08/01 15:53:46  dennis
 *  Added save NeXus file operator.
 *
 *  Revision 1.13  2001/04/26 19:09:34  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.12  2000/11/07 15:52:37  dennis
 *  Added "echo" operator.
 *
 *  Revision 1.11  2000/10/03 21:28:51  dennis
 *  Renamed FudgeFactor operator to DetectorNormalizationFactors operator
 *
 *  Revision 1.10  2000/08/03 22:09:44  dennis
 *  added operator to pause for a specified number of milli-seconds
 *
 *  Revision 1.9  2000/08/03 21:45:49  dennis
 *  Added Dongfeng's utility for quick printing.
 *
 *  Revision 1.8  2000/08/03 15:47:30  dennis
 *  Added $Log to enable recording of comments.
 *
 */

package DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;

/**
  *  This class keeps a list of generic operators that are not associated
  *  with any DataSet and includes methods to get the operator names and
  *  construct instances of the operators.
  *
  *  @see Operator
  */

public class GenericOperatorList implements Serializable
{

  static private final String names[] = { "SumFiles",
                                          "LoadRemote",
                                          "OneFile",
                                          "Mon",
                                          "OneHist",
                                          "DetNormFac",
                                          "PrintDS",
                                          "SaveNX",
                                          "SaveGSAS",
                                          "Pause",
                                          "Echo"    };

  /**
   *  Private constructor ... don't let anyone instantiate this class
   */
   private GenericOperatorList()
   {
   }


  /* ---------------------------- getNum_operators ------------------------- */
  /**
   * Get the number of operators in the list of operators. 
   *
   * @returns  The number of generic operators currently available in this
   *           list of operators.
   */
  static public int getNum_operators() 
  { 
    return names.length;
  }

  /* ---------------------------- getCommand ------------------------- */
  /**
   * Get the command name of an operator in the list of operators.    
   *
   * @param  index  The index specifing which operator name is needed from the 
   *                list of operators.
   *   
   * @returns  The command name of the specified operator.
   */
  static public String getCommand( int index )
  { 
    if ( index < 0 || index >= names.length )
      return null;

    return names[index];
  }

  /* ---------------------------- getOperator ------------------------------ */
  /**
   *  Get a reference to a new instance of an operator from the list of
   *  available operators.  If the specified operator is not in the list,
   *  this method returns null.
   *
   *  @param  index  The index specifing which operator is needed from the 
   *                 list of operators.
   *   
   *  @return a reference to a new instance of the type of operator stored
   *          in the specified position in the list.  If the index is invalid,
   *          return null.
   */
   static public Operator getOperator( int index )
   {
      if ( index < 0 || index >= names.length )
        return null;

      return getOperator( names[index] ); 
   }

  /* ---------------------------- getOperator ------------------------------ */
  /**
   * Get a reference to the operation in the list of available operators
   * with the specified op name.  If the named operator is not in the list, 
   * this method returns null.
   *
   * @param  op_name   The name of the requested operation
   *
   * @return a reference to a new instance of the specified type of operator.
   *         If there is no such operator, return null.
   */
  static public Operator getOperator( String op_name )
  {

    if ( op_name.equals( "SumFiles") )
      return new SumRunfiles();

    else if ( op_name.equals( "LoadRemote" ) )
      return new LoadRemoteData();

    else if ( op_name.equals( "OneFile" ) )
      return new LoadOneRunfile();

    else if ( op_name.equals( "Mon" ) )
      return new LoadMonitorDS();

    else if ( op_name.equals( "OneHist" ) )
      return new LoadOneHistogramDS();

    else if ( op_name.equals( "DetNormFac" ) )
      return new SpectrometerDetectorNormalizationFactor();

    else if ( op_name.equals( "PrintDS" ) )
      return new DataSetPrint();

    else if ( op_name.equals( "SaveNX" ) )
      return new WriteNexus();

    else if ( op_name.equals( "SaveGSAS" ) )
      return new WriteGSAS();

    else if ( op_name.equals( "Pause" ) )
      return new pause();

    else if ( op_name.equals( "Echo" ) )
      return new EchoObject();

     return null;
  }

}
