/*
 * File:  NxBean.java 
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
 * $Log$
 * Revision 1.2  2001/08/09 16:44:02  rmikk
 * Added Documentation
 *
 */
package NexIO;

import DataSetTools.dataset.*;


public class NxBeam
{
    String errormessage;
    public NxBeam()
    {errormessage = "";}
    public String getErrorMessage()
     { return errormessage;
     }
    public boolean processDS(  NxNode node,  DataSet DS)
    {errormessage = "Improper inputs to NxBeam";
    if( node == null)
	return true;
    if( DS == null)
       return true;
    if( !node.getNodeClass().equals( "NXbeam"))
       return true;
    errormessage = "";
     NxData_Gen ng = new NxData_Gen();
    NXData_util nu = new NXData_util();
    NxNode n1 = node.getChildNode( "incident_energy");
    if( n1 != null)
      {Object O = n1.getNodeValue();
       float x[];
       float f;
       x = nu.Arrayfloatconvert( O );
       if( x!= null)
         if( x.length > 0)
	     f = x[0];
         else
            f = Float.NaN;
       else f = Float.NaN;
      if( f == Float.NaN)
       {Float F = ng.cnvertoFloat( O );
        if( F != null)
         f = F.floatValue();
       }
      if( f != Float.NaN)
       { FloatAttribute FA = new FloatAttribute( Attribute.NOMINAL_ENERGY_IN,
                             (f));
       
         DS.setAttribute( FA );
          FA = new FloatAttribute( Attribute.ENERGY_IN, (f));
        
         DS.setAttribute( FA );
       }
      }      

//  energy out
  n1 = node.getChildNode( "final_energy");
    if( n1 != null)
      {Object O = n1.getNodeValue();
       float x[];
       float f;
       x = nu.Arrayfloatconvert( O );
       if( x!= null)
         if( x.length > 0)
	     f = x[0];
         else
            f = Float.NaN;
       else f = Float.NaN;
      if( f == Float.NaN)
       {Float F = ng.cnvertoFloat( O );
        if( F != null)
         f = F.floatValue();
       }
      if( f != Float.NaN)
       { FloatAttribute FA = new FloatAttribute( Attribute.ENERGY_OUT,
                 (f));
        
         DS.setAttribute( FA );
       }
      }    
   return false;  
    }//processDS



}
