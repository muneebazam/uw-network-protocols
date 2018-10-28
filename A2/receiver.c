/* A simple receiver which receives a file via the Go-Back-N protocol */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h> 
#include <sys/socket.h>
#include <netinet/in.h>
#include "helpers.c"

// function that takes care of socket configuration
void setupSocket(int *sockfd, struct sockaddr_in addr, int *port, int SOCKET_TYPE) {

    int outcome;
    // continously try to open a UDP socket connection until one is successfully opened
    do {
        *sockfd = socket(AF_INET, SOCKET_TYPE, 0);
    } while (*sockfd < 0);

    // socket configuration
    bzero((char *) &addr, sizeof(addr));
    addr.sin_family = AF_INET;
    addr.sin_addr.s_addr = htonl(INADDR_ANY);

    // continously try to bind socket, incrementing port on every failure until a port is successfully binded
    do {
        *port += 1;
        addr.sin_port = htons(*port);
        outcome = bind(*sockfd, (struct sockaddr *) &addr, sizeof(addr));
    } while (outcome < 0);
}

// receiver main
int main(int argc, char *argv[]) {

    // declare necessary structs and variables
    char *emulator_hostname;
    char *file_name;
    int emulator_port;
    int receiver_port;
    int WINDOW = 10;
    int ack_num = 0;

    // handle command line arguments
    if (argc != required_args) {
        fprintf(stderr, "ERROR invalid number of arguments.\n");
        fprintf(stderr, "USAGE: ./server.sh <network emulator hostname>
                                            <Emulator UDP port to receive ACKs>
                                            <Receiver UDP port to receive Data>
                                            <name of file to write data to>\n");
        exit(1);
    } else {
        emulator_hostname = argv[1];
        emulator_port = atoi(argv[2]);
        receiver_port = atoi(argv[3]);
        file_name = argv[4];
    }

    // set up UDP (SOCK_DGRAM) socket and start listening on it
    setupSocket(&udp_sockfd, serv_addr, &portno, SOCK_DGRAM);
    printf("SERVER_PORT=%d\n", portno);
    listen(udp_sockfd, 5);

    // loop continously, always keeping UDP socket open to handle new connections
    while(1) {
        printf("its lit.\n");
    }

    return 0; 
}