#!/bin/bash

#Run script for client distributed as part of 
#Assignment 2
#Computer Networks (CS 456)
#Number of parameters: 4
#Parameter:
#    $1: <network emulator hostname>
#    $2: <Emulator UDP port to receive data at>
#    $3: <Sender UDP port to receive ACKs at>
#    $4: name of the file to transfer

./sender $1 $2 $3 "$4"

