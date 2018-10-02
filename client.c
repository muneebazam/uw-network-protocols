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

// client application main
int main(int argc, char *argv[])
{
    // declare neccesary structs and variables
    int sockfd, portno, req_code, r_port, outcome, sockfd_tcp;
    int required_args = 5; // client script requires 5 arguments
    struct sockaddr_in serv_addr, serv_addr_tcp;
    struct hostent *server;
    int buffer_len = 256; // limit buffer length to 256
    char buffer[buffer_len];
    char *server_address;
    char req_code_str[32];
    socklen_t server_len;
    char *msg;

    // handle command line arguments
    if (argc != required_args) {
        fprintf(stderr, "ERROR invalid number of arguments.\n");
        fprintf(stderr, "USAGE: ./client.sh <server address> <n_port> <req_code> <msg>\n");
        exit(1);
    } else {
        server_address = argv[1];
        portno = atoi(argv[2]);
        req_code = atoi(argv[3]);
        msg = argv[4];
        sprintf(req_code_str, "%d", req_code); 
    }

    // continously try to open a UDP socket connection
    do {
        sockfd = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
    } while (sockfd < 0);

    // configure server hostname 
    server = gethostbyname(argv[1]);
    if (server == NULL) {
        fprintf(stderr,"ERROR, no such host.\n");
        exit(1);
    }

    // socket configuration 
    bzero((char *) &serv_addr, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    bcopy((char *)server->h_addr, 
         (char *)&serv_addr.sin_addr.s_addr,
         server->h_length);
    serv_addr.sin_port = htons(portno);
    server_len = sizeof(serv_addr);

    //send the request code to the server
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

    // close UDP socket
    close(sockfd);

    // create TCP socket 
    sockfd_tcp = socket(AF_INET, SOCK_STREAM, 0);
    verify(sockfd_tcp);

    // TCP socket configuration
    bzero((char *) &serv_addr_tcp, sizeof(serv_addr_tcp));
    serv_addr_tcp.sin_family = AF_INET;
    bcopy((char *)server->h_addr, 
         (char *)&serv_addr_tcp.sin_addr.s_addr,
         server->h_length);
    serv_addr_tcp.sin_port = htons(r_port);

    // connect and handshake protocol for TCP
    if (connect(sockfd_tcp, (struct sockaddr *) &serv_addr_tcp, sizeof(serv_addr_tcp)) < 0) 
        exception("ERROR connecting");
        
    // send message string to the server
    outcome = write(sockfd_tcp, msg, strlen(msg));
    verify(outcome);

    // recieve the reversed string from the server
    bzero(buffer,256);
    outcome = read(sockfd_tcp, buffer, 255);
    verify(outcome);

    // print the reversed string, close the TCP socket and exit
    printf("CLIENT_RCV_MSG=%s\n", buffer);
    close(sockfd_tcp);
    return 0;
}