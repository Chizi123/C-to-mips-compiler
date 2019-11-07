int test()
{
    char i;
    i = read_c();
	if(i=='a') {
	    int i;
		if (i==2)
			if (i==3)
				return 1;
			else
				return 2;
		else
			return 3;
	} else if (i==2) {
	    void *i;
		return 4;
	} else
		return 5;
}

int main()
{
    print_i(test());
    print_c('\n');
}