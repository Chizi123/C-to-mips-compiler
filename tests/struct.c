struct b {
    int b;
};

struct test
{
    int i;
    int j;
    char *str;
    char *a[5];
    struct b b;
};

struct llist {
    int i;
    struct llist* next;
};

struct b a;

int strcpy(char *src, char* dest){}

int main ()
{
	struct test aaa;
//	struct test* bbb;
	struct b b;
	int d[5][10];
    int c[5];
    int i;
	b.b = 1;
	a.b=2;
	aaa.b.b = 5;
	aaa.i = 3;
	aaa.j = 4;

	d[1][0] = 10;

//    (aaa.a)[1];
//    (*(aaa.i).i)[4];
//	strcpy("Hello", aaa.str);
//	aaa.i = 4;
//	*(bbb.a[1])[0].a.a = 'c';

//	d[1][1] = 2;
//    print_i(d[1][1]);
//    print_c('\n');
//
//    i = 0;
//    while(i < 5) {
//        int j;
//        j = 0;
//        while ( j < 10) {
//            d[i][j] = 10*i+j;
//            print_i(10*i+j);
//            print_c(' ');
//            j = j+1;
//        }
//        print_c('\n');
//        i = i+1;
//    }
//    i = 0;
//    while (i < 5) {
//        int j;
//        j = 0;
//        while (j < 10) {
//            print_i(d[i][j]);
//            print_c(' ');
//            j = j+1;
//        }
//        print_c('\n');
//        i=i+1;
//    }
//
//    i = 0;
//    while (i < 5) {
//        c[i] = i;
//        i = i + 1;
//    }
//    while (i > 0) {
//        i = i - 1;
//        print_i(c[i]);
//        print_c(' ');
//    }
//    print_c('\n');
//
//    c[i] = 0; c[i+1] = 1; c[i+2] = 2; c[3] = 3; c[4] = 4;
//    print_i(c[0]); print_i(c[1]); print_i(c[2]); print_i(c[3]); print_i(c[4]); print_c('\n');

    print_i(aaa.i);
    print_c('\n');
    print_i(aaa.j);
    print_c('\n');
    print_i(aaa.b.b);
    print_c('\n');
    print_i(b.b);
    print_c('\n');
    print_i(a.b);
    print_c('\n');

	return aaa.i;
	return aaa.j;
	return 0;
}