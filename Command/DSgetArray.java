/*
 * File:  DSgetArray.java 
 *             
 * Copyright (C) 2001, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
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
 * Revision 1.4  2001/06/26 14:39:36  rmikk
 * Changed DataSetListHandler to IDataSetListHandler
 *
 * Revision 1.3  2001/06/01 21:14:13  rmikk
 * Added Documentation for javadocs etc.
 *
 *
 *  5-25-2001  Created
 *
 */
package Command;
import DataSetTools.dataset.*;
import DataSetTools.components.ParametersGUI.*;

import IsawGUI.*;
import java.util.*;
import javax.swing.tree.*;
import javax.swing.*;

/** Implements IDataSetListHandler when the Data Set list is in a JTreeUI
 */
public class DSgetArray implements IDataSetListHandler
{DataSet DS[];


 public DataSet[] getDataSets()

   {return DS;}

/**
*@param jtui  the JTreeUI with the list of data sets
*@see  #IsawGUI.JTreeUI

*/
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
