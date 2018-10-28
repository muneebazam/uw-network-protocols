Networks Assignment 1 - Go-Back-N Protocol
==========================================

Compiling and Setup
-------------------

The sender and receiver applications that I have created can both be compiled by running the 
'make' command. 

To start the sender application simply run the sender script by typing ./sender.sh in the 
command line and passing the receiver address, port, request code and message.

E.g. ./sender.sh <host address of the network emulator>
				 <UDP port number used by the emulator to receive data from the sender>
				 <UDP port number used by the sender to receive ACKs from the emulator>
				 <name of the file to be transferred>

To start the receiver application simply run the receiver script by tyiping ./receiver.sh in
the command line and passing the request code.

E.g. ./receiver.sh <hostname for the network emulator>
				   <UDP port number used by the link emulator to receive ACKs from the receiver>
				   <UDP port number used by the receiver to receive data from the emulator>
				   <name of the file into which the received data is written>

* Make sure to start the receiver script before starting the sender script or else the sender 
will not be able to communicate with the receiver

Machines
--------

The sender and receiver applications can successfully communicate with each other if run on two
diferrent linux.student.cs.uwaterloo.ca machines and if on the same machine, as well.

When running on the same machine: pass 'localhost' or '127.0.0.1' as the receiver address

When running on different machines: use 'ifconfig' to find the machines public IP and use that as receiver address

General Flow
------------

To-Do


