
/*
 * File:  IWrappableWithCategoryList.java 
 *             
 * Copyright (C) 2005, Ruth Mikkelson
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
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2005/01/07 16:31:14  rmikk
 * Initial Checkin.
 * This new interface requires a Wrappable to have the getCategoryList
 *     method
 * Also,this interface contains some static String lists that can be used as
 *    return values for the getCategoryListMethod
 *
 */

package DataSetTools.operator;

/**
 * This interface is just the Wrappable Interface with a getCategoryList method
 *  added
 */
public interface IWrappableWithCategoryList extends Wrappable {

   /**
    *   String arrays that are usable return value of the getCategoryList
    *   method.
    */
   public static final String[] TOF_NSAS= {"operator","Instrument Type",
                                                   "TOF_NSAS"};
                                     
   public static final String[] TOF_NSCD={"operator","Instrument Type",
                                                     "TOF_NSCD"};
                                                     
   public static final String[] TOF_NPD={"operator","Instrument Type",
                                                     "TOF_NPD"};
                                                     
   public static final String[] TOF_NDGS={"operator","Instrument Type",
                                                        "TOF_NDGS"};
                                                        
   public static final String[] TOF_NGLAD={"operator","Instrument Type",
                                                       "TOF_NGLAD"};
   
   /**
    * Get an array of strings listing the operator category names  for 
    * this operator. The first entry in the array is the 
    * string: Operator.OPERATOR. Subsequent elements of the array determine
    * which submenu this operator will reside in.
    * 
    * @return  A list of Strings specifying the category names for the
    *          menuing system 
    *        
    */
   public String[] getCategoryList();
}
