This directory contains basic utilities for doing tcp/udp communications. 

*  ITCPUser.java           ->INTERFACE that a user of ThreadedTCPComm must
                             implement
*  TCPComm.java            ->creates Object I/O streams given a TCP socket 

*  ThreadedTCPComm.java    ->Contains a thread that can receive an object via
                             TCP and call the ProcessData routine of it's
                             TCPUser.
*  TCPServiceInit.java     ->Used by server to listen for clients then create 
                             a ThreadedTCPComm object to handle client requests 

*  TCPCommExitClass.java   ->object that is sent through a ThreadedTCPComm 
                             connection to terminate the connection

   TCPRetrieverTest.java   ->crude test program... obsolete 

*  IUDPUser.java           ->INTERFACE that a user of UPDReceive must implement

*  UDPReceive.java         ->Thread that can receive a whole UPD packet and
                             call the ProcessData routine of it's IUPDUser.

*  UDPSend.java            ->Creates a UDP socket and sends a UDP packet.

*  ByteConvert.java        -> static methods for packing/unpacking integers
                              in an array of bytes

It also contains two components that use the above utilities to implement 
some of the capabilities of a live data server system. 

*  DASOutputTest.java      -> Simulates the DAS and sends UDP packets to a 
                              specified instrument computer
*  LiveDataServer.java     -> Runs on the instrument computer, assembles UDP
                              packets from the DAS and forms DataSets.  Uses
                              TPC to serve DataSets to clients.

TO DO LIST, and QUESTIONS:  ( 1/24/2001 )

1. How can we determine appropriate ports to use?  Currently we are using
   6087 for UDP and 6088 for TCP.  Can we continue to use these?  

2. How are TCP connections created and destroyed?  What happens if one of 
   the communicating programs is shutdown, or if a network problem interrupts
   communications?

3. How do we test this???

4. The LiveDataServer should be getting the name of the runfile from the DAS
   so that if the run changes, the server will automatically switch to a
   different run.

5. JavaDoc documentation must be added.

6. LiveDataServer should add Time attribute to each spectrum.

