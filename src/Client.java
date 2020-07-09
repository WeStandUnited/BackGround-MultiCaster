import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Scanner;


public class Client {

    public static void SetLedger(long ledger) throws IOException {

        FileWriter myWriter = new FileWriter("LedgerClient.txt");
        String s=String.valueOf(ledger);
        myWriter.write(s);
        myWriter.close();


    }
    public static long getCurrentLedger() throws FileNotFoundException {

        File file =
                new File("LedgerClient.txt");
        Scanner sc = new Scanner(file);


        return sc.nextLong();
    }


    public static void WriteBytes(byte [] b,long ledger) throws IOException {
        File file = new File("Image"+ledger+".jpg");
        RandomAccessFile accessFile = new RandomAccessFile(file,"rw");
        accessFile.write(b);

        accessFile.close();


    }

    public static void WriteArrayList(ArrayList<DatagramPacket>data,File file) throws IOException {
        RandomAccessFile randaccess = new RandomAccessFile(file,"rw");
        for (long i = 0; i <data.size() ; i+=1024) {
            randaccess.seek(i);
            ByteBuffer buffer = ByteBuffer.allocate(data.get((int)i).getLength());
            buffer.put(data.get(((int) i)).getData());
            buffer.flip();
            buffer.getShort();
            buffer.getLong();

            byte[] bytes = new byte[data.get((int)i).getLength()-10];


            buffer.get(bytes, 0, bytes.length);
            randaccess.write(bytes);

        }

    }
    public static void BashCommand(String filename) throws IOException {
        //gsettings set org.gnome.desktop.background picture-uri file:///home/cj/csc445/Project3/
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("gsettings set org.gnome.desktop.background picture-uri file:///home/cj/csc445/Project3/"+filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void WindowsCommand(String filename) throws IOException {
        Runtime.getRuntime().exec("python DesktopChanger.py "+filename);

    }

    public static void main(String[] args) throws Exception {

        System.exit(1);
        int mcPort = 2770;
        String mcIPStr = "230.1.1.1";
        MulticastSocket mcSocket = null;
        InetAddress mcIPAddress = null;
        mcIPAddress = InetAddress.getByName(mcIPStr);
        mcSocket = new MulticastSocket(mcPort);
        System.out.println("Multicast Receiver running at:"
                + mcSocket.getLocalSocketAddress());
        mcSocket.joinGroup(mcIPAddress);


//        DatagramPacket Kpacket = new DatagramPacket(new byte[69], 69);
//        System.out.println("Waiting for a  multicast key...");
//        mcSocket.receive(Kpacket);
//        System.out.println("[Recieved]!");
//        ByteBuffer keybuff = ByteBuffer.allocate(69);
//        keybuff.put(Kpacket.getData());
//        keybuff.flip();
//        SecretKeySpec keySpec = new SecretKeySpec(keybuff.array(),"AES");


        short opcode = -1;

        while (opcode != -2) {

            DatagramPacket packet = new DatagramPacket(new byte[18], 18);
            System.out.println("Waiting for a  multicast message...");
            mcSocket.receive(packet);
            System.out.println("[Recieved]!");


            ByteBuffer byteBuffer = ByteBuffer.allocate(2 + 8 + 8);
            System.out.println(new String(packet.getData()));
            byteBuffer.put(packet.getData());
            byteBuffer.flip();


            opcode = byteBuffer.getShort();
            long ledgernum = byteBuffer.getLong();
            SetLedger(ledgernum);
            long filesize = byteBuffer.getLong();
            System.out.println("opcode:" + opcode);
            System.out.println("ledgernum:" + ledgernum);
            System.out.println("filesize:" + filesize);




            //if opcode is from server
            if (opcode == 0) {
                DatagramPacket filepacket = new DatagramPacket(new byte[(int) filesize], (int) filesize);
                mcSocket.receive(filepacket);
                ByteBuffer b = ByteBuffer.allocate((int) filesize);
                b.put(filepacket.getData());
                b.flip();
                WriteBytes(b.array(), ledgernum);
                //If windows

                //if linux
                //BashCommand();
                if(System.getProperty("os.name").equals("Linux")) {
                    BashCommand("Image" + getCurrentLedger() + ".jpg");
                    System.out.println("BackGround Change!");
                }else{

                }
                //end if
            }







        }

        mcSocket.leaveGroup(mcIPAddress);
        mcSocket.close();
    }
}