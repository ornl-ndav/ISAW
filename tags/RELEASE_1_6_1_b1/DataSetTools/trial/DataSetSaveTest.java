/*
 * File:  DataSetSaveTest.java
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
 * Revision 1.3  2002/11/27 23:23:30  pfpeterson
 * standardized header
 *
 */

import javax.swing.*;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.viewer.*;
import DataSetTools.retriever.*;
import DataSetTools.util.*;
import java.io.*;
import java.util.zip.*;

/**
 *  Test binary i/o of DataSets
 */

public class DataSetSaveTest 
{

/**
  *  The main program method for this object
  */
public static void main(String args[])
{
  DataSet      A_histogram_ds = null;  
  DataSet      B_histogram_ds = null;
  String       run_A = "/IPNShome/dennis/ARGONNE_DATA/gppd9902.run";

  // Get a DataSet from the runfile and show it.

  RunfileRetriever rr;   
  rr = new RunfileRetriever( run_A ); 
  A_histogram_ds = rr.getDataSet( 1 );
  rr = null;

  ViewManager view_manager_A = null;  
  view_manager_A = new ViewManager( A_histogram_ds, IViewManager.IMAGE );

  DataSet_IO.SaveDataSet( A_histogram_ds, "serialized_data.bin" );
  B_histogram_ds = DataSet_IO.LoadDataSet( "serialized_data.bin" );

  ViewManager view_manager_B = null;
  view_manager_B = new ViewManager( B_histogram_ds, IViewManager.IMAGE );
  }

} 
