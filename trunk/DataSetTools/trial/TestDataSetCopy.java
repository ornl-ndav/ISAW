/*
 * File:  TestDataSetCopy.java
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
 * $Log$
 * Revision 1.2  2001/04/26 15:29:07  dennis
 * Added copyright and GPL info at the start of the file.
 *
 *
 */

import javax.swing.*;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.viewer.*;
import DataSetTools.retriever.*;

/**
  *    This class tests the DataSet copy method, 
  *    ( Derived from DataSetDemo1.java )
  */  

public class TestDataSetCopy 
{

/**
  *  The main program method for this object
  */
public static void main(String args[])
{
  DataSet      A_histogram_ds,
               B_histogram_ds;  
  String       run_A = "/IPNShome/dennis/ARGONNE_DATA/gppd9902.run";
  String       run_B = "/IPNShome/dennis/ARGONNE_DATA/hrcs2444.run";



  // Get the DataSet from the runfile and show it.

  RunfileRetriever rr;    // The RunfileRetriever object calls John's runfile
                          // package and returns the data as DataSets

  ViewManager view_manager;  // Variable to hold reference to a ViewManager
                             // that will display a DataSet

                                         // Load and show run A histograms 
  rr = new RunfileRetriever( run_A ); 
  A_histogram_ds = rr.getDataSet( 1 );
  rr = null;
  view_manager = new ViewManager( A_histogram_ds, IViewManager.IMAGE );

  rr = new RunfileRetriever( run_B ); 
  B_histogram_ds = rr.getDataSet( 1 );
  rr = null;
  view_manager = new ViewManager( B_histogram_ds, IViewManager.IMAGE );

  B_histogram_ds.copy( A_histogram_ds );

  }

} 
