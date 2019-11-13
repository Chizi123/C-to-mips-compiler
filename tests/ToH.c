// C recursive function to solve tower of hanoi puzzle
void towerOfHanoi(int n, char from_rod, char to_rod, char aux_rod)
{
    if (n == 1)
    {
        print_s("\n Move disk 1 from rod "); print_c(from_rod); print_s(" to rod "); print_c(to_rod);
        return;
    }
    towerOfHanoi(n-1, from_rod, aux_rod, to_rod);
    print_s("\n Move disk "); print_i(n); print_s(" from rod "); print_c(from_rod); print_s(" to rod "); print_c(to_rod);
    towerOfHanoi(n-1, aux_rod, to_rod, from_rod);
}

int main()
{
    int n;
    n = 4; // Number of disks
    towerOfHanoi(n, 'A', 'C', 'B');  // A, B and C are names of rods
    return 0;
}