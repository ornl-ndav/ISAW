package DataSetTools.viewer.Table;

import DataSetTools.operator.*;
import DataSetTools.math.*;
import javax.swing.*;
import DataSetTools.dataset.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import DataSetTools.util.*;
import java.io.*;
import javax.swing.table.*;
import IsawGUI.*;
public class table_view extends JPanel implements ActionListener
  { 

    String Fields[] = { "Title", "X_units", "Y_units", "X_label", "Y_label",
                        "Initial_Path", "Run Number", "Pressur", "Temp",
                        "Group ID", "detector x Pos", "detector y Pos",
                        "detector z Pos",
                        "detector r Pos", "detector rho Pos", 
                        "detector theta Pos", "detector phi Pos", 
			"Group Index","Raw Angle", "Solid Angle",
                        "x value", "y value", "error" }; 
    String args[] ={ DSFieldString.TITLE, DSFieldString.X_UNITS,
                     DSFieldString.Y_UNITS, 
                     DSFieldString.X_LABEL, DSFieldString.Y_LABEL,
                     Attribute.INITIAL_PATH, Attribute.RUN_NUM, 
                     Attribute.PRESSURE, Attribute.TEMPERATURE,
                     Attribute.GROUP_ID, "x", "y","z",
                     "r", "R", "t", "p", 
                     null,Attribute.RAW_ANGLE, Attribute.SOLID_ANGLE,
                     "x", "y" , "e" }; 

   int nDSfields = 5;
   int nDSattr =4;   
   int nDBattr = 11;                        
   //JComboBox OutputMode; 
   //JCheckBox Sequent;//paired or sequential
   //JCheckBox UseAll;
   boolean useAll = false;
   String filename = null;
   JButton  Add, Remove, Up, Down;
   JList   unsel, sel;
   DefaultListModel unselModel, selModel;    
   int use[], Nuse[];
   DataSet DSS[];
   boolean DBSeq = false; // DB are paired 
   boolean XYcol = false;
   boolean DBcol = false;
   ExcelAdapter EA=null;
  
   public table_view( int outputMedia) //entry for non-GUI use
     { initt();
       mode = outputMedia;
     }//call Showw with args to execute

   private void initt()
    {//OutputMode = null; 
     //Sequent= null;paired or sequential
    // UseAll= null;
      Add= Remove= Up= Down= null;
     unsel =sel= null;
     unselModel= selModel= null;    
     use = Nuse= null;
     DSS = null;
     mode = 0;
   
    }
   
   public table_view( DataSet DS[])
    {
      
      DSS = DS;
     // System.out.println( "# data Sets="+DS.length);
      //setLayout( new BorderLayout() );

//Top component
      //JPanel JP = new JPanel();
      //JP.setLayout( new GridLayout( 1, 7 ));      
      
     // OK = new JButton( "OK" );
      //OK.addActionListener( this );
      //JP.add( new JLabel("Select:",SwingConstants.RIGHT));
      //Sequent = new JCheckBox( "Dat.Bl Seq" , false );
     // JP.add( Sequent );
     // UseAll = new JCheckBox( "Use All DB" , false );
     // JP.add( UseAll );
     // String Outs[]={"Console", "File" , "Table" };
     // OutputMode = new JComboBox(Outs);
     // JP.add (new JLabel("Output:",SwingConstants.RIGHT));
     // JP.add(OutputMode);
      //JP.add( OK );
      //OK.setActionListener( new myActionListener( OK ));
      //add( JP, BorderLayout.NORTH);

// Bottom components
       JPanel JP = new JPanel();
       JP.setLayout( new GridLayout( 1, 2));
// Bottom left
       JPanel JPbl = new JPanel();
       JPbl.setLayout( new BorderLayout());
       
       unselModel = new DefaultListModel();

       unsel = new JList( unselModel ); 
       Nuse = new int[ Fields.length +1];
       for( int i = 0; i< Fields.length ; i++)
          {unselModel.addElement( Fields[i] );
           Nuse[i] = i;
          }
       Nuse[Fields.length] = -1;
       JPbl.add( new JScrollPane(unsel, 
          JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ,
           JScrollPane.HORIZONTAL_SCROLLBAR_NEVER ),
                       BorderLayout.CENTER );
       JPanel JP2 = new JPanel();
       JP2.setLayout( new GridLayout(6,1));
       Add = new JButton( "Add");
       Remove = new JButton( "Remove" );
       JP2.add( new JLabel(""));
       JP2.add( new JLabel(""));
       JP2.add( Add );
       JP2.add( Remove);
       JP2.add( new JLabel(""));
       JP2.add( new JLabel(""));
       JPbl.add( JP2, BorderLayout.EAST );
       JP.add( JPbl );
       Add.addActionListener( this );
       Remove.addActionListener( this );
       JPanel JPbr = new JPanel();
       JPbr.setLayout( new BorderLayout());
       
       selModel = new DefaultListModel();
       sel = new JList( selModel ); 
       use = new int[Fields.length+1];
        Arrays.fill( use, -1);
       JPbr.add( sel, BorderLayout.CENTER );
        JP2 = new JPanel( new GridLayout( 6,1));
       Up = new JButton( "Up");
       Down = new JButton( "Down" );
       JP2.add( new JLabel(""));
       JP2.add( new JLabel(""));
       JP2.add( Up);
       JP2.add( Down);
       JP2.add( new JLabel(""));
       JP2.add( new JLabel(""));
       JPbr.add( JP2, BorderLayout.EAST ); 
       
       JP.add(JPbr);
       Up.addActionListener( this );
       Down.addActionListener( this );
       add( JP ); 

      
 
     }
   private void remove( int indx, int listt[])
     {if( indx < 0 ) return;
      if( indx >= listt.length) return;
      if( listt == null ) return;
      for( int i = indx; i+1< listt.length ; i++)
         listt[i] = listt[i+1];
      listt[ listt.length-1 ] = -1;
     }
   private void insertElementAt( int indx, int value, int list[] )
     {if( indx < 0 ) return;
      if( indx > list.length) return;
      for( int i = list.length-1; i> indx; i--)
         list[i] = list[i-1];
      list[indx] = value;
      }
   private void addElement( int value, int list[] )
     {if( list == null ) return;
      if( list.length == 0) return;
      if( list[0] == -1)
       {list[0] =value;
        return;
       }
      int k = list.length -1;   
      for( int i = list.length - 1; (i >0)&&( list[i]< 0) ; i--){k = i;}
      if( list[k] < 0)
        list[k] = value;
      else //no more values
        return;
     }
   public void show()
     {int i;
      System.out.print("unsel=");
        for( i=0; (i < Nuse.length)&&(Nuse[i] >=0); i++)
         System.out.print(Nuse[i]+":"+ Fields[Nuse[i]]+" ");
        System.out.println("");  

      System.out.print("sel=");
        for( i=0; (i < use.length)&&(use[i] >=0); i++)
         System.out.print(use[i]+":"+Fields[use[i]]+" ");
        System.out.println(""); 
    }

   public void actionPerformed( ActionEvent e )
   {if( e.getSource().equals( Add))
     { int indx = unsel.getSelectedIndex();
      
        if( indx < 0 ) return;
        String S = (String)(unsel.getSelectedValue());
        int FieldIndx = Nuse[indx];
        //unselModel.remove( indx);
        // remove( indx, Nuse );
        selModel.addElement( S );
        addElement( FieldIndx, use);
        if( indx+1 < unselModel.getSize())
           unsel.setSelectionInterval(indx+1,indx+1);
        unsel.requestFocus();
      }
     else if( e.getSource().equals( Remove ))
      { int indx = sel.getSelectedIndex();      
 
        if( indx < 0 ) return;
         String S = (String)(sel.getSelectedValue());
         int FieldIndx = use[indx];
 
         selModel.remove( indx);
         remove( indx, use);
         //unselModel.addElement( S );
         // addElement( FieldIndx, Nuse);
        if( indx < selModel.getSize())
          sel.setSelectionInterval(indx,indx);
        sel.requestFocus();
       }
      else if( e.getSource().equals( Up ))
       {int indx = sel.getSelectedIndex();
        if( indx <= 0 ) return;
        String S = (String)(sel.getSelectedValue());
        int FieldIndx = use[indx];

        selModel.remove( indx);
        remove( indx, use);
        selModel.insertElementAt( S, indx-1);
        insertElementAt( indx-1, FieldIndx , use );
        if( (indx >= 1) &&(indx -1 < selModel.getSize()))
            sel.setSelectionInterval(indx -1,indx -1);
        sel.requestFocus();
       }
      else if( e.getSource().equals( Down ))
       {int indx = sel.getSelectedIndex();
        if( indx < 0 ) return;
        if( indx+1 >= selModel.size()) return;
        String S = (String)(sel.getSelectedValue());
        int FieldIndx = use[indx];

        selModel.remove( indx);
        remove( indx, use );
        selModel.insertElementAt( S, indx + 1 );
        insertElementAt( indx + 1 , FieldIndx , use );
        if( indx +1  < selModel.getSize())
         sel.setSelectionInterval(indx + 1 , indx + 1 );
        sel.requestFocus();
       }
     /*  else if( e.getSource().equals( OK ))
       { //System.out.println( "Sequential?"+Sequent.isSelected());
          mode = OutputMode.getSelectedIndex();
          Showw( DSS, use , Sequent.isSelected(),UseAll.isSelected() );
       }
     */
     }

  public void setFileName( String filename)
   { this.filename = filename;
   }
  FileOutputStream f = null;
  JFrame  JF;
  JTable JTb;
  DefaultTableModel DTM;
  Vector V = new Vector();
  int mode=0;
  boolean startline = true;

  public void initOutput(DataSet D)
    { //mode = OutputMode.getSelectedIndex();
     startline = true;
     if( mode == 0)
       {}
     else if( mode == 1)
      {
       String S;
       if( filename == null )
          {   
            S = D.toString()+".dat";
            int k = S.indexOf(":");
            if( k >=0) 
               S = S.substring(k+1);
           }
       else
             S = filename;                        
      
       File fl = new File(S );
       try{ 
            f = new FileOutputStream( fl );
          }
       catch( IOException u){f = null;
             System.out.println("IOExon init="+u+","+
             D.toString()+".dat");}

      }
    else
     { JF = new JFrame(D.toString());
        
       JMenu JM = new JMenu( "Select");
       JMenuItem JMi = new JMenuItem( "All");
       JM.add(JMi);
       JMenuBar JMB= new JMenuBar();
       JMB.add(JM);
       JF.setJMenuBar( JMB);
       JMi.addActionListener( new MyActionListener());
       DTM = new DefaultTableModel();
       JTb= new JTable(DTM);
        EA = new ExcelAdapter( JTb );
       JF.getContentPane().add( new JScrollPane(JTb ,
                                 JScrollPane.VERTICAL_SCROLLBAR_ALWAYS ,
                               JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED  ));
       JF.setSize( 400,400 );
       JF.show();
       JF.validate();
       row = 0;
       col = 0;
       V = new Vector();
     }
   }
  int row=0;
  int col = 0;
  public void OutputField( Object res )
    {
     String S;
     if( res != null)
          S = new NexIO.NxNodeUtils().Showw(res) ;
     else
          S ="";
     //System.out.print("Output field mode="+mode);
     if( mode == 0)
       if( startline)
           System.out.print( S);
       else
            System.out.print("\t"+S);
     else if( mode == 1)
      try
        {if( f != null)
           if( startline)
           f.write( S.getBytes());
        else
           f.write(("\t"+S).getBytes());
        }
     catch(IOException ss){}
     else 
       {V.addElement( S );
        //System.out.println("add element to V"+ new NexIO.NxNodeUtils().Showw(V));
        
       } 
     startline = false;
    }
  public void OutputEndField()
   {//System.out.print("Endfield");
    if( mode == 0)
       System.out.println("");
    else if(mode ==1)
       try{if( f != null );
            f.write("\n".getBytes());
          }
       catch( IOException sss){}
    else
     {//System.out.println("Added Row"+ new NexIO.NxNodeUtils().Showw(V));
      for( int i = DTM.getColumnCount(); i < V.size(); i++)
          DTM.addColumn( new Integer( i ));
       DTM.addRow( V);
       
       V = new Vector();
      }
    startline = true;
   }
 public void OutputClose()
  {System.out.print("Close field");
   if( mode ==0 )
    {}
   else if( mode ==1)
    try{
          f.close();
          f = null;
          filename = null;
       }
    catch(IOException ss){}
   else
     V = new Vector();
   
  }

  public int[] Convertt( String fields[])
     {int Res[];
     
      if( fields == null )
        return null;
      if( fields.length <1 )
        return null;     
      Res = new int[ fields.length +1 ];
      Res[ fields.length ] = -1;     
     
      for( int i = 0; i < fields.length ; i++)
       { Res [ i ] = -1;    
        
         for( int j = 0; (j< Fields.length)&&( Res[ i ] < 0) ; j++ )                       if( Fields[j].equals(fields[i]))
              Res[i] = j;
               
         if( Res[i] < 0)
           return null;
       }
     return Res;
     }


  public void MakeHeaderInfo( DataSet DSS[] ,boolean UseAll )
    {String S;
     int i;
     NexIO.NxNodeUtils nd = new NexIO.NxNodeUtils();
     // 1st line data set name(s)
     S = "#Data Set";
     if( DSS.length > 1)
        S = S ="s";
     S = S+":";
     for( i = 0; i< DSS.length; i++)
        {S = S + DSS[i].toString();
         if( i+1 < DSS.length)
           S = S +";";
        }
     OutputField( S);
     OutputEndField();
    //Next Line are selected groups for each
    OutputField( "#Selected Groups"); OutputEndField();
    for( i = 0; i< DSS.length; i++)
     {OutputField("#     "+DSS[i].toString()+":"+ 
                nd.Showw( DSS[i].getSelectedIndices() ));
      OutputEndField();
     }
    // Next Lines are the operation logs;
    OutputField( "#Operations"); OutputEndField();
    for( i = 0; i< DSS.length ; i++)
      {DataSet DS = DSS[i];
       S = "#     "+DS.toString()+":";
       OperationLog oplog = DS.getOp_log();
       if( oplog != null)
       for( int j= 0; j< oplog.numEntries(); j++)
         { S += oplog.getEntryAt( j);
           if( j+1 < oplog.numEntries())
              S +=";";
         }
       OutputField( S); OutputEndField();
      }
    }
  public void Showw()
      { //useAll = UseAll.isSelected();
       
        Showw( DSS, use, DBSeq, useAll );
      }
  public void Showw( DataSet DSS[],int used[], boolean  DBSeq, boolean UseAll)
    {int ncols=0;
     int i=0;
     
     while(( i< used.length) &&  (used[ i ] >= 0) )
        {i++;
         ncols++;
        }
     DataHandler DH[];
     DBcol = XYcol = false;
     DH = new DataHandler[ncols];
   
    
      for( i=0; i< ncols; i++)
      {if( used[i] < nDSfields)
         DH[i] = new DSfield( args[used[i]]);
       else if( used[i] < nDSfields+nDSattr)
         DH[i] = new DSattr( args[used[i]]);       
       else if( used[i] <nDSfields+nDSattr+nDBattr)
        { DH[i] = new DBattr(args[used[i]]);
          DBcol = true;
        }
       else
         {
           DH[i] = new XYData(args[used[i]]);
           XYcol = true;
           DBcol = true;
         }
      }          
/*      i = JCBNuse.getSelectedIndex();
     if( (i < 0) || ( i >= DSS.length )) 
        {System.out.println( "No Data Sets are Selected" );
         return;
        }
*/
    initOutput( DSS[0] );
    MakeHeaderInfo( DSS , UseAll);
    for(  i = 0; i < DSS.length; i++)
    if( DBSeq)   
      {DataSet DS = DSS[i];
       
       for( int ii = 0; ii < ncols ; ii++)
          OutputField( Fields[ used[ ii ]]);
       OutputEndField();
       for( int j = 0; j< DS.getNum_entries(); j++)
         if( UseAll || DS.getData_entry( j ).isSelected() )
          {Data DB = DS.getData_entry(j);
           float xx[];
           if( XYcol)
               xx = DB.getX_scale().getXs();
           else
               xx = new float[1];
           for( int k = 0; k<xx.length; k++)
             { for (int l=0; l< ncols; l++)
                  { 
                    OutputField( DH[l].getVal( DSS, i, j,k) );
                  }
               OutputEndField();
               
             }             
          }
      }
    else //paired
     {float xvals[]; 
      xvals = null;
     
      DataSet DS = DSS[i];
      if( XYcol)
           {
            xvals = MergeXvals( 0, DS, null, UseAll );          
            }
      int n=1;
      if( xvals != null)
         n = xvals.length;
      if( n <= 0 )
         n = 1;
       int count =  0;   
       for( int j = 0; j < DS.getNum_entries(); j++)
          {Data DB = DS.getData_entry( j );
           if( UseAll || DB.isSelected())
            { 
                       
            
              for(int l = 0; l < ncols; l++)
                { 
                    if((count == 0)||
                          ( used[l]!= nDSfields+ nDSattr + nDBattr))  
                      OutputField( "Group"+DB.getGroup_ID()+":"+
                                                      Fields[ used[l]]);
                                    
                 if( used[l]== nDSfields+ nDSattr + nDBattr) 
                   { count = 1;                                        
                   }
                }
             }//if DB.isSelected                        
            }
       OutputEndField();
      for( int k = 0; k < n; k++) //xvals
       {float x =Float.NaN;
        if( xvals != null)           
           x = xvals[k];
        count = 0;
        for( int j = 0; j < DS.getNum_entries(); j++)
          {Data DB = DS.getData_entry( j );
           if(UseAll ||  DB.isSelected())
            { float xx[];
              xx = DB.getX_scale().getXs();
            
              boolean C = contains( xx, x);
              if( xvals == null)
                    C = true;
              for(int l = 0; l < ncols; l++)
                { if( C )
                    {if((count == 0)||
                          ( used[l]!= nDSfields+ nDSattr + nDBattr))  
                      OutputField( DH[l].getVal( DSS, i, j,k));
                    }
                 else 
                    OutputField("----  ");
                 if( used[l]== nDSfields+ nDSattr + nDBattr) 
                   { count = 1;                                        
                   }
                }                        
            }
          }//for
         OutputEndField();


       }
           
      }//end paired Data Blocks

    }
  private float[] MergeXvals ( int db , DataSet DS , float xvals[]
                              ,boolean UseAll )
    {if( db >= DS.getNum_entries() )
        return xvals; 
     if( db == 0 )
       { Data DB = DS.getData_entry(0 );
         XScale XX = DB.getX_scale();
         return MergeXvals( 1 , DS , XX.getXs() , UseAll );
        }
     Data DB = DS.getData_entry( db );
     if(UseAll ||  !DB.isSelected())
        return MergeXvals( db+1, DS,xvals , UseAll );
     XScale XX = DB.getX_scale();
     float xlocvals[];
     xlocvals = XX.getXs();
     float Delta = ( xvals[ xvals.length -1] -xvals[0] )/xvals.length /20.0f;
     //System.out.println("Delta = "+Delta );
     if( Delta < 0 ) Delta = 0.0f;
     int j = 0; 
     int i = 0;
     int n = 0;
  
     while( ( i < xvals.length ) ||( j < xlocvals.length ) )
       { 
         if( i >= xvals.length )
           {j++;n++;}
         else if( j >= xlocvals.length )
           {i++;n++;}

         else if( xvals[i] < xlocvals[j]-Delta )
          { i++; n++;}
        else if(xvals[i] > xlocvals[j]+Delta )
          {j++;n++;}
        else
          {i++;j++;n++;}
       }  
  
     float Res[];
     Res = new float[ n  ];
     j = 0; i = 0;
     n = 0;
     while( ( i < xvals.length ) ||( j < xlocvals.length ) )
       { if( i >= xvals.length )
           {Res[n] = xlocvals[j];j++;n++;}
         else if( j >= xlocvals.length )
           {Res[n] = xvals[i]; i++;n++;}

         else if( xvals[i] < xlocvals[j]-Delta )
          { Res[n] = xvals[i];i++; n++;}
        else if(xvals[i] > xlocvals[j]+Delta )
          {Res[n] = xlocvals[j];j++;n++;}
        else
          {Res[n] = ( xvals[i]+xlocvals[j] )/2.0f;i++;j++;n++;}
       }

      return MergeXvals ( db+1 ,  DS , Res , UseAll);

    }
  private boolean contains( float xx[], float x)
    {float delta;
     if( xx == null ) 
       return false;
     if( xx.length <= 0)
       return false;
     delta = (xx[xx.length - 1] - xx[0])/ xx.length/ 20.0f;
     // System.out.print("delta="+delta+","+x+":");
     for( int i = 0; i< xx.length; i++)
        {//if( i < 12) System.out.print(xx[i]+":");
         if( java.lang.Math.abs( xx[i] -x) < delta)
           return true;
         else if(  xx[i] > x)
            return false;
     
        }
     //System.out.println("");
     return false;
    }

  class DataHandler
    {
     String arg;
   
     public DataHandler( String argument){arg = argument;}
     public Object getVal( DataSet DSS[], int DS_index, int DB_index, 
                           int XY_index)
        {return null;}
    }


  class DSfield extends DataHandler
     {public DSfield( String arg){super(arg) ;}

      public Object getVal( DataSet DSS[], int DS_index , int DB_index, 
                                    int XY_index)
         {if( arg == null) 
               return new Integer ( DS_index );
          if( (DSS == null) ||(DS_index < 0) ) 
             return null;
          if( DS_index >= DSS.length) 
              return null;//"J$";
          DataSet D= DSS[DS_index];
          GetField SF = new GetField( D, new DSFieldString( arg));
          Object X= SF.getResult();
          if(X instanceof ErrorString)
               return null;//"K$";
          return X;
        } 
   
   }// DSfield

  class DSattr extends DataHandler
     {public DSattr( String arg){super(arg) ;}

      public Object getVal( DataSet DSS[], int DS_index,  int DB_index, 
                     int XY_index)
        {if( (DSS == null) ||(DS_index < 0) ) 
            return null;//"A$";
         if( DS_index >= DSS.length) 
             return null;//"B$";

         DataSet D= DSS[ DS_index ];
         GetDSAttribute SF = new GetDSAttribute( D, 
                                  new AttributeNameString( arg));
         Object X= SF.getResult();
         if(X instanceof ErrorString) 
            return null; //"C$"+X;
         return X;
        }    
   }//DSattr

  class DBattr extends DataHandler //Detector position args do special
     {public DBattr( String arg){super(arg) ;}

      public Object getVal( DataSet DSS[], int DS_index,  
                            int DB_index, int XY_index)
        {
          if( arg == null) 
              return new Integer( DB_index );
          if( (DSS == null) ||(DS_index < 0)||( DB_index < 0) ) 
	    return null; //"D$";
          if( DS_index >= DSS.length) 
            return null; //"E$"+DS_index+","+DSS.length;

          DataSet DS = DSS[DS_index];
        
          if( DB_index >= DS.getNum_entries()) 
             return null;
          int match=";x;y;z;r;t;p;R;".indexOf(";"+arg+";");
          String args;
          if( match>=0)
            args= Attribute.DETECTOR_POS;
          else 
            args = arg;
          GetDataAttribute SF = new GetDataAttribute( DS, 
                                              new Integer(DB_index ),
                                              new AttributeNameString( args));
          Object Result = SF.getResult();
          if( Result == null) 
             return null ;
          if( Result instanceof ErrorString) 
              return null;
          if( !(Result instanceof DetectorPosition)) 
             return Result;
          float coords[];
          if( match < 5)
            coords= ((DetectorPosition)Result).getCartesianCoords();
          else if( match <9)
            coords = ((DetectorPosition)Result).getCylindricalCoords();
          else
            coords= ((DetectorPosition)Result).getSphericalCoords();
          if( (match ==0) ||(match==6)||(match ==12)) 
             return new Float( coords[0]);
          if( (match ==2) ||(match==8)) 
             return new Float( coords[1]);
          return new Float( coords[2]);

        
        }    
   }
  class XYData extends DataHandler
    {public XYData( String arg){super(arg) ;}
 
      public Object getVal( DataSet DSS[], int DS_index,  int DB_index, 
                 int XY_index)
         {if( arg == null) 
             return new Integer( XY_index );
          if( (DSS == null) ||(DS_index < 0)||( DB_index < 0) 
                         ||(XY_index < 0 )) 
             return null;
          if( DS_index >= DSS.length) 
                return null;

          DataSet DS = DSS[DS_index];
     
          if( DB_index >= DS.getNum_entries()) 
             return null;
          Data DB = DS.getData_entry( DB_index );
          float Res[];
          if( arg.equals("x"))
             Res= (DB.getX_scale().getXs());
          else if( arg.equals("y"))
             Res = DB.getY_values();
          else if( arg.equals("e"))
             Res = DB.getErrors();
          else 
             return null; 
          if( XY_index < 0)
             return null;
          if( Res == null) 
              return null;
          if( XY_index >= Res.length) 
            return null;
          return  new Float(Res[XY_index]);
         }

   }

  public class MyActionListener implements ActionListener
    {
      public void actionPerformed( ActionEvent e)
        {JTb.setRowSelectionInterval(0, DTM.getRowCount()-1);
         JTb.setColumnSelectionInterval( 0 , DTM.getColumnCount() -1 );
        }
    }

  public static void main( String args[] )
   {JFrame JF = new JFrame("Test");
   
    IsawGUI.Util ut = new IsawGUI.Util();
    String filename = "C:\\SampleRuns\\gppd9899.run";
    if( args != null)
      if( args.length > 0)
         filename = args[0];
    DataSet DS[];
    DS =ut.loadRunfile( filename );
    System.out.println("DS length="+DS.length);
    if( DS == null )
      { System.out.println( "Error loading file ");
        System.exit( 0 );
      }
    int choice = 1;
    if( args != null) if (args.length >1)
       try{
         choice = (new Integer( args[1])).intValue();
          }
       catch( Exception uu)
         {choice = 1;
         }
    if( choice >= DS.length)
       choice = 0;
    DataSet DSS[];
    DSS = new DataSet[ 1 ];
    DSS[0] = DS[ choice]; 
    
    int i= 0;
     
    DSS[i].setSelectFlag(0,true);
    DSS[i].setSelectFlag(5,true);
    DSS[i].setSelectFlag(8,true);
      
    table_view TV = new table_view (DSS );
    JF.getContentPane().add( TV );
    JF.setSize( 400 , 300 );
    JF.show();
    JF.validate();

   }

  }
