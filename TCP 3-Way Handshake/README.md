TCP 3-Way Handshake
===================

About
-----

This program features a simple client server interaction in which the client will send requests to the server to reverse strings over the network using sockets. 

It involves a two stage communication process, a negotiation (handshake) stage in which the client and server establish a connection, followed by a transaction stge in which the actual data (string) is transferred. 


Compiling and Setup
-------------------

The client and server applications can both be compiled via the 'make' command. 

To start the client application, run the client script passing the server address, server port, request code and message as command line parameters.

*E.g. ./client.sh <server_address> <server_port> <request_code> <message_string>*

To start the server application, run the server script, passing the request code on the command line.

*E.g. ./server.sh <request_code>*

* Make sure to start the server program before starting the client program

Program Flow
------------

1. Server will print a port associated with a UDP socket for negotiation

2. Client will create a socket and initiate connection to that server address/port

3. Client will send the request code to the server

4. Server will send the client another port number associated with a TCP socket for negotation IF request code is correct

5. Client will send a confirmation over the UDP socket to the server

6. Server will send an acknowledgement to the client over its UDP socket

7. Server will then close its UDP socket and start listening on its TCP socket

8. Client will then connect to the new TCP socket and send the message over that socket

9. Server will print the message to stdout and then reverse the message, sending it back over the TCP socket

10. Client will recieve the reversed string and print it to stdout

11. Client will close it's connection with the server and exit

12. Server will be always on, waiting for new connections if found


