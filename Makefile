# build a program from 2 files and one shared header
CC = gcc
CFLAGS = -g

all: server client

server: server.c
	gcc -o server server.c

client: client.c
	gcc -o client client.c 
