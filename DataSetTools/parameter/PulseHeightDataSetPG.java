/*
 * File:  PulseHeightDataSetPG.java
 *
 * Copyright (C) 2006, Dennis Mikkelson
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
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.16  2006/07/10 22:28:36  dennis
 * Removed unused imports after refactoring to use new Parameter GUIs
 * in gov.anl.ipns.Parameters.
 *
 * Revision 1.15  2006/07/10 15:32:38  dennis
 * Removed as part of change over to new parameter GUIs in
 * gov.anl.ipns.Parameters
 *
 * Revision 1.1  2006/07/04 00:21:09  dennis
 * Refactored type-specific DataSet PG that extends the abstract
 * base class DataSetChoiceListPG.
 *
 */
package DataSetTools.parameter;

import DataSetTools.dataset.Attribute;

/**
 * 
 * Class to deal with lists of DataSets, restricted to only pulse heigh
 * DataSets.
 *
 * @author Dennis Mikkelson
 */
public class PulseHeightDataSetPG extends DataSetPG 
{

   /**
    * Constructor
    * @param name   The Prompt for this data set
    * @param val    An initial sample DataSet value, or null
    * @throws IllegalArgumentException
    */
   public PulseHeightDataSetPG( String name, Object val ) 
                                             throws IllegalArgumentException 
   {
      super( name, val, Attribute.PULSE_HEIGHT_DATA );
   }

   /**
    * Get a new PulseHeightDataSetPG that is a copy of the current one.
    * This overides the getCopy() method in DataSetPG.
    *
    * @see gov.anl.ipns.Parameters.IParameter#clone()
    * @return a new PulseHeightDataSetPG
    */
   public Object clone() 
   {
      PulseHeightDataSetPG copy = new PulseHeightDataSetPG(getName(), ds_value);
      
      for( int i=0; i < ds_list.size(); i++)
         copy.addItem( ds_list.elementAt( i ));
      
      copy.setValidFlag( getValidFlag() );

      return copy;
   }

}
