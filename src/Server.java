import javax.xml.crypto.Data;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Scanner;

public class Server {

    public static long getCurrentLedger() throws FileNotFoundException {

        File file =
                new File("LedgerServer.txt");
        Scanner sc = new Scanner(file);


        return sc.nextLong();
    }


    public static byte [] ImageToBytes(File file) throws IOException {
        RandomAccessFile accessFile = new RandomAccessFile(file,"rw");
        //iterate through file , but do not over extend
        ByteBuffer buffer = ByteBuffer.allocate((int)file.length()+8);//file length plus the ledger long
        byte [] bytes = new byte[(int)file.length()];

        accessFile.readFully(bytes);
        buffer.putLong(getCurrentLedger());
        buffer.put(bytes);
        buffer.flip();

        return buffer.array();
    }

    public static ArrayList<DatagramPacket> ImagetoPacketArray(File file) throws IOException {

        ArrayList<DatagramPacket> data = new ArrayList<>();

        for (long i = 0; i <file.length(); i+=1024) {
            if (i+1024 > file.length()){
                RandomAccessFile accessFile = new RandomAccessFile(file,"rw");
                long temp = file.length() - i+1014;
                ByteBuffer buffer = ByteBuffer.allocate((int)temp+2+8);
                byte [] b = new byte[(int)temp];
                accessFile.read(b);
                buffer.putShort((short)1);
                buffer.putLong(getCurrentLedger());
                buffer.put(b);
                buffer.flip();
                data.add(new DatagramPacket(buffer.array(),buffer.array().length));
                return data;
            }
            RandomAccessFile accessFile = new RandomAccessFile(file,"rw");
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            //read 1024 bytes
            byte [] b = new byte[1014];
            accessFile.read(b);
            buffer.putShort((short)1);
            buffer.putLong(getCurrentLedger());
            buffer.put(b);
            buffer.flip();
            data.add(new DatagramPacket(buffer.array(),buffer.array().length));




            //place packet in data list
        }
        return data;
    }





   public static void main(String[] args) throws Exception {
        int mcPort = 12345;
        String mcIPStr = "230.1.1.1";
        DatagramSocket udpSocket = new DatagramSocket();
        InetAddress mcIPAddress = InetAddress.getByName(mcIPStr);
       ArrayList<DatagramPacket> data = ImagetoPacketArray(new File("Image.jpg"));
       byte[] msg = ImageToBytes(new File("Image.jpg"));
        ByteBuffer b = ByteBuffer.allocate(2+2+2);
        b.putShort((short)0);       //send packet with opcode 0
        b.putShort((short)data.size());
        b.putShort((short)data.get(data.size()-1).getLength());
        b.flip();


        DatagramPacket packet = new DatagramPacket(b.array(), b.array().length);
        packet.setAddress(mcIPAddress);
        packet.setPort(mcPort);
        udpSocket.send(packet);
        for (DatagramPacket d: data){

            d.setAddress(mcIPAddress);
            d.setPort(mcPort);
            System.out.println("Sending:"+data.indexOf(d)+" :Size:"+d.getLength());
            udpSocket.send(d);
        }


        System.out.println("Sent a  multicast message.");
        System.out.println("Exiting application");
        udpSocket.close();
    }
}

