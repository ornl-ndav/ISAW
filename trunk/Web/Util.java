/* 
 * File: Util.java
 *
 * Copyright (C) 2010, Ruth Mikkelson
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
 *  $Author:$
 *  $Date:$            
 *  $Rev:$
 */
package Web;

import gov.anl.ipns.Util.File.FileIO;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.*;
import javax.xml.bind.Unmarshaller;

import Command.ScriptUtil;
import DataSetTools.dataset.DataSet;

/**
 * Class containing Utility methods for accessing WebServices
 *   and sample code to retrieve data from the web services
 *
 * The exact syntax/formats for many of the web services is not yet
 *   stable, so much of this is changeable.
 * 
 * @author ruth
 *
 */
public class Util
{

   //TODO: Use a web service to get this..
   static String ACCESS_KEY = "20fe0awkedjhatxnbowxei8p";

   static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
   
   static String CommandStart = "https://orbiter.sns.gov/orbiter/service/util/";
  //example of command to send into SignedCommand Method
  //      CommandStart+"OrbiterFindNexusService.php/facil/SNS/inst/SNAP/run/240/format/XML
   
   
   /**
    * Gets the private key from the user's home directory
    * 
    * @return the private key from the user's home directory
    *
    *TODO: Use a web service to get this private key for this session only.
    *NOTE: There was talk of getting a different form of authentication
    */
   public static String PrivateKey( )
   {

      String fileName = FileIO.appendPath( System.getProperty( "user.home" ) ,
                          ".orbiter" ) + File.separator + "ehx.key";
      
      String PRIVATE_KEY = "";
      
      try
      {
         FileInputStream fin = new FileInputStream( fileName );

         for( int c = fin.read( ) ; c >= 5 ; c = fin.read( ) )
         {
            PRIVATE_KEY += ( char ) c;
         }
         
      } catch( Exception s )
      {
         return null;
      }
      
      return PRIVATE_KEY;
   }

   /**
    * Deprecated. The Expires and Access_key will be added when signed
    * Adds the EXPIRES time and ACCESS_KEY to the command.
    * 
    * @param Command  The command to be executed, including host and arguments
    * 
    * @return  The new command with the Expiration time and access key added to it
    */
   public static String GetCommand( String Command)
   {
      String EXPIRES = ( "" + ( System.currentTimeMillis( )/1000 + 60 ) ).trim( );
      
      //String command = Command + "/OrbiterAccessKeyId/" + ACCESS_KEY + "/Expires/"
      //      + EXPIRES;
      
      String command = Command + "?OrbiterAccessKeyId=" + ACCESS_KEY + "&Expires="
                              + EXPIRES;
      
      return command;
   }
   
   
   /**
    * Signs the command
    * 
    * @param command  The command including host,job, arguments, Expiration time
    *                 and access key
    *                 
    * @param PRIVATE_KEY   The private key
    * 
    * @return  The whole command with the signature added
    */
   public static String SignedCommand( String command, String PRIVATE_KEY)
   {
      try
      {

         SecretKeySpec signingKey = new SecretKeySpec( PRIVATE_KEY.getBytes( ) ,
               HMAC_SHA1_ALGORITHM );

         Mac mac = Mac.getInstance( HMAC_SHA1_ALGORITHM );
         mac.init( signingKey );
         
        
         String EXPIRES = ( "" + ( System.currentTimeMillis( )/1000 + 60 ) ).trim( );
         
         String str = command+"/OrbiterAccessKeyId/" + ACCESS_KEY + "/Expires/"+EXPIRES;
        
         System.out.println("string to be encoded"+str);
         
         byte[] rawHmac = mac.doFinal( str.getBytes( ) );
         byte[] SIGNATURE = DataSetTools.dataset.xml_utils.encode( rawHmac );

         String signature = new String( SIGNATURE );
         signature = signature.trim( );
         
         String S = "";  //Eliminate all forward slashes
         for( int i = 0 ; i < signature.length( ) ; i++ )
         {
            char c = signature.charAt( i );
            if ( c != '/' )
               S += c;
         }
         
         signature = S;
        
         String Command = command + "/OrbiterAccessKeyId/" + ACCESS_KEY + "/Expires/"
         + EXPIRES;
         
         return ( Command + "/Signature/" + signature );
         
         
         
      }catch( Throwable t)
      { 
         t.printStackTrace( );
         return null;
      }

   }
   
   //Returns a JAXBElement<Class for the Response> which can use .getValue
   //   method on this JAXBElement to get the Object containing the response/
   private static Object SendGetObject( URL url, String Dir)
   {

      try
      {
         InputStream inp = url.openStream( );
         JAXBContext jc = JAXBContext.newInstance( Dir );// "Web");
         // System.out.println("content="+content);
         // content ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
         // "<findnexus><facil>SNS</facil><inst>SNAP</inst><prop>2008_3_3_SCI</prop><coll>3</coll><run>240</run><path>/SNS/SNAP/2008_3_3_SCI/3/240</path></findnexus>";
         Unmarshaller u = jc.createUnmarshaller( );
         return u.unmarshal( inp );
        /*//rest of the code to get value out
         JAXBElement< Web.FindNexusResponse > o = //
         ( JAXBElement< Web.FindNexusResponse > ) u.unmarshal( inp );
         // new StreamSource( new StringReader(content)));
         ;
         Web.FindNexusResponse resp = o.getValue( );
         System.out.println("\n\nxxxxxxxxxxxxxxxx"+ resp.file.get( 0 ).path );
         return o;
        */
         
      } catch( Exception ss )
      {
         System.out.println("error in SendGetObject "+ss);
         ss.printStackTrace( );
         return null;
      }
   }
   
   /**
    * Returns error string or data set
    */
   public static Object  MakeSerializedDataSetFromEventFile( String inst,
                                                             int    runNum,
                                                             String Filter,
                                                             String AxisType, 
                                                             float FirstEvent,
                                                             float LastEvent ,
                                                             float MinX ,
                                                             float MaxX ,
                                                             boolean IsLog,
                                                             float FirstStep,
                                                             int NumBins,
                                                             String Output
                                                             )
   {
      String urlString = SignedCommand( CommandStart+"MakeSerializedDataSet/inst/"+inst+
                                                       "/runNum/"+runNum+"/Filter/"+Filter+
                                                       "/AxisType/"+AxisType+"/FirstEvent/"+
                                                       FirstEvent+"/LastEvent/"+LastEvent+
                                                       "/MinX/"+MinX+"/MaxX/"+MaxX+"/isLog/"+
                                                       IsLog+"/FirstStep/"+FirstStep+"/NumBins/"+
                                                       NumBins+"/Output/"+Output, Util.PrivateKey( ));
      try
      {
         URL url = new URL( urlString );
         URLConnection con = url.openConnection( );
         con.connect( ); // can use con.getContent() here.
         
         InputStream inp = con.getInputStream( );
         ObjectInputStream oStream = new ObjectInputStream( inp );
         DataSet D = (DataSet)oStream.readObject( );
         return D;
         

      } catch( Throwable t )
      {
         t.printStackTrace( );
         return new ErrorString( t.getMessage( ) );
      }

      //TODO: Only add the ones that are legitimate
      
    
   }
   
   //Used to get textual output for the result of this command
   // Also, if response is NOT xml, use this as an example.
   public static String SendGetStream(URL url)
   {

      // URLConnection con = url.openConnection( );
      // con.connect( ); //can use con.getContent() here.
      try
      {
          URLConnection con = url.openConnection( );
          con.connect( ); //can use con.getContent() here.
         InputStream inp = con.getInputStream( );
          System.out.println("after get input stream");
         // reply. Assumes Text here. Could be binary file.
         String content = "";
         if ( inp != null )
            for( int c = inp.read( ) ; c >= 0 ; c = inp.read( ) )
               {
              
                 content += ( ( char ) c );
               }

         return content;

      } catch( Exception ss )
      {  System.out.println("In error");
         ss.printStackTrace( );
         return null;
      }
   }

   /**
    * Test program for retrieving findnexus data
    * (A) Just gets the output as a Stream.  If the output is not in xml format this is
    *               needed, or returning an object
    * (B) Uses the JAXB stuff to translate to the correct structure.
    *    See the file FindNexusResponse.jav  for more information on how to 
    *    get everything going.
    * @param args
    */
   public static void main(String[] args)
   {

     //old form-did not translate over well
      String url = Util.SignedCommand( Util.CommandStart+"OrbiterFindNexusService.php/facil/SNS/inst/SNAP/run/240/format/XML/filter/nxs",
                                       Util.PrivateKey() );
      System.out.println( url.toString());
      try
      {
      //Util.SendGetStream( new URL( "https://orbiter.sns.gov/restricted/orbiter-user-service.php/keys"));
       // System.out.println( Util.SendGetStream( new URL(url)));//A
         JAXBElement< FindNexusResponse > o =(JAXBElement< FindNexusResponse >) 
                                    Util.SendGetObject( new URL(url) ,"Web");//B
         
         FindNexusResponse resp = o.getValue( );
         for( int i=0;i< resp.file.size();i++)
         {
            OneElement elt = resp.file.get(i);
            System.out.println( "run"+elt.run);
            System.out.println( "Inst"+elt.inst);
            System.out.println( "Coll"+elt.coll);
            System.out.println( "getPath"+elt.path);
            System.out.println( "Prop"+elt.prop);
            System.out.println("----------------------------------------------");
         }
      }catch(Exception ss)
      {
         ss.printStackTrace();
      }

   }
   
  //Attempt(unsuccessful) to run a spccific operator over the web
  //   The web service was not started.
   public static void main1( String[] args)
   {
      Object res = Util.MakeSerializedDataSetFromEventFile( "PG3",
             527,
            "_neutron_event.dat",
            "d-spacing", 
            0,
            1E8f ,
            .2f ,
            3.5f,
            true,
            2.0e-4f,
            5000,
            "/SNS/users/eu7/diamond.isd"
            );
      if( res instanceof ErrorString)
         System.out.println("Error="+res);
      else
      {
         ScriptUtil.display( res );
      }
   }

}
