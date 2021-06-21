package pl.watchsync;

import java.net.*;
import java.io.*;


//this class manages incoming connections connects through socket reads incoming objects and download files to temp
public class MultiServer extends Thread {
    private ServerSocket server;
    private final String mask;
    private final String range_ip;
    private final FileManager fm;

    public MultiServer(int port, String ip, FileManager filemanager) {
        String[] result = ip.split("/");
        this.mask = result[1];
        this.range_ip = result[0];
        this.fm = filemanager;

        try {
            server = new ServerSocket(port);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        while (true) {
            Socket sock = null;
            try {

                sock = server.accept();
                SocketAddress socketAddress = sock.getRemoteSocketAddress();

                if (socketAddress instanceof InetSocketAddress) {
                    InetAddress inetAddress = ((InetSocketAddress) socketAddress).getAddress();
                    if (inetAddress instanceof Inet4Address) {
                        System.out.println("Inbound From (IPv4): " + inetAddress);
                        System.out.println("My range ip: " + this.range_ip);
                        //System.out.println("Mask: " + this.mask); // for debugging
                        boolean test = CheckIpRange.checkIpInSubnet(this.range_ip, this.mask, inetAddress.toString().substring(1));
                        if (!test) {
                            sock.close();
                        }
                    } else
                        System.err.println("Not an IP address.");
                } else {
                    System.err.println("Not an internet protocol socket.");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            System.err.println("multiserver " + sock);
            new echo(sock, this.fm);
        }
    }
}

//MultiServer listener on designed port
class echo extends Thread {
    Socket sock;
    TransmiterData td = new TransmiterData();
    private final FileManager fm;

    echo(Socket sock, FileManager fm) {
        this.sock = sock;
        this.fm = fm;
        start();
    }

    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
            ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
            BufferedInputStream bis = new BufferedInputStream(sock.getInputStream());
            BufferedOutputStream bos = new BufferedOutputStream(sock.getOutputStream());

            //while (!in.readLine().equals("end")) {
                System.err.println("incoming connection from: " + sock.getInetAddress());
                td = (TransmiterData) ois.readObject();

                if (td.getEvent_type().equals("ENTRY_CREATE") || td.getEvent_type().equals("ENTRY_MODIFY")) {
                    System.err.println(td.getPath());
                    System.err.println(td.getFilename());
                    if (td.getType().equals("file")) {
                        File directory = new File("./temp/");
                        if (!directory.exists()) {
                            directory.mkdir();
                        }
                        try{


                        FileOutputStream fos = new FileOutputStream("./temp/" + td.getFilename());
                        byte[] buffer = new byte[1024];
                        int count;
                        while ((count = bis.read(buffer)) > 0) {
                            fos.write(buffer, 0, count);
                        }
                        fos.close();
                        //bis.close();
                        }
                        catch (Exception e){
                            System.out.println("Exception reading buffer " + e);
                        }
                        System.out.println("File downloaded ... ");
                        try {
                            String sum = MD5Checksum.getMD5Checksum("./temp/" + td.getFilename());
                            if (td.getSum().equals(sum)) {
                                System.out.println("sum ok");
                            }
                            td.setTempPath("./temp/" + td.getFilename());
                            fm.addObject(td);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                else if (td.getEvent_type().equals("ENTRY_DELETE")) {
                    fm.addObject(td);
                }
            //}
            sock.close();
        } catch (OptionalDataException e) {
            e.printStackTrace();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Receiver exception " + e);
            e.printStackTrace();
        }
    }

    }
