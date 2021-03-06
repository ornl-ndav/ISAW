/*
 * File:  NxNode.java 
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
 * Revision 1.5  2002/11/27 23:28:17  pfpeterson
 * standardized header
 *
 * Revision 1.4  2002/11/20 16:14:49  pfpeterson
 * reformating
 *
 * Revision 1.3  2002/02/26 15:45:21  rmikk
 * Added a getDimension routine
 *
 */
package NexIO;

/**
 * Interface that must be implemented by every "datasource" to get at
 * individual data values.  These nodes MUST obey the Nexus standard
 */
public interface NxNode{
  public String errormessage = ""; 

  public int getNChildNodes();
  
  public NxNode getChildNode( int index );
  
  public NxNode getChildNode( String NodeName );  //not class
  
  public String getNodeName();
  
  public String getNodeClass();

  public int getNAttributes();

  public Object getNodeValue();

  public Attr getAttribute( int index );

  public String getErrorMessage();

  public int[] getDimension();//of the Node's value

  public Object getAttrValue( String AttrName );

  public String getLinkName();
   
  public boolean equals( String linkName);

  public void close();
  
  public String show();//for Debug purposes
}
