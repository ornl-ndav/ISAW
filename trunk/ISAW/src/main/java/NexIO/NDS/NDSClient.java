  /**
    *                   N D S C L I E N T
    *
    * Some Java code which interfaces to a NeXus Data Server.
    *
    * copyleft: Mark Koennecke, August 1998
    */
package NexIO.NDS;
import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

  public class NDSClient {

  /*=====================  Variables ===================================*/
    /**
      * Connection Data 
    */
    protected Socket sock;
    protected String computer;
    protected String proxyserver;
    protected int port, proxyport; 
    protected InputStream sin;
    protected OutputStream sout;
    protected DataOutputStream dout;
    protected DataInputStream din;
    protected int iMagic;

    /**
      * Result Data
    */
    protected String stringResult;
    protected int    intResult[];
    protected byte   byteResult[];
    protected float  floatResult[];
    protected double doubleResult[];
    protected int iType;
    protected int iDim[];
    public final static int FLOAT  = 1;
    public final static int INT    = 2;
    public final static int BYTE   = 3;
    public final static int STRING = 4;
    public final static int DOUBLE = 5;    
    public final static int FILE   = 6;

    /**
      * stuff 
    */
    static final private int debug = 0;

  /*====================== Constructors =================================*/
    /**
      * Constructs a new NDSClient object to connect to a NDS server.
      * living at the computer host and listening at the TCP/IP
      * port port. iMagic is the magic integer used for verifying
      * access rights. Set iMagic to 0 if you use the login facility 
      * for getting the magic ID.
      *
      * @param host The host computer to connect to.
      * @param port The TCP/IP port the host is listening to.
      * @param iMagic The magic number.
      */
    public NDSClient(String host, int port, int iMagic)
    {
         this(host,port,null,0,iMagic);
    }
  /*---------------------------------------------------------------------*/
   /**
     * same as above. But you are in an applet and the NDS server can only
     * be reached through a Proxy server. Then the additional parameters
     * pproxyserver and pproxport give the computer where the proxy server
     * resides and the port number where it is listening.
    */
    public NDSClient(String hhost, int pport, 
                     String pproxyserver, int pproxyport, int iiMagic)
    {
        computer = hhost;
        port = pport;
        proxyserver = pproxyserver;
        proxyport = pproxyport;
        iMagic = iiMagic;
    }
  /**
    * setProxy sets the proxyserver and port 
  */
   public void setProxy(String pserver, int pPort)
   {
         proxyserver = pserver;
         proxyport =   pPort;
   } 
  /**
    *
  */
   public boolean isConnected()
   {
       if(sock == null)
          return false;
       else
          return true;
   }
  /*======================== Methods ====================================*/
  /**
    * connect connects to the NDS server. If a proxy server is
    * set, through the proxyserver stated above. 
    *
    * Please note, that the code dealing with the proxy server may need
    * hacking if another proxy server is used. This code works well with
    * the free Java Proxy server supplied with this package. 
    */
   public synchronized boolean  connect()
   {
         /* valid adress ? */
         if((computer == null) || computer.length() == 0)
         {
             return false;
         }

         if(proxyserver == null)
         {
            /* try a connect */
            try {
                    sock = new Socket(computer,port);
                } catch(Exception  e)
                { 
                   return false;
                }    
                try {
                   sin = sock.getInputStream();
                   sout = sock.getOutputStream();
                   dout = new DataOutputStream(sout);
                   din = new DataInputStream(sin);
                }catch(Exception e2) {
                 return false;
                }
                return true;
             }
          else
          {
             try {
                    sock = new Socket(proxyserver,proxyport);
                    sin = sock.getInputStream();
                    sout = sock.getOutputStream();
                    dout = new DataOutputStream(sout);
                    din = new DataInputStream(sin);
                    dout.writeBytes("[ " + computer + " " 
                                   + port + " ]"); 
                } catch(Exception  e)
                { 
                   return false;
                }    
                return true;
          }
     }
   /*-----------------------------------------------------------------*/
   /**
     * does what it says
   */
     public void disconnect()
     {
         if(sock != null)
         {
            try {
                sock.close();
            }catch(Exception e) {
            }
         }
         sin = null;
         sout = null;
         din = null;
         dout = null;
     }
   /**
    * Retrieves a directory listing of the directory
    * specified by dir. 
    *
    * @param dir Directory relative to the root directory of
    * the NDS server.
   */
     public synchronized  boolean getDirectory(String dir) throws IOException
     {
        int iCode = 701;

        if(sock == null)
        {
           stringResult = new String("Not Connected");
           return false;
        }

        clearResult();
     
        /* send request */
        dout.writeInt(iMagic); 
        dout.writeInt(iCode);
        dout.writeInt(dir.length());
        sout.write(dir.getBytes());

        return readReply();
     }
   /**
    * gets a listing of the content of the vGroup described by
    * the definition string def in the NeXus file file. The name of 
    * the NeXus file is relative to the root directory of the
    * NDS server.
   */
     public synchronized boolean getVGroupDirectory(String file, String def)
                   throws IOException
     {
        int iCode = 702;

        if(sock == null)
        {
           stringResult = new String("Not Connected");
           return false;
        }
     
        clearResult();

        /* send request */
        dout.writeInt(iMagic); 
        dout.writeInt(iCode);
        StringBuffer stb = new StringBuffer();
        stb.append(file);
        stb.append("\n");
        stb.append(def);
        String all = new String(stb);
        if(debug > 0)
        {
          System.out.println("Sending " + all);
        }
        dout.writeInt(all.length());
        sout.write(all.getBytes());

        return readReply();
     }
   /**
    * gets a listing of the attributes of the SDS described by
    * the definition string def in the NeXus file file. The name of 
    * the NeXus file is relative to the root directory of the
    * NDS server.
   */
     public synchronized boolean getAttrib(String file, String def)
                   throws IOException
     {
        int iCode = 704;

        if(sock == null)
        {
           stringResult = new String("Not Connected");
           return false;
        }

        clearResult();
     
        /* send request */
        dout.writeInt(iMagic); 
        dout.writeInt(iCode);
        StringBuffer stb = new StringBuffer();
        stb.append(file);
        stb.append("\n");
        stb.append(def);
        String all = new String(stb);
        if(debug > 0)
        {
          System.out.println("Sending " + all);
        }
        dout.writeInt(all.length());
        sout.write(all.getBytes());

        return readReply();
     }
   /**
    * getData retrieves the data content of the SDS described by the
    * definition string def. The name of the NeXus file is relative to 
    * the root directory of the NDS server.
   */
     public synchronized boolean getData(String file, String def)
                   throws IOException
     {
        int iCode = 703;

        if(sock == null)
        {
           stringResult = new String("Not Connected");
           return false;
        }
     
        clearResult();

        /* send request */
        dout.writeInt(iMagic); 
        dout.writeInt(iCode);
        StringBuffer stb = new StringBuffer();
        stb.append(file);
        stb.append("\n");
        stb.append(def);
        String all = new String(stb);
        if(debug > 0)
        {
          System.out.println("Sending " + all);
        }
        dout.writeInt(all.length());
        sout.write(all.getBytes());

        return readReply();
     }
   /**
    * logon sends a username and password to the NDS server,which in turn
    * sends a magic ID for further communication.
   */
     public boolean logon(String user, String password)
                   throws IOException
     {
        int iCode = 705;

        if(sock == null)
        {
           stringResult = new String("Not Connected");
           return false;
        }
     
        /* send request */
        dout.writeInt(iCode); 
        dout.writeInt(iCode);
        StringBuffer stb = new StringBuffer();
        stb.append(user);
        stb.append("\n");
        stb.append(password);
        String all = new String(stb);
        if(debug > 0)
        {
          System.out.println("Sending " + all);
        }
        dout.writeInt(all.length());
        sout.write(all.getBytes());

        return readReply();
     }
   //=================================================================
   /**
     * getFile copies a file through the network buffer to us.
     * If successful you may retrieve an InputStream on it for further
     * processing. This was installed in order to server ASCII files
     * with the NDS as well.
     */
     public synchronized boolean getFile(String name) throws IOException
     {
        int iCode = 706;

        if(sock == null)
        {
           stringResult = new String("Not Connected");
           return false;
        }

        clearResult();

        /* send message */
       dout.writeInt(iMagic);
       dout.writeInt(iCode);
       dout.writeInt(name.length());
       dout.write(name.getBytes());

       return readReply();
     }

   /*-----------------------------------------------------------------*/
   /**
     *The methods below are primitive data access methods.
   */
     public String getStringData()
     {
         if(iType == STRING)
         {
            return  stringResult;
         } else if(iType == BYTE) /* be nice */ 
         {
           return new String(byteResult);
         }
         return null;
     }
   /*-----------------------------------------------------------------*/
     public byte[] getByteData()
     {
          return byteResult;
     }
   /*-----------------------------------------------------------------*/
     public int[] getIntData()
     {
          return intResult;
     }
   /*-----------------------------------------------------------------*/
     public float[] getFloatData()
     {
          return floatResult;
     }
   /*-----------------------------------------------------------------*/
     public double[] getDoubleData()
     {
          return doubleResult;
     }
   /*-----------------------------------------------------------------*/
     public int  getType()
     {
          return iType;
     }
   /*-----------------------------------------------------------------*/
     public int[]  getDimension()
     {
          return iDim;
     }
   //==================================================================
     public InputStream getFileStream()
     {
          if(iType != FILE)
          {
             return null; 
          }
          return new ByteArrayInputStream(byteResult);
     }
   /*------------------------------------------------------------------*/
     protected boolean readReply() throws IOException
     {
          int iMessageType, iLength, iCount;
          int iRank, iDT, i;
          byte buffer[], inbuffer[];
          int iOff, iRead;

          /* read Message Type */
          iMessageType = din.readInt();
          if(debug > 0)
          {
              System.out.println("Received Message of Type: " 
                                  + iMessageType);
          }

          switch(iMessageType)
          {
             case -705: /* Error */
                        iLength = din.readInt();
                        buffer = new byte[iLength];
                        iCount = sin.read(buffer);
                        if( (iCount != iLength) && (debug > 0) )
                        {
                            System.out.println("Received incomplete message");
                        }
                        stringResult = new String(buffer);
                        buffer = null;
                        iType = STRING;
                        return false;
             case 706: /* directory data */
                        iLength = din.readInt();
                        if(debug > 0)
                        {
                          System.out.println("Receiving "
                              + iLength + " bytes of dir data");
                        }
                        buffer = new byte[iLength];
                        inbuffer = new byte[8192];
                        iOff = 0;
                        try{
                            while(iLength > 0)
                            {
                              iRead = sin.read(inbuffer);
                              System.arraycopy(inbuffer,0,
                                     buffer,iOff,iRead);
                              iOff += iRead;
                              iLength -= iRead;
                              if(debug > 4)
                              {
                                  System.out.println("Received " + iRead +
                                                     " bytes");
                              }
                            }
                        }catch(Exception furz){
                            System.out.println("Received incomplete message");
                        }
                        if(debug > 2)
                        {
                             System.out.println("Received dir data");
                        }
                        stringResult = new String(buffer);
                        buffer = null;
                        inbuffer = null;
                        iType = STRING;
                        return true;
             case 707: /* data, the real work */
                       /* first item is type */
                       iDT = din.readInt();
                       iRank = din.readInt();
                       /* read dimensions */
                       iDim = new int[iRank];
                       iLength = 1;
                       for(i = 0; i < iRank; i++)
                       {
                          iDim[i] = din.readInt();
                          iLength *= iDim[i];
                       }
                       /* read data, according to data type */
                       if(debug > 0)
                       {
                          System.out.println("Received " +
                             iLength + " data of type " + iDT); 
                       }
                       switch(iDT)
                       {
                          case 800: /* byte data */
                                   byteResult = new byte[iLength];
                                   for(i = 0; i < iLength; i++)
				   {
                                     byteResult[i] = 0;
                                   }
                                   inbuffer = new byte[8192];
                                   iOff = 0;
                                   try{
                                      while(iLength > 0)
                                      {
                                         iRead = sin.read(inbuffer);
                                         System.arraycopy(inbuffer,0,
                                               byteResult,iOff,iRead);
                                          iOff += iRead;
                                          iLength -= iRead;
                                       }
                                     }catch(Exception furz2){
                                         System.out.println(
                                            "Received incomplete message");
                                   }
                                   iType = BYTE;
                                   break;
                          case 801: /* int data */
                                   intResult = new int[iLength];
                                   for(i = 0; i < iLength; i++)
                                   {
                                      intResult[i] = din.readInt();
                                   }
                                   iType = INT;
                                   break;
                          case 802:  /* float */
                                   floatResult = new float[iLength];
                                   for(i = 0; i < iLength; i++)
                                   {
                                        floatResult[i] = din.readFloat();
                                   }
                                   iType = FLOAT;
                                   break;
                          case 803:  /* double */
                                   doubleResult = new double[iLength];
                                   for(i = 0; i < iLength; i++)
                                   {
                                        doubleResult[i] = din.readDouble();
                                   }
                                   iType = DOUBLE;
                                   break;
                          default:
                                   if(debug > 0)
                                   {
                                      System.out.println("Received bad type "
                                                         + iDT);
                                   }
                                  stringResult = new String("Invalid message received");
                                  iType = STRING;
                                  return false; 
                       }
                       if(debug > 0)
                       {
                           System.out.println("Data read");
                       }
                       return true;
             case 708: /* magic ID */
                       iMagic = din.readInt();
                       if(debug > 0)
                       {
                         System.out.println("New magic ID: "
                                             + iMagic);
                       }
                       return true;
             case 709: /* file data */
                        iLength = din.readInt();
                        if(debug > 0)
                        {
                          System.out.println("Receiving "
                              + iLength + " bytes of file data");
                        }
                        byteResult = new byte[iLength];
                        inbuffer = new byte[8192];
                        iOff = 0;
                        try{
                            while(iLength > 0)
                            {
                              iRead = sin.read(inbuffer);
                              System.arraycopy(inbuffer,0,
                                     byteResult,iOff,iRead);
                              iOff += iRead;
                              iLength -= iRead;
                              if(debug > 4)
                              {
                                  System.out.println("Received " + iRead +
                                                     " bytes");
                              }
                            }
                        }catch(Exception furz){
                            System.out.println("Received incomplete message");
                        }
                        if(debug > 2)
                        {
                             System.out.println("Received dir data");
                        }
                        inbuffer = null;
                        iType = FILE;
                        return true;
             default:
                       if(debug > 0)
                       {
                           System.out.println(
                                  "Received bad message ID: " 
                                   + iMessageType);
                       }
                       stringResult = new String("Invalid message received");
                       iType = STRING;
                       break;
          }
          return false;
     }
  /** 
    * a finalizer, ask the Sun what for
  */     
    protected void finalize() throws Throwable
    {
      floatResult = null;
      stringResult = null;
      doubleResult = null;
      byteResult = null;
      intResult = null;
      iDim = null;
      sock = null;
      computer = null;
      proxyserver = null;
      sout = null;
      sin = null;
      dout = null;
      din = null;
      super.finalize();
    }
  /**
    * clears all data items in order to stop overwrite at errors
    */
   private void clearResult()
   {
       floatResult = null;
       byteResult = null;
       doubleResult = null;
       iDim = null;
       intResult = null;
       stringResult = null;
   }
  } /* end of class definition */
