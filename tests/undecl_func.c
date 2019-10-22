struct aaa
{
    int i;
};

int foo(){}

struct aaa bar()
{
    struct aaa thing;
    thing.i = 1;
//    thing.j = 2;
    return thing;
}

int main ()
{
    foo();
    bar();
}