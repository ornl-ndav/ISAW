/*
 * File:  TestIntegHist.java
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
 * Revision 1.4  2002/03/13 16:13:41  dennis
 * Converted to new abstract Data class.
 *
 * Revision 1.3  2002/02/28 19:57:47  dennis
 * Modified import statements due to operator reorganization.
 *
 * Revision 1.2  2001/04/26 15:29:10  dennis
 * Added copyright and GPL info at the start of the file.
 *
 */

import DataSetTools.dataset.*;
import DataSetTools.dataset.Data;
import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.Math.Analyze.*;
import DataSetTools.viewer.*;
import DataSetTools.retriever.*;


public class TestIntegHist 
{
  public static void main(String args[])
  {
    DataSet      monitor_ds;

    String       run = "/usr/home/dennis/ARGONNE_DATA/SEPD_DATA/SEPD16444.RUN";

    RunfileRetriever rr;    
    ViewManager view_manager;  
    rr = new RunfileRetriever( run ); 
    monitor_ds = rr.getDataSet( 0 );

    view_manager = new ViewManager( monitor_ds, IViewManager.IMAGE);

    Operator op = new IntegrateGroup( monitor_ds, 4, 3000, 25000 );

    Data monitor = monitor_ds.getData_entry_with_id( 4 );

    //monitor.print( 0, 10 );
    //monitor.print( 20, 30 );

    //monitor.print( 1120, 1130 );

    float y[] = monitor.getY_values();

    double sum = 0;
    for ( int i = 25; i <= 1124; i++ )
      sum += y[i];

    System.out.println("-----------------------------------------------------");
    System.out.println("Sum of group 4, beginning with bin 25, t_left = 3000");
    System.out.println("ending with bin 1124, tleft = 24980 gives:");
    System.out.println("Sum of bins = "+ sum );
    System.out.println("IntegrateGroup 4 on [3000,25000] = " + op.getResult());

    op = new IntegrateGroup( monitor_ds, 4, 3000, 25020 );
    sum = 0;
    for ( int i = 25; i <= 1125; i++ )
      sum += y[i];
    System.out.println("-----------------------------------------------------");
    System.out.println("Sum of group 4, beginning with bin 25, t_left = 3000");
    System.out.println("ending with bin 1125, tleft = 25000 gives:");
    System.out.println("Sum of bins = "+ sum );
    System.out.println("IntegrateGroup 4 on [3000,25020] = " + op.getResult());

    System.out.println("-----------------------------------------------------");
    System.out.println("NOTE: The result 6565210 from the faxed copy could");
    System.out.println("be obtained by adding bin 1124 twice.");
  } 
} 
