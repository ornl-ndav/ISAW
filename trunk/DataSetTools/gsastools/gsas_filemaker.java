/*
 * File:  gsas_filemaker.java
 *
 * Copyright (C) 1999, Dongfeng Chen, Ruth Mikkelson, Alok Chatterjee 
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
 * Contact : Alok Chatterjee <AChatterjee@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           Argonne, IL 60439-4845
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.3  2001/06/08 23:24:25  chatter
 *  Fixed GSAS write file for SEPD
 *
 *  Revision 1.2  2001/04/25 19:26:07  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 */
package DataSetTools.gsastools;

import DataSetTools.dataset.*;
import DataSetTools.util.*; 
import java.awt.*;
import java.io.*;
import javax.swing.*;
import java.text.DateFormat;
import java.text.*;


/**
 * Transfer diffractometer Data Set histogram to GSAS file format.
 * @version 1.0
 */

public class gsas_filemaker
{                      
   
    public gsas_filemaker(){};
   
    public gsas_filemaker( DataSet ds, String filename){
  
    File f= new File(filename);
    try{
        FileOutputStream op= new FileOutputStream(f);
        OutputStreamWriter opw = new OutputStreamWriter(op);
         System.out.println("the file name isx " +filename);
         String S = ds.getTitle();
        // opw.write("#" + "     BANK" + "    Ref Angle" + "     Total length");
        // opw.write("\n");
         for (int j = ds.getTitle().length(); j<80; j++)
             S = S +" ";
         System.out.println("filename length="+S.length());
         opw.write( S +"\n");
         int en= ds.getNum_entries();
         int bank=0;
        
         for(int i=1; i<=en; i++)
        {
            DecimalFormat df=new DecimalFormat(  "000000");
            DecimalFormat dff=new DecimalFormat( "00.0000000");
            DecimalFormat dff1 =new DecimalFormat("000000.0000000");
            float [] y = ds.getData_entry(i-1).getCopyOfY_values();
            
            DataSetTools.dataset.Data dd = ds.getData_entry(i-1);
            DataSetTools.dataset.XScale xx = dd.getX_scale();
            if(i==1)
            System.out.println("dat="+i+","+xx.getEnd_x()+","+xx.getStart_x()+","+xx.getNum_x());
            float binwidth = (xx.getEnd_x()-xx.getStart_x())/((float)xx.getNum_x() - 1);
            opw.write("BANK   "+df.format( i )+"    "+ df.format(y.length)+"     "+
                      df.format((int)(y.length/10.0+0.9))+" CONST  "+dff1.format(xx.getStart_x())+
         "          "+dff.format(binwidth)+"    \n");
         
            System.out.println("last y ="+ y[y.length-1]);
            loop:for(int j=0; j<y.length; j+=10)
            {
                  for(int l=j+0; l<j+10; l++)
                   {
                      if(l>=y.length)  
                       {    
                        opw.write("        ");                
                        }
                      else 
                        opw.write("  "+df.format(y[l]));
                  }
                  opw.write("\n");
                  
            }
       
        }
        opw.flush();
        opw.close();
        Thread.sleep(100);
        } catch(Exception d){}
    
    }
    public static void main(String[] args){}


}
