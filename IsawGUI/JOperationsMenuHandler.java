/*
 * File: JOperationsMenuHandler.java
 *
 * Copyright (C) 1999, Alok Chatterjee
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
 * Contact : Alok Chatterjee <achatterjee@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * listens to the menu that OperatorMenu.build(...) returns and handles
 * each selection appropriatly.
 *
 * $Log$
 * Revision 1.19  2002/11/27 23:27:07  pfpeterson
 * standardized header
 *
 * Revision 1.18  2002/02/22 20:39:13  pfpeterson
 * Operator reorganization.
 *
 */
 
package IsawGUI;

import DataSetTools.dataset.DataSet;
import DataSetTools.util.IObserver;
import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.*;
import DataSetTools.components.ParametersGUI.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import javax.swing.text.*; 
import Command.*;
 
/**
 * 
 */
public class JOperationsMenuHandler 
  implements ActionListener, 
             Serializable
{

                          //these are the DataSet object(s) to
                          //apply the operator to.  only the first
                          //element is used when 'use_array' remains
                          //false.  if 'use_array' is true, then the
                          //operator is applied to all DataSet objects
                          //in this array.
  private DataSet[] dss_to_act_upon;

                          //allows this menu acess to all of the DataSet
                          //objects that could be used in the case that
                          //the operator needs DataSet objects as parameters.
  private IDataSetListHandler alt_ds_src;  
  private boolean use_array;


                          //if an operator generates a new DataSet, this is
                          //the only object that is notified.  it is sent
                          //via the IObserver.update(...) method, where
                          //'reason' is an instnace of DataSet.
  private IObserver observer;

                          //the sessionLog must be in scope so that 
                          //messages can be appended to it.  since the 
                          //intent of the log is to keep track of everything 
                          //done to the DataSet object, we should update
                          //the log when an operator is applied ...for
                          //good karma.
  Document sessionLog;


  /**
   * constructs this object with the appropriate links to ISAW's tree and
   * session log.
   */
  public JOperationsMenuHandler( DataSet             ds, 
                                 IDataSetListHandler alt_ds_src,
                                 IObserver           observer,
                                 Document            sessionLog )
  {
    dss_to_act_upon = new DataSet[1];  
    dss_to_act_upon[0] = ds;
    use_array = false;
    this.alt_ds_src = alt_ds_src;
    this.observer = observer;
    this.sessionLog = sessionLog;
  }


  /**
   * constructs this object with the appropriate links to ISAW's tree and
   * session log.  the array of DataSet objects allows the JParametersDialog
   * to offer only selected DataSet objects as additional parameters.
   *
   * @param dss_to_act_upon  the DataSet object or objects on which to operate.  
   *                         by default, only the first element is used.  to
   *                         override this behavior, set use_array.  this 
   *                         capability is provided so that the programmer can 
   *                         apply operators to more than one DataSet object 
   *                         at a time.
   * @param tree             reference to a JDataTree as a container of DataSet 
   *                         objects.
   *                         this parameter is used to get a list of all of the 
   *                         DataSet objects that can be operated upon.  this is
   *                         distinct from the multiple DataSet capability that
   *                         the above parameter provides.
   * @param use_array        overrides default behavior of using only the first
   *                         element in dss_to_act_upon.
   */
  public JOperationsMenuHandler( DataSet[]           dss_to_act_upon, 
                                 boolean             use_array, 
                                 IDataSetListHandler alt_ds_src,
                                 IObserver           observer,
                                 Document            sessionLog )
  {
    this.dss_to_act_upon = dss_to_act_upon;
    this.alt_ds_src = alt_ds_src;
    this.observer = observer;
    this.sessionLog = sessionLog;
    use_array = use_array;
  }


  /** 
   * handles all of the menu selection events.  note that 'observer'
   * is the only object that is notified of new DataSet that might 
   * be generated by this method. 
   */     
  public void actionPerformed( ActionEvent e ) 
  {
    String s = e.getActionCommand();

    for( int dataset=0;  dataset<dss_to_act_upon.length;  dataset++ )
    {
      DataSet ds = dss_to_act_upon[ dataset ];

      for( int i=0;  i<ds.getNum_operators();  i++ )
      {
        if( !use_array  )
        {
          if(   s.equalsIgnoreCase(  ds.getOperator(i).getTitle()  )   )
          {
            DataSetOperator op = ds.getOperator(i);
            JParametersDialog pDialog = new JParametersDialog( op,
                                                               alt_ds_src, 
                                                               sessionLog,
                                                               observer );
          }
        }
        else
        {
          System.out.println( 
            "JOperationsMenuHandler.actionPerformed(...): feature not implemented" );
        }
      }
    }
  }
}


