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
 * 
 * Modified:
 * 
 * $Log$
 * Revision 1.4  2005/02/16 23:29:58  kramer
 * Modified Log to L0G in the previous log message in the source file.
 *
 * Revision 1.3  2005/02/16 02:02:00  kramer
 *
 *
 * Added javadoc documentation and $L0G:$ to the file's header.  Also, the
 * method getNxLogDataSet(int i) will return 'null' if 'i' is invalid.
 *
 */
package NexIO;

import java.util.Vector;

import NexIO.NexApi.NexNode;

/**
 * This class is used to search all of the nodes under a given 
 * {@link NexIO.NxNode NxNode} for nodes that are NxLog nodes.
 * @author Dominic Kramer
 */
public class NxlogLocator
{
   /** Vector of NxNode used to hold all of the NxLog nodes found. */
   private Vector logNodeVec;
   
   /**
    * This constructor searches for all of the NxLog nodes under the 
    * NxNode <code>rootNode</code>.  The methods 
    * {@link #getNumNxLogDataSets() getNumNxLogDataSets()} and 
    * {@link #getNxLogDataSet(int) getNxLogDataSet(int)} can be used to 
    * access the nodes.
    * @param rootNode The node where the searching begins.
    */
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
      if (node == null)
         return;
      else if (node.getNodeClass().equalsIgnoreCase("CDF0.0"))
         return;  //CDF0.0 nodes contain links to everything in the entire 
                  //NeXus file.  If they are "scanned" an infinite loop will 
                  //occur because they contain a link to themselves.
      
      if (isNxLog(node))
         logNodeVec.add(node);
      
      int numChildren = node.getNChildNodes();
      for (int i=0; i<numChildren; i++)
         scanForNxLogUnderNode(node.getChildNode(i));
   }
   
   /**
    * Wraps the ith NxLog node found in a {@link DataSetInfo DataSetInfo} 
    * object.
    * @param i The index of the NxLog node to access.  This index is valid 
    *          only if 
    *          0<i<={@link #getNumNxLogDataSets() getNumNxLogDataSets()}.
    *  @return The ith NxLog node found or <code>null</code> if 
    *          <code>i</code> is invalid.
    */
   public DataSetInfo getNxLogDataSet(int i)
   {
      if (i<0 || i>=getNumNxLogDataSets())
         return null;
      
      NxNode ithLogNode = (NxNode)logNodeVec.elementAt(i);
      //TODO FIXME I DON'T KNOW IF THIS IS IMPLEMENTED CORRECTLY
      return new DataSetInfo(ithLogNode,ithLogNode,0,0,"");
   }
   
   /**
    * Get the number of NxLog nodes found.
    * @return The number of NxLog nodes found.
    */
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
      String str = new String();
      if (node == null || (str = node.getNodeClass()) == null)
         return false;
      else
         return str.equals("NXlog");
   }
   
   /**
    * Testbed.
    * @param args Unused.
    */
   public static void main(String[] args)
   {
      System.out.println("Using file:  "+args[0]);
      
      NxNode rootNode =(NxNode) new NexNode(args[0]);
      rootNode = rootNode.getChildNode("Entry0");
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
