import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lunatic on 05.06.2017.
 */

public class Message implements Serializable {
    private static final int nZycia_start = 3;

    public enum Polecenie_Id { Szukaj, SzukajOdpowiedz, Kup, KupOdpowiedz }

    public Polecenie_Id polecenie_id;
    public String cOd;
    public String cDo;
    public String cNadawca;
    public boolean lResponse;

    public List<String> aDroga = new ArrayList<>();
    public int nZycia = nZycia_start;
    public Magazyn.Produkty towar = Magazyn.Produkty.Brak;
    public int ilosc = 0;

    public Message(Polecenie_Id polecenie_id, String cDo, String cNadawca, boolean lResponse)
    {
        this.polecenie_id = polecenie_id;
        this.cDo = cDo;
        this.cOd = cNadawca;
        this.cNadawca = cNadawca;
        this.lResponse = lResponse;
    }

    public boolean przygotujDoWysylki(String cOd) {
        if( cOd.isEmpty() ) {
            System.out.println("Wiadomość nie wysłana. Nieprawidłowy nadawca wiadomości");
            return false;
        }

        if( lResponse && this.aDroga.isEmpty() )
        {
            System.out.println("Gołąb pogubił drogę. Nie jestem nadawcą a nie wiem komu wysłać");
            return false;
        }

        if( this.cDo.isEmpty() ) {
            System.out.println("Wiadomość nie wysłana. Brak odbiorcy");
            return false;
        }

        if( this.nZycia == 0 ) {
            System.out.println("Gołąb umarł z wycieńczenia, wiadomość nie może być juz wysłana");
            return false;
        }

        this.cOd = cOd;
        this.nZycia--;

        if(this.lResponse) {
            this.cDo = this.aDroga.get(this.aDroga.size() - 1);
            this.aDroga.remove(this.aDroga.size() - 1);
        }
        else
            this.aDroga.add(cOd);

        System.out.println("DROGA: " + aDroga.toString());

        return true;
    }

    public boolean isNadawca() {
        return this.aDroga.size() == 0;
    }

    public String getPolecenieNazwa()
    {
        switch (this.polecenie_id)
        {
            case Szukaj:
                return "Szukaj";
            case SzukajOdpowiedz:
                return "SzukajOdpowiedz";
            case Kup:
                return  "Kup";
            case KupOdpowiedz:
                return  "KupOdpowiedz";
            default:
                return  "PolecenieNieznane";
        }
    }
}
