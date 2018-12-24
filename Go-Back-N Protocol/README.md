Go-Back-N Protocol
==================

About
-----

This program implements the Go-Back-N protocol, which can be used to transfer a text file between hosts over an unreliable network, handling network errors, packet loss and duplicates.

To achieve this, the sender sends packets to an emulator which will randomly discard/delay packets, instead of sending them directly to the receiver.


Compiling and Setup
-------------------

The emulator is an executable file which should be run before both sender and receiver programs. When running the emulator, pass the emulator send port, receiver host, receiver port, emulator recieve port, sender host, sender port, max delay (ms), discard probability and verbose mode flag as command line parameters in that order.

E.g. ./nEmulator-linux386 <emulator send port>

The sender and receiver applications can both be compiled via the 'make' command. 

To start the sender program, run the send script (sender.sh), passing the emulator address, port, request code and message on the command line.

E.g. ./sender.sh 
  
To start the receiver application, run the receiver script (receiver.sh), passing the request code on the command line.

E.g. ./receiver.sh 

* Make sure to start the receiver before starting the sender


Example Execution
-----------------

1. nEmulator 9991 127.94.48.56 9994 9993 127.43.45.754 9992 1 0.2 1
2. java Receiver 127.59.09.342 9993 9994 output.txt
3. java Sender 127.59.09.342 9991 9992 input.txt


 

