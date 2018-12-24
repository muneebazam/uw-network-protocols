Go-Back-N Protocol
==================

About
-----

This program implements the Go-Back-N protocol, which can be used to transfer a text file between hosts over an unreliable network, handling network errors, packet loss and duplicates.

To achieve this, the sender and receiver both send packets to an emulator which will randomly discard/delay packets, instead of directly connecting to each other.


Compiling and Setup
-------------------

The emulator is an executable file which should be run before both sender and receiver programs. When running the emulator, pass the emulator send port, receiver host, receiver port, emulator recieve port, sender host, sender port, max delay (ms), discard probability [0-1], and verbose mode flag as command line parameters in that order.

E.g. ./nEmulator-linux386 <emulator_send_port> <receiver_host> <receiver_port> <emulator_receive_port> <sender_host> <sender_port> <max_delay> <discard_probability> <verbose_mode_flag> 

The sender and receiver applications can both be compiled via the 'make' command. 

To start the sender program, run the sender program passing the emulator address, emulator send port, sender port and input file as command line parameters.

E.g. java Sender <emulator_host> <emulator_sender_port> <sender_port> <input_file>
  
To start the receiver application, run the receiver program passing the emulator address, emulator receive port, receiver port and output file as command line parameters.

E.g. java Receiver <emulator_host> <emulator_receive_port> <receiver_port> <output_file>

* Make sure to start the receiver before starting the sender


Example Execution
-----------------

1. ./nEmulator-linux386 9991 127.94.48.56 9994 9993 127.43.45.754 9992 1 0.2 1
2. java Receiver 127.59.09.342 9993 9994 output.txt
3. java Sender 127.59.09.342 9991 9992 input.txt


 

