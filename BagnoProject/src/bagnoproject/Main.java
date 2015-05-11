/* Progetto Sistemi Operativi   
   Università degli Studi di Urbino Carlo Bo
   Anno Accademico 2012/2013 - Sessione Estiva 
   Michele Sorcinelli - Matricola n° 248412
   Gestione di un bagno unisex */

package bagnoproject;

/** Classe principale del programma. */
public class Main {

    /** Metodo main: avvia la simulazione impostando i parametri con i 
	 * valori contenuti nel comando di lancio.
     * 
     * Parametri da fornire: 
     * 1 - numero di uomini; 
     * 2 - numero di donne;
     * 3 - capacità del bagno, ossia numero di posti massimi disponibili;
     * 4 - valore di trigger dell'aging (facoltativo).
     * 
     * I valori devono essere forniti come numeri naturali.
     * La capienza deve essere maggiore di 0, il valore di trigger dell'aging 
     * dev'essere maggiore della capienza del bagno.*/
    public static void main(String[] args) {
        int m = 0, n = 0, x = 0, a = 0;
		Bagno bagno;
		// se ci sono troppi pochi parametri il programma 
		// esce stampando un messaggio di errore sullo stderr 
		if (args.length < 3) {
			System.err.println("Lanciare i programma con i seguenti " +
							   "parametri: m n x (a)");
			System.exit(1);
		}

		try {
			// acquisizione dei parametri 
			m = Integer.parseInt(args[0]);
			n = Integer.parseInt(args[1]);
			x = Integer.parseInt(args[2]);
			// se c'è un quarto parametro viene acquisito 
			if (args.length >= 4) {
				a = Integer.parseInt(args[3]);
				// se il valore di trigger dell'aging è sbagliato esce 
				// stampando un messaggio di errore sullo stderr
				if (a <= x) {
					System.err.println("Il valore di trigger dell'aging " + 
									   "dev'essere > del numero di posti");
					System.exit(2);
				}
			}
		}

		// se il programma non riesce a convertire i parametri in interi 
		// esce stampando un messaggio di errore sullo stderr
		catch (NumberFormatException ex) {
			System.err.println("Inserire valori interi come parametri di lancio");
			System.exit(3);
		}

		// se i valori non sono corretti esce
		// stampando un messaggio di errore sullo stderr
		if (m < 0 || n < 0 || x < 1) {
			System.err.println("I valori devono essere >= 0 (la capienza >= 1)");
			System.exit(4); 
		}

		System.out.println("Inizio simulazione: \n" +
						   "Numero uomini = " + m + "\n" +
						   "Numero donne = " + n + "\n" +
						   "Capacità bagno = " + x);
		// istanzia gli array di uomini e donne
		Uomo[] uomini = new Uomo[m];
		Donna[] donne = new Donna[n];
		// istanzia il bagno
		// se è stato fornito un valore di trigger dell'aging
		// lo passa al costruttore del bagno, altrimenti usa 
		// direttamente il costruttore a 1 parametro 
		if (a > 0) {
			bagno = new Bagno(x, a);
		}
		else {
			bagno = new Bagno(x); 
		}

		// istanzia gli uomini
		for (int i = 0; i < uomini.length; i++) {
			uomini[i] = new Uomo(bagno, "Uomo #" + i);
		}

		// istanzia le donne
		for (int i = 0; i < donne.length; i++) {
			donne[i] = new Donna(bagno, "Donna #" + i);
		}

		// lancia i thread uomini
		for (int i = 0; i < uomini.length; i++) {
			uomini[i].getThread().start();
		}

		// lancia i thread donne
		for (int i = 0; i < donne.length; i++) {
			donne[i].getThread().start();
		}

		// aspetta la terminazione dei thread uomini e donne
		try {
			for (int i = 0; i < uomini.length; i++) {
				uomini[i].getThread().join();
			}
			for (int i = 0; i < donne.length; i++) {
				donne[i].getThread().join();
			}
		} 
		catch (InterruptedException ex) {
			System.err.println(ex);
			System.exit(5);
		}

		System.out.println("Simulazione terminata");
		// stampa il tempo di attesa medio
		try {
			System.out.println("Il tempo d'attesa medio è " + 
							   bagno.calcolaTempoAttesaMedio(m, n) + " ms");
		}
		// cattura l'eccezione relativa alla divisione per 0
		// stampa un errore sullo stderr 
		catch (ArithmeticException ex) {
			System.err.println("Impossibile calcolare il tempo medio " +
							   "di attesa\n"+
							   "quando le donne e gli uomini sono 0!");
		}
    }
}