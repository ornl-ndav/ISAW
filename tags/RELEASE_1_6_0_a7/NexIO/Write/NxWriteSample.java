/*
 * File:  NxWriteSample.java 
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
 * Revision 1.5  2003/11/24 14:12:15  rmikk
 * Implemented NxWriteSample
 * Changed the signature on the processDS method
 *
 * Revision 1.4  2002/11/27 23:29:19  pfpeterson
 * standardized header
 *
 * Revision 1.3  2002/11/20 16:15:43  pfpeterson
 * reformating
 *
 * Revision 1.2  2002/03/18 20:58:56  dennis
 * Added initial support for TOF Diffractometers.
 * Added support for more units.
 *
 */

package NexIO.Write;
import DataSetTools.dataset.*;

/**
 * Class responsible for saving NXsample information from data sets to
 * a nexus file.<P>
 *
 * NOT USED YET
 */
public class NxWriteSample{
  String errormessage;
  int instrType;
  public NxWriteSample(int instrType){
    errormessage = "";
    this.instrType = instrType;
  }

  /**
   * Returns an errormessage or "" if none
   */
  public String getErrorMessage(){
    return errormessage;
  }

  /**
   * Writes the NXsample information from a data set to a Nexus file
   *
   * @param node a NXentry node
   * @param DS the data set with the information to be written
   */
  public boolean processDS( NxWriteNode node , DataSet DS){
    errormessage = "";
    
    NxWriteNode NxSampNode = node.newChildNode( "Sample", "NXsample");
    
    Object X = DS.getAttributeValue( Attribute.SAMPLE_NAME);
    if( X !=  null){
      String Samp_name = NexIO.Util.ConvertDataTypes.StringValue( X);
      if( Samp_name != null){
        NxWriteNode Instrnode = node.newChildNode( "sample", "NXsample");
        NxWriteNode nameNode = Instrnode.newChildNode( "name", "SDS");
        int[] ranks = new int[1];
        ranks[0] = Samp_name.length()+1;
        nameNode.setNodeValue( (Samp_name+(char)0).getBytes(),
                     NexIO.Types.Char,ranks);
    }}
    NxWriteNode NxLognode = NxSampNode.newChildNode("Log_7","NXlog");
    NxWriteLog writelog = new NxWriteLog( 5);
    writelog.processDS( NxLognode, null, 7);

    
    NxWriteBeam writeBeam = new NxWriteBeam(instrType);
    NxWriteNode beamNode = NxSampNode.newChildNode("Beam", "NXbeam");
    if( writeBeam.processDS( beamNode, DS))
        errormessage += writeBeam.getErrorMessage();
    return false;
  }
}
