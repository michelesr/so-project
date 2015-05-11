/* Progetto Sistemi Operativi   
   Università degli Studi di Urbino Carlo Bo
   Anno Accademico 2012/2013 - Sessione Estiva 
   Michele Sorcinelli - Matricola n° 248412
   Gestione di un bagno unisex */

package bagnoproject;

/** Rappresentazione dell'entità Uomo, 
 * estende la classe Persona. */
public class Uomo extends Persona implements Runnable {

    /** Costruttore della classe Uomo, richiama il costruttore
	 * della classe Persona fornendogli il reference al bagno e
	 * i parametri che descrivono il comportamento dell'uomo
	 * (nome del thread, tempi di attesa, numero di volte che 
	 * l'uomo va al bagno e sesso). */
    public Uomo(Bagno bagno, String nome) {
		super(bagno, nome, 100, 500, 100, 200, 4, Persona.Sesso.maschio);
    }
}
