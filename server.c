/* A simple server in the internet domain via UDP */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h> 
#include <sys/socket.h>
#include <netinet/in.h>

// prints the appropriate error message
void exception(const char *msg)
{
    perror(msg);
    exit(1);
}

// server application main
int main(int argc, char *argv[])
{
    // declare neccesary structs and variables
    int udp_sockfd;
    int tcp_sockfd;
    int newsockfd;
    int portno = 5000;
    int req_code;
    int bind_socket;
    int recv_len;
    int success;
    int required_args = 2;
    int buffer_len = 256;
    socklen_t client_len;
    socklen_t client_len_tcp;
    char buffer[buffer_len];
    char r_port_str[10];
    struct sockaddr_in serv_addr;
    struct sockaddr_in cli_addr;
    struct sockaddr_in serv_addr_tcp;
    struct sockaddr_in cli_addr_tcp;

    // check if correct arguments are passed and instantiate
    if (argc != required_args) {
        fprintf(stderr, "ERROR invalid number of arguments.\n");
        fprintf(stderr, "USAGE: ./server.sh <req_code>\n");
        exit(1);
    } else {
        req_code = atoi(argv[1]);
    }

    // test to see if command line arguments passed correctly
    printf("The req_code is: %d\n", req_code);

    // continously try to open a UDP socket connection
    do {
        udp_sockfd = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
    } while (udp_sockfd < 0);

    // socket configuration
    bzero((char *) &serv_addr, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);

    // continously try to bind socket, incrementing portno on failure
    do {
        portno += 1;
        serv_addr.sin_port = htons(portno);
        bind_socket = bind(udp_sockfd, (struct sockaddr *) &serv_addr, sizeof(serv_addr));
    } while (bind_socket < 0);
    printf("SERVER_PORT=%d\n", portno);

    // listen and accept on socket
    listen(udp_sockfd, 5);
    client_len = sizeof(cli_addr);
    //try to receive some data, this is a blocking call
    do {
        bzero(buffer,256);
        if ((recv_len = recvfrom(udp_sockfd, buffer, buffer_len, 0, (struct sockaddr *) &cli_addr, &client_len)) < 0) {
           exception("recvfrom()");
        }
        //print details of the data received
        printf("Data: %s\n" , buffer);
    } while (atoi(buffer) != req_code);
    
    // create new TCP socket 
    tcp_sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (tcp_sockfd < 0) {
       exception("ERROR opening socket");
    }
    bzero((char *) &serv_addr_tcp, sizeof(serv_addr_tcp));
    serv_addr_tcp.sin_family = AF_INET;
    serv_addr_tcp.sin_addr.s_addr = INADDR_ANY;
    do {
       portno += 2;
       serv_addr.sin_port = htons(portno);
      bind_socket = bind(tcp_sockfd, (struct sockaddr *) &serv_addr_tcp, sizeof(serv_addr_tcp));
    } while (bind_socket < 0);
    sprintf(r_port_str, "%d", portno);

    //now reply with the <r_port> value
    if (sendto(udp_sockfd, r_port_str, strlen(r_port_str), 0, (struct sockaddr*) &cli_addr, client_len) < 0) {
        exception("sendto()");
    }

    //  // wait off for this just deal with creation before udp reply
    // listen(tcp_sockfd,5);
    // client_len_tcp = sizeof(cli_addr_tcp);
    //  newsockfd = accept(tcp_sockfd, 
    //              (struct sockaddr *) &cli_addr_tcp, 
    //              &client_len_tcp);
    //  if (newsockfd < 0) 
    //       error("ERROR on accept");
    //  bzero(buffer,256);
    //  success = read(newsockfd,buffer,255);
    //  if (success < 0) error("ERROR reading from socket");
    //  printf("Here is the message: %s\n", buffer);
    //  success = write(newsockfd,"I got your message",18);
    //  if (success < 0) error("ERROR writing to socket");
    //  close(newsockfd);
         

    // // read client message into buffer and print to stdout
    // bzero(buffer,256);
    // success = read(newsockfd,buffer,255);
    // if (success < 0) {
    //     exception("ERROR reading from socket.\n");
    // }
    // printf("Here is the message: %s.\n", buffer);

    // // write acknowledgement back to client and close connection
    // success = write(newsockfd, "I got your message.\n", 18);
    // if (success < 0) {
    //     exception("ERROR writing to socket.\n");
    // }
    close(udp_sockfd);
    close(tcp_sockfd);
    //close(newsockfd);
    return 0; 
}