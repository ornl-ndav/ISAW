/*
 * @(#)Util.java     0.7  00/05/31  Alok Chatterjee
 *
 * 0.7  00/05/31  A Utility class that provides different methods like load, 
 * 			delete(to be added later) etc.
 * 
 */
 
package IsawGUI;

import IPNS.Runfile.*;
import IPNS.Runfile.Header.*;
import DataSetTools.dataset.*;
import DataSetTools.util.*;
import DataSetTools.retriever.*;
import javax.swing.text.*; 
import java.io.*;
import java.util.*;
import java.awt.*; 
import javax.swing.filechooser.*;
import javax.swing.table.*;
import javax.swing.*;
import DataSetTools.components.ParametersGUI.*;
import DataSetTools.operator.*;
/**
 * Utility class for ISAW. 
 *
 * @version 0.7  
 */


public class Util
{ 
  public Util()
  {
  }
  
 /**
  * This returns an array of DataSets that are created from a runfile. 
  *
  * @filename String that gives the absolute path for a runfile  
  */
  public DataSet[] loadRunfile(String filename)
  {
     // System.out.println("Filename is before" +filename);
      filename = StringUtil.fixSeparator(filename);
     // System.out.println("Filename is after" +filename);

      Retriever r;
      if ( filename.endsWith( "nxs" )  ||
           filename.endsWith( "NXS" )  ||
           filename.endsWith( "hdf" )  ||
           filename.endsWith( "HDF" )  )
        r = new NexusRetriever( filename );
      else if( filename.toUpperCase().endsWith(".ISD"))
       { DataSet dss[];
         dss = new DataSet[1];
         
         dss[0] = DataSet_IO.LoadDataSet( filename );
        
         return dss;
        }
      else
        r = new RunfileRetriever( filename );

      int numberOfDataSets = r.numDataSets();
      DataSet[] dss = new DataSet[numberOfDataSets];
      if(numberOfDataSets >= 0)
      {
      for (int i = 0; i< numberOfDataSets; i++)
           dss[i] = r.getDataSet(i);
      }
      r = null;
      System.gc();
      return dss;
  }

 public void Save( String filename, DataSet ds, IDataSetListHandler lh)
   { 
     Operator X = null;
     
     if( filename== null) return;
     else if( filename.length()<4) return;
     else if ( filename.endsWith( "nxs" )  ||
           filename.endsWith( "NXS" )  ||
           filename.endsWith( "hdf" )  ||
           filename.endsWith( "HDF" )  )
     {  X = new WriteNexus();
      }
    else if( filename.toUpperCase().endsWith(".XML"))
      { X = new WriteNexus();
       }
    else if( filename.toUpperCase().endsWith(".ISD"))
        {DataSet_IO.SaveDataSet(  ds, filename  ) ;
         
         return;  
        }
    else if( filename.toUpperCase().endsWith(".GSA") ||
	     filename.toUpperCase().endsWith(".GDAT") ||
	     filename.toUpperCase().endsWith(".GSAS"))
	{
	    X=new WriteGSAS();
	}
    else 
      { 
	/* int i= filename.lastIndexOf('.');
	   if( i<0) return;
	   if( ".GSA;.GDAT;.GSAS;".indexOf(filename.toUpperCase().substring(i))
	   >=0)
	   { 
	   DataSetTools.gsastools.gsas_filemaker XX =
	   new DataSetTools.gsastools.gsas_filemaker( null, ds, filename);
	   }*/
        return;
      }
   
   X.setParameter( new Parameter("filename",filename ), 2);
   X.setParameter( new Parameter("dataset",ds),1);

   JParametersDialog JP = new JParametersDialog( X , lh,
          null,null);


   }
 class ArrayDSHandler implements IDataSetListHandler
    {  DataSet DS[];
      public ArrayDSHandler( DataSet DS[])
        { this.DS = DS;
        }
   public DataSet[] getDataSets()
       {return DS;
       }

    }
 public Document openDoc(String filename)
 {
	FileReader fr; 
    
	if(filename == null)
	   return null;
       try{
	   File f = new File(filename); 
         try{ 
         	 fr = new FileReader( f ) ; 
            }
          catch (FileNotFoundException s){return null;}

           int c , offset ; 
	   String line ; 
         Document doc = new PlainDocument() ; 
 
	     
           line = "" ; offset = 0 ; 
           for( c = fr.read() ; c != -1 ;   )
	       {  
                line = line + new Character( ( char )c ).toString() ; 
                if( c < ' ' ) //assumes the new line character
			{ 
                    doc.insertString( offset , line , null ) ; 
		        offset+=line.length() ; 
                    line = "" ; 
                   }
		
                 c = fr.read() ; 
              }
               //offset is doc.getLength(?)-1
		  if( line.length() > 0 )doc.insertString( offset , line , null ) ; 
                  fr.close() ;
			return doc; 	    
	  }
	  catch( Exception s )
         {return null; }
       
 }

public void appendDoc( Document doc , String S)
   {
     if( doc == null )return;
     int end = doc.getLength();
    // if( S.length()>0)
     //   if( S.charAt( S.length() - 1) < ' ')
         //  S = S.substring( 0 , S.length() - 1);
     try{
       doc.insertString( end , S+ "\n" , null);
        }
     catch( Exception s)
       {System.out.println("Error in appendDoc="+s); }

   }


public String saveDoc(Document doc, String filename)
{
 if (doc ==null)
  {return null;}
  Element line ;
 if(filename == null)
  {return null;}
   File f = new File(filename);
	     try{ 
                 FileWriter fw = new FileWriter( f ) ; 
                
	         int i ; 
		 
                 Element  root ; 

                 root = doc.getDefaultRootElement() ; 
		 
                 for( i = 0 ; i < root.getElementCount() ; i++ )
                    {line = root.getElement( i ) ; 
		    
		         fw.write( doc.getText( line.getStartOffset() , line.getEndOffset() - 
                           line.getStartOffset() - 1 ) ) ; 
                     fw.write( "\n" ) ; 
                     }
		     fw.close() ; 
                 return null;
                
	         }
	     catch( IOException s )
                     {return "Status: Unsuccessful"  ; }
          catch( javax.swing.text.BadLocationException s )
                 {return "status Usuccessful";                 }
	      

}

   public Vector listProperties()
   {
      StringBuffer buf = new StringBuffer();
      String pName, pVal;
      Vector data = new Vector();
      Properties props = new Properties();

      String path = System.getProperty("user.home")+"\\";
       path = StringUtil.fixSeparator(path);
       try {
	    FileInputStream input = new FileInputStream(path + "IsawProps.dat");
          props.load(input);
         }
       
       catch (IOException ex) {
          System.out.println("Properties file could not be loaded due to error :" +ex);
       }
        
      Enumeration enum = props.propertyNames();

      while( enum.hasMoreElements() ) {
         pName = enum.nextElement().toString();
         pVal = props.getProperty( pName );
         Vector oo = new Vector();
	        oo.addElement(pName); 
	        oo.addElement(pVal);
	        data.addElement(oo);
       
       }
               return data ;
   }

   public JScrollPane viewProperties()
   {
       JTable table;
           	
       Vector heading = new Vector();
	 heading.addElement("Attribute" ); 
	 heading.addElement("Value");
	 Vector data = listProperties();
	 DefaultTableModel dtm = new DefaultTableModel(data, heading);
       table = new JTable(dtm);
	 table.setModel(dtm);
       table.setSize( 30, 30 );    // the numbers used don't seem to
                                   // be important, but setting the 
                                   // size get's the table to fill out
                                   // the available space.
       ExcelAdapter myAd = new ExcelAdapter(table);
       JScrollPane scrollPane = new JScrollPane(table);
       //getContentPane().add( scrollPane );

       
       return scrollPane ;
   }


}
