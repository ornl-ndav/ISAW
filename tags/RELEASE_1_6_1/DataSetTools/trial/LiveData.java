/*
 * File:  LiveData.java
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
 * Revision 1.4  2002/11/27 23:23:30  pfpeterson
 * standardized header
 *
 * Revision 1.3  2002/02/28 19:57:37  dennis
 * Modified import statements due to operator reorganization.
 *
 */

import javax.swing.*;

import DataSetTools.dataset.*;
import DataSetTools.dataset.Data;
import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.*;
import DataSetTools.operator.DataSet.Math.Analyze.*;
import DataSetTools.viewer.*;
import DataSetTools.retriever.*;

/**
  *    This demonstrates repeatedly reading and processing files from a 
  *    sequence of runfiles.
  */  

public class LiveData 
{

/**
  *  The main program method for this object
  */
public static void main(String args[])
{
  DataSet          histogram_ds;             // we'll reuse these two DataSets
  DataSet          sum_ds; 

  ViewManager      hist_view_manager = null; // we'll reuse these view managers 
  ViewManager      sum_view_manager  = null; 

  DataSetOperator  op;
  RunfileRetriever rr; 

  String           path = "/usr/home/dennis/ARGONNE_DATA/GPPD_DATA/";
  String           file; 
  int              first = 11471;      // read runfiles with these run numbers
  int              last  = 11602;
  float            min_time = 0;       // sum over this time interval;
  float            max_time = 33000; 

                                               // repeatedly read a new file
                                               // and update the views
  for ( int num = first; num <= last; num++ )
  { 
    file = path + "GPPD" + num + ".RUN";       // get the new file
    System.out.println( "Opening" + file );
    rr = new RunfileRetriever( file ); 
    histogram_ds = rr.getDataSet( 2 );
                                              // form the total counts function 
    op = new DataSetCrossSection( histogram_ds,    
                                  min_time, 
                                  max_time, 
                                  Attribute.GROUP_ID );
    sum_ds = (DataSet)op.getResult();
                                              // construct the view managers
                                              // the first time, other times,
                                              // reuse them.
    if ( num == first )
    {
      hist_view_manager = new ViewManager(histogram_ds, IViewManager.IMAGE );
      sum_view_manager  = new ViewManager(sum_ds, IViewManager.SCROLLED_GRAPHS);
    }
    else
    {
      hist_view_manager.setDataSet( histogram_ds );
      sum_view_manager.setDataSet( sum_ds );
    }
                                             // pause.... this would be replace
                                             // by code to check for a new file
    try
    {
      Thread.sleep( 10000 );
    }
    catch ( InterruptedException e )
    {
      System.out.println("ERROR in sleep "+e );
    }

  }
} 

}
