import java.io.*;
import java.lang.reflect.Field;
import java.net.DatagramPacket;
import java.net.FileNameMap;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;


public class Client {
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

        DatagramPacket packet = new DatagramPacket(new byte[18], 18);


        System.out.println("Waiting for a  multicast message...");
        mcSocket.receive(packet);
        System.out.println("[Recieved]!");
        ByteBuffer byteBuffer = ByteBuffer.allocate(2+8+8);
        byteBuffer.put(packet.getData());
        byteBuffer.flip();
        short opcode = byteBuffer.getShort();
        long ledgernum = byteBuffer.getLong();
        long filesize = byteBuffer.getLong();
        System.out.println("opcode:"+opcode);
        System.out.println("ledgernum:"+ledgernum);
        System.out.println("filesize:"+filesize);

        DatagramPacket filepacket = new DatagramPacket(new byte[(int)filesize], (int)filesize);
        mcSocket.receive(filepacket);
        System.out.println(new String(filepacket.getData()));
        ByteBuffer b = ByteBuffer.allocate((int)filesize);
        b.put(filepacket.getData());
        b.flip();
        WriteBytes(b.array(),ledgernum);


        mcSocket.leaveGroup(mcIPAddress);
        mcSocket.close();
    }
}