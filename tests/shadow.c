#include "minic-stdlib.h"

int a;

void foo (int a) {
    int i;
    i = 0;
    while (i > 10){
        char a;
        a = 'a';
    }
    a = 1;
}

int main()
{
    int a;
    a=1;
    print_i(a);
    print_c('\n');
    {char a;
    {
    //    char a;
        a='a';
        print_c(a);
        print_c('\n');}}
    print_i(a);
    print_c('\n');
    print_i('a'=='a');
    print_c('\n');
}