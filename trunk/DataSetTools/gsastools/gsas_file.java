/*
 * File: gsas_file.java
 *
 * Copyright (C) 1999, Dongfeng Chen 
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
 *  Revision 1.2  2001/04/25 19:26:04  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 */
package DataSetTools.gsastools;

import IPNS.Runfile.*;
import DataSetTools.retriever.*;
import DataSetTools.dataset.*;
import DataSetTools.util.*; 
import java.awt.*;
import java.io.*;
import javax.swing.*;
import java.text.DateFormat;
import java.text.*;


/**
 * Show image views diffractometer Data Set and transfer the 
 * histogram to GSAS file format.
 * @version 1.0
 */

public class gsas_file
{                      
    
    public gsas_file(){};

    public static void gsasfilemaker( DataSet ds, String filename){
  
    File f= new File(filename +".dat");
    try{
        FileOutputStream op= new FileOutputStream(f);
        OutputStreamWriter opw = new OutputStreamWriter(op);
        System.out.println("the file name is " +filename);
         opw.write(ds.getTitle()+"                                                         \n");
         int en= ds.getNum_entries();
         int bank=0;
         float [] step={5.0000000f,8.0000000f,10.0000000f};
         for(int i=0; i<6; i+=2)
        {
            DecimalFormat df=new DecimalFormat("00000");
            DecimalFormat dff=new DecimalFormat("00.0000000");
            
            float [] y = ds.getData_entry(i).getCopyOfY_values();
            float [] yadd = ds.getData_entry(i+1).getCopyOfY_values();
            bank++;
//            String
            if(bank==1){
            opw.write("BANK       "+df.format(bank)+"       "+ df.format(y.length+1)+"       "+
                      df.format(y.length/10+1)+" CONST   3000.0000000      5.0000000    \n");
            }
            else if (bank==2){
            opw.write("BANK       "+df.format(bank)+"       "+ df.format(y.length+1)+"       "+
                      df.format(y.length/10+1)+" CONST   3000.0000000      8.0000000    \n");
            }
            else if (bank==3){
            opw.write("BANK       "+df.format(bank)+"       "+ df.format(y.length+1)+"       "+
                      df.format(y.length/10+1)+" CONST   3000.0000000     10.0000000    \n");
            }
            
         //*   
            
            int nOfy = y.length;
            int nOfline = nOfy/10;
            
            System.out.println( "Start Loop bank "+bank);
            System.out.println( "nOfy =  "+nOfy);
            System.out.println( "y.length =  "+y.length);
            System.out.println( "yadd.length =  "+yadd.length);
            
 
         
            loop:for(int j=0; j<nOfy; j+=10)
            {                 
                  for(int l=j; l<j+10; l++){
                    if(l==nOfy) 
                    {     
                        System.out.println( "end bank "+bank);
                        opw.write("\n");                        
                        break loop;               
                    }
                    opw.write("   "+df.format(y[l]+yadd[l]));
                  }
                  opw.write("\n");
            }
            //*/
       
        }
        opw.flush();
        opw.close();
        
        Thread.sleep(100);
        } catch(Exception d){}
    
    }
    public static void main(String[] args){
        
        String run= "C:/CHOP/DataSetTools/gsastools/GPPD10628.RUN"; 
        
        Runfile r = null;
        RunfileRetriever data_retriever = null;
        DataSet   data_set = null;
        try
        {
            r= new Runfile(run);
            data_retriever = new RunfileRetriever(run );
            data_set = data_retriever.getDataSet(1);// Important things happened here!
            gsasfilemaker(data_set, "Isaw_Gsas");
            } catch ( Exception e )
        {
            System.out.println( e );
            System.out.println("ERROR: Data retriever threw Exception" );
        }
        
        }


}
