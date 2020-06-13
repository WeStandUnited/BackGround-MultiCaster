import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.net.DatagramPacket;
import java.net.FileNameMap;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.ArrayList;


public class Client {
    public static void WriteBytes(byte [] b,long ledger) throws IOException {
        File file = new File("Image"+ledger+".jpg");
        RandomAccessFile accessFile = new RandomAccessFile(file,"rw");
        accessFile.write(b);

        accessFile.close();


    }
    public static void WriteArrayList(ArrayList<DatagramPacket>data,File file) throws FileNotFoundException {
        RandomAccessFile randaccess = new RandomAccessFile(file,"rw");


    }

    public static void main(String[] args) throws Exception {
        int mcPort = 12345;
        String mcIPStr = "230.1.1.1";
        MulticastSocket mcSocket = null;
        InetAddress mcIPAddress = null;
        mcIPAddress = InetAddress.getByName(mcIPStr);
        mcSocket = new MulticastSocket(mcPort);
        System.out.println("Multicast Receiver running at:"
                + mcSocket.getLocalSocketAddress());
        mcSocket.joinGroup(mcIPAddress);

        DatagramPacket packet = new DatagramPacket(new byte[6], 6);


        System.out.println("Waiting for a  multicast message...");
        mcSocket.receive(packet);
        System.out.println("[Recieved]!");
        ByteBuffer byteBuffer = ByteBuffer.allocate(packet.getLength());
        byteBuffer.put(packet.getData());
        byteBuffer.flip();
        short opcode = byteBuffer.getShort();
        short dataSize = byteBuffer.getShort();
        short oddpacket = byteBuffer.getShort();
        System.out.println("opcode:"+opcode);
        System.out.println("dataSize:"+dataSize);
        System.out.println("oddpacket:"+oddpacket);

        ArrayList<DatagramPacket> data = new ArrayList<>(dataSize);

        for (int i = 0; i < dataSize; i++) {

            if(i == dataSize-1){
                System.out.println("Last Packet #:"+i);
                DatagramPacket p = new DatagramPacket(new byte[oddpacket], oddpacket);

                mcSocket.receive(p);
                data.add(p);
                break;
            }
            System.out.println("Packet #:"+i);
            DatagramPacket p = new DatagramPacket(new byte[1024], 1024);

            mcSocket.receive(p);
            data.add(p);


        }

        // WriteBytes(bytes,ledger);

        mcSocket.leaveGroup(mcIPAddress);
        mcSocket.close();
    }
}