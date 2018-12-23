Networks Assignment 1 - Socket Programming
==========================================

Compiling and Setup
-------------------

The client and server applications that I have created can both be compiled by running the 
'make' command. 

To start the client application simply run the client script by typing ./client.sh in the 
command line and passing the server address, port, request code and message.

E.g. ./client.sh <server_address> <server_port> <request_code> <message string>

To start the server application simply run the server script by tyiping ./server.sh in
the command line and passing the request code.

E.g. ./server.sh <request_code>

* Make sure to start the server script before starting the client script or else the client 
will not be able to communicate with the server

Machines
--------

The client and server applications can successfully communicate with each other if run on two
diferrent linux.student.cs.uwaterloo.ca machines and if on the same machine, as well.

When running on the same machine: pass 'localhost' or '127.0.0.1' as the server address

When running on different machines: use 'ifconfig' to find the machines public IP and use that as server address

General Flow
------------

1. Server will print a port associated with a UDP socket for negotiation

2. Client will create a socket and initiate connection to that server address/port

3. Client will send the request code to the server

4. Server will send back another port number associated with a TCP socket for negotation IF request code is correct

5. Client will send back a confirmation over the UDP socket back to the server

6. Server will send back an acknowledgement to the client over its UDP socket

7. Server will then close its UDP socket and start listening on the TCP socket

8. Client will then connect to the new TCP socket and send the message over that socket

9. Server will print the message to its stdout and then reverse the message, sending it back over the TCP socket

10. Client will recieve the reversed string and print it to its stdout

11. Client will close it's connection with the server and exit

12. Server will be always on, waiting for new connections and if found starts over from step 2  


