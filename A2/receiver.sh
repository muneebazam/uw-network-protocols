#!/bin/bash

#Run script for the server distributed as a part of 
#Assignment 2
#Computer Networks (CS 456)
#
#Number of parameters: 4
#Parameter:
#    $1: <network emulator hostname>
#    $2: <Emulator UDP port to receive ACKs>
#    $3: <Receiver UDP port to receive Data>
#    $4: name of file to write data to
#

./receiver $1 $2 $3 "$4"

