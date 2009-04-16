/*
 * File:  NxFileOpenThread
 *
 * Copyright (C) 2008, Ruth Mikkelson
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
 * Contact :  Ruth Mikkelson <mikkelsonr@uwstout.edu> *          
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the The Spallation Neutron Source at Oakridge
 * National Laboratory, Oakridge, Tennessee
 *
 * For further information, see  <http://ftp.sns.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 */
package NexIO;

import org.nexusformat.NexusException;
/**
 * This class is used to open and close a Nexus file. These operations must 
 * durrently be done in the same thread.
 */

/**
 * @author Ruth
 *
 */
public class NxFileOpenThread extends Thread {

    
   String NexusFilename;
   int access;
   CNexusFile  NxFile;
   boolean  close;
   boolean finished;
   /**
    * @param NexusFilename  The name of the Nexus file to  be opened
    */
   public NxFileOpenThread( String NexusFilename , int access ) throws NexusException{

      super( NexusFilename );
      this.NexusFilename = NexusFilename;
      this.access = access;
      NxFile = null;
      close = false;
      finished = false;
      NxFile = null;
   }
   
   public CNexusFile  getNxFile(){
      while( NxFile == null && !finished )
         try{
            Thread.sleep( 500 );
         }catch( Exception s){
            finished = true;
         }
      return NxFile;
   }
   
   public void close(){
      close = true;
   }

   /* (non-Javadoc)
    * @see java.lang.Thread#run()
    */
 
   public void run() {
     try{
      NxFile = new CNexusFile( NexusFilename, access);
      NxFile.setIOThread( this );
     }catch(Throwable ss){
        System.out.println("Cannot open file "+ NexusFilename+"-"+ss.toString());
        finished = true;
        NxFile = null;
        return;
     }
      boolean done = false;
      while( !done)
         try{
            if( finished)
               return;
            if( close){               
               NxFile.Doclose();
               finished = true;
               done = true;
               return;
            }

            Thread.sleep(300 );
            
         }catch(Exception s){
            done =true;
         }
     
   }

   
}
