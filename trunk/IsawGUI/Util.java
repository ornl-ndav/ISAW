/*
 * File:  Util.java 
 *             
 * Copyright (C) 2000, Alok Chatterjee
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
 * Contact : Alok Chatterjee <achatterjee@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.24  2003/10/15 03:18:31  bouzekc
 * Fixed javadoc errors.
 *
 * Revision 1.23  2003/10/14 22:08:04  dennis
 * Fixed javadoc so that it builds cleanly on jdk 1.4.2.
 * Also, corrected javadoc to include support for NeXus, GSAS, ISD
 * and XML files.
 *
 * Revision 1.22  2003/08/28 18:53:42  dennis
 * Added support for loading .csd files (concatenated files
 * from Ideas MC simulation.)
 * Did minor clean up of code for SDDS files.
 *
 * Revision 1.21  2003/06/06 22:28:36  pfpeterson
 * Changed to use a BufferedReader for reading in text files.
 *
 * Revision 1.20  2003/05/28 18:58:20  pfpeterson
 * Changed System.getProperty to SharedData.getProperty
 *
 * Revision 1.19  2003/03/10 19:14:57  pfpeterson
 * Moved code that reads a text file into a StringBuffer to be its
 * own method.
 *
 * Revision 1.18  2003/03/07 21:53:43  pfpeterson
 * openDoc(String) now uses a StringBuffer for putting the file together
 * then adds it to a document.
 *
 * Revision 1.17  2003/02/13 21:45:13  pfpeterson
 * Removed calls to deprecated function fixSeparator.
 *
 * Revision 1.16  2002/12/20 20:00:04  pfpeterson
 * Now puts up a warning dialog when the save method is called with an
 * unknown file extension.
 *
 * Revision 1.15  2002/11/27 23:27:07  pfpeterson
 * standardized header
 *
 * Revision 1.14  2002/09/30 19:59:54  pfpeterson
 * Now allows gda extension for gsas powder files.
 *
 * Revision 1.13  2002/08/06 21:31:24  pfpeterson
 * Now calls GsasRetriever as well.
 *
 * Revision 1.12  2002/06/18 19:49:42  rmikk
 * -Eliminated extra returns and prettified the code.
 * -Added code to read and write xmi, xmn and zip files
 *   xml files are no longer supported
 *
 * Revision 1.11  2002/05/02 19:14:57  pfpeterson
 * Changed the SDDS import statement.
 *
 * Revision 1.10  2002/05/02 15:55:07  rmikk
 * Eliminated a few segments of dead code
 * Sent it through a Prettifier code
 * Eliminated a writing of a "/n" at the end of a file so the files do not keep
 *    adding extra lines.
 * 
 */
package IsawGUI;


import IPNS.Runfile.*;
import IPNS.Runfile.Header.*;
import DataSetTools.dataset.*;
import DataSetTools.util.*;
import DataSetTools.retriever.*;
import DataSetTools.writer.*;
import javax.swing.text.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.filechooser.*;
import javax.swing.table.*;
import javax.swing.*;
import DataSetTools.components.ParametersGUI.*;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.Load.*;
import DataSetTools.operator.Generic.Save.*;
import SDDS.java.SDDS.*;


/**
 * Utility class for ISAW. 
 *
 * @version 0.7  
 */


public class Util
{
   public Util()
   {}


   /**
    * This returns an array of DataSets that are created from a runfile,
    * NeXus file, "ISD" file, GSAS file or XML file. 
    *
    * @param filename Fully qualified runfile name.
    */
   public DataSet[] loadRunfile( String filename )
   {
      filename = StringUtil.setFileSeparator( filename );

      Retriever r;

      if( filename.endsWith( "nxs" ) ||
          filename.endsWith( "NXS" ) ||
          filename.endsWith( "hdf" ) ||
          filename.endsWith( "HDF" ) )
         r = new NexusRetriever( filename );

      else if( filename.toUpperCase().endsWith("ZIP") ||
               filename.toUpperCase().endsWith("XMI"))
         r = new XmlDFileRetriever( filename);

      else if( filename.toUpperCase().endsWith( ".ISD" ) )
      {
         DataSet dss[];

         dss = new DataSet[1];

         dss[0] = DataSet_IO.LoadDataSet( filename );

         return dss;
      }
      else if( filename.toUpperCase().endsWith( ".GSA" ) 
               || filename.toUpperCase().endsWith( ".GDA" ) )
        r=new GsasRetriever(filename);

      else if( filename.toUpperCase().endsWith( ".SDDS" ) )
         r = new SDDSRetriever( filename );

      else if( filename.toUpperCase().endsWith( ".CSD" ) )
         r = new IdeasRetriever( filename );

      else
         r = new RunfileRetriever( filename );

      int numberOfDataSets = r.numDataSets();
      DataSet[] dss = new DataSet[numberOfDataSets];

      if( numberOfDataSets >= 0 )
      {
         for( int i = 0; i < numberOfDataSets; i++ )
            dss[i] = r.getDataSet( i );
      }
      r = null;
      System.gc();
      return dss;
   }


   public void Save( String filename, DataSet ds, IDataSetListHandler lh )
   {
      Operator X = null;

      if( filename == null )
         return;
      else if( filename.length() < 4 )
         return;
      else if( filename.endsWith( "nxs" ) ||
         filename.endsWith( "NXS" ) ||
         filename.endsWith( "hdf" ) ||
         filename.endsWith( "HDF" ) )
      {
         X = new WriteNexus();
      }
      else if( filename.toUpperCase().endsWith( ".XMN" ) )
      {
         X = new WriteNexus();
      }
      else if( filename.toUpperCase().endsWith("ZIP") ||
               filename.toUpperCase().endsWith("XMI"))
      { XmlDWriter xwr = new XmlDWriter( filename);
        DataSet[] DSS = new DataSet[1];
        DSS[0]= ds;
        xwr.writeDataSets( DSS );
        return;
       }
      else if( filename.toUpperCase().endsWith( ".ISD" ) )
      {
         DataSet_IO.SaveDataSet( ds, filename );

         return;
      }
      else if( filename.toUpperCase().endsWith( ".GSA" ) ||
         filename.toUpperCase().endsWith( ".GDAT" ) ||
         filename.toUpperCase().endsWith( ".GDA" ) ||
         filename.toUpperCase().endsWith( ".GSAS" ) )
      {
         X = new WriteGSAS();
      }
      else
      {
        // determine what the extension is
        int index=filename.lastIndexOf(".");
        if(index<0)
          index=filename.lastIndexOf("/");
        if(index<0)
          index=filename.lastIndexOf("\\");
        if(index<0)
          index=0;
        if(index>0)
          index++;

        // pop up a warning dialog
        JOptionPane.showMessageDialog(new JFrame(),
                               "Unknown extension: "+filename.substring(index),
                               "Error in Saving", JOptionPane.WARNING_MESSAGE);

         return;
      }

      X.setParameter( new Parameter( "filename", filename ), 2 );
      X.setParameter( new Parameter( "dataset", ds ), 1 );

      JParametersDialog JP = new JParametersDialog( X, lh,
            null, null );

   }
   class ArrayDSHandler implements IDataSetListHandler
   {
      DataSet DS[];
      public ArrayDSHandler( DataSet DS[] )
      {
         this.DS = DS;
      }


      public DataSet[] getDataSets()
      {
         return DS;
      }

   }

  /**
   * Loads a file into a Document. If an Exception is encountered this
   * prints information to the StatusPane and returns null.
   */
   public Document openDoc( String filename )
   {
     StringBuffer buffer=readTextFile(filename);
     if(buffer==null || buffer.length()<=0)
       return null;

     Document doc=new PlainDocument();
     try{
       doc.insertString(0,buffer.toString(),null);
       return doc;
     }catch(BadLocationException e){
       SharedData.addmsg("BadLocationException while reading "+filename);
       return null;
     }
   }

  /**
   * Load an ascii file into a StringBuffer. If an Exception is
   * encountered this prints information to the StatusPane and returns
   * null.
   */
   public static StringBuffer readTextFile(String filename){
     if( filename==null || filename.length()<=0 ) return null;
     
     FileReader fr=null;
     StringBuffer buffer=new StringBuffer();
     try{
       fr=new FileReader(filename);
       BufferedReader br=new BufferedReader(fr);
       while(br.ready())
         buffer.append(br.readLine()+"\n");
     }catch(FileNotFoundException e){
       SharedData.addmsg("FileNotFoundException: "+filename);
       return null;
     }catch(IOException e){
       SharedData.addmsg("Something went wrong while reading "+filename);
       return null;
     }finally{
       if(fr!=null){
         try{
           fr.close();
         }catch(IOException e){
           // let it drop on the floor
         }
       }
     }

     return buffer;
   }

   public void appendDoc( Document doc, String S )
   {
      if( doc == null )return;
      int end = doc.getLength();

      try
      {
         doc.insertString( end, S + "\n", null );
      }
      catch( Exception s )
      {
         System.out.println( "Error in appendDoc=" + s );
      }

   }


   public String saveDoc( Document doc, String filename )
   {
      if( doc == null )
      {
         return null;
      }
      Element line;

      if( filename == null )
      {
         return null;
      }
      File f = new File( filename );

      try
      {
         FileWriter fw = new FileWriter( f );

         int i;

         Element root;

         root = doc.getDefaultRootElement();
         String c = "";

         for( i = 0; i < root.getElementCount(); i++ )
         {
            line = root.getElement( i );

            fw.write( doc.getText( line.getStartOffset(), line.getEndOffset() -
                  line.getStartOffset() - 1 ) );
            c = doc.getText( line.getEndOffset() - 1, 1 );
            if( i + 1 < root.getElementCount() )
               fw.write( "\n" );
         }
         fw.close();
         return null;

      }
      catch( IOException s )
      {
         return "Status: Unsuccessful";
      }
      catch( javax.swing.text.BadLocationException s )
      {
         return "status Usuccessful";
      }

   }


   public Vector listProperties()
   {
      StringBuffer buf = new StringBuffer();
      String pName, pVal;
      Vector data = new Vector();
      Properties props = new Properties();

      String path = SharedData.getProperty( "user.home" ) + "\\";

      path = StringUtil.setFileSeparator( path );
      try
      {
         FileInputStream input = new FileInputStream( path + "IsawProps.dat" );

         props.load( input );
      }
      catch( IOException ex )
      {
         System.out.println( "Properties file could not be loaded due to error :" + ex );
      }

      Enumeration enum = props.propertyNames();

      while( enum.hasMoreElements() )
      {
         pName = enum.nextElement().toString();
         pVal = props.getProperty( pName );
         Vector oo = new Vector();

         oo.addElement( pName );
         oo.addElement( pVal );
         data.addElement( oo );

      }
      return data;
   }


   public JScrollPane viewProperties()
   {
      JTable table;

      Vector heading = new Vector();

      heading.addElement( "Attribute" );
      heading.addElement( "Value" );
      Vector data = listProperties();
      DefaultTableModel dtm = new DefaultTableModel( data, heading );

      table = new JTable( dtm );
      table.setModel( dtm );
      table.setSize( 30, 30 );    // the numbers used don't seem to
      // be important, but setting the 
      // size get's the table to fill out
      // the available space.
      ExcelAdapter myAd = new ExcelAdapter( table );
      JScrollPane scrollPane = new JScrollPane( table );

      return scrollPane;
   }

}

