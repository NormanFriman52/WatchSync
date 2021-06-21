package pl.watchsync;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MultiServer extends Thread {
    private int port;
    private List<Shared> shared;;
    private ServerSocket server;
    private String mask;
    private String range_ip;
    private FileManager fl;

    public MultiServer(int port, ArrayList<Shared> sh, String ip, FileManager filemanager) throws UnknownHostException {
        String[] result = ip.split("/");
        this.mask = result[1];
        this.range_ip = result[0];
        this.fl = filemanager;

        this.port = port;
        try {
            this.shared = sh;
            server = new ServerSocket(this.port);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        while (true) {
//            if(shared.isTo_update())
//            {
//                //System.out.println("MultiServer " + shared.getPath());
//                //shared.setTo_update(false);
//                //System.out.println("MultiServer " + shared.isTo_update());
//            }
            Socket sock = null;
            try {

                sock = server.accept();
                SocketAddress socketAddress = sock.getRemoteSocketAddress();

                if (socketAddress instanceof InetSocketAddress) {
                    InetAddress inetAddress = ((InetSocketAddress) socketAddress).getAddress();
                    if (inetAddress instanceof Inet4Address) {
                        System.out.println("IP check");
                        System.out.println("IPv4: " + inetAddress);
                        System.out.println("My range ip: " + this.range_ip);
                        System.out.println("Mask: " + this.mask);
                        boolean test = checkIpInSubnet(this.range_ip, this.mask, inetAddress.toString().substring(1));
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
            new echo(sock, (ArrayList<Shared>) shared, this.fl);
        }
    }

    public int convertIpToInteger(final String ip) {
        try {
            String s = ip;
            Inet4Address a = (Inet4Address) InetAddress.getByName(s);
            byte[] b = a.getAddress();
            int i = ((b[0] & 0xFF) << 24) | ((b[1] & 0xFF) << 16) | ((b[2] & 0xFF) << 8) | ((b[3] & 0xFF) << 0);
            return i;
        } catch (final Throwable ignored) {
            return -1;
        }
    }

    public boolean checkIpInSubnet(String ip, String mask, String ipToCheck) {
        int integerIp = convertIpToInteger(ip);
        int integerIpToCheck = convertIpToInteger(ipToCheck);
        int bits = Integer.parseInt(mask);
        int byte_mask = -1 << (32 - bits);

        return (integerIp & byte_mask) == (integerIpToCheck & byte_mask);
    }
}


class echo extends Thread {
    Socket sock;
    private List<Shared> shared;
    TransmiterData td = new TransmiterData();
    private FileManager fm;

    echo(Socket sock, ArrayList<Shared>  sh, FileManager fm) {
        this.sock = sock;
        this.shared = sh;
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
                TransmiterData td = (TransmiterData) ois.readObject();

                System.err.println("event " + td.getEvent_type());

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
