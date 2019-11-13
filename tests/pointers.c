int main()
{
    int *i;
    int *j;
    i = (int *) mcmalloc(sizeof(int));
    *i = 4;
    print_i(*i);
    print_c('\n');

    j = (int *) mcmalloc(sizeof(int));
    *j = 567;
    print_i(*j);
    print_c('\n');
}