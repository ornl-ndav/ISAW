
/*
 * File:  NxfileStateInfo.java
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
 * Contact :  Ruth Mikkelson <mikkelsonr@uwstout.edu>
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
 * Revision 1.1  2003/11/16 21:44:26  rmikk
 * Initial Checkin
 *
 */

package NexIO.State;
import NexIO.*;
import NexIO.Util.*;
import java.util.*;

/**
 *   This class is the root node for all the Params link list arguments.
 *   It contains information necessary to process a NeXus file
 */
public class NxfileStateInfo extends StateInfo{
   

   public String NexusVersion;

   public String HDFVersion;

   public Date Time;

   /**
    *  This contains the list of spectra ID's to be retrieved. If null all
    *  spectra( data blocks) will be retrieved.  The id's are the default ID's
    *  unless there is an int id field in the NXdetector.
    */
   public int[] Spectra;

   /**
    *     Constructor
    *     @param  NxfileNode  the NxNode containing information on the top node
    *                         of the NeXus file.
    */
   public NxfileStateInfo( NxNode NxfileNode ){
     NexusVersion =NexUtils.getStringAttributeValue( NxfileNode, "NeXus_version");
     HDFVersion = NexUtils.getStringAttributeValue( NxfileNode, "HDF_version");
     String time =NexUtils.getStringAttributeValue( NxfileNode, "file_time");
     Time = ConvertDataTypes.parse( time);
     Spectra = null;
   }


   /**
    *     Copy Constructor
    *     @param state the state to copy
    */
  public NxfileStateInfo( NxfileStateInfo state){
     this.NexusVersion = state.NexusVersion;
     this.HDFVersion =state.HDFVersion;
     this.Time = state.Time;
     this.Spectra = state.Spectra;
  }
   
  
 }


