/*
 * $Id$
 *
 * listens to the menu that OperatorMenu.build(...) returns and handles
 * each selection appropriatly.
 *
 * $Log$
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
    sessionLog = sessionLog_;
  }


  /**
   * constructs this object with the appropriate links to ISAW's tree and
   * session log.  the array of DataSet objects allows the JParametersDialog
   * to offer only selected DataSet objects as additional parameters.
   */
  public JOperationsMenuHandler( DataSet[] dss_, 
                                 JTreeUI treeUI_ )
  {
    dss = dss_;
    treeUI = treeUI_;
    sessionLog = null;
  }


  /** 
   * handles all of the menu selection events.  when there are multiple
   * DataSet objects selected,
   */     
  public void actionPerformed( ActionEvent e ) 
  {
    String s = e.getActionCommand();

//    System.out.println( "actionCommand: " + s );
    for( int i=0;  i<dss[0].getNum_operators();  i++ )
    {
//      System.out.println(  "title: " + dss[0].getOperator(i).getTitle()  );
      if(   s.equalsIgnoreCase(  dss[0].getOperator(i).getTitle()  )   )
      {
/*
        DSgetArray DSA = new DSgetArray( treeUI );  //DataSet objects to be
        DataSet Dss[];                              //shown as additional args
        Dss = DSA.getDataSets();                    //in JParameterDialog

        JParametersDialog pDialog = new JParametersDialog( op,
                                                           Dss, 
                                                           sessionLog,
                                                           treeUI );
*/
        DataSetOperator op = dss[0].getOperator(i);
        JParametersDialog pDialog = new JParametersDialog( op,
                                                           dss,
                                                           sessionLog,
                                                           treeUI );
      }
    }
  }
}


