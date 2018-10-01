/* A simple client in the internet domain connecting to server via UDP */
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h> 

// prints the appropriate error message
void exception(const char *msg)
{
    perror(msg);
    exit(1);
}

// client application main
int main(int argc, char *argv[])
{
    // declare neccesary structs and variables
    int sockfd;
    int sockfd_tcp;
    int portno;
    int req_code;
    int success;
    int required_args = 5;
    struct sockaddr_in serv_addr;
    struct sockaddr_in serv_addr_tcp;
    struct hostent *server;
    int buffer_len = 256;
    char buffer[buffer_len];
    char *server_address;
    char req_code_str[256];
    socklen_t server_len;
    char *msg;

    // check if correct arguments are passed and instantiate
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

    // test to see if command line arguments passed correctly
    printf("The server_address is %s\n", server_address);
    printf("The n_port is %d\n", portno);
    printf("the req_code is %d\n", req_code);
    printf("The msg is %s\n", msg);

    // continously try to open a UDP socket connection
    do {
        sockfd = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
        printf("testazzzzzz");
    } while (sockfd < 0);

    // configure server hostname
    server = gethostbyname(argv[1]);
    if (server == NULL) {
        fprintf(stderr,"ERROR, no such host.\n");
        exit(1);
    }

    printf("testazzzzzz + %s\n", msg);

    // socket configuration
    bzero((char *) &serv_addr, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    bcopy((char *)server->h_addr, 
         (char *)&serv_addr.sin_addr.s_addr,
         server->h_length);
    serv_addr.sin_port = htons(portno);

<<<<<<< HEAD
=======
    printf("testazzzzzz");

>>>>>>> 9c21f3ff6e3e3fa6bcd11f462bb42714a0bddfc9
    server_len = sizeof(serv_addr);
    //send the messages
    if (sendto(sockfd, req_code_str, strlen(req_code_str), 0, (struct sockaddr *) &serv_addr, server_len) < 0) {
        exception("Error sending message");
    }
    bzero(buffer,256);

    //try to receive some data, this is a blocking call
    if (recvfrom(sockfd, buffer, buffer_len, 0, (struct sockaddr *) &serv_addr, &server_len) < 0) {
        exception("Error receiving message");
    }
    int trans_port = atoi(buffer);
    //print details of the data received
<<<<<<< HEAD
    printf("Data: %s\n" , buffer);    

    // // connect to server 
    // if (connect(sockfd,(struct sockaddr *) &serv_addr,sizeof(serv_addr)) < 0) { 
    //     exception("ERROR connecting.\n");
    // }

    // // get a message from stdin and send it to server over socket
    // printf("Please enter the message: ");
    // bzero(buffer,256);
    // fgets(buffer,255,stdin);
    // success = write(sockfd, msg, strlen(msg));
    // if (success < 0) {
    //     exception("ERROR writing to socket.\n");
    // }

    // // get response from server and print it out
    // bzero(buffer,256);
    // success = read(sockfd, buffer, 255);
    // if (success < 0) {
    //     exception("ERROR reading from socket");
    // }
    // printf("Response from server: %s\n", buffer);
=======
    printf("Transaction Port: %d\n" , trans_port);   

    char confirmation[] = "Confirmed receipt of transaction port.\n";

    bzero(buffer,256);
    if (sendto(sockfd, confirmation, strlen(confirmation), 0, (struct sockaddr *) &serv_addr, server_len) < 0) {
        exception("Error sending message");
    }

    // wait to recieve ok now close socket connection
    if (recvfrom(sockfd, buffer, buffer_len, 0, (struct sockaddr *) &serv_addr, &server_len) < 0) {
        exception("Error receiving message");
    }

    printf("Server said: %s\n" , buffer);   

>>>>>>> 9c21f3ff6e3e3fa6bcd11f462bb42714a0bddfc9
    close(sockfd);

    printf("Closed the UDP socket connection with the server.\n");

    sockfd_tcp = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd_tcp < 0) {
        exception("ERROR opening socket");
    }
    bzero((char *) &serv_addr_tcp, sizeof(serv_addr_tcp));
    serv_addr_tcp.sin_family = AF_INET;
    bcopy((char *)server->h_addr, 
         (char *)&serv_addr_tcp.sin_addr.s_addr,
         server->h_length);
    printf("The target conncection port is: %d\n", trans_port);
    serv_addr_tcp.sin_port = htons(trans_port);
    if (connect(sockfd_tcp, (struct sockaddr *) &serv_addr_tcp, sizeof(serv_addr_tcp)) < 0) {
        exception("ERROR connecting");
    }
    success = write(sockfd_tcp, msg, strlen(msg));
    if (success < 0) 
         exception("ERROR writing to socket");
    bzero(buffer,256);
    success = read(sockfd_tcp, buffer, 255);
    if (success < 0) 
         exception("ERROR reading from socket");
    printf("%s\n",buffer);
    close(sockfd_tcp);

    return 0;
}
