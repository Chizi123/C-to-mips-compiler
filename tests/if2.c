int foo()
{
    print_s("foo\n");
    return 0;
}

int main()
{
    int i;
    i=4;
    if (i==3 || foo()) {
        print_i(1);
    } else {
        print_i(2);
    }
    print_c('\n');

}