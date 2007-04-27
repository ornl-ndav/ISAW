/*
 * File:  INewOperatorHandler.java 
 *             
 * Copyright (C) 2006, Ruth Mikkelson
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
 * This work was supported by the National Science Foundation under
 * grant number DMR-0218882
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 * * Modified:
 *
 * $Log$
 * Revision 1.2  2007/04/27 12:48:24  rmikk
 * Fixed javadoc errors
 *
 * Revision 1.1  2007/02/21 20:19:08  rmikk
 * New class that Handle  the external operators.  There are methods to get the
 *    operators(saved or new), save and restore the operators.
 *
 * */
package Command;
import DataSetTools.operator.Generic.*;

/**
 * To incorporate external operators in the operators list, this interface MUST be implemented
 * AND the class name must be added as an external operator in the class
 * Command.InstallExternalOperators
 * @author Ruth
 * NOTE:  These classes MUST have a constructor with no arguments. Will be using Class.getInstance()
 * to get  instances of these classes.
 *
 */
public interface INewOperatorHandler {

  
   
   
   /**
    * This will be repeatedly called (until a null is returned) to get NEW
    * operators that were not in the retored operators.  These operators are
    * added to the list of installed operators
    * 
    *   
    */
   public GenericOperator  getNextOperator();
   
   /**
    * loads  the "installed" Generic operator described by ExternalOpnInfo cp. 
    * @param opInfo  Contains the information to get at the operator that is 
    *                 to be loaded 
    * @return  returns the corresponding Generic operator
    */
   public GenericOperator  getOperator( ExternalOpnInfo opInfo);
      
      
   /**
    *  This will allow the user to save the operators in a form that is faster to
    *  restore.
    *  NOTE: How the operator info is saved, verified, and restored is up to the 
    *  implementer of this method and the Restore method below.
    *  
    *  NOTE: This will be called every time after a Restore then 
    */
   public void SaveOperators();
   
   
   /**
    * Adds the resultant operator info structures to the operator list.  Do 
    * not add these operators with the getNexOperator  method above.
    * The actual generic operator does not have to be loaded  until the 
    * getOperator( ExtgernalOpnInfo) is called.
    * 
    * @return  The list of information about the operators that were saved
    *        with the fast save
    */
   public ExternalOpnInfo[] Restore();
}
