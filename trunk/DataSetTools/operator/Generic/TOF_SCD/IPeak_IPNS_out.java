/*
 * File:  IPeak_IPNS_out.java 
 *             
 * Copyright (C) 2008, Ruth Mikkelson
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
 * Revision 1.1  2008/01/29 19:13:02  rmikk
 * Initial Checkin
 * 
 */
package DataSetTools.operator.Generic.TOF_SCD;




/**
 * @author Ruth
 *
 */
public interface IPeak_IPNS_out extends IPeak{
   

   /**
    *  Accessor method for the detector angle in degrees
    */
   public float detA();
   
   /**
    *  Accessor method for the second detector angle in degrees
    */
   public float detA2();
   
   /**
    *  Accessor method for the detector distance which is given in cm.
    */
   public float detD();
     
 

   

}
