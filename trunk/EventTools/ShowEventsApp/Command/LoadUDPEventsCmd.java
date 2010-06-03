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
   private float  absorption_power;
   private float  absorption_radius;
   private float  absorption_smu;
   private float  absorption_amu;
   private float  minQValue;
   private float  maxQValue;
   private long   nEventsToShow;
   
   /**
    *  Constructor 
    * @param Instrument   The name of the instrument
    * @param port         The port where the UDP packets come in on
    * @param detFile      The detectorFile(DETCAL)
    * @param specFile     The name of the spec File
    * @param detEffFile   The name of the detector efficiency file private 
    * @param bankFile     The name of the file with bank vs pixel_id's
    * @param IDMapFile    The name of the file that maps DAS ID's to NeXus ID's
    * @param matFile      The name of a matrix file
    * @param power        The power of lambda for the absorption correction
    * @param radius       The radius for the absorption correction
    * @param smu          The total scattering for absorption correction
    * @param amu          True absorption at lambda = 1.8 Angstoms
    * @param minQValue    The minimum Q value to load
    * @param maxQValue    The maximum Q value to load
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
                            float  power,
                            float  radius,
                            float  smu,
                            float  amu,
                            float  minQValue,
                            float  maxQValue,
                            long   nEventsToShow)
   {
      this.Instrument      = Instrument;
      this.port            = port;
      this.detFile         = detFile;
      this.specFile        = specFile;
      this.detEffFile      = detEffFile; 
      this.bankFile        = bankFile; 
      this.IDMapFile       = IDMapFile; 
      this.matFile         = matFile;
      this.absorption_power = power;
      this.absorption_radius= radius;
      this.absorption_smu = smu;
      this.absorption_amu  = amu;
      this.minQValue       = minQValue;
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

   
   public float getAbsorptionPower()
   {
      if (Float.isNaN( absorption_power )|| absorption_power < 0 )
        return Float.NaN;

      return absorption_power;
   }
   
   public float getAbsorptionRadius()
   {
      if (Float.isNaN( absorption_radius )|| absorption_radius < 0 )
        return Float.NaN;

      return absorption_radius;
   }
   
   public float getAbsorptionSMU()
   {
      if (Float.isNaN( absorption_smu )|| absorption_smu < 0 )
        return Float.NaN;

      return absorption_smu;
   }
   
   public float getAbsorptionAMU()
   {
      if (Float.isNaN( absorption_amu )|| absorption_amu < 0 )
        return Float.NaN;

      return absorption_amu;
   }

   public float getMinQValue()
   {
      if (Float.isNaN( minQValue )|| minQValue <= 0 )
        return 0;                               // use all Q's

      return minQValue;
   }
   
   public float getMaxQValue()
   {
      if (Float.isNaN( maxQValue )|| maxQValue <= 0 )
        return 100;                             // use all Q's

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
             "\nAbsorptionPower : " + getAbsorptionPower() +
             "\nAbsorptionRadius: " + getAbsorptionRadius()+
             "\nTotalAbsorption : " + getAbsorptionSMU()   +
             "\nAbsorptionTrue  : " + getAbsorptionAMU()   +
             "\nMin Q Value     : " + getMinQValue()       +
             "\nMax Q Value     : " + getMaxQValue()       +
             "\n#Events to Show"+ getNEventsToShow();
   }

}
