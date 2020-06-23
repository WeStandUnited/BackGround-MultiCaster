import javax.imageio.ImageIO;
import javax.xml.crypto.Data;
import java.awt.image.BufferedImage;
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
    public static void WriteBytes(byte [] b,long ledger) throws IOException {
        File file = new File("Image"+ledger+".jpg");
        RandomAccessFile accessFile = new RandomAccessFile(file,"rw");
        accessFile.write(b);

        accessFile.close();


    }

    public static byte [] ImageToBytes(File file) throws IOException {
        RandomAccessFile accessFile = new RandomAccessFile(file,"rw");
        //iterate through file , but do not over extend
        ByteBuffer buffer = ByteBuffer.allocate((int)file.length()+8);//file length plus the ledger long
        byte [] bytes = new byte[(int)file.length()];

        accessFile.readFully(bytes);
        buffer.put(bytes);
        buffer.flip();

        return buffer.array();
    }

    public static ArrayList<DatagramPacket> ImagetoPacketArray(File file) throws IOException {

        ArrayList<DatagramPacket> data = new ArrayList<>();
        for (long i = 0; i <file.length(); i+=1014) {

            RandomAccessFile rand = new RandomAccessFile(file,"rw");

            rand.seek(i);

            byte [] bytes = new byte[1014];

            rand.read(bytes);


            ByteBuffer buff = ByteBuffer.allocate(1024);

            buff.putShort((short)1);//opcode

            buff.putLong(getCurrentLedger());

            buff.put(bytes);

            buff.flip();

            DatagramPacket d = new DatagramPacket(buff.array(),buff.array().length);

            data.add(d);

        }







        return data;
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





    public static void WriteArrayList(ArrayList<DatagramPacket>data,File file) throws IOException {
        RandomAccessFile randaccess = new RandomAccessFile(file,"rw");
        for (long i = 0; i <data.size() ; i+=1014) {
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




   public static void main(String[] args) throws Exception {
        int mcPort = 2770;
        String mcIPStr ="230.1.1.1";
        DatagramSocket udpSocket = new DatagramSocket();
        InetAddress mcIPAddress = InetAddress.getByName(mcIPStr);
       File f = new File("Image.jpg");
       byte[] filebytes = ImageToBytes(f);
        ByteBuffer b = ByteBuffer.allocate(2+8+8);
        b.putShort((short)0);       //send packet with opcode 0
        b.putLong(getCurrentLedger());
        b.putLong(f.length());
       System.out.println(f.length());
        b.flip();


        DatagramPacket packet = new DatagramPacket(b.array(), b.array().length);
        packet.setAddress(mcIPAddress);
        packet.setPort(mcPort);
        udpSocket.send(packet);

       DatagramPacket filep = new DatagramPacket(filebytes, filebytes.length);
       filep.setAddress(mcIPAddress);
       filep.setPort(mcPort);
       System.out.println(new String(filep.getData()));
       udpSocket.send(filep);


        System.out.println("Sent a  multicast message.");
        System.out.println("Exiting application");
        udpSocket.close();
    }
}

