public class comm_test2
{
    public static void main(String args[])
    {
        Communication mycom = new Communication("192.168.74.1");
        mycom.send_stuff("hello");
    }
}

