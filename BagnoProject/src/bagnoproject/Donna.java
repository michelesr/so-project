/* Progetto Sistemi Operativi   
   Università degli Studi di Urbino Carlo Bo
   Anno Accademico 2012/2013 - Sessione Estiva 
   Michele Sorcinelli - Matricola n° 248412
   Gestione di un bagno unisex */

package bagnoproject;

/** Rappresentazione dell'entità Donna,
 *  estende la classe Persona. */
public class Donna extends Persona implements Runnable {
    
    /** Costruttore della classe Donna, richiama il costruttore
	 * della classe Persona fornendogli il reference al bagno e
	 * i parametri che descrivono il comportamento della donna
	 * (nome del thread, tempi di attesa, numero di volte che 
	 * la donna va al bagno e sesso). */
    public Donna(Bagno bagno, String nome) {
		super(bagno, nome, 100, 200, 200, 600, 8, Persona.Sesso.femmina);
    }
}
