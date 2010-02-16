/* 
 * File: LoadUDPEventsCmd.java
 *
 * Copyright (C) 2009, Ruth Mikkelson
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
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author$:
 *  $Date$:            
 *  $Rev$:
 */
package EventTools.ShowEventsApp.Command;

public class LoadUDPEventsCmd
{
   private String Instrument;
   private int    port;
   private String detFile;
   private String specFile;
   private String detEffFile; 
   private String bankFile;
   private String IDMapFile;
   private String matFile;
   private float  maxQValue;
   private long   nEventsToShow;
   
   /**
    *  Construct
    * @param Instrument  The name of the instrument
    * @param port        The port where the UDP packets come in on
    * @param detFile     The detectorFile(DETCAL)
    * @param specFile    The name of the spec File
    * @param detEffFile  The name of the detector efficiency file private 
    * @param bankFile    The name of the file with bank vs pixel_id's
    * @param IDMapFile   The name of the file that maps DAS ID's to NeXus ID's
    * @param matFile     The name of a matrix file
    * @param maxQValue   The maximum Q value to load
    * @param nEventsToShow The number of events to show in the 3D view
    */
   public LoadUDPEventsCmd( String Instrument, 
                            int port,
                            String detFile,
                            String specFile, 
                            String detEffFile, 
                            String bankFile,
                            String IDMapFile,
                            String matFile, 
                            float  maxQValue,
                            long nEventsToShow)
   {
      this.Instrument      = Instrument;
      this.port            = port;
      this.detFile         = detFile;
      this.specFile        = specFile;
      this.detEffFile      = detEffFile; 
      this.bankFile        = bankFile; 
      this.IDMapFile       = IDMapFile; 
      this.matFile         = matFile;
      this.maxQValue       = maxQValue;
      this.nEventsToShow   = nEventsToShow;
   }

  
   public String getInstrument()
   {
      return Instrument;
   }
   
   public int getPort()
   {
      return port;
      
   }
   public String getDetFile()
   {
      if (detFile == null || detFile.trim().equals(""))
        return null;

      return detFile;
   }

   public String getIncSpectrumFile()
   {
      if (specFile == null || specFile.trim().equals(""))
         return null;

      return specFile;
   }
   
   public String getDetEffFile()
   {
      if (detEffFile == null || detEffFile.trim().equals(""))
        return null;

      return detEffFile;
   }

   
   public String getBankFile()
   {
      if (bankFile == null || bankFile.trim().equals(""))
        return null;

      return bankFile;
   }
   
   public String getIDMapFile()
   {
      if (IDMapFile == null || IDMapFile.trim().equals(""))
        return null;

      return IDMapFile;
   } 
   
   
   public String getMatFile()
   {
      if (matFile == null || matFile.trim().equals(""))
        return null;

      return matFile;
   }

   
   public float getMaxQValue()
   {
      if (Float.isNaN( maxQValue )|| maxQValue <=0 )
        return Float.NaN;

      return maxQValue;
   }
   
  public long getNEventsToShow()
  {
     return nEventsToShow;
  }
  
   public String toString()
   {
      return "\nInstrument  : " + getInstrument()      +
             "\nPort        : " + getPort()            +
             "\nDet. File   : " + getDetFile()         +
             "\nSpec File   : " + getIncSpectrumFile() +
             "\nDet Eff File: " + getDetEffFile()      +
             "\nBank File   : " + getBankFile()        +
             "\nID Map File : " + getIDMapFile()       +
             "\nMatrix File : " + getMatFile()         +
             "\nMax Q Value : " + getMaxQValue()       +
             "\n#Events to Show"+ getNEventsToShow();
   }

}
