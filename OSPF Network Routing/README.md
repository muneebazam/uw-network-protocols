Open Shortest Path Routing
==========================

About
-----

This program contains a java implemententation for the Open Shortest Path First routing algorithm (OSPF). 
*For more information on link-state routing algorithms see https://searchnetworking.techtarget.com/definition/OSPF-Open-Shortest-Path-First*

To achieve this, a Network State Emulator (NSE) with the following hardcoded circuit database:

![](images/table%201-1.png)


Compiling and Setup
-------------------

The router application that I have created can both be compiled by running the 
'make' command. 

To start the application simply run the compiled java program while passing
the router id, emulator host address, emulator port number and router port

E.g. java Router <router_id> <emulator_host> <emulator_port> <router_port>

Machines
--------

The router application can successfully run on the same host as the emulator and on
a diferrent linux.student.cs.uwaterloo.ca host, as well.

When running on the same machine: pass 'localhost' or '127.0.0.1' as the server address

When running on different machines: use 'ifconfig' to find the machines public IP and use that as server address

Versions
--------

Make -> GNU Make 4.1 (built for x86-64-pc-linux-gnu)
Compiler -> Java 8 (major version 54)

