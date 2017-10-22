import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Lunatic on 05.06.2017.
 */
public class Tracker {
    private static final String cPath = "D:\\SR\\kupcy\\";
    private static final String cFileName = "atab.txt";

    public static String addDealer( String cHost, String cPort ) throws Exception
    {
        Path cFilePath = Paths.get(cPath, cFileName);
        String cId;

        if(!Files.exists(cFilePath)) {
            Files.createFile(cFilePath);
            cId = "0";
        }
        else {
            cId = getNextId();
        }

        // stan | id | ip:port
        String cLine = prepareRow(" ", cId, cHost +":"+ cPort) +"\r\n";

        if(!lockFile())
            throw new Exception("Brak dostępu do pliku!");

        Files.write(cFilePath, cLine.getBytes(), StandardOpenOption.APPEND);
        removeLock();

        return cId;
    }

    public static void changeStatusDealer( String cId, boolean lKupuje ) throws Exception {
        // ' ' - niezarejestrowany, 'K' - kupujący, 'S' - sprzedający
        Path cFilePath = Paths.get(cPath, cFileName);

        if(!lockFile())
            throw new Exception("Brak dostępu do pliku!");

        List<String> aLines = Files.readAllLines(cFilePath);
        List<String> aLinesCopy = new ArrayList<>();

        for (String cLine :
                aLines) {

            if (readIdInFile(cLine).equals(cId)) {
                cLine = prepareRow((lKupuje ? "K" : "S"), cId, readAddressInFile(cLine));
            }

            aLinesCopy.add(cLine);
        }

        Files.write(cFilePath, aLinesCopy);

        removeLock();

        System.out.println("Zarejestrowano jako: " + (lKupuje ? "Kupujący" : "Sprzedający"));
    }

    public static void delDealer( String cId ) throws Exception {
        Path cFilePath = Paths.get(cPath, cFileName);

        if(!lockFile())
            throw new Exception("Brak dostępu do pliku!");

        List<String> aLines = Files.readAllLines(cFilePath);
        List<String> toRemove = new ArrayList<>();

        for (String cLine :
                aLines) {

            if( readIdInFile(cLine).equals(cId) ) {
                toRemove.add(cLine);
            }
        }

        aLines.removeAll(toRemove);
        Files.write(cFilePath, aLines);

        removeLock();
    }

    public static String getNextId() throws Exception{
        Path cFilePath = Paths.get(cPath, cFileName);
        String cId;

        if(!lockFile())
            throw new Exception("Brak dostępu do pliku!");

        List<String> aLines = Files.readAllLines(cFilePath);

        if( aLines.isEmpty() )
            cId = "0";
        else {
            cId = readIdInFile(aLines.get(aLines.size()-1));
            int nId  = Integer.parseInt(cId);
            cId = Integer.toString(++nId);
        }

        removeLock();

        return cId;
    }

    private static String prepareRow( String cState, String cId, String cAddress)
    {
        return cState +"->"+ cId +"->"+ cAddress;
    }

    private static String readStateInFile(String cLine) throws Exception
    {
        return cLine.split("->")[0];
    }

    private static String readIdInFile(String cLine) throws Exception
    {
        return cLine.split("->")[1];
    }

    private static String readAddressInFile(String cLine) throws Exception
    {
        return cLine.split("->")[2];
    }

    public static String getSeller() {
        return getSeller(new ArrayList<String>());
    }

    public static String getSeller( List<String> aWykorzystane ) {
        Path cFilePath = Paths.get(cPath, cFileName);

        Random r = new Random();
        int proba = 100;
        String cLine, cAdress = "", cAdressTmp;

        try {
            if(!lockFile())
                throw new Exception("Brak dostępu do pliku!");

            List<String> aLines = Files.readAllLines(cFilePath);

            while (proba > 0) {
                cLine = aLines.get(r.nextInt(aLines.size()));

                cAdressTmp = readAddressInFile(cLine);
                if (readStateInFile(cLine).equals("S") && !aWykorzystane.contains(cAdressTmp)) {
                    cAdress = cAdressTmp;
                    break;
                }

                proba--;
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            System.out.println(e);
        }

        removeLock();

        if(cAdress.isEmpty())
            System.out.println("Nie znaleziono żadnego sprzedawcy");

        return cAdress;
    }

    public static boolean lockFile()
    {
        float nTimeSec = 30;
        Path cFilePath = Paths.get(cPath, cFileName+".lock");

        while (true) {

            try {
                if (!Files.exists(cFilePath)) {
                    Files.createFile(cFilePath);
                    return true;
                }

                nTimeSec -= 0.5;
                if (nTimeSec <= 0)
                    break;

                Thread.sleep(500);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.out.println(e.getMessage());
            }
        }

        return false;
    }

    public static void removeLock()
    {
        Path cFilePath = Paths.get(cPath, cFileName+".lock");
        try {
            Files.deleteIfExists(cFilePath);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
}
