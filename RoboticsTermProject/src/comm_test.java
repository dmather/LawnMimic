public class comm_test
{
    public static void main(String args[])
    {
        Communication mycom = new Communication("192.168.74.131");
        System.out.println(mycom.get_stuff());
    }
}

