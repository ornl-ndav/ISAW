package Command;
import DataSetTools.dataset.*;
import DataSetTools.components.ParametersGUI.*;

import IsawGUI.*;
import java.util.*;
import javax.swing.tree.*;
import javax.swing.*;
public class DSgetArray implements DataSetListHandler
{DataSet DS[];


 public DataSet[] getDataSets()

   {return DS;}


public DSgetArray( JTreeUI jtui)
  { if( jtui == null)
     { DS = null;
       return;
      }
     Vector V = new Vector();
     
     DefaultMutableTreeNode root = (DefaultMutableTreeNode)jtui.getTree().getModel().getRoot();
        int n_containers = root.getChildCount();
  
        for(int i = 0; i<n_containers; i++)
        {
            DefaultMutableTreeNode  container =(DefaultMutableTreeNode)root.getChildAt(i);
            //DefaultMutableTreeNode  container =(DefaultMutableTreeNode)jtui.getSelectedNode();
            int n_children = container.getChildCount();
            for(int k = 0; k < n_children; k++)
            {
                DefaultMutableTreeNode  child =(DefaultMutableTreeNode)container.getChildAt(k);
                DataSet ds = (DataSet)child.getUserObject();
                V.addElement(ds);
            }
        }
     
     DS = new DataSet[V.size()];
     for(int i=0;i<V.size(); i++)
        DS[i]= (DataSet)(V.elementAt(i));   

  }


 }