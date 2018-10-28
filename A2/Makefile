# build a program from 2 files and one shared header
CC = gcc
CFLAGS = -g

all: receiver sender helpers

helpers: helpers.c
	gcc -c -o helpers helpers.c

receiver: receiver.c
	gcc -o receiver receiver.c

sender: sender.c
	gcc -o sender sender.c 
