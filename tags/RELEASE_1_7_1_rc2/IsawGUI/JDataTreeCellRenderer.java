/*
 * File:  JDataTreeCellRenderer.java
 *
 * Copyright (C) 2004 Dominic Kramer
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
 *           Dominic Kramer <kramerd@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 * $Log$
 * Revision 1.3  2004/07/30 16:01:51  kramer
 * Added methods to test if a DataSetMutableTreeNode represents a pulsed height
 * DataSet.  Now the renderer uses different icons for DataMutableTreeNodes
 * corresponding to a monitor, pulsed height histogram, and 'regular' histogram.
 * It also uses a special icon for pulsed height DataSets.  Changed the icons
 * from .gif to .png format.
 *
 * Revision 1.2  2004/07/19 20:01:58  kramer
 *
 * Modified the tree to use custom icons located in ISAW's image directory.  If
 * an icon doesn't exist or isn't really an icon, the renderer will use the
 * corresponding default icon (as displayed in a JTree).  Also, now when a Data
 * object is selected, the corresponding node in the tree has a blue box
 * drawn around its icon in addition to having its font color set to blue.
 *
 * Revision 1.1  2004/07/07 16:23:11  kramer
 *
 * This is the renderer used to render the components added to a JDataTree.
 *
 */
package IsawGUI;

import gov.anl.ipns.Util.Sys.StringUtil;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import DataSetTools.util.SharedData;

/**
 * This is used to render the nodes in JDataTree.
 * @author Dominic Kramer
 */
public class JDataTreeCellRenderer extends DefaultTreeCellRenderer
{
   //these fields are static because there only needs to be one copy of 
   //each in memory
   /**
    * If a node's text equals this String it is assumed to be the 
    * tree's 'Modified' node.
    */
   private static final String MODIFIED_EXPERIMENT_NAME = "Modified";
   
   /** The icon for the root node of the tree. */
   private static final ImageIcon ROOT_IMAGE          = getImageIconForName("session.png");
   
   /** The icon for a node representing a DataSet that is representing a histogram. */
   private static final ImageIcon HISTOGRAM_IMAGE     = getImageIconForName("histogram.png");
   /** The icon for a node representing a Data object. */
   private static final ImageIcon DATA_IMAGE          = getImageIconForName("data.png");
   /** The icon for a node representing a Data object that is selected. */
   private static final ImageIcon SELECTED_DATA_IMAGE = getImageIconForName("data_selected.png");
   
   /** The icon for an Experiment tree node. */
   private static final ImageIcon EXPERIMENT_IMAGE    = getImageIconForName("experiment.png");
   /** The icon for the tree's 'Modified' node. */
   private static final ImageIcon MODIFIED_IMAGE      = getImageIconForName("modified.png");
   
   /** The icon for a pulse height DataSet. */
   private static final ImageIcon PULSE_HEIGHT_DATASET_IMAGE
                                                      = getImageIconForName("pulse_height_dataset.png");
   /** The icon for node representing a pulse height Data block. */
   private static final ImageIcon PULSE_HEIGHT_DATA_IMAGE
                                                      = getImageIconForName("pulse_height_data_block.png");
   /** The icon for node representing a selected pulse height Data block. */
   private static final ImageIcon SELECTED_PULSE_HEIGHT_DATA_IMAGE
                                                      = getImageIconForName("selected_pulse_height_data_block.png");
   
   /** The icon for a node representing a DataSet that is representing a monitor. */
   private static final ImageIcon MONITOR_IMAGE       = getImageIconForName("monitor_dataset.png");
   /** The icon for a node representing a Data object in a monitor DataSet. */
   private static final ImageIcon MONITOR_DATA_BLOCK_IMAGE
                                                      = getImageIconForName("monitor_data_block.png");
   /** The icon for a node representing a selected Data object in a monitor DataSet. */
   private static final ImageIcon SELECTED_MONITOR_DATA_BLOCK_IMAGE
                                                      = getImageIconForName("selected_monitor_data_block.png");

   /**
    * Get the configured component which is to be placed in the tree.
    */
   public Component getTreeCellRendererComponent(JTree tree, Object value, 
                        boolean sel, boolean expanded, boolean leaf, 
                           int row, boolean hasFocus)
   {
      super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, 
            row, hasFocus);
         
      if (value instanceof DataMutableTreeNode)
      {
            if (((DataMutableTreeNode)value).getParent() instanceof DataSetMutableTreeNode )
            {
               if (isAPulseHeight((DataSetMutableTreeNode)((DataMutableTreeNode)value).getParent()))
               {
                  if (((DataMutableTreeNode)value).isSelected())
                  {
                     if (SELECTED_PULSE_HEIGHT_DATA_IMAGE != null)
                        setIcon(SELECTED_PULSE_HEIGHT_DATA_IMAGE);
                     else
                        setIcon(PULSE_HEIGHT_DATA_IMAGE);
                     
                     setForeground(Color.BLUE);
                  }
                  else
                  {
                     setIcon(PULSE_HEIGHT_DATA_IMAGE);
                     setForeground(Color.BLACK);
                  }
               }
               else if (isAMonitor((DataSetMutableTreeNode)((DataMutableTreeNode)value).getParent()))
               {
                  if (((DataMutableTreeNode)value).isSelected())
                  {
                     if (SELECTED_MONITOR_DATA_BLOCK_IMAGE != null)
                        setIcon(SELECTED_MONITOR_DATA_BLOCK_IMAGE);
                     else
                        setIcon(MONITOR_DATA_BLOCK_IMAGE);
                     
                     setForeground(Color.BLUE);
                  }
                  else
                  {
                     setIcon(MONITOR_DATA_BLOCK_IMAGE);
                     setForeground(Color.BLACK);
                  }
               }
               else
               {
                  if (((DataMutableTreeNode)value).isSelected())
                  {
                     if (SELECTED_DATA_IMAGE != null)
                        setIcon(SELECTED_DATA_IMAGE);
                     else
                        setIcon(DATA_IMAGE);

                     setForeground(Color.BLUE);
                  }
                  else
                  {
                     setIcon(DATA_IMAGE);
                     setForeground(Color.BLACK);
                  }
               }
            }
            //if its parent is not a DataSet don't set a custom icon
      }
      else if (value instanceof DataSetMutableTreeNode)
      {
         if (isAMonitor((DataSetMutableTreeNode)value))
            setIcon(MONITOR_IMAGE);
         else if (isAPulseHeight((DataSetMutableTreeNode)value))
            setIcon(PULSE_HEIGHT_DATASET_IMAGE);
         else
            setIcon(HISTOGRAM_IMAGE);
      }
      else if (value instanceof Experiment)
      {
         if (((Experiment)value).getName().equals(MODIFIED_EXPERIMENT_NAME))
            setIcon(MODIFIED_IMAGE);
         else
            setIcon(EXPERIMENT_IMAGE);
      }
      else if (value instanceof DefaultMutableTreeNode)
         setIcon(ROOT_IMAGE);
            
      return this;
   }
   
   //-------------------=[ Overriden Methods ]=----------------------------
   /**
    * Sets the icon to the specified icon if <code>icon</code> is not null.  
    * If <code>icon</code> is null, nothing is done.
    * @param icon The icon that is to be set.
    */
   public void setIcon(Icon icon)
   {
      if (icon != null)
         super.setIcon(icon);
      //otherwise do not set the icon
   }
   
   //---------------------=[ Private Methods ]=-----------------------------
   /**
    * Used to determine if the DataSetMutableTreeNode represents a monitor.  
    * If it has a name with the format *:M* (where * represents any sequence 
    * of characters), it is considered a monitor.
    */
   private static boolean isAMonitor(DataSetMutableTreeNode dsNode)
   {
      return matchesPattern(dsNode.toString(),'M');
   }
   
   /**
    * Used to determine if the DataSetMutableTreeNode represents a pulse height 
    * DataSet.  If it has a name with the format *:P* (where * represents any 
    * sequence of characters), it is considered a pulse height DataSet.
    */
   private static boolean isAPulseHeight(DataSetMutableTreeNode dsNode)
   {
      return matchesPattern(dsNode.toString(),'P');
   }
   
   /**
    * Used to determine if the String <code>str</code> maches the 
    * pattern *:<code>identifier</code>* (where * represents any sequence of characters).  
    * The comparison is case sensitive.
    * @param str The String to process
    * @param identifier The special character right after the colon.
    * @return True if <code>str</code> matches the pattern *:<code>identifier</code>* 
    * and false otherwise.
    */
   private static boolean matchesPattern(String str, char identifier)
   {
      int index = str.indexOf(':');
      if (index>=0 && (index+1)<str.length())
         return (str.charAt(index+1) == identifier);
      else
         return false;
   }
   /**
    * Get the ImageIcon corresponding to the icon in ISAW's image's 
    * directory with the specified name.
    * @param name The name of the icon to use.  This can be either a 
    * GIF, JPEG, or PNG image.  Also, this method will search in ISAW's 
    * image's directory for the icon with this name.  
    * @return The correct ImageIcon or null if the file specified by 
    * name does not exist or does not represent an icon.
    */
   private static ImageIcon getImageIconForName(String name)
   {
      String ipath=SharedData.getProperty("IMAGE_DIR");
      if ( ipath == null ) 
         return null;
      ipath    = StringUtil.setFileSeparator(ipath);
      String fullName = ipath+name;
      File imageFile = new File(fullName);
      if (imageFile.exists())
      {
         Image image = Toolkit.getDefaultToolkit().getImage(fullName);
         if (image != null)
         {
            ImageIcon icon = new ImageIcon(image);
            if (icon.getIconHeight()>0 && icon.getIconWidth()>0)
               return icon;
            else
               return null;
         }
         else
            return null;
      }
      else
         return null;
   }
}
