package EventTools.ShowEventsApp.Command;




public class UBwTolCmd
{

   float[][]UBT;
   float OffIntMax;
   
   public UBwTolCmd( float[][]UBT,  float OffIntMax)
   {

      this.UBT = UBT;
      this.OffIntMax = OffIntMax;
   }
   
   public float[][] getUB()
   {
      return UBT;
   }
   
   public float getOffIntMax()
   {
      return OffIntMax;
   }
   
   public String toString()
   {
      return "\nUB Transp:"+gov.anl.ipns.Util.Sys.StringUtil.toString( UBT,true )+
             "\n Max Displacement from Integer:"+ OffIntMax+"\n";
   }
}
