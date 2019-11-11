int main()
{
    int i;
    i = 0;
    while (i < 10) {
        int j;
        j = 0;
        while (j < 10) {
            print_i(i);
            print_i(j);
            print_c('\n');
            j = j + 1;
        }
        i = i + 1;
    }
    print_s((char *) "Ended loop\n");
}