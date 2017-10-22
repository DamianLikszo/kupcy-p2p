import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Lunatic on 05.06.2017.
 */
public class Polaczenie {
    private static final String cHost_start = "localhost";
    private static final int nTimeOut_Send = 8*1000;

    public String cHost = cHost_start;              // moze del
    public String cPort;                            // moze del
    public ServerSocket oSerwer;
    public String cId;

    public Polaczenie() throws Exception{
        this.cHost = cHost_start;
        this.cPort = this.getFreePort();
        this.cId = Tracker.addDealer(this.cHost, this.cPort);

        System.out.println("ID: "+ this.cId +" | Host: "+ this.cHost +" | Port: "+ this.cPort);
    }

    private String getFreePort() throws Exception
    {
        this.oSerwer = new ServerSocket(0);

        return Integer.toString(oSerwer.getLocalPort());
    }

    public String getMyAdress() {
        return cHost +":"+ cPort;
    }


    // SERWER
    public Message listen(boolean lTimeOut)
    {
        Message msg = null;

        try {
            System.out.println("Nasłuchuje ...");
            Socket clientReq = oSerwer.accept();
            System.out.println("Przyszła wiadomość od kupca");
            msg = serviceClient(clientReq);
        }
        catch( Exception e) {
            System.out.println("Exception: "+e);
        }

        return msg;
    }

    public Message listen()
    {
        return this.listen(false);
    }

    public Message serviceClient( Socket s) {
        ObjectInputStream inStream = null;
        Message msg = null;

        try {
            inStream = new ObjectInputStream(s.getInputStream());

            msg = (Message) inStream.readObject();

            System.out.println("[<-] Otrzymałem zapytanie \""+ msg.getPolecenieNazwa()+"\" z "+ msg.cOd );

            s.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception: " + e);
            msg = null;
        }

        return msg;
    }

    // CLIENT
    public boolean sendMessage (Message msg) {
        ObjectOutputStream outStream = null;
        Socket oSocket = null;
        String[] aAdress = null;

        if(!msg.przygotujDoWysylki(this.getMyAdress()))
            return false;

        try {
            aAdress = msg.cDo.split(":");
            if( aAdress.length != 2 )
                throw new Exception("Błędny adres odbiorcy");

            // USPIENIE BY WIDZIEC TERMINAL
            Thread.sleep(nTimeOut_Send);

            System.out.println("[->] Wysyłam zapytanie \""+ msg.getPolecenieNazwa()+"\" do "+ aAdress[0] +":"+ aAdress[1] );

            oSocket = new Socket(aAdress[0], Integer.parseInt(aAdress[1]));
            outStream = new ObjectOutputStream(oSocket.getOutputStream());

            outStream.writeObject(msg);
            outStream.flush();

            oSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception: " + e);

            return false;
        }

        return true;
    }
}
