import java.util.*;

/**
 * Created by Lunatic on 05.06.2017.
 */
public class Kupiec {
    public boolean lKupuje;
    public Polaczenie oPolaczenie;
    public Magazyn oMagazyn;

    public Kupiec() throws Exception {
        this.oPolaczenie = new Polaczenie();
        this.lKupuje = (Integer.parseInt(this.getId()) % 3) == 1;       // 1/3 kupuje
        this.oMagazyn = new Magazyn();
        this.zmienStatusKupca(this.lKupuje);
    }

    public String getId(){
        return oPolaczenie.cId;
    }

    public boolean sluchaj() {
        Message msg = this.oPolaczenie.listen();

        if( msg == null ) {
            System.out.println("Brak wiadomości");
            return false;
        }

        // Obsługa wiadomości
        switch (msg.polecenie_id) {
            case Szukaj:
                polecenie_szukaj(msg);
                break;
            case SzukajOdpowiedz:
                polecenie_szukaj_odp(msg);
                break;
            case Kup:
                polecenie_kup(msg);
                break;
            case KupOdpowiedz:
                polecenie_kup_odp(msg);
                break;
        }

        return true;
    }

    public void polecenie_kup_odp(Message msg)
    {
        if( msg.ilosc > 0)
        {
            System.out.println("[+] Kupiłem "+ Magazyn.getNazwaTowar(msg.towar) + " w ilości "+
                    Integer.toString(msg.ilosc));

            this.oMagazyn.zmien(msg.towar, msg.ilosc);

            // założenie: Kupiec staje się sprzedawcą
            this.zmienStatusKupca(false);
        }
        else
        {
            System.out.println("[-] Nie udało się kupić "+ Magazyn.getNazwaTowar(msg.towar) );
            System.out.println("Szukam towaru ponownie");

            // założenie: Szukam jeszcze raz danej iloći tego samego towaru
            Message msgSzukaj = this.przygotujWiadomosc(Message.Polecenie_Id.Szukaj, Tracker.getSeller(), false, msg);
            this.wyslijWiadomosc(msgSzukaj);
        }

        return;
    }

    public void polecenie_kup(Message msg)
    {
        boolean lPosiada = this.oMagazyn.sprawdzStan(msg.towar, msg.ilosc);
        Message msgOdp = this.przygotujWiadomosc(Message.Polecenie_Id.KupOdpowiedz, msg.cOd, true, msg);

        if(lPosiada) {
            this.oMagazyn.zmien(msg.towar, -msg.ilosc);
        }
        else {
            msgOdp.ilosc = 0;
        }

        this.wyslijWiadomosc(msgOdp);

        if( lPosiada )
            this.zmienStatusKupca(true);

        return;
    }

    public void polecenie_szukaj_odp(Message msg)
    {
        if(msg.isNadawca())
        {
            // jeżeli ja pytałem to obróbka i wysłanie kup
            System.out.println("Ja byłem nadawcą, więc kupuję towar");

            // kierunek P2P: wysyłam do ostatniej osoby z tablicy routingu
            Message msgKup = this.przygotujWiadomosc(Message.Polecenie_Id.Kup, msg.cNadawca, false, msg);
            this.wyslijWiadomosc(msgKup);
        }
        else {
            // jeżeli nie ja to prześlij do nadawcy
            System.out.println("Nie jestem nadawcą, wiadomość przesyłam dalej.");

            this.wyslijWiadomosc(msg);
        }

        return;
    }

    public void polecenie_szukaj(Message msg){
        boolean lPosiada = this.oMagazyn.sprawdzStan(msg.towar, msg.ilosc);

        if(lPosiada)
        {
            // założenie: jak odpowiadam znaczy mam
            // kierunek P2P: przesyłam do osoby która mnie zapytała ona powina to przesłać do nadawcy lub być nadawcą
            Message msgOdp = this.przygotujWiadomosc(Message.Polecenie_Id.SzukajOdpowiedz, msg.cOd, true, msg);
            this.wyslijWiadomosc(msgOdp);
        }
        else
        {
            // Losuje towar
            Random r = new Random();
            System.out.println("ZATOWAROWANIE");
            this.oMagazyn.zmien(Magazyn.losujTowar(), r.nextInt(20)+10);

            System.out.println("Przesyłam dalej");

            List<String> aWykluczone = new ArrayList<>(msg.aDroga);
            aWykluczone.add(this.oPolaczenie.getMyAdress());
            msg.cDo = Tracker.getSeller(aWykluczone);
            this.wyslijWiadomosc(msg);
        }

        return;
    }

    public Message przygotujWiadomosc(Message.Polecenie_Id polecenie_id, String cDo, boolean lResponse) {
        Message msg = new Message(polecenie_id, cDo, this.oPolaczenie.getMyAdress(), lResponse);

        switch (polecenie_id)
        {
            case Szukaj:
                Random r = new Random();
                msg.towar = Magazyn.losujTowar();
                msg.ilosc = r.nextInt(10)+3;
                break;
        }

       return msg;
    }

    public Message przygotujWiadomosc(Message.Polecenie_Id polecenie_id, String cDo, boolean lResponse, Message msgPyt) {
        Message msg = this.przygotujWiadomosc(polecenie_id, cDo, lResponse);

        msg.towar = msgPyt.towar;
        msg.ilosc = msgPyt.ilosc;
        msg.aDroga = new ArrayList<String>(msgPyt.aDroga);

        return msg;
    }

    public void wyslijWiadomosc( Message msg ) {this.oPolaczenie.sendMessage(msg);}

    public void del() {
        try {
            Tracker.delDealer(this.getId());
        } catch (Exception e)
        {
            System.out.println("Nie udało się usunąć kupca z listy ip.\r\n"+e);
        }
    }

    public void rynek() {
        boolean lResponse = false;
        boolean lOldKupuje = this.lKupuje;

        while (true) {
            // pytam jeżeli jestem kupcem i nie dostałem odpowiedzi lub zmienił mi się status
            if ( (!lResponse || lOldKupuje != this.lKupuje) && this.lKupuje) {
                Message msg = this.przygotujWiadomosc(Message.Polecenie_Id.Szukaj, Tracker.getSeller(), false);
                this.wyslijWiadomosc(msg);
            }

            lOldKupuje = this.lKupuje;
            if( !(lResponse = this.sluchaj()) ) {
                System.out.println("Nie otrzymałem odpowiedzi, może gołąb się zmęczył");
            }
        }
    }

    public void zmienStatusKupca(boolean lKupuje)
    {
        try {
            Tracker.changeStatusDealer(this.getId(), lKupuje);
            System.out.println("Zmieniłem status na: " + (lKupuje ? "KUPCA" : "SPRZEDAWCE") );

            // kupujacy ma okreslony czas
            if( lKupuje )
                this.oPolaczenie.oSerwer.setSoTimeout(60*1000);  // 60 sekund
            else
                this.oPolaczenie.oSerwer.setSoTimeout(0);       // brak

            this.lKupuje = lKupuje;
        }
        catch (Exception e)
        {
            System.out.println("[!] Nie udało mi się zmienić statusu kupca!");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    protected void finalize() throws Throwable {
        this.del();
        super.finalize();
    }

    public static void main(String[] args) {
        Kupiec kupiec = null;

        try {
            kupiec = new Kupiec();
            kupiec.rynek();
        } catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Exception: "+ e);
            System.exit(1);
        }

        kupiec.del();
    }
}

