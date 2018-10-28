/* A simple client in the internet domain connecting to server via UDP */
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h> 
#include "helpers.c"

// configure server hostname 
void configureHost(struct hostent **server, char **server_address) {
    *server = gethostbyname(*server_address);
    if (server == NULL) {
        fprintf(stderr,"ERROR, no such host.\n");
        exit(1);
    }
}

// continously try to open a socket connection of SOCKET_TYPE
void setupSocket(int *sockfd, int SOCKET_TYPE) {
    do {
        *sockfd = socket(AF_INET, SOCKET_TYPE, 0);
    } while (*sockfd < 0);
}

// client main
int main(int argc, char *argv[]) {

    // declare neccesary structs and variables
    int sockfd, portno, r_port, outcome, sockfd_tcp;
    int required_args = 5; // required # args for client script (including program name)
    struct sockaddr_in serv_addr, serv_addr_tcp;
    struct hostent *server;
    int buffer_len = 1024; // limit buffer length to 1024
    char buffer[buffer_len];
    char *server_address;
    char req_code_str[256];
    socklen_t server_len;
    char *message;

    // handle command line arguments
    if (argc != required_args) {
        fprintf(stderr, "ERROR invalid number of arguments.\n");
        fprintf(stderr, "USAGE: ./client.sh <server address> <n_port> <req_code> <msg>\n");
        exit(1);
    } else {
        server_address = argv[1];
        portno = atoi(argv[2]);
        message = argv[4];
        sprintf(req_code_str, "%d", atoi(argv[3])); 
    }

    // configure hostname and UDP socket
    configureHost(&server, &server_address);
    setupSocket(&sockfd, SOCK_DGRAM);
    bzero((char *) &serv_addr, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    bcopy((char *)server->h_addr, 
         (char *)&serv_addr.sin_addr.s_addr,
         server->h_length);
    serv_addr.sin_port = htons(portno);
    server_len = sizeof(serv_addr);

    /********************************* NEGOTIATION STAGE BEGINS ********************************************/

    // send the request code to the server
    if (sendto(sockfd, req_code_str, strlen(req_code_str), 0, (struct sockaddr *) &serv_addr, server_len) < 0) {
        exception("Error sending message");
    }

    // block until we recieve a response from the server
    bzero(buffer, 256);
    if (recvfrom(sockfd, buffer, buffer_len, 0, (struct sockaddr *) &serv_addr, &server_len) < 0) {
        exception("Error receiving message");
    }

    // convert <r_port> to int and send a confirmation back to the server
    r_port = atoi(buffer);
    if (sendto(sockfd, buffer, strlen(buffer), 0, (struct sockaddr *) &serv_addr, server_len) < 0) {
        exception("Error sending message");
    }

    // block until we receive acknowledgement from the server
    if (recvfrom(sockfd, buffer, buffer_len, 0, (struct sockaddr *) &serv_addr, &server_len) < 0) {
        exception("Error receiving message");
    }

    /********************************* NEGOTIATION STAGE ENDS ********************************************/

    // close UDP socket and configure a TCP socket
    close(sockfd);
    setupSocket(&sockfd_tcp, SOCK_STREAM);
    bzero((char *) &serv_addr_tcp, sizeof(serv_addr_tcp));
    serv_addr_tcp.sin_family = AF_INET;
    bcopy((char *)server->h_addr, 
         (char *)&serv_addr_tcp.sin_addr.s_addr,
         server->h_length);
    serv_addr_tcp.sin_port = htons(r_port);

    /********************************* TRANSACTION STAGE BEGINS ********************************************/

    // connect and handshake protocol for TCP
    if (connect(sockfd_tcp, (struct sockaddr *) &serv_addr_tcp, sizeof(serv_addr_tcp)) < 0) {
        exception("ERROR connecting");
    }

    // send message string and receive the reversed string from the server
    outcome = write(sockfd_tcp, message, strlen(message));
    verify(outcome);
    bzero(buffer,256);
    outcome = read(sockfd_tcp, buffer, 255);
    verify(outcome);
    printf("CLIENT_RCV_MSG=%s\n", buffer);

    /********************************* TRANSACTION STAGE ENDS ********************************************/

    // close TCP socket and exit
    close(sockfd_tcp);
    return 0;
}