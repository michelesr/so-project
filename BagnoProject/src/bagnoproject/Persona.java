/* Progetto Sistemi Operativi   
   Università degli Studi di Urbino Carlo Bo
   Anno Accademico 2012/2013 - Sessione Estiva 
   Michele Sorcinelli - Matricola n° 248412
   Gestione di un bagno unisex */

package bagnoproject;

import java.util.Random;
import java.util.concurrent.locks.Condition;

/** Rappresentazione della persona generica 
 * e dei suoi comportamenti. */
public abstract class Persona implements Runnable {

	/** Enumerazione che rappresenta i possibili sessi di una Persona. */ 
    public enum Sesso {maschio, femmina};
	/** Sesso della persona. */
    public Sesso sesso;
	// reference al bagno
    private Bagno bagno;
	// reference al thread
    private Thread thread;
	// tempi di attesa minimi e massimi e numero di volte che
	// il comportamento dev'essere ripetuto
    private int tempoMinArrivo, tempoMaxArrivo,
	            tempoMinServizio, tempoMaxServizio, numeroIterazioni;
	// generatore di numeri pseudocasuali
    private Random random;
	// reference alla variabile condizionale 
    private Condition condition;

	/** Costruttore della classe Persona: inizializza gli attributi
	 * della persona. */
    public Persona(Bagno bagno, String nome,
		    	   int tempoMinArrivo, int tempoMaxArrivo,
				   int tempoMinServizio, int tempoMaxServizio,
				   int numeroIterazioni, Sesso sesso) {
		this.bagno = bagno;	
		this.thread = new Thread(this, nome);
		this.tempoMinArrivo = tempoMinArrivo;
		this.tempoMaxArrivo = tempoMaxArrivo;
		this.tempoMinServizio = tempoMinServizio;
		this.tempoMaxServizio = tempoMaxServizio;
		this.numeroIterazioni = numeroIterazioni;
		this.random = new Random();
		this.sesso = sesso;
    }

	/** Restituisce la variabile condizionale della persona. */
    public Condition getCondition() {
		return this.condition;
    }

	/** Setta la variabile condizionale della persona. */
    public void setCondition(Condition condition) {
		this.condition = condition;
    }
    
	/** Restituisce il thread relativo alla persona. */
    public Thread getThread() {
		return this.thread;
    }
   
	// Simula l'arrivo causuale della persona in bagno 
    private void simulaArrivoCasuale() {
		// calcola il tempo di attesa casuale nel range predefinito
		int tempoAttesa = random.nextInt(this.tempoMaxArrivo - 
										 this.tempoMinArrivo + 1) + 
						  this.tempoMinArrivo;

		System.out.println(this.getThread().getName() + " attende " + 
					       tempoAttesa + " ms prima di arrivare al bagno");
		// attende
		try {
			Thread.sleep(tempoAttesa);
		} 
		
		catch (InterruptedException ex) {
			System.err.println(ex);
		}

		System.out.println(this.getThread().getName() + 
						   " arriva davanti al bagno dopo " 
						   + tempoAttesa + " ms");
    } 

	// Simula il tempo di servizio della persona in bagno 
    private void simulaTempoServizio() {
		// calcola il tempo di attesa casuale nel range predefinito
		int tempoAttesa = random.nextInt(this.tempoMaxServizio - 
										 this.tempoMinServizio + 1) + 
						  this.tempoMinServizio;

		System.out.println(this.getThread().getName() + 
						   " utilizza il bagno per " +
						   tempoAttesa + " ms");

		// attende
		try{
			Thread.sleep(tempoAttesa);
		} 
		
		catch (InterruptedException ex) {
			System.err.println(ex);
		}
    }

	// Rappresenta il comportamento della persona al bagno 
    private void simulaComportamento() {
		// simula l'arrivo casuale
		this.simulaArrivoCasuale();
		// prende il tempo prima all'inizio della richiesta (t1)
		long t1 = System.currentTimeMillis();
		// effettua la richiesta
		this.bagno.accessoAlBagno(this);
		// prende il tempo dopo la richiesta (t2) 
		long t2 = System.currentTimeMillis();
		// simula il tempo di servizio
		this.simulaTempoServizio();
		// notifica l'uscita
		this.bagno.notificaUscita(this);
		// aggiunge il tempo di attesa alla sommatoria tempi di attesa (t2 - t1) 
		this.bagno.addTempoAttesa(t2 - t1);

    }

	/** Metodo che viene chiamato all'avvio del thread. */
    @Override
    public void run() {
		System.out.println(this.thread.getName() + " inizia l'esecuzione");
		// va al bagno 4 volte se è maschio, 8 se è femmina
		for (int i = 0; i < this.numeroIterazioni; i++) {
			this.simulaComportamento();
		}
		System.out.println(this.thread.getName() + " termina l'esecuzione");
    }
}
