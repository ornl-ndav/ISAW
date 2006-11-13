/* 
 * File: MemSeardhEngine.java
 *  
 * Copyright (C) 2005  Dominic Kramer and Galina Pozharsky
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
 * Contact :  Dennis Mikkelson<mikkelsond@uwstout.edu>
 *            Galina Pozharsky<pozharskyg@uwstout.edu>
 *            MSCS Department
 *            HH237H
 *            Menomonie, WI. 54751
 *            (715)-232-2291
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0426797, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 *
 * Modified:
 * $Log$
 * Revision 1.4  2006/11/13 23:16:59  dennis
 * Made the message in the dialog box telling the user to 'Update Search
 * Data Base' more explanatory.
 *
 * Revision 1.3  2006/11/04 16:48:18  rmikk
 * Added a message indicating how to update the search data base if this
 *   data base does not exist.  If all the class files are recompiled, the data
 *   base needs to be updated again too.
 *
 * Revision 1.2  2006/10/22 18:28:19  rmikk
 * Changed directory where the presearched database info is stored to the
 * user's home directory
 *
 * Revision 1.1  2006/09/15 14:45:10  rmikk
 * Initial Checkin.  This is the search Engine that gets the information from the
 * index files produced by devTools.MakeSearchData
 *
 */

package IsawHelp.HelpSystem;

import java.net.URL;

import java.net.URLStreamHandler;

import java.security.InvalidParameterException;
import java.util.*;
import java.io.*;
import javax.help.search.*;
import javax.swing.JOptionPane;

import Command.*;

/**
 * This search engine gets info from files created by devTools.MakeSearchData.
 * 
 * @author Ruth
 * 
 */
public class MemSearchEngine extends SearchEngine implements SearchListener {

   java.io.RandomAccessFile  fout;

   long                      lastmodified;

   int                       nWords;

   String[]                  Words;

   int[]                     buffptrs;

   Script_Class_List_Handler SCLH;

   URLStreamHandler          urlh;


   /**
    * Constructor for the Search Engine
    * 
    * @param arg0
    *           url for the search engine
    * @param arg1
    *           hashtable of other arguments
    * 
    * @throws InvalidParameterException
    */
   public MemSearchEngine( URL arg0, Hashtable arg1 )
            throws InvalidParameterException {


      super( arg0 , arg1 );
      setup();
   }


   private void setup() {

      DataSetTools.util.SharedData Sdat = new DataSetTools.util.SharedData();
      String path = System.getProperty( "user.home" );
      if( path == null ) {

         fout = null;
         nWords = 0;
         Words = null;
         buffptrs = null;
         return;

      }
      path = path.replace( '\\' , '/' );
      if( ! path.endsWith( "/" ) )
         path += '/';
      path += "ISAW/";
      java.io.File f1 = new java.io.File( path + "/data.txt" );
      if( ! f1.exists() ) {

         fout = null;
         nWords = 0;
         Words = null;
         buffptrs = null;
         return;

      }
      try {

         fout = new java.io.RandomAccessFile( f1 , "r" );

      }
      catch( Exception s ) {

         fout = null;
         nWords = 0;
         Words = null;
         buffptrs = null;
         return;

      }


      f1 = new java.io.File( path + "keys.txt" );
      if( ! f1.exists() ) {

         fout = null;
         nWords = 0;
         Words = null;
         buffptrs = null;
         return;
      }
      lastmodified = 0;
      try {

         java.io.FileInputStream fin = new java.io.FileInputStream( f1 );
         String S = getLine( fin );
         if( S == null ) {
            fout = null;
            nWords = 0;
            Words = null;
            buffptrs = null;
            return;
         }

         lastmodified = ( new Long( S ) ).longValue();


         S = getLine( fin );
         if( S == null ) {
            
            fout = null;
            nWords = 0;
            Words = null;
            buffptrs = null;
            return;
            
         }
         nWords = ( ( new Integer( S ) ).intValue() );
         Words = new String[ nWords ];
         buffptrs = new int[ nWords ];
         for( int i = 0 ; i < nWords ; i++ ) {
            
            Words[ i ] = getNchars( fin , 8 ).trim();
            String SS = getNchars( fin , 8 );
            buffptrs[ i ] = ( new Integer( SS.trim() ) ).intValue();
            
         }

      }
      catch( Exception s ) {

         fout = null;
         nWords = 0;
         Words = null;
         buffptrs = null;
         return;
      }
      // Read in all the words and the pointers to the fout file.

      SCLH = new Script_Class_List_Handler();
      // urlh = ( URLStreamHandler)( new IsawHelp.HelpSystem.memory.Handler() );
   }


   private String getLine( java.io.FileInputStream fin ) {

      String S = "";
      int ic;
      try {
         
         for( ic = ( fin.read() ) ; ( ic <= ' ' ) && ( ic != - 1 ) ; ic = fin
                  .read() ) {}
        
         if( ic == - 1 ) {
            
            fout = null;
            nWords = 0;
            Words = null;
            buffptrs = null;
            return null;
            
         }
         
         S += (char) ( ic );
         for( ic = ( fin.read() ) ; ( ic != (int) '\n' ) && ( ic != - 1 ) ; ic = fin
                  .read() ) {
            
            S += (char) ic;

         }
         
         if( ic == - 1 )
            return null;
         
         return S;
         
      }
      catch( Exception s ) {
         
         return null;
         
      }
   }


   private String getNchars( java.io.FileInputStream fin , int nchars ) {


      String S = "";
      try {
         
         for( int i = 0 ; i < nchars ; i++ ) {
            
            int ic = fin.read();
            if( ic == - 1 )
               return null;
            
            S += (char) ic;
            
         }
         return S;
         
      }
      catch( Exception s ) {
         
         return null;
         
      }
   }

   
   

   /**
    *   Constructor for this Search engine
    */
   public MemSearchEngine() {


      super();
      setup();
   }


   /*
    *  Creates a SearchQuery to query this search engine
    * 
    * @see javax.help.search.SearchEngine#createQuery()
    */
   @Override
   public SearchQuery createQuery() throws IllegalStateException {


      return new MemSearchQuery( this );
   }


   /**
    *  Shows the information stored in the Words list
    *
    */
   public void show1() {


      System.out.println( "lastmodified " + lastmodified );
      System.out.println( "nWords " + nWords );
      for( int i = 0 ; i < nWords ; i++ )
         System.out.println( "     " + Words[ i ] + "::" + buffptrs[ i ] );


   }


   
   
   /**
    * Test program for parts of this class
    * 
    * @param args  No arguments are needed
    */
   public static void main( String[] args ) {


      MemSearchEngine Srch = new MemSearchEngine( null , null );
      SearchQuery sq = Srch.createQuery();
      sq.addSearchListener( Srch );
      sq.start( args[ 0 ] , null );


   }

   
   /**
    *  This is the SearchQuery that the Memory Search Engine uses to search
    *  for words
    * @author Ruth
    *
    */
   class MemSearchQuery extends SearchQuery {

      SearchEngine srchEngine;

      Vector       listeners;

      boolean      searching;


      public MemSearchQuery( SearchEngine se ) {


         super( se );
         srchEngine = se;
         listeners = new Vector();
         searching = false;
         
      }


      public void addSearchListener( SearchListener l ) {


         super.addSearchListener( l );
         if( ! listeners.contains( l ) )
            listeners.addElement( l );
         
      }


      public void removeSearchListener( SearchListener l ) {


         super.removeSearchListener( l );
         if( ! listeners.contains( l ) )
            listeners.remove( l );

      }


      public void start( java.lang.String searchparams , java.util.Locale l )
               throws java.lang.IllegalArgumentException ,
               java.lang.IllegalStateException {

         if( searchparams == null )
              throw new java.lang.IllegalArgumentException( "search string cannot be null" );
         searchparams = searchparams.toLowerCase();
         if( searchparams.length( ) >=8 )
            searchparams = searchparams.substring(0, 8);
         
         super.start( searchparams , l );
         searching = true;
         // SearchEvent evt = new SearchEvent( this, searchparams, true );
         // for( int i=0; i< listeners.size(); i++)
         // ((SearchListener)(listeners.elementAt(i))).searchStarted(evt);
         // super.fireSearchStarted();
         if( ! searching ) {
            exitt( 2 , searchparams );
            return;
         }
         if( fout == null ) {
            JOptionPane.showMessageDialog( null, 
              "Please do: 'Update Search Data Base' on the Help Menu,\n" +
              "wait for 'Done updating', then re-open the help window.");
            exitt( 2 , searchparams );
            return;
         }

         int indx = Arrays.binarySearch( Words , searchparams );
         
         if( indx < 0 ) {
            
            exitt( 2 , searchparams );
            return;
         }
         
         try {
            
            Vector FoundItems = new Vector();
            URL url = new URL( "memory" , "memory" , 322 , "" );
            
            for( int Next = buffptrs[ indx ] ; Next >= 0 ; ) {
               
               fout.seek( Next );
               String SS = fout.readLine();
               Next = ( new Integer( SS.trim() ) ).intValue();
               SS = fout.readLine();
               int Pos = ( new Integer( SS.trim() ) ).intValue();
               SS = fout.readLine();
               String tag = SS.trim();
               SS = fout.readLine();
               String Command = SS.trim();
               SS = fout.readLine();
               String filename = SS.trim();

               int index;
               if( tag.equals( "Generic" ) )
                  index = SCLH.getOperatorPosition( Command );
               else
                  index = SCLH.getDSOperatorPosition( Command );

               while( Match( index , Command , filename , tag ) < 0 )
                  index++ ;

               if( Match( index , Command , filename , tag ) == 0 ) {
                  
                  String host = "Generic";
                  if( ! tag.equals( "Generic" ) )
                     host = "DataSet";

                  MSearchItem srchItem = new MSearchItem( url , Command ,
                           Locale.US.toString() , host + "-" + index , 1.0 ,
                           Pos , Pos + searchparams.length() , new Vector() );
                  
                  FoundItems.addElement( srchItem );
                  
               }

            }
            
            
            searching = false;
            // super.itemsFound( true, FoundItems);
            super.fireItemsFound( false , FoundItems );
            super.fireSearchFinished();
            // for( int i=0; i< listeners.size(); i++)
            // ((SearchListener)(listeners.elementAt(i))).searchFinished(evt);

         }
         catch( Exception s ) {
            
            exitt( 2 , searchparams );
            System.out.println( "Error= " + s );
            s.printStackTrace();
            return;
            
         }

      }


      
      
      // return 0 means match, negative means CommandNames match but something
      // else does not
      private int Match( int index , String CommandName , String Source ,
               String tag ) {


         if( tag.equals( "Generic" ) )
            return MatchGeneric( index , CommandName , Source );
         return MatchDataSet( index , CommandName , Source );
         
      }


      // return 0 means match, negative means CommandNames match but something
      // else does not
      private int MatchGeneric( int index , String CommandName , String Source ) {


         try {
            
            if( index < 0 )
               return 2;
            
            if( ! SCLH.getOpInfo( index ).CommandName.equals( CommandName ) )
               return 1;
            
            if( Source == null )
               return 2;
            
            String opSource = SCLH.getOperator( index ).getSource();
            if( opSource.startsWith( "class " ) ) {
               
               if( ! opSource.equals( Source ) )
                  return 2;
               Class C = Class.forName( Source.substring( 5 ).trim() );

               File FF = getFileForClass( C );
               if( FF == null )
                  return 2;
               if( ! FF.exists() )
                  return 2;
               if( FF.lastModified() > lastmodified )
                  return 2;
               return 0;
               

            }
            else {// Is a filename of a script. Must eliminate prefix
                  // directories.
               String IsawHome = System.getProperty( "ISAW_HOME" );
               String UserHome = System.getProperty( "user.home" );
               String Src = ( new File( Source ) ).getCanonicalPath();
               if( Src == null )
                  return 2;
               String X = Src;
               if( ( IsawHome != null )
                        && ( new File( IsawHome ) ).getCanonicalPath() != null ) {
                  
                  IsawHome = ( new File( IsawHome ) ).getCanonicalPath();
                  if( Src.startsWith( IsawHome ) )
                     X = X.substring( 0 , IsawHome.length() );
                  
               }
               else if( ( UserHome != null )
                        && ( ( new File( UserHome ) ).getCanonicalPath() != null ) ) {
                  
                  UserHome = ( new File( UserHome ) ).getCanonicalPath();
                  if( Src.startsWith( UserHome ) )
                     X = X.substring( 0 , UserHome.length() );
                  
               }
               if( ! Src.endsWith( X ) )
                  return - 1;
               
               File FF = new File( Source );
               
               if( ! FF.exists() )
                  return 2;
               
               if( FF.lastModified() > lastmodified )
                  return 2;
               
               return 0;


            }

         }
         catch( Exception s ) {
            
            System.out.println( "Error =" + s.toString() );
            s.printStackTrace();
            return 2;
            
         }
      }


      // return 0 means match, negative means CommandNames match but something
      // else does not
      private int MatchDataSet( int index , String CommandName , String Source ) {


         try {

            if( index < 0 )
               return 2;
            
            DataSetTools.operator.DataSet.DataSetOperator dsop = SCLH
                     .getDataSetOperator( index );
            if( ! dsop.getCommand().equals( CommandName ) )
               return 1;
            
            String opSource = dsop.getSource();

            if( ! opSource.equals( Source ) )
               return - 1;

            Class C = dsop.getClass();

            File FF = getFileForClass( C );
            if( FF == null )
               return 2;
            
            if( ! FF.exists() )
               return 2;
            
            if( FF.lastModified() > lastmodified )
               return 2;
            
            return 0;
            
         }
         catch( Exception s ) {
            
            System.out.println( "Error =" + s.toString() );
            s.printStackTrace();
            return 2;
            

         }
      }

      

      private File getFileForClass( Class C ) {
         

         try {
            
            String F = C.getName().replace( '.' , File.separatorChar )
                     + ".class";

            java.security.ProtectionDomain d = C.getProtectionDomain();
            java.security.CodeSource codeBase = d.getCodeSource();
            URL location = codeBase.getLocation();
            File FF = new File( location.getFile() );
            if( FF.isDirectory() ) {
               
               FF = new File( FF , F );
               
            }

            return FF;

         }
         catch( Exception s ) {
            
            s.printStackTrace();
            return null;
            
         }

      }


      private void exitt( int nstepsLeft , String searchparams ) {


         searching = false;
         super.fireSearchFinished();
         // SearchEvent evt = new SearchEvent( this, searchparams, false );
         // for( int i=0; i< listeners.size(); i++)
         // ((SearchListener)(listeners.elementAt(i))).searchStarted(evt);
      }


      public void stop() throws java.lang.IllegalStateException {


         super.stop();
         searching = false;
      }


      // public void itemsFound(boolean inSearch, java.util.Vector docs){
      // System.out.println("What do I do with this????");
      // }

      public boolean isActive() {


         return searching;
      }
   }

  /**
   * Used for testing purposes, only
   */
   public void itemsFound( SearchEvent e ) {
    
      for( Enumeration S = e.getSearchItems() ; S.hasMoreElements() ; ) {
         System.out.println( (MSearchItem) ( S.nextElement() ) );
      }
   }

   /**
    * Used for testing purposes, only
    */
   public void searchStarted( SearchEvent e ) {


      System.out.println( "Search started " + e.getParams() );
   }

   /**
    * Used for testing purposes, only
    */
   public void searchFinished( SearchEvent e ) {


      System.out.println( "search ended" );

   }
   
   

   /**
    * This class was introduced solely for debugging purposes.  It was used to 
    * determine which methods were invoked on this class.
    * 
    * @author Ruth
    *
    */
   class MSearchItem extends SearchItem {

      public MSearchItem( java.net.URL base, java.lang.String title,
               java.lang.String lang, java.lang.String filename,
               double confidence, int begin, int end, java.util.Vector concepts ) {


         super( base , title , lang , filename , confidence , begin , end ,
                  concepts );

      }


      public java.net.URL getBase() {


         // System.out.println( "in getBase()"+super.getBase());
         return super.getBase();
      }


      public int getBegin() {


         // System.out.println( "in getBegin()"+super.getBegin());
         return super.getBegin();
      }


      public java.util.Enumeration getConcepts() {


         // System.out.println( "in getConcepts()"+super.getConcepts());
         return super.getConcepts();
      }


      public double getConfidence() {


         //System.out.println( "in Confidence()"+super.getConfidence());
         return super.getConfidence();
      }


      public int getEnd() {


         //System.out.println( "in getEnd()"+super.getEnd());
         return super.getEnd();
      }


      public java.lang.String getFilename() {


         //System.out.println( "in getFilename()"+super.getFilename());
         return super.getFilename();
      }


      public java.lang.String getLang() {


         // System.out.println( "in getLang()"+super.getLang());
         return super.getLang();
      }


      public java.lang.String getTitle() {


         // System.out.println( "in getTitle()");

         return super.getTitle();
      }


      public java.lang.String toString() {


         //  System.out.println( "in toString() "+super.toString());
         return super.toString();
      }

   }
}
