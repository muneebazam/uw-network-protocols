/* A simple sender which transfers a file via the Go-Back-N protocol */
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h> 
#include "helpers.c"
#include <pthread.h> 

// configure emulator hostname 
void configureEmulatorHost(struct hostent **emulator, char **emulator_hostname) {
    *emulator = gethostbyname(*emulator_hostname);
    if (emulator == NULL) {
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

// A normal C function that is executed as a thread  
// when its name is specified in pthread_create() 
void *myThreadFun(void *vargp) 
{ 
    for (int i = 0; i < 100; i++) {
        printf(i);
    }
} 

// sender main
int main(int argc, char *argv[]) {

    //  UNCOMMENT THIS ENTIRE SECTION ONCE YOU FIGURE OUT PTHREADS
    // // declare necessary structs and variables
    // char *emulator_hostname;
    // char *server_address;
    // char *file_name;
    // int emulator_port;
    // int sender_port;
    // int sockfd;
    // int sockfd_tcp;
    // int WINDOW = 10;
    // int required_args = 5;
    // int portno = 5000;
    // struct hostent *server;
    // struct sockaddr_in serv_addr;
    // socklen_t server_len;

    // // handle command line arguments
    // if (argc != required_args) {
    //     fprintf(stderr, "ERROR invalid number of arguments.\n");
    //     fprintf(stderr, "USAGE: ./sender.sh <network emulator hostname> \
    //                                         <Emulator UDP port to receive data at> \
    //                                         <Sender UDP port to receive ACKs at> \
    //                                         <name of the file to transfer>\n");
    //     exit(1);
    // } else {
    //     emulator_hostname = argv[1];
    //     emulator_port = atoi(argv[2]);
    //     sender_port = atoi(argv[3]);
    //     file_name = argv[4];
    // }

    // // configure hostname and UDP socket
    // configureEmulatorHost(&server, &server_address);
    // setupSocket(&sockfd, SOCK_DGRAM);
    // bzero((char *) &serv_addr, sizeof(serv_addr));
    // serv_addr.sin_family = AF_INET;
    // bcopy((char *)server->h_addr, 
    //      (char *)&serv_addr.sin_addr.s_addr,
    //      server->h_length);
    // serv_addr.sin_port = htons(portno);
    // server_len = sizeof(serv_addr);

    // // close TCP socket and exit
    // close(sockfd_tcp);
    // return 0;

    for (int i = 0; i < 100; i++) {
        printf(i);
    }

    pthread_t thread_id; 
    printf("Before Thread\n"); 
    pthread_create(&thread_id, NULL, myThreadFun, NULL); 
    pthread_join(thread_id, NULL); 
    printf("After Thread\n"); 
    return 0;
}