/*
 * File:  NxLogLocator.java 
 *             
 * Copyright (C) 2003, Ruth Mikkelson
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
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 */
package NexIO;

import java.util.Vector;

import NexIO.NexApi.NexNode;

/**
 * @author Dominic Kramer
 */
public class NxlogLocator
{
   private Vector logNodeVec;
   
   public NxlogLocator(NxNode rootNode)
   {
      if (rootNode == null)
         throw new IllegalArgumentException("ERROR:  Cannot pass a 'null' " +
               "root node to the NxlogLocator constructor.");
      this.logNodeVec = new Vector();
      
      scanForNxLogUnderNode(rootNode);
   }
   
   //----------=[ Private Methods ]=--------------
   /**
    * Recursively scans <code>node</code> and all of its child nodes for 
    * Nxlog nodes.  If <code>node</code> or any of its children are Nxlog 
    * nodes, they are appended to the end of the Vector 
    * {@link #logNodeVec logNodeVec}.
    * <p>
    * Note:  This method does not clear the Vector 
    * {@link #logNodeVec logNodeVec} before it adds extra nodes to it.
    */
   private void scanForNxLogUnderNode(NxNode node)
   {
      if (isNxLog(node))
         logNodeVec.add(node);
      
      for (int i=0; i<node.getNChildNodes(); i++)
         scanForNxLogUnderNode(node.getChildNode(i));
   }
   
   /**
    *  Returns the list of NxLog data sets
    *  @return NxNode[i] is the NxNode corresponding to the ith NXlog NeXus
    *          class which is converted to the ith log DataSet
    */
   public DataSetInfo getNxLogDataSet(int i)
   {
      NxNode ithLogNode = (NxNode)logNodeVec.elementAt(i);
      
      //TODO FIXME I DON'T KNOW IF THIS IS IMPLEMENTED CORRECTLY
      return new DataSetInfo(null,ithLogNode,0,0,"");
   }
   
   public int getNumNxLogDataSets()
   {
      return logNodeVec.size();
   }
   
   /**
    * Determines if the {@link NxNode NxNode} passed to this method is an 
    * Nxlog node.
    * @param node The node to check.
    * @return True if <code>node</code> is a Nxlog node and false if it isn't.
    */
   public static boolean isNxLog(NxNode node)
   {
      return node.getNodeClass().equals("NXlog");
   }
   
   /**
    * Testbed.
    * @param args Unused.
    */
   public static void main(String[] args)
   {
      NxNode node =(NxNode) new NexNode(args[0]);
      NxNode rootNode = node.getChildNode("Entry0");
      //node = node.getChildNode("Sample");
      //node = node.getChildNode("Beam");
      //node = node.getChildNode("Log_2");
      //node = node.getChildNode("temperature");
            
      NxlogLocator locator = new NxlogLocator(rootNode);
      
      int numLogs = locator.getNumNxLogDataSets();
      System.out.println("Number of NxLog nodes:  "+numLogs);
      for (int i=0; i<numLogs; i++)
        System.out.println(locator.getNxLogDataSet(i).NxdataNode.getNodeName());
   }
}
