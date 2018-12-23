/* A simple server in the internet domain via UDP */
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

// server main
int main(int argc, char *argv[]) {

    // declare neccesary structs and variables
    int udp_sockfd, tcp_sockfd, newsock_fd, req_code, recv_len, outcome;
    int required_args = 2; // required # args for server script (including program name)
    int buffer_len = 1024; // fix buffer length at 256
    int portno = 5000; // start search at min port 5000
    socklen_t client_len, clientlen, clilen;
    char buffer[buffer_len];
    char r_port_str[32];
    struct sockaddr_in serv_addr, cli_addr, serv_addr_tcp, cli_addr_tcp;

    // handle command line arguments
    if (argc != required_args) {
        fprintf(stderr, "ERROR invalid number of arguments.\n");
        fprintf(stderr, "USAGE: ./server.sh <req_code>\n");
        exit(1);
    } else {
        req_code = atoi(argv[1]);
    }

    // set up UDP (SOCK_DGRAM) socket and start listening on it
    setupSocket(&udp_sockfd, serv_addr, &portno, SOCK_DGRAM);
    printf("SERVER_PORT=%d\n", portno);
    listen(udp_sockfd, 5);

    // loop continously, always keeping UDP socket open to handle new connections
    while(1) {

        /********************************* NEGOTIATION STAGE BEGINS ********************************************/

        // try to receive a request form the client, this is a blocking call
        do {
            bzero(buffer,256);
            client_len = sizeof(cli_addr);
            if ((recv_len = recvfrom(udp_sockfd, buffer, buffer_len, 0, (struct sockaddr *) &cli_addr, &client_len)) < 0) {
                exception("recvfrom()");
            }
        } while (atoi(buffer) != req_code);
    
        // setup TCP (SOCK_STREAM) socket
        setupSocket(&tcp_sockfd, serv_addr_tcp, &portno, SOCK_STREAM);

        // send <r_port> number to the client over UDP Socket
        sprintf(r_port_str, "%d", portno);
        printf("SERVER_TCP_PORT=%s\n", r_port_str);
        if (sendto(udp_sockfd, r_port_str, strlen(r_port_str), 0, (struct sockaddr*) &cli_addr, client_len) < 0) {
            exception("sendto()");
        }

        // recieve <r_port> confirmation from client, this is a blocking call
        if ((recv_len = recvfrom(udp_sockfd, buffer, buffer_len, 0, (struct sockaddr *) &cli_addr, &client_len)) < 0) {
            exception("recvfrom()");
        }

        // send acknowledgement of <r_port> confirmation to the client 
        char confirm[] = "ok";
        if (sendto(udp_sockfd, confirm, strlen(confirm), 0, (struct sockaddr*) &cli_addr, client_len) < 0) {
            exception("sendto()");
        }

        /********************************* NEGOTIATION STAGE ENDS ********************************************/


        /********************************* TRANSACTION STAGE BEGINS ********************************************/

        // start listening on TCP transaction port
        listen(tcp_sockfd,5);
        clilen = sizeof(cli_addr_tcp);
        newsock_fd = accept(tcp_sockfd, (struct sockaddr *) &cli_addr_tcp, &clilen);
        verify(newsock_fd);

        // Receive the message string and send the reversed string back to the client
        bzero(buffer,256);
        outcome = read(newsock_fd, buffer, 255);
        verify(outcome);
        printf("SERVER_RCV_MSG=%s\n", buffer);
        reverse_string(buffer);
        outcome = write(newsock_fd, buffer, strlen(buffer));
        verify(outcome);

        /********************************* TRANSACTION STAGE BEGINS ********************************************/

        // close the TCP connection
        close(newsock_fd);
        close(tcp_sockfd);
    }
    return 0; 
}