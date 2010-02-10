/* 
 * File: UBwTolCmd.java
 *
 * Copyright (C) 2009, Ruth Mikkelson
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
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author$:
 *  $Date$:            
 *  $Rev$:
 */
package EventTools.ShowEventsApp.Command;



/**
 * This command structure holds a UB matrix and the tolerance from and
 * integer that is allowed for a peak to be indexed successfully.
 * @author ruth
 *
 */
public class UBwTolCmd
{

   float[][]UBT;
   float OffIntMax;
   
   /**
    * Constructor
    * @param UBT  The transpose of the UB matrix.
    * @param OffIntMax  The max distance(absolute values) from and
    *                   integer an h,k, AND l value of the peak must
    *                   be to be successfully indexed.
    */
   public UBwTolCmd( float[][]UBT,  float OffIntMax)
   {

      this.UBT = UBT;
      this.OffIntMax = OffIntMax;
   }
   
   /**
    * 
    * @return  The UB matrix
    */
   public float[][] getUB()
   {
      return UBT;
   }
   
   /**
    *  
    * @return The max distance(absolute values) from and
    *                   integer an h,k, AND l value of the peak must
    *                   be to be successfully indexed.
    */
   public float getOffIntMax()
   {
      return OffIntMax;
   }
   
   /**
    * A string representation of this command.
    */
   public String toString()
   {
      return "\nUB Transp:"+gov.anl.ipns.Util.Sys.StringUtil.toString( UBT,true )+
             "\n Max Displacement from Integer:"+ OffIntMax+"\n";
   }
}
