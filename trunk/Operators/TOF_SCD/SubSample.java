/* 
 * File: SubSample.java
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA.
 *
 *  Last Modified:
 * 
 *  
 */

package Operators.TOF_SCD;

import java.io.File;
import java.util.Vector;

import gov.anl.ipns.Parameters.LoadFilePG;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;
import DataSetTools.components.ParametersGUI.JParametersDialog;
import DataSetTools.dataset.*;
import DataSetTools.operator.Generic.Save.GenericSave;
import NexIO.Write.*;
import NexIO.Write.NexApi.NexWriteNode;
import DataSetTools.operator.DataSet.Math.Analyze.*;
import DataSetTools.retriever.NexusRetriever;
import gov.anl.ipns.Parameters.*;
public class SubSample extends GenericSave {

	private static final String TITLE ="SubSampled Data";
	private static final String COMMAND ="SubSample";
	public SubSample() {
		super( TITLE );
		setDefaultParameters();
	}

	/**
	 * Creates a NeXus file in outputFileName from the data sets in the
	 *  inputFileName, subsammpled as indicated by the parameters.
	 *
	 * @param inputFileName   The input filename
	 * @param outputFileName   The output Nexus filename 
	 * @param rowGrouping      Number of rows that are grouped(2 only)
	 * @param colGrouping      Number of cols that are grouped(2 only)
	 * @param startTime        -1 if time scale stays same, otherwise minTime
	 * @param endTime          end Time for the rebinned time data
	 * @param firstBinLength   The length of the first bin
	 * @param isLog            The rebinned time is logarithmic if true 
	 *                         otherwise it will be linear
	 * @return  null or an ErrorString
	 * 
	 * NOTE: All data sets will be placed in one NXentry, so the inputFileName
	 * must be a file with only ONE NXentry.
	 */
	public static Object sub_sample( String  inputFileName, 
			                       String   outputFileName,
			                       int      rowGrouping, 
			                       int      colGrouping,
			                       int      timeGrouping,
			                       float    startTime,
			                       float    endTime,  
			                       float    firstBinLength,
			                       boolean   isLog){
		
		
		NexWriteNode topNode;
		String errorMessage="";
		if( inputFileName == null || outputFileName == null||
				inputFileName.length() < 1 || outputFileName.length() < 1)
			return new ErrorString("a Filename is null or empty" );
		File F = new File(outputFileName);
		if( F.exists())
		   F.delete();
		F = null;
		NexusRetriever ret = new NexusRetriever( inputFileName );	
		int ndataSets = ret.numDataSets();
		errorMessage = ret.getErrorMessage();
		if((errorMessage != null && errorMessage.length()>0)|| ndataSets < 1 )
			return new ErrorString("Input File "+ inputFileName +"improper");
		
		topNode = new NexWriteNode( outputFileName);
		if( topNode.getErrorMessage().length() > 0)
			return new ErrorString(" Cannot create "+outputFileName+" :"+
					 topNode.getErrorMessage());
		
		NxWriteNode entryNode = topNode.newChildNode("entry", "NXentry");
		//Write global attributes
		int inst_type =  DataSetTools.instruments.InstrumentType.TOF_SCD;
        NxWriteEntry Wentry = new NxWriteEntry( inst_type );
                 
		NxWriteNode instrNode = entryNode.newChildNode("instrument", "NXinstrument");
		for( int i=0; i< ndataSets; i++){
			DataSet DD = ret.getDataSet(i ) ;
			
			boolean isMonitor = true;
			if( !AttrUtil.getDSType(DD).equals(Attribute.MONITOR_DATA)){
				String Res= FixUpGrid(DD);
				if( Res== null || Res.length() > 1)
					errorMessage +=Res +";";
				isMonitor = false;
				
			}
                        
			Object Result= null;
			if( startTime >=0 )
				if( isLog){
					Result = (new ResampleOnGeometricProgression( DD, 
							startTime,endTime,
							firstBinLength, false)).getResult();
				}else{
					int nbins =(int)( (endTime-startTime)/firstBinLength);
					Result = (new ResampleDataSet(DD,startTime, nbins*firstBinLength,
							nbins, false)).getResult();
				}

			if(Result instanceof ErrorString)
				errorMessage +=Result.toString()+";";
			if( !isMonitor){
				
				NxWriteData dataWrite = new NxWriteData( inst_type);
				dataWrite.write = true;
				if( dataWrite.processDS(entryNode, instrNode,  DD, true))
				   errorMessage +=dataWrite.getErrorMessage();

            
				   
			}else{
				NxWriteMonitor monitorWrite = new NxWriteMonitor( inst_type);
				for( int kk=0; kk< DD.getNum_entries() ;kk++){
					NxWriteNode monNode = entryNode.newChildNode( "monitor"+kk, "NXmonitor");
					if( monitorWrite.processDS(monNode, DD,kk))
						errorMessage += monitorWrite.getErrorMessage();
					monNode.write();	

	           
				}
				
			}
		   DD = null;	
		}
		

		DataSet D1 = ret.getDataSet(Math.min(1,  ndataSets-1));

		if( Wentry.processDS( entryNode, D1))
			errorMessage += Wentry.getErrorMessage();
		
		NxWriteInstrument instrWrite = new NxWriteInstrument( inst_type);
		if(instrWrite.processDS( instrNode, D1))
				errorMessage += instrWrite.getErrorMessage();
		NxWriteSample sampWrite = new NxWriteSample( inst_type);
		if(sampWrite.processDS(entryNode, D1))
		   errorMessage +=sampWrite.getErrorMessage()+";";
		topNode.write();
		topNode.close();
		if( errorMessage == null || errorMessage.length()<1)
		   return "Wrote Nexus file "+outputFileName;
		
		return errorMessage;
	}
	
	private static String FixUpGrid( DataSet DS ){
	       int[] IDs = Grid_util.getAreaGridIDs( DS);
	       if( IDs == null)
	          return "No Data Grids in this Data Set";
	       int groupID = DS.getData_entry(0).getGroup_ID();
	       Vector<Data> saves = new Vector<Data>( DS.getNum_entries()/4);
	       for( int i=0; i < IDs.length; i++){
	          
	          IDataGrid grid = Grid_util.getAreaGrid( DS, IDs[i]);
	          if( grid instanceof RowColGrid){
	        	  grid =RowColGrid.getUniformDataGrid((RowColGrid)grid, .01f);
	        	  if( grid == null  || !(grid instanceof UniformGrid))
	        		  return "Could Not get Uniform Grid";
	          }
	          
	          if( !grid.isData_entered())
	             grid.setData_entries( DS );
	          
	          UniformGrid grid1;
	          
	          if( 2*(grid.num_cols()/2)!= grid.num_cols())
	             return "Number of rows not divisible by 2";
	          
	          if( 2*(grid.num_rows()/2)!= grid.num_rows())
	             return "Number of cols not divisible by 2";
	          if( grid.position() == null || grid.x_vec() == null)
	             return "grid is in improper format";
	          if( grid.y_vec()== null )
	             return  "grid is in improper format";
	          grid1= new UniformGrid(grid.ID(), grid.units(),grid.position(),
	                    grid.x_vec(),grid.y_vec(),grid.width(),grid.height(),
	                    grid.depth(),grid.num_rows()/2,grid.num_cols()/2);
	          
	          
	          for( int row =1; row<= grid1.num_rows();row++ )
	             for( int col=1; col<= grid1.num_cols();col++){
	                
	                int r = 1+(row-1)*2;
	                int c = 1+(col-1)*2;
	                
	                Data DB = grid.getData_entry( r,c);
	                
	                float[] yvalues = DB.getY_values();
	                
	                Data DB1=grid.getData_entry(r, c+1);
	                Data DB2=grid.getData_entry(r+1, c+1);
	                Data DB3=grid.getData_entry(r+1, c);
	                
	                for( int t=0; t< yvalues.length; t++){
	                   
	                   yvalues[t] += DB1.getY_values()[t];
	                   yvalues[t] += DB2.getY_values()[t];
	                   yvalues[t] += DB3.getY_values()[t];
	                   
	                }
	                //DS.removeData_entry_with_id( DB1.getGroup_ID());
	               // DS.removeData_entry_with_id( DB2.getGroup_ID());
	               // DS.removeData_entry_with_id( DB3.getGroup_ID());
	                
	                DB.setAttribute( new PixelInfoListAttribute( Attribute.PIXEL_INFO_LIST,
	                         new PixelInfoList( new DetectorPixelInfo( DB1.getGroup_ID(),
	                         (short) row,(short)col, grid1))));
	                DB.setGroup_ID( groupID++ );
	                saves.addElement( DB);
	             }
	          grid1.setData_entries( DS );
	          
	       }//for IDs
	       DS.removeAll_data_entries();
	       for( int i=0; i< saves.size(); i++)
	          DS.addData_entry(saves.elementAt(i));
	          
	       return "";
	    }
	public Object getResult() {
	   String  inputFileName =getParameter(0).getValue().toString(); 
      String   outputFileName=getParameter(1).getValue().toString(); 
      int      rowGrouping=  ((IntegerPG)getParameter(2)).getintValue(); 
      int      colGrouping = ((IntegerPG)getParameter(3)).getintValue();
      int      timeGrouping = ((IntegerPG)getParameter(4)).getintValue();
      float    startTime=      ((FloatPG)getParameter(5)).getfloatValue();
      float    endTime= ((FloatPG)getParameter(6)).getfloatValue(); 
      float    firstBinLength= ((FloatPG)getParameter(7)).getfloatValue();
      boolean   isLog =((BooleanPG)getParameter(8)).getbooleanValue();
      
      return SubSample.sub_sample(inputFileName, outputFileName, rowGrouping,
            colGrouping, timeGrouping, startTime, endTime, firstBinLength, isLog);
	}

	
	public String getCommand() {
		
		return COMMAND;
	}

	
	public String getDocumentation() {
		
	   StringBuffer Res = new StringBuffer(10*50);
      Res.append("This operator groups rows(currently only 2) and columns(only 2)"+
                  "and \n");
      Res.append("and time channels. Time channels have a choice to be "+
                    "rebinned.\n");
      Res.append("@param inputFileName  input Nexus filename\n");
      Res.append("@param outputFileName output Nexus filename and not the "+
                "same as the inputFileName\n");
      Res.append("@param     rowGrouping  the number of rows to be grouped "+
            "Only 1 or 2 are currently supported. Groups are disjoint\n");
      Res.append("@param     colGrouping,  colGrouping  the number of columns  "+
            "to be grouped. Only 1 or 2 are currently supported. Groups are disjoint\n");
      Res.append("@param     timeGrouping,  -1 or the number of time bins(not supported)  ");
      Res.append("@param    startTime  The start time for new time scale or -1 "+
            "for no rebinning\n");
      Res.append("@param   endTime The end time for new time scale \n"); 
      Res.append("@param   firstBinLength the length of the first bin if there"
            +" is a new time scale\n");
      Res.append("@param   isLog true if the new time scale is logarithmic "+
            "otherwise it will be linear\n");
      Res.append("@return   null or an error string\n");
      return Res.toString();
	}

	
	public void setDefaultParameters() {
	   this.clearParametersVector();
	   
	   addParameter(new LoadFilePG("Input File",System.getProperty( "Data_Directory"))); 
	   ((LoadFilePG)getParameter(0)).setFilter( new NexIO.NexusfileFilter() );
	   addParameter(new SaveFilePG("Output File",System.getProperty( "Data_Directory"))); 
      ((SaveFilePG)getParameter(1)).setFilter( new NexIO.NexusfileFilter() );
	   addParameter(new IntegerPG("row grouping",2)); 
      addParameter(new IntegerPG("col grouping",2));
      addParameter(new IntegerPG("Time grouping",1));

      addParameter(new FloatPG("start time(us) or -1(no rebin)",-1)); 
      addParameter(new FloatPG("end time(us)",16666)); 
      addParameter(new FloatPG("Length 1st bin(us)",4)); 
      addParameter(new BooleanPG("Log time binning?", false));
	}

	@Override
	public String[] getCategoryList() {
		// TODO Auto-generated method stub
		return DataSetTools.operator.Operator.FILE_SAVE;
	}

	/**
	 * Application to run this "operator"
	 * @param args
    *    arg 1- Input filename(Nexus)");
    *    arg 2- Output filename(Nexus)");
    *    
    *    The following are optional defauls(in parens)");
    *    arg 3- row Grouping(2) Must be 1 or 2");
    *    arg 4- col grouping(2) Must be 1 or 2");
    *    arg 5- time grouping( not used)");
    *    arg 6- min time for binning(-1 means no rebinning)");
    *    arg 7- max time for binning(100000)");
    *    arg 8- bin width first bin(40)");
    *    arg 9-log binning( false)");
	 */
	public static void main(String[] args) {
	   DataSetTools.util.SharedData ut = new DataSetTools.util.SharedData();
	   
	   if( args == null || args.length < 2){
	      
	      JParametersDialog jp=(new JParametersDialog( new SubSample(),null, null, null));
	     
	   }else{
	   
		String filename =args[0];
		String outfile = args[1];
		int rowGroup = 2;
		int colGroup = 2;
		int timeGroup = 1;
		float minTime = -1;
		float maxTime = 10000;
		float firstBin = 40;
		boolean isLog = false;
		try{
		if( args.length >=3)
		   rowGroup = Integer.parseInt(  args[2] );
      if( args.length >=4)
         colGroup = Integer.parseInt(  args[3] );
      if( args.length >=5)
         timeGroup = Integer.parseInt(  args[4] );
      if( args.length >=6)
         minTime = Float.parseFloat(  args[5] );
      if( args.length >=7)
         maxTime = Float.parseFloat(  args[6] );
      if( args.length >=8)
         firstBin = Float.parseFloat(  args[7] );
      if( args.length >=9)
         isLog = Boolean.parseBoolean(  args[8] );
		}catch( Exception ss){
		   System.out.println(" arguments do not parse correctly ");
		   System.exit( 0 );
		}
		System.out.println("Total Result="+SubSample.sub_sample(filename,  
		         outfile,rowGroup,colGroup,timeGroup, minTime,maxTime,
		         firstBin, isLog));
		 System.exit(0);  
	   }
      

	}

}
