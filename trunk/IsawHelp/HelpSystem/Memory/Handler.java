/*
 * File:  Handler.java 
 *
 * Copyright (C) 2002, Ruth Mikkelson
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
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.2  2002/11/27 23:27:38  pfpeterson
 * standardized header
 *
 * Revision 1.1  2002/11/18 17:54:50  dennis
 * Handler for virtual html pages for documentation.
 *
 *
 */
package IsawHelp.HelpSystem.Memory;
import java.net.*;
import java.io.*;
import javax.swing.*;


/** This a URLStreamHandler that is associated with a URLConnection that
 *   calculates the information at run time.  The protocol for this handler
 *   is "Memory" instead of http, ftp, etc.
 *   This handler will eventually get the appropriated html pages for operator
 *   documentation and send these to the JavaHelp system to be displayed
 */
public class Handler extends URLStreamHandler
{ 
    public Handler()
    {
      super();
    }

 /** Returns the URLConnection that handles the "Memory:" protocol for
  *  creating html pages for Isaw Operators in memory
  */
  public URLConnection openConnection(URL u) throws IOException
  { 
     if( !(u.getProtocol().equals("Memory")))
               throw new IOException("improper protocol");
        
     return new MURLConnection( u);
  }


 /** The URLConnection class that handles the "Memory:" protocol for html pages
  *   for ISAW operators
  */
  class MURLConnection extends URLConnection
  {
     URL  url;

    /** 
     *  Constructor for this URL connection.
     *  The url most be of the form Memory://Generic;xx.. or 
     *  Memory://DataSet;xx where xx is the port an represents the index in 
     *  the Script_Class_List_Handler of the
     *  associated Generic or DataSet operator installed by the ISAW system
     */
     public MURLConnection(URL url)
     {
        super(url);
        this.url=url;
        setDoInput(true);
     }
    
  /** 
   *  Creates an InputStream from the characters in the html page 
   *  associated with the url
   */
  public  InputStream getInputStream()  throws IOException
  {
     return new ByteArrayInputStream( ((String)getContent()).getBytes());
  }


 /**
  *   Connects the system to the "resource"
  */
  public void connect() 
  {
     setDoInput( true );
  }


  /** 
   *  Same as getContent()
   */
  public Object getContent(Class[] classes) throws IOException
  { 
    return getContent();
  }


  /** 
   *  Returns the length of the html page associated with the ISAW operator 
   *  described by the url used to open this URLConnection
   */
  public  int getContentLength() 
  {
    try{
        return ((String)getContent()).length();
       }
   catch(IOException s)
       {
        return 0;
        }
  }


  /** 
   * Returns "text/html", the type associated with browser html pages
   */
  public String getContentType() 
  {
     return "text/html";
  }
 

  /** 
   *  Gets the content of the html page for the ISAW operator associated 
   *  with this URL NOTE: Not quite implemented yet
   */
  public Object getContent() throws IOException
  {
     return "<html><body> this is an html<P> page I hope<P>works"+
          url.getHost()+":"+url.getPort()+"</body></html>";
  }

}//class MURLConnection


  /**  
   *  Test program 
   */
  public static void main( String args[])
    {
     JFrame jf= new JFrame( "Test");
     try{
        System.setProperty("java.protocol.handler.pkgs","test");//did not work
        URLStreamHandler MyurlStreamHandler = (URLStreamHandler)(new Handler());
        URL url=new URL("Memory","DSOperator",35,"x",MyurlStreamHandler);
        JEditorPane jep = new JEditorPane( url);
        jf.getContentPane().add(jep);
        jf.setSize(600,600);
        jf.show();
        jf.validate();
       }
     catch(Exception ss)
       {
         System.out.println("Exception="+ss);
       }
     }//method main

}//class Handler
