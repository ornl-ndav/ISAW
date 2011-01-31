
package Operators.TOF_SCD;

import EventTools.EventList.*;


public class Formatter extends Thread
  {
    private int       first;
    private int       num;
    private String[]  string_list;
    private FloatArrayEventList3D q_list;

    public Formatter( int                   first,
                      int                   num,
                      String[]              string_list,
                      FloatArrayEventList3D q_list )
    {
      this.first       = first;
      this.num         = num;
      this.string_list = string_list;
      this.q_list      = q_list;
    }
    public void run()
    {
      for ( int i = first; i < first + num; i++ )
        string_list[i] = String.format( "%13.6e %13.6e %13.6e\n",
                      q_list.eventX(i), q_list.eventY(i), q_list.eventZ(i) );
    }
  }

