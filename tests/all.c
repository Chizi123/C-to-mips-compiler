#include "minic-stdlib.c"
#include "thing.c"

struct aaa {
    int a;
    int *b;
    char c;
    char *d;
    void e;
    void *f;
    struct bbb g;
    struct bbb *h;
}; //valid

struct bbb {
    int i;
}; //valid

int a;
int *b;
char c;
char *d;
void e;
void *f;
struct bbb g;
struct bbb *h;

int foo(int j, char k, struct aaa l) {
    int m;
    int *n;
    char o;
    char *p;
    void q;
    void *r;
    struct bbb s;
    struct bbb *t;
    return print_s("Hello World");
} //valid

void bar() {
    return;
}

void null(){}

int main() {
    int m;
    int *n;
    int nn[2];
    char o;
    char *p;
    char pp[2];
    void q;
    void *r;
    void rr[2];
    struct bbb s;
    struct bbb *t;
    struct bbb tt[2];

    while(1==2) {
        if (2==3)
            *n=(2+2);
        if (1==1) {
            m=m;
        } else {
            n=n;
        }
    }

    bar();
    foo(m,o,tt[0]);
    1;i;
    -1;
    i=-1;
    'c';
    "Hello";
    i&&i;
    i||i;
    i%i;
    i*i;
    i/i;
    i-i;
    i+i;
    i==i;
    i!=i;
    i<=i;
    i>=i;
    i<i;
    i>i;
    sizeof(int);
    (int) i;
}