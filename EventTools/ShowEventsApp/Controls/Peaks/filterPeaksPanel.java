package EventTools.ShowEventsApp.Controls.Peaks;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;

import EventTools.ShowEventsApp.Command.*;
import MessageTools.*;
import gov.anl.ipns.Parameters.FileChooserPanel;
import gov.anl.ipns.Parameters.IParameterGUI;
import gov.anl.ipns.Parameters.IntArrayPG;
import gov.anl.ipns.Util.Numeric.ClosedInterval;
import gov.anl.ipns.Util.Numeric.IntList;
import gov.anl.ipns.ViewTools.UI.FontUtil;
import gov.anl.ipns.ViewTools.UI.TextRangeUI;

public class filterPeaksPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	private MessageCenter message_center;
	
	private JTabbedPane tabPane;
	private JTable      DetectorTable;
	private FileChooserPanel fileChooser;
    JCheckBox d_unit ;
    JCheckBox q1_unit;
    JCheckBox q2_unit;
    JTextField ValueList;
    JTextField DVbyV;
    JCheckBox  OmitValues;
	public static String CLEAR ="Clear omitted Pixels";
	public static String APPLY ="Set as omitted Pixels";
    public static String CLEAR_D ="Clear omitted D(Q)Values";
    public static String APPLY_D ="Set as omitted D(Q) Values";
    
	
	public filterPeaksPanel(MessageCenter mc)
	{
		message_center = mc;
		this.setLayout(new GridLayout(1,1));

		this.add(buildPanel());
		//this.add(buildButton());
	}
	
	private JPanel buildPanel()
	{
		JPanel panel = new JPanel(new GridLayout(1,1));
		tabPane = new JTabbedPane();
		tabPane.add("Detector", buildDetectorPanel());
		tabPane.add("D or Q", buildDQPanel());
		
		panel.add(tabPane);
		return panel;
	}
	
	private JPanel buildDetectorPanel()
	{
		JPanel panel = new JPanel();
		
		panel.setLayout(  new BorderLayout() );

        fileChooser = new FileChooserPanel(
                            FileChooserPanel.LOAD_FILE, "File with Omit information");
        panel.add( fileChooser, BorderLayout.NORTH);

		String[][] Data = new String[15][3];
		for( int i=0;i<15;i++)
		   for( int j=0; j<3;j++)
		      Data[i][j] = new String("   ");
		String[] colNames = new String[]{"Detector(s)","Row(s)","Col(s)"};
		DetectorTable = new JTable( Data, colNames);

       // DetectorTable.setPreferredSize( new Dimension(100, 90) );
		JScrollPane jscr =new JScrollPane(DetectorTable);

        jscr.setBorder(  new TitledBorder( 
             new LineBorder(Color.black), "Detectors and Rows/Cols to omit") );
		panel.add( jscr, BorderLayout.CENTER);
		    
		JButton Clear = new JButton( CLEAR );
		JButton Apply = new JButton( APPLY );
		JPanel pp = new JPanel( new BorderLayout());
		pp.add( Clear, BorderLayout.WEST );
		pp.add( Apply, BorderLayout.EAST );
		panel.add(pp, BorderLayout.SOUTH);
		Clear.addActionListener( new buttonListener());
		Apply.addActionListener(  new buttonListener() );
		
		
		return panel;
	}
	
	private JPanel buildDQPanel()
	{
	    JPanel Main = new JPanel( new BorderLayout());
		JPanel panel = new JPanel();
        BoxLayout bl = new BoxLayout( panel, BoxLayout.Y_AXIS);
        panel.setLayout( bl);
        
        JPanel unitPanel = new JPanel( new GridLayout(1,3));
        d_unit  = new JCheckBox("d(Angst)",true);
        q1_unit = new JCheckBox("1/d",false);
        q2_unit = new JCheckBox("2"+FontUtil.PI+"/d",false);
        unitPanel.add(  d_unit );
        unitPanel.add( q1_unit);
        unitPanel.add( q2_unit);
        ButtonGroup BGroup = new ButtonGroup();
        BGroup.add( d_unit );
        BGroup.add(q1_unit);
        BGroup.add(q2_unit);
        unitPanel.setBorder( new TitledBorder( new LineBorder(Color.black),"Value units"));
        Main.add( unitPanel, BorderLayout.NORTH);
        
        JPanel dlistPanel = new JPanel( new GridLayout(5,2));
        OmitValues = new JCheckBox( "Omit Values \u00b1 range", true);
        JCheckBox KeepValues = new JCheckBox( "Keep Values "+"\u00b1"+" range", false);
        dlistPanel.add( OmitValues);
        dlistPanel.add( KeepValues);
        ButtonGroup omit_keep = new ButtonGroup();
        omit_keep.add( OmitValues );
        omit_keep.add( KeepValues);
        dlistPanel.add(  new JLabel("List Values") );
        ValueList = new JTextField("     ");
        dlistPanel.add( ValueList);
        
        dlistPanel.add(  new JLabel("Dvalue/value range") );
        DVbyV = new JTextField("  ");
        dlistPanel.add( DVbyV);
        for( int i=0; i< 4;i++)
           dlistPanel.add( new JLabel("") );
        
        panel.add( dlistPanel);
        
        panel.add( Box.createVerticalGlue( ));
        
        Main.add( panel, BorderLayout.CENTER );
        JPanel commPanel = new JPanel( new GridLayout(1,2));
        JButton Clear = new JButton( CLEAR_D);
        JButton Apply= new JButton( APPLY_D);
        Clear.addActionListener( new buttonListener());

        Apply.addActionListener( new buttonListener());
        commPanel.add( Clear);
        commPanel.add( Apply);
        Main.add( commPanel, BorderLayout.SOUTH);
        return Main;

	}
	
	
	private class buttonListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			if(e.getActionCommand() ==  CLEAR)
			{
			   
			   JOptionPane.showMessageDialog(  null , "<html><body><center><font size=4>"+ 
			             "This Operation has been <P>Registered<P> It will not "+
			             "take effect until<P>the next Load Events</font>"+
			             "</center></body></html>");
			   
			   message_center.send(  new Message( Commands.CLEAR_OMITTED_PIXELS, null,true) );

		        for( int i=0;i<15;i++)
		           for( int j=0; j<3;j++)
		              DetectorTable.setValueAt( "   " , i , j);
		        fileChooser.getTextField( ).setText( "       " );
			   
			   return;	
			}
			
			
			String  S = fileChooser.getTextField( ).getText( );
			if( S != null && S.trim().length() < 1)
			   S = null;
			
			if(e.getActionCommand() == APPLY)
			{
			   
			   Vector V = new Vector();
			   V.add(  S );
			   String[] Line = new String[3];
			   for ( int row = 0; row < 15; row++ )
			   {
				Line[0] = (String)DetectorTable.getValueAt(row,0);
				Line[1] = (String)DetectorTable.getValueAt(row,1);
				Line[2] = (String)DetectorTable.getValueAt(row,2);
			      int[][] id_vals = new int[3][];
			      boolean hasData = false;
			      for( int j = 0; j < 3; j++ )
			         if( Line[j] != null && Line[j].trim( ).length()>0)
			         {
			            try
			            {
			               int[] T = IntList.ToArray(Line[j].trim()) ;
			               if( T == null || T.length < 1)
			               {
			                  JOptionPane.showMessageDialog( null ,
	                                 " Format Error In row,col="+(row+1)+"/"+(j+1));
			                  return;
			               }
			               id_vals[j] = T;
			               hasData = true;
			            }catch(Exception s)
			            {
			               JOptionPane.showMessageDialog( null ,
                                 " Format Error In row, col = " + row + "/" + j );
			               return;
			            }
			         }
                                   
                  if( hasData)
                     V.add( id_vals );
			   }
			   System.out.println(gov.anl.ipns.Util.Sys.StringUtil.toString( V ));
			   
			   if( V.size() > 0)
			      message_center.send(  new Message(Commands.APPLY_OMITTED_PIXELS,
			                                           V,true) );
			   
               JOptionPane.showMessageDialog(  null , "<html><body><center><font size=4>"+ 
                         "This Operation has been <P>Registered<P> It will not "+
                         "take effect until<P>the next Load Events</font>"+
                         "</center></body></html>");
              return; 
		     }
			if( e.getActionCommand( ).equals(CLEAR_D ))
			{
			   message_center.send( new Message( Commands.CLEAR_OMITTED_DRANGE, null,true));
			   
			   ValueList.setText( "      " );
			   

               JOptionPane.showMessageDialog(  null , "<html><body><center><font size=4>"+ 
                         "This Operation has been <P>Registered<P> It will not "+
                         "take effect until<P>the next Load Events</font>"+
                         "</center></body></html>");
			   return;
			}
			
			if( e.getActionCommand( ).equals( APPLY_D ))
			{
            String SS = ValueList.getText( );
            if ( SS != null && SS.length( ) < 1 )
               return;
            String[] DD = SS.split( "," );
            float[] vs = new float[ DD.length ];
            String mess = "Improper List Input format ";
            int pos =-1;
            try
            {

               for( int i = 0 ; i < vs.length ; i++ )
               {
                  pos=i+1;
                  vs[i] = Float.parseFloat( DD[i].trim( ) );
               }
               pos = -1;
               float[] vv = new float[ vs.length * 2 ];
               mess = "Improper DV/V format";
               float ratio = Float.parseFloat( DVbyV.getText( ).trim( ) );
               if ( ratio < 0 )
               {
                  mess = "Negative DV/v not allowed";
                  JOptionPane.showMessageDialog( null , mess );
                  return;
               }
               for( int i = 0 ; i < vs.length ; i++ )
               {
                  float D = vs[i] * ratio;
                  vv[2 * i] = vs[i] - D;
                  vv[2 * i + 1] = vs[i] + D;
               }

               float mult = 1;

               if ( d_unit.isSelected( ) )
                  mult = .5f / ( float ) Math.PI;
               else if ( q1_unit.isSelected( ) )
                  mult = 2 * ( float ) Math.PI;
               for( int i = 0 ; i < vv.length ; i++ )
                  vv[i] *= mult;
               if ( d_unit.isSelected( ) )
                  for( int i = 0 ; i < vv.length ; i += 2 )
                  {
                     float save = vv[i];
                     vv[i] = 1 / vv[i + 1];
                     vv[i + 1] = 1 / save;
                  }

               Vector Res = new Vector( 2 );
               Res.addElement( OmitValues.isSelected( ) );
               Res.add( vv );
               message_center.send( new Message( Commands.APPLY_OMITTED_DRANGE ,
                     Res , true ) );

               JOptionPane
                     .showMessageDialog(
                           null ,
                           "<html><body><center><font size=4>"
                                 + "This Operation has been <P>Registered<P> It will not "
                                 + "take effect until<P>the next Load Events</font>"
                                 + "</center></body></html>" );
               
               System.out.println( gov.anl.ipns.Util.Sys.StringUtil
                     .toString( vv ) );
            } catch( Exception sss )
            {
               if(pos >=0)
                  mess +=" at "+pos;
               JOptionPane.showMessageDialog( null , mess );
               return;
            }
            
               
			}
			
		}
	}
	
   /**
    * Sends a message to the messagecenter
    * 
    * @param command
    * @param value
    */
   private void sendMessage(String command, Object value)
   {
      Message message = new Message( command,
                                     value,
                                     true );
      
      message_center.send( message );
   }
	
	public static void main(String[] args) 
	{
	      MessageCenter mc = new MessageCenter("Test Peak Filters");
	      TestReceiver tr = new TestReceiver("Testing Peak Filters");
	      
	      mc.addReceiver(tr, Commands.FILTER_DETECTOR);
	      mc.addReceiver(tr, Commands.FILTER_QD);
	      mc.addReceiver(tr, Commands.FILTER_PEAKS);
	      
	      filterPeaksPanel fp = new filterPeaksPanel(mc);
	      
	      JFrame View = new JFrame("Test Peak Options");
	      View.setBounds(10, 10, 300, 300);
	      View.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	      View.setVisible(true);
	      
	      View.add(fp);
	      
	      new UpdateManager(mc, null, 100);
	}}
