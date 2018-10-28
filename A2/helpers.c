/* helper functions for the socket application */
#include <string.h>
#include <stdlib.h>
#include <stdio.h>

// prints the appropriate error message
void exception(const char *msg)
{
    perror(msg);
    exit(1);
}

void verify(int outcome) {
    if (outcome < 0) {
        exception("ERROR");
    }
}

void reverse_string(char *str) {
    /* skip null/empty strings */
    if (str == 0 || *str == 0) return;

    /* get range */
    char *start = str;
    char *end = start + strlen(str) - 1; /* -1 for \0 */
    char temp;

    /* reverse */
    while (end > start) {
        /* swap */
        temp = *start;
        *start = *end;
        *end = temp;

        /* move */
        ++start;
        --end;
    }
}
