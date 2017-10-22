import java.util.Random;

/**
 * Created by Lunatic on 05.06.2017.
 */
public class Magazyn {
    public enum Produkty { Brak, Ryby, Chleb, Jablka }

    public int nRyby = 0;
    public int nChleb = 0;
    public int nJablka = 0;

    public void dodajProdukt() {
        Random r = new Random();

        int nIlosc = r.nextInt(10) + 1;
        Produkty produkt = losujTowar();

        switch (produkt) {
            case Chleb: this.nChleb += nIlosc; break;
            case Jablka: this.nJablka += nIlosc; break;
            case Ryby: this.nRyby += nIlosc; break;
        }
    }

    public static Produkty losujTowar() {
        Random r = new Random();
        return Produkty.values()[r.nextInt(Produkty.values().length - 1) + 1];
    }

    public boolean sprawdzStan( Produkty towar, int ilosc)
    {
        boolean lPosiada = false;

        switch (towar)
        {
            case Ryby:
                lPosiada = this.nRyby >= ilosc;
                break;
            case Chleb:
                lPosiada = this.nChleb >= ilosc;
                break;
            case Jablka:
                lPosiada = this.nJablka >= ilosc;
                break;
            default:
                break;
        }

        System.out.println("Towar: "+ getNazwaTowar(towar)+" | Ilość: " +Integer.toString(ilosc) +" | "+
                (lPosiada ? "Posiadam" : "Nie posiadam"));

        return lPosiada;
    }

    // ilosc +/-
    public void zmien( Produkty towar, int ilosc )
    {
        int nOldValue = 0;
        int nValue = 0;

        switch (towar)
        {
            case Ryby:
                nOldValue = this.nRyby;
                this.nRyby += ilosc;
                nValue = this.nRyby;
                break;
            case Chleb:
                nOldValue = this.nChleb;
                this.nChleb += ilosc;
                nValue = this.nChleb;
                break;
            case Jablka:
                nOldValue = this.nJablka;
                this.nJablka += ilosc;
                nValue = this.nJablka;
                break;
        }

        System.out.println("Towar: "+ getNazwaTowar(towar)+" | Ilość: " +Integer.toString(ilosc) +" | Aktualnie: "+
            Integer.toString(nValue) +" | Było: " + Integer.toString(nOldValue));

        return;
    }

    public static String getNazwaTowar( Produkty produkt ) {
        switch (produkt)
        {
            case Chleb:
                return "Chleb";
            case Jablka:
                return  "Jabłka";
            case Ryby:
                return "Ryby";
            case Brak:
                return "Brak";
            default:
                return "Nieznany";
        }
    }

}
