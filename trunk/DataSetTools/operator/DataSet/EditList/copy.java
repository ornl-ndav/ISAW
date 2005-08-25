/*
 * Created on Aug 25, 2005
 *
 */
package DataSetTools.operator.DataSet.EditList;
import DataSetTools.parameter.*;
import DataSetTools.dataset.*;
import gov.anl.ipns.Util.Messaging.*;
/**
 * @author MikkelsonR
 *
 *
 */
public class copy extends DS_EditList{

  /**
   * 
   */
  public copy() {
    super("copy a DataSet");
    setDefaultParameters();
  }
  
  public void setDefaultParameters(){
    clearParametersVector();
    addParameter( new DataSetPG("DataSet to copy from",null));
  }
  
  public Object getResult(){
     DataSet DS2= (DataSet)(getParameter(0).getValue());
     DataSet DS1= getDataSet();
     DS1.copy(DS2);
     DS1.notifyIObservers( IObserver.DATA_REORDERED );
     return null;
    
  }

}
