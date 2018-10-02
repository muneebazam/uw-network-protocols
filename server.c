/* A simple server in the internet domain via UDP */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h> 
#include <sys/socket.h>
#include <netinet/in.h>
#include "helpers.c"

// server application main
int main(int argc, char *argv[])
{
    // declare neccesary structs and variables
    int udp_sockfd, sockfd_tcp, newsock_fd, tcp_sockfd;
    int req_code, bind_socket, recv_len, outcome;
    int required_args = 2; // required args for server script
    int buffer_len = 256; // fix buffer length at 256
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

    // continously try to open a UDP socket connection until one is successfully opened
    do {
        udp_sockfd = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
    } while (udp_sockfd < 0);

    // socket configuration
    bzero((char *) &serv_addr, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);

    // continously try to bind socket, incrementing portno on every failure until a port is successfully binded
    do {
        portno += 1;
        serv_addr.sin_port = htons(portno);
        bind_socket = bind(udp_sockfd, (struct sockaddr *) &serv_addr, sizeof(serv_addr));
    } while (bind_socket < 0);
    printf("SERVER_PORT=%d\n", portno);

    // listen and accept on UDP socket
    listen(udp_sockfd, 5);
    client_len = sizeof(cli_addr);

    // loop continously, keeping UDP socket always open to handle new connections
    while(1) {

        //try to receive a request form the client, this is a blocking call
        do {
            bzero(buffer,256);
            if ((recv_len = recvfrom(udp_sockfd, buffer, buffer_len, 0, (struct sockaddr *) &cli_addr, &client_len)) < 0) {
            exception("recvfrom()");
            }
        } while (atoi(buffer) != req_code);
    
        // create new TCP socket for transaction message
        sockfd_tcp = socket(AF_INET, SOCK_STREAM, 0);
        verify(sockfd_tcp);

        // socket configuration
        bzero((char *) &serv_addr_tcp, sizeof(serv_addr_tcp));
        serv_addr_tcp.sin_family = AF_INET;
        serv_addr_tcp.sin_addr.s_addr = INADDR_ANY;

        // continously try to bind socket, incrementing portno on every failure until a port is successfully binded
        do {
            portno += 1;
            serv_addr_tcp.sin_port = htons(portno);
        bind_socket = bind(sockfd_tcp, (struct sockaddr *) &serv_addr_tcp, sizeof(serv_addr_tcp));
        } while (bind_socket < 0);

        // convert portno to string and print it to stdout
        sprintf(r_port_str, "%d", portno);
        printf("SERVER_TCP_PORT=%s\n", r_port_str);

        // now reply to the client with the <r_port> value
        if (sendto(udp_sockfd, r_port_str, strlen(r_port_str), 0, (struct sockaddr*) &cli_addr, client_len) < 0) {
            exception("sendto()");
        }

        // recieve <r_port> confirmation from client, this is a blocking call
        if ((recv_len = recvfrom(udp_sockfd, buffer, buffer_len, 0, (struct sockaddr *) &cli_addr, &client_len)) < 0) {
            exception("recvfrom()");
        }

        // send back acknowledgement of confirmation to the client 
        char confirm[] = "ok";
        if (sendto(udp_sockfd, confirm, strlen(confirm), 0, (struct sockaddr*) &cli_addr, client_len) < 0) {
            exception("sendto()");
        }

        // start listening on TCP transaction port
        listen(sockfd_tcp,5);
        clilen = sizeof(cli_addr_tcp);
        newsock_fd = accept(sockfd_tcp, 
                    (struct sockaddr *) &cli_addr_tcp, 
                    &clilen);
        verify(newsock_fd);

        // Receive the message string from the client, this is a blocking call
        bzero(buffer,256);
        outcome = read(newsock_fd, buffer, 255);
        verify(outcome);
    
        // reverse the message string receieved from the client
        printf("SERVER_RCV_MSG=%s\n", buffer);
        char *message = buffer;
        reverse_string(message);

        // send the reversed string back to client
        outcome = write(newsock_fd, message, strlen(message));
        verify(outcome);

        // close the TCP connection
        // ** server is still listening on the UDP socket **
        close(newsock_fd);
        close(sockfd_tcp);
    }
    return 0; 
}