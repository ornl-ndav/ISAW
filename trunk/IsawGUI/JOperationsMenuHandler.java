/*
 * $Id$
 *
 * listens to the menu that OperatorMenu.build(...) returns and handles
 * each selection appropriatly.
 *
 * $Log$
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

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.components.ParametersGUI.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import javax.swing.text.*; 
import Command.*;
 
public class JOperationsMenuHandler 
  implements ActionListener, 
             Serializable
{
  private DataSet[] dss;
  private JTreeUI treeUI;
  Document sessionLog;
  boolean use_array;

  /**
   * constructs this object with the appropriate links to ISAW's tree and
   * session log.
   */
  public JOperationsMenuHandler( DataSet ds_, 
                                 JTreeUI treeUI_, 
                                 Document sessionLog_ )
  {
    dss = new DataSet[1];  dss[0] = ds_;
    treeUI = treeUI_;
    dss = (new DSgetArray( treeUI )).getDataSets(); 
    sessionLog = sessionLog_;
    use_array = false;
  }


  /**
   * constructs this object with the appropriate links to ISAW's tree and
   * session log.  the array of DataSet objects allows the JParametersDialog
   * to offer only selected DataSet objects as additional parameters.
   *
   * @param dss_       the DataSet object or objects on which to operate.  
   *                   by default, only the first element is used.  to override
   *                   this behavior, set use_selected_dss.  this capability is
   *                   provided so that the programmer can apply operators to
   *                   more than one DataSet object at a time.
   * @param treeUI_    reference to a JTreeUI as a container of DataSet objects.
   *                   this parameter is used to get a list of all of the 
   *                   DataSet objects that can be operated upon.  this is
   *                   distinct from the multiple DataSet capability that the
   *                   above parameter provides.
   * @param use_array_ overrides default behavior of using only the first
   *                   element in dss_.
   */
  public JOperationsMenuHandler( DataSet[] dss_, 
                                 JTreeUI treeUI_,
                                 boolean use_array_ )
  {
    dss = dss_;
    treeUI = treeUI_;
    sessionLog = null;
    use_array = use_array_;
  }


  /** 
   * handles all of the menu selection events.  
   */     
  public void actionPerformed( ActionEvent e ) 
  {
    String s = e.getActionCommand();

    for( int i=0;  i<dss[0].getNum_operators();  i++ )
    {
      if( !use_array  )
      {
        if(   s.equalsIgnoreCase(  dss[0].getOperator(i).getTitle()  )   )
        {
          DSgetArray DSA = new DSgetArray( treeUI );  //DataSet objects to be
          DataSet[] all_dss;                          //shown as additional args
          all_dss = DSA.getDataSets();                //in JParameterDialog

          DataSetOperator op = dss[0].getOperator(i);
          JParametersDialog pDialog = new JParametersDialog( op,
                                                             all_dss, 
                                                             sessionLog,
                                                             treeUI );
        }
      }
      else
      {
        System.out.println( 
          "JOperationsMenuHandler.actionPerformed(): not implemented" );
      }
    }
  }
}


