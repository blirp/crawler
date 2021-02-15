package org.larma;

import java.util.Base64;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Crawler {
    private static String BASE_URL = "https://martinssystem.herokuapp.com/";

    // De sidene vi skal hente
    private final Set<Integer> plan = new HashSet<>();

    // De sidene vi har hentet
    private final Set<Integer> hentet = new HashSet<>();

    // Den som henter sider
    private final OkHttpClient client = new OkHttpClient();

    public Crawler() {
        plan.add(0);
        //BASE_URL = "http://localhost:5000/";
    }

    public void run() throws Exception
    {
        Iterator<Integer> planIterator = plan.iterator();
        while (planIterator.hasNext()) {
            int side = getNesteSideNr(planIterator);

            String innhold = getInnhold(side);
            hentet.add(side);

            // Her er det naturlig å behandle innholdet

            Set<Integer> nye = getNyeLenker(innhold);
            plan.addAll(nye);

            planIterator = plan.iterator();
        }
    }

    private int getNesteSideNr(
            final Iterator<Integer> planIterator)
    {
        // Det er ikke noen metode for å fjerne et element fra et Set<>
        // Derimot kan det gjøres via en Iterator<>.
        // Set<>.Iterator().next() henter et element fra Set<>
        // Set<>.Iterator().remove() fjerner det elementet vi hentet sist.
        int side = planIterator.next();
        planIterator.remove();
        return side;
    }

    private String getInnhold(
            final int side
            ) throws Exception
    {
        // Gjør et HTTP kall for å hente en webside
        Request request = new Request.Builder()
                    .url(BASE_URL + side)
                    .addHeader("Authorization", getBasicAuth())
                    .build();
        Call call = client.newCall(request);
		try (
            Response response = call.execute()
        )
        {
            if (!response.isSuccessful()) {
                throw new Exception();
            }
            String innhold = response.body().string();

            return innhold;
        } 
    }

    private Set<Integer> getNyeLenker(
            final String innhold)
    {
        // Lenker er av typen
        //     <a href="{id}">{Tekst}</a>
        // Så søker frem til 'href="' og kopierer frem til neste '"'
        // Det som står mellom 'href="' og '"' er da id'en vi skal sjekke
        // Gjenta til det ikke er flere 'href' i innholdet
        Set<Integer> result = new HashSet<>();        
        int forrigeIndeks = 0;
        while (forrigeIndeks >= 0) {
            int startIndeks = innhold.indexOf("href", forrigeIndeks);
            forrigeIndeks = -1;
            if (startIndeks >= 0) {
                startIndeks += "href=\"".length();
                int sluttIndeks = innhold.indexOf("\"", startIndeks);
                String lenke = innhold.substring(startIndeks, sluttIndeks);
                result.add(Integer.parseInt(lenke));
                forrigeIndeks = sluttIndeks;
            }
        }
        // Til slutt fjerner vi alle de vi allerede har behandlet
        result.removeAll(hentet);
        return result;
    }

    private String getBasicAuth() 
    {
        return Base64.getEncoder().encodeToString("bruker:passord".getBytes());
    }
}
