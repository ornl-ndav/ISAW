/*
 * @(#)JOperationsMenuHandler.java     1.0  99/09/02  Alok Chatterjee
 *
 * 1.0  99/09/02  Added the comments and made this a part of package IsawGUI
 * 
 */
 
package IsawGUI;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import javax.swing.text.*; 
/**
 * The main class for ISAW. It is the GUI that ties together the DataSetTools, IPNS, 
 * ChopTools and graph packages.
 *
 * @version 1.0  
 */
 
public class JOperationsMenuHandler implements ActionListener, Serializable
{
    private DataSet ds;
    private JTreeUI treeUI;
    Document sessionLog;
    public JOperationsMenuHandler(DataSet ds, JTreeUI treeUI, Document sessionLog)
    
    { this.ds = ds ;
      this.treeUI = treeUI;
      this.sessionLog = sessionLog;
    }
 
    public void actionPerformed(ActionEvent ev) 
        
        {
            DataSetOperator op = ds.getOperator(0);
            String s=ev.getActionCommand();
           // for (int i = 0; i<ds.getNum_operators(); i++)
    
           boolean found = false;
           int i = 0;
           while(!found && i<ds.getNum_operators())
            { 
                if (s.equalsIgnoreCase(ds.getOperator(i).getTitle()))
                {op = ds.getOperator(i);
                found = true;
                }
                else i++;
                //System.out.println("We are looking for s:" +s);
            }
            if (found)
            {
                JParametersDialog pDialog = new JParametersDialog(op, treeUI, sessionLog);

            }
        }

  
    


}
