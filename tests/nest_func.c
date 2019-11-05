#include "minic-stdlib.h"

int a;


int foo(int a){
    return a;
}

int main(){
    int b;
    a = 1;
    b = foo(foo(a));
    print_i(a);
    print_c('\n');
    print_i(b);
    print_c('\n');
}