


package NexIO.Write.NexApi;
import NexIO.Write.*;
import NexIO.*;
import NexIO.Write.NexApi.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.lang.reflect.*;
import java.lang.*;
public class XmlWriteNode extends NexWriteUtil
{  static FileOutputStream fout;
   
    protected XmlWriteNode parent; //set
    public XmlWriteNode( String filename)
     { 
       super(filename);
       fout = null;
     }
    private XmlWriteNode(  String node_name, String class_name, 
                         FileOutputStream fout, XmlWriteNode parent ,
                         String filename , Hashtable linkInfo)
     { super( filename );
       errormessage = "";
       this.filename = filename;
      
       children = new Vector();
       attributes = new Vector();
       this.linkInfo = linkInfo;
       classname = class_name;
       nodename =  node_name;
       written = false;
       value = ranks = null;
       type = -1;
       //this.fout = fout;
       this.parent = parent;
       this.filename = filename;
       num_nxEntries = 0;
       
     }
    public NxWriteNode newChildNode( String node_name, String class_name)
      { NxWriteNode U=
              (NxWriteNode)(new XmlWriteNode( node_name, class_name, 
                                               fout, this ,filename , linkInfo));
        children.add( U );
        if( classname.equals("File"))
        if(class_name.equals("NXentry"))
              num_nxEntries++;
        return U;
      }
    private XmlWriteNode getParent()
    {return parent;
    }
    public void write()
      {if( (!classname.equals("File"))||!(nodename.equals("File")))
       {errormessage ="Improper Node to write from";
        return;
       }
        if( fout == null)
        try{
            fout = new FileOutputStream( filename );
           }
        catch( Exception s)
          {errormessage = s.getMessage();
           return;
          }
      
      Write( fout);
      NxNodeUtils nu = new NxNodeUtils();
      /*System.out.println(" key values=");
      for(Enumeration E = linkInfo.keys();E.hasMoreElements();)
         {System.out.println(E.nextElement());
         }
     System.out.println(" result values=");
      for(Enumeration E = linkInfo.elements();E.hasMoreElements();)
         {System.out.println(E.nextElement());
         }
      */  

      }
    private void Write( FileOutputStream fout )
     {/*if( (!classname.equals("File"))||!(nodename.equals("File")))
       {errormessage ="Improper Node to write from";
        return;
       }
      */
     //System.out.println("Writing "+ nodename+","+classname);
     if( written) if( !classname.equals("File"))
         return;
      
      String S="";
      if( classname.equals("File"))
      {if( !written)
        {
         S = "<?xml version=\"1.0\" ?> <NXfile filename=";
         S += '"'+filename+'"'+ " file_time=\"";
         Date D= new Date();
         D.setTime( System.currentTimeMillis());
         S += new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss").format( D)+"\" ";
        }
      }
      else 
	{
          S = "<"+classname+" ID =\""+nodename+"\" ";
        }
    
        S +="\n";
        for( int i = 0; i< attributes.size(); i++)
          {Vector V =(Vector)( attributes.elementAt( i ));
           S += (String)(V.firstElement())+"=\"";
           S += Stringify( V.elementAt(1) ,
                        ((Integer)(V.elementAt(2))).intValue()
                   ,(int[]) V.elementAt(3))+ "\" ";
          }
        S += ">\n ";
        //System.out.println("C"+S);
        try{//System.out.println("::"+S+"::");
          fout.write( S.getBytes());
         

          written = true;
          S = "";
       if( classname.equals( "SDS" ))
           { S=Stringify( value , type ,ranks  );
          // System.out.println("::"+S+"::");
           fout.write( S.getBytes() );
            S = "";
            
          
          }
       
        for( int i = 0; i< children.size(); i++)
          { Object U = children.elementAt ( i );
            if( U instanceof XmlWriteNode)
              ((XmlWriteNode)U).Write( fout );
            else if( classname.equals("File"))
               {errormessage +="No links at root level";
	       //System.out.println("ERRR="+U+","+U.getClass());
               }
            else if( U instanceof String)
              { Object SS = linkInfo.get( U );
	      //System.out.print("in add link key, res="+U+","+ SS);
                if( SS == null)
                  errormessage +="No link here";
                else
                 { fout.write( ("<link>"+SS+"</link>").getBytes());
                  
                   // System.out.print("::"+"<link>"+SS+"</link>");
                 }
               
              }
            else
             {String linkhandle= (String) ((Vector)U).firstElement();
              XmlWriteNode N =(XmlWriteNode)((Vector)U).lastElement();
              linkInfo.put(linkhandle, Pathify( N ));
              //System.out.println("link set key,value"+linkhandle+","+Pathify(N));
             }
          
          }
        if( classname != "File")
           {fout.write(("</"+classname+">").getBytes());
            
            //System.out.print("::"+"</"+classname+">");
           }
        }
        catch( IOException ss)
          { errormessage = ss.getMessage();
            return;
          }

      }
   
   public String Stringify( Object Value, int type, int rank[])
    {int l=1 ;
     String S ="";
     if( (rank == null) ||(Value == null))
        return S;
    
    if( Value instanceof String) return (String)Value;
    errormessage ="Not an array";
    if( !(Value.getClass().isArray()))
      {//System.out.println("Stringify not array ="+Value.getClass());
       if( Value instanceof Number)
           return Value.toString();
         return Value.toString();
      }
    errormessage ="";
    if( rank.length <= 0)
        return S;
    int i, size=1;
    if( rank.length == 1)
       {for (i = 0 ; i < Array.getInt( rank , 0 ); i++)
          if( type == Types.Int)
             S += Array.getInt( Value , i )+" ";
          else if( type == Types.Byte)
            S += Array.getByte( Value , i )+" ";
          else if( type == Types.Short)
            S += Array.getShort( Value , i )+" ";
          else if( type == Types.Char )
            S += (char)(Array.getByte( Value , i ));
          else if( type == Types.Long)
            S += Array.getLong( Value , i )+" ";
          else if( type == Types.Float)
            S += Array.getFloat( Value , i )+" ";
          else if( type == Types.Double)
            S += Array.getDouble( Value , i )+" ";
          else
            {errormessage = "unsupported type";
             return null;
            }
        return S;
       }
    int rank1[];
    rank1 = new int[ rank.length - 1];
    System.arraycopy( rank ,1, rank1 ,0, rank1.length );
    for( i=0; i < rank[0] ; i++)
      {Object U = Array.get( Value ,i);
       S += Stringify( U, type, rank1 );
      }
   return S;   
    }
   public String Pathify( XmlWriteNode NN)
    {String S = nodename;
     XmlWriteNode n;
     for( n = NN.getParent(); n!= null ; n = n.getParent())
      {S = n.nodename+"."+S;
      }
     return S;
    }
       public void close()
      {if( fout != null )
        try{
         //System.out.println("::"+"</NXfile>");
         fout.write( "</NXfile>".getBytes());
          fout.close();
         }
       catch(IOException s)
         {}
       
      }
    public static void main( String args[] )
     {XmlWriteNode XX = new XmlWriteNode( "C:\\xmlText.xml" );
      int rank[];
      rank = new int[1];
      rank[0] = 4;
      XX.addAttribute( "age", "abcd".getBytes(), Types.Char ,rank);
      float xvals[];
      xvals = new float [3];
      xvals[0] = 1.3f; xvals[1] = -3.4f; xvals[2] = 1.1f;
      rank[0] = 3;
      XX.addAttribute("numb", xvals, Types.Float, rank );
      NxWriteNode U = XX.newChildNode( "entry1", "NXentry");
      NxWriteNode UV= XX.newChildNode( "title", "SDS");
      rank[0] =3;
      UV.addAttribute( "units", ("sec").getBytes(), Types.Char, rank);
      rank[0] = 5;
      UV.setNodeValue( "abcdef".getBytes(),Types.Char, rank );
     
      XX.show( );
      XX.write();
      XX.close();
     
  /* try{
     FileOutputStream  fout = new FileOutputStream( "C:\\xmltext.xml");
     
     fout.write( "abcdefg".getBytes());
     fout.write("uvwxyz".getBytes());
     fout.close();
     }
     catch(Exception s)
      {System.out.println("Error="+s);
      }
  */
     
     }//main 


 }






