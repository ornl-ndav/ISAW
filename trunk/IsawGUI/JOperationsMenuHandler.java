/*
 * $Id$
 *
 * listens to the menu that OperatorMenu.build(...) returns and handles
 * each selection appropriatly.
 *
 * $Log$
 * Revision 1.14  2001/07/25 16:14:59  neffk
 * fixed constructor to assign this.observer to the 'observer' parameter
 * and changed some names to make the code easier to read.
 *
 * Revision 1.13  2001/07/23 13:56:59  neffk
 * removed some code that was commented out.
 *
 * Revision 1.12  2001/07/18 17:05:30  neffk
 * fixed bug that selected the incorrect operator.  also moved from
 * JTreeUI to JDataTree.
 *
 * Revision 1.11  2001/07/11 15:51:03  neffk
 * added a bool flag to allow operator to be applied on multiple
 * DataSet objects.  also, fixed a bug in the list of DataSet objects
 * displayed for the user to chose from for applying operators to
 * individual DataSet objects.
 *
 * Revision 1.10  2001/07/09 22:18:38  chatter
 * Corrected the code for minor problems
 *
 * Revision 1.9  2001/07/03 13:50:31  neffk
 * all operators can be invoked from the main menu and right-click menu
 * once again.
 *
 * Revision 1.8  2001/06/27 20:47:32  neffk
 * updated to play nice with the tree and command pane
 *
 * 
 */
 
package IsawGUI;

import DataSetTools.dataset.DataSet;
import DataSetTools.util.IObserver;
import DataSetTools.operator.*;
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

      for( int i=0;  i<dss_to_act_upon[0].getNum_operators();  i++ )
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


