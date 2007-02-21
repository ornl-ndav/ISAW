/*
 * File:  ExternalOpnInfo.java 
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
 * Revision 1.1  2007/02/21 20:16:28  rmikk
 * New Class for holding information on external operators( those not in the Isaw
 *     distribution- Isaw_Home/Operators , Group_Home/Operators o
 *    Operators) . The operator does not have to be loaded until it is needed.
 *
 * */
package Command;

import DataSetTools.operator.Operator;


/**
 * Use this for DataSetTools.operator.Generic.GenericOperator's that come from
 *  others ources besides the [GROUP_HOME[x]] Operators and Script   
 * subdirectories, Isaw.jar in [ISAW_HOME] or  the operators in  
 * [Isaw_HOME]/Operators  or [Isaw_HOME]/Scripts Directories.
 * @author Ruth
 *
 */
public class ExternalOpnInfo extends OpnInfo {
  
   public String HandlerClassName;
   
   public ExternalOpnInfo( Operator op ) {

      super( op );
    
   }


   public ExternalOpnInfo( String FileName, String ClassName,
            long lastmodified, boolean isHidden, String CommandName,
            String Title, int NArgs, String[] CatList, String ToolTip )
            throws IllegalArgumentException {
    

      super( FileName , ClassName , lastmodified , isHidden , CommandName ,
               Title , NArgs , CatList , ToolTip );
     
   }
   public void setHandlerClassName( String className){
      this.HandlerClassName = className;
   }
}
