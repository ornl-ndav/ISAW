

package DataSetTools.operator;
import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.viewer.Table.*;
import DataSetTools.util.*;
import java.util.*;
import DataSetTools.util.*;
public class XYDataTable  extends DataSetTools.operator.GenericSave
{  


    public XYDataTable( )
     { super( "Table x, y, error");
       setDefaultParameters();
     }
    
    public XYDataTable( DataSet DS, boolean showErrors , MediaList outputMedia,
                DataDirectoryString filename, IntListString SelectedGroups )
      { super( "Table x,y, error");
        parameters = new Vector();
        addParameter( new Parameter( "Data Set", DS ));
        addParameter( new Parameter("Show Errors ", new Boolean(showErrors) ));
        addParameter( new Parameter( "Output:", 
                      new MediaList( "Console" )));
        addParameter( new Parameter("filename ", filename));
        addParameter( new Parameter("Selected Groups", 
                                    SelectedGroups));
                                 
      }

    public void setDefaultParameters()
     {parameters = new Vector();
        addParameter( new Parameter( "Data Set", new DataSet("","") ));
        addParameter( new Parameter("Show Errors ", new Boolean( true ) ));
        addParameter( new Parameter( "Output", new MediaList("Console")));
        addParameter( new Parameter("filename ",new DataDirectoryString()));
        addParameter( new Parameter("Selected Groups", 
                                    new IntListString("1,3:8")));
    }

   public String getCommand()
     {return "Table";
     }

   public Object getResult()
    { DataSet DS = (DataSet)(getParameter( 0 ).getValue());
      boolean showerrors = ((Boolean)(getParameter(1).getValue())).
                        booleanValue();
     String output = ((MediaList)(getParameter(2).getValue())).toString();
     String filename = getParameter(3).getValue().toString();
     IntListString SelGroups = (IntListString)(getParameter(4).getValue());
     int mode = 0;
     System.out.println("output="+output);
     if( output .equals("Console"))
         mode = 0;
     else if( output.equals("File"))
         mode = 1;
     else if( output.equals("Table"))
         mode = 2;
   
     if( (mode < 0) || (mode >2))
       mode = 0;
     table_view TB = new table_view( mode );
     TB.setFileName( filename );
     DataSetOperator op = new SetField( DS,
			new DSSettableFieldString( 
                        DSFieldString.SELECTED_GROUPS), SelGroups);
     op.getResult();
     String Used[];
     if( showerrors )
        {Used = new String[3];
         Used[2] = "error";
        }
     else
        Used = new String[2];
     Used[0] = "x value"; 
     Used[1] = "y value";
     int used[];
     used = TB.Convertt( Used );
     if( used == null)
        return new ErrorString( "improper field names");
     
     DataSet DSS[];
     DSS = new DataSet[1];
     DSS[0] = DS;
     TB.Showw( DSS , used , true , false );
     return "Finished";
    }

  public Object clone()
   {XYDataTable Res = new XYDataTable();
    Res.CopyParametersFrom( this );
   return Res;
   }

public static void main( String args[])
  {System.out.println("XYDataTable");
   String Used[];
   int used[];
   Used = new String[3];
   Used[2] = "error";
   Used[0] = "xval";
   Used[1] = "yval";
   
  }
}















