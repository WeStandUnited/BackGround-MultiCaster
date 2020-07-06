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
    public static void BashCommand() throws IOException {
        //gsettings set org.gnome.desktop.background picture-uri file:///home/cj/Pictures/Wallpapers/back.jpg
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("gsettings set org.gnome.desktop.background picture-uri file:///home/cj/Pictures/Wallpapers/back.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
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
                System.out.println("BackGround Change!");

                //end if
            }


            if (opcode == 1) {
                //TODO Check if new ledger is greater than current if so send Broadcast request!

                if (ledgernum > getCurrentLedger()){
                    //Send a Please send file!
                    ByteBuffer byteBuffer1 = ByteBuffer.allocate(2+8+8);
                    byteBuffer1.putShort((short)1);       //send packet with opcode 0
                    byteBuffer1.putLong(getCurrentLedger());
                    byteBuffer1.putLong(filesize);
                    byteBuffer1.flip();


                    //send ledger challenge
                    DatagramPacket requestPacket = new DatagramPacket(byteBuffer1.array(), byteBuffer1.array().length);
                    requestPacket.setAddress(mcIPAddress);
                    requestPacket.setPort(mcPort);
                    mcSocket.send(requestPacket);
                    System.out.println("Sending Request!");


                }

            }

            //Ledger Challenge Packing!
            ByteBuffer bb = ByteBuffer.allocate(2+8+8);
            bb.putShort((short)1);       //send packet with opcode 0
            bb.putLong(getCurrentLedger());
            bb.putLong(filesize);
            bb.flip();


            //send ledger challenge
            DatagramPacket p = new DatagramPacket(bb.array(), bb.array().length);
            p.setAddress(mcIPAddress);
            p.setPort(mcPort);
            mcSocket.send(packet);
            System.out.println("Sending Ledger Challenge!");







        }

        mcSocket.leaveGroup(mcIPAddress);
        mcSocket.close();
    }
}