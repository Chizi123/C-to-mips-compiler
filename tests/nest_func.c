#include "minic-stdlib.h"

int *a;


int foo(int a){
    return a;
}

int main(){
    int b;
    *a = 1;
    b = foo(foo(*a));
}