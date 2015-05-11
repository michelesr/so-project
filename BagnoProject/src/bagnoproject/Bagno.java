/* Progetto Sistemi Operativi   
   Università degli Studi di Urbino Carlo Bo
   Anno Accademico 2012/2013 - Sessione Estiva 
   Michele Sorcinelli - Matricola n° 248412
   Gestione di un bagno unisex */

package bagnoproject;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/** Implementazione del Bagno 
 * e della sua logica di gestione degli accessi. */
public class Bagno {

	// determina il sesso delle persone che possono entrare in bagno
    private Persona.Sesso sessoCorrente;
	// semaforo che gestisce l'accesso al bagno da parte di persone
	// dello stesso sesso
    private Semaphore semaforoPosti;
	// capienza del bagno;
	// valore di trigger dell'aging;
	// contatore utilizzato nel meccanismo di aging: conta quante persone
	// dello stesso sesso sono entrate in bagno dall'ultimo cambio di sesso
    private int capienza, valoreTriggerAging, contatoreAging;
	// sommatoria dei tempi di attesa
	private long sommaTempiAttesa;
	// coda che contiene gli uomini in attesa di essere svegliati
    private LinkedList<Uomo> codaUomini;
	// coda che contiene le donne in attesa di essere svegliate
    private LinkedList<Donna> codaDonne;
	// lock usati nel programma per proteggere, rispettivamente:
	// le code, il semaforo posti, il sesso corrente, la sommatoria dei tempi
	// d'attesa, e il contatore dell'aging
	private ReentrantLock lockCode, lockPosti, 
			              lockSesso, lockTempoAttesa,
			              lockAging;

	/** Costruttore del Bagno: richiama l'altro costruttore passandogli
	 * la capienza del bagno e il valore di trigger dell'aging di default
	 * (3 volte la capienza del bagno). */
	public Bagno (int capienza) {
		this(capienza, 3*capienza);
	}
	
	/** Costruttore del Bagno: inizializza tutti gli attributi del bagno, e,
	 * in particolare, la capienza e il valore di triggering dell'Aging. */
    public Bagno(int capienza, int valoreTriggerAging) {
		this.capienza = capienza;
		this.valoreTriggerAging = valoreTriggerAging;
		System.out.println("Trigger aging = " + this.valoreTriggerAging);
		this.contatoreAging = 0;
		this.sommaTempiAttesa = 0L;
		this.semaforoPosti = new Semaphore(capienza, true);
		this.codaUomini = new LinkedList<>();
		this.codaDonne = new LinkedList<>();
		this.lockCode = new ReentrantLock();
		this.lockPosti = new ReentrantLock();
		this.lockSesso = new ReentrantLock();
		this.lockTempoAttesa = new ReentrantLock();
		this.lockAging = new ReentrantLock();
	}
   
	/** Richiesta di accesso al bagno; il metodo blocca la persona
	 * chiamante finchè non è possibile accedere al bagno. */
    public void accessoAlBagno(Persona persona) {
		// se il bagno è vuoto, e il sesso corrente è diverso da quello
		// della persona, viene settato il sesso del bagno con quello
		// della persona 
		if (this.isEmpty() && this.getSessoCorrente() != persona.sesso) {
			System.out.println(persona.getThread().getName() + " trova il bagno vuoto");
			this.setSessoCorrente(persona.sesso);
		}
		// se il sesso della persona non è quello corrente del bagno, oppure 
		// se il contatore dell'aging ha raggiunto (o superato) il valore
		// di trigger, e la coda del sesso opposto non è vuota,
		// mettiti in attesa sulla coda del tuo sesso
		if ((this.getSessoCorrente() != persona.sesso) || 
			(this.getSessoCorrente() == persona.sesso &&
		     this.getContatoreAging() >= this.valoreTriggerAging &&
			 this.codaSessoOppostoIsNotEmpty(persona.sesso))) {
			this.mettiPersonaInAttesa(persona);
		}	
		// incrementa il contatore dell'aging
		this.incrementaContatoreAging();
		// acquisisci un posto in bagno (cioè un permesso nel semaforo posti) 
		try {
		// attende
			this.getSemaforoPosti().acquire();
		}
		catch (InterruptedException ex) {
			System.err.println(ex);
		}
	}
    
	/** Notifica l'uscita dal bagno; questo metodo sveglia le altre
	 * persone che sono in attesa di accedere al bagno. */ 
    public void notificaUscita(Persona persona) {
		System.out.println(persona.getThread().getName() + " esce dal bagno");
		// restituisce il permesso al semaforo, liberando un posto in bagno
		this.getSemaforoPosti().release();
		// se il bagno è vuoto
		if (this.isEmpty()) {
			System.out.println("Dopo l'uscita di " + persona.getThread().getName() +
					           " il bagno è nuovamente vuoto");
			// se la coda del sesso opposto non è vuota,
			// inverti il sesso corrente del bagno
			if (this.codaSessoOppostoIsNotEmpty(this.getSessoCorrente())) {
				this.invertiSessoCorrente();
			}
			// sveglia le persone in attesa sulla coda
			// relativa al sesso corrente del bagno
			this.svegliaPersoneInAttesa(this.getSessoCorrente());
		}
	}

	/** Aggiunge il tempo di attesa alla sommatoria dei tempi. */
	public void addTempoAttesa(long tempoAttesa) {
		// acquisisce il lock della sommatoria tempi di attesa
		this.lockTempoAttesa.lock();
		// aggiunge il tempo di attesa alla sommatoria
		try {
			this.sommaTempiAttesa += tempoAttesa;	
		}
		// rilascia il lock della sommatoria tempi di attesa
		finally {
			this.lockTempoAttesa.unlock();
		}
	}

	/** Restituisce il tempo di attesa medio; questo metodo viene richiamato
	 * alla fine della simulazione (altrimenti restituirebbe un risultato
	 * errato). */
	public long calcolaTempoAttesaMedio(int numeroUomini, int numeroDonne)
	throws ArithmeticException {
		// acquisisce il lock sulla sommatoria tempi di attesa
		this.lockTempoAttesa.lock();
		// ritorna la media, calcolata dividendo la somma dei tempi di attesa
		// per il numero totale di accessi al bagno
		try {
			return this.sommaTempiAttesa / (numeroUomini * 4 + numeroDonne * 8);
		}
		// rilascia il lock della sommatoria tempi di attesa 
		finally {
			this.lockTempoAttesa.unlock();
		}
	}


	// Restituisce true se ci sono persone sulla coda del sesso opposto 
    // a quello che gli viene passato, false altrimenti 
	private boolean codaSessoOppostoIsNotEmpty(Persona.Sesso sesso) {
		return sesso == Persona.Sesso.maschio? !this.getCodaDonne().isEmpty() : 
				                               !this.getCodaUomini().isEmpty();
	}
    
	// Restituisce true se il bagno è vuoto, false altrimenti
    private boolean isEmpty() {
		// vengono controllati i permessi nel semaforo, e se sono uguali alla capienza
		// del bagno significa che il bagno è vuoto, viene quindi restituito il
		// valore dell'espressione logica che segue
		return this.getSemaforoPosti().availablePermits() == this.capienza;
	}

	// inverte il sesso corrente del bagno
	private void invertiSessoCorrente() {
		this.setSessoCorrente(this.getSessoCorrente() == Persona.Sesso.maschio?
							  Persona.Sesso.femmina : Persona.Sesso.maschio);
	}


	// Restituisce il reference al semaforo dei posti
    private Semaphore getSemaforoPosti() {
		// acquisisce il lock del semaforo posti
		this.lockPosti.lock(); 
		// restituisce il reference al semaforo
		try {
			return this.semaforoPosti;
		}
		// rilascia il lock del semaforo posti
		finally {
			this.lockPosti.unlock();
		}
	}
	
	// Restituisce il sesso corrente del bagno 
	private Persona.Sesso getSessoCorrente() {
		// acquisisce il lock del sesso corrente
		this.lockSesso.lock();
		// restituisce il sesso
		try {
			return this.sessoCorrente;
		}
		// rilascia il lock del sesso corrente
		finally {
			this.lockSesso.unlock();
		}
	}

	// Restituisce il reference alla coda delle donne
	private LinkedList getCodaDonne() {
		// acquisisce il lock delle code
		this.lockCode.lock();
		// restituisce il reference della coda
		try {
			return this.codaDonne;
		}
	    // rilascia il lock delle code
		finally {
			this.lockCode.unlock();
		}
	}

	// Restituisce il reference alla coda degli uomini
	private LinkedList getCodaUomini() {
		// acquisisce il lock delle code
		this.lockCode.lock();
		// restituisce il reference della coda
		try {
			return this.codaUomini;
		}
		// rilascia il lock delle code
		finally {
			this.lockCode.unlock();
		}
}
	
	// Restituisce il valore del contatore dell'aging
	private int getContatoreAging() {
		// acquisisce il lock del contatore dell'aging
		this.lockAging.lock();
		// restituisce il contatore dell'aging
		try {
			return this.contatoreAging;
		}
		// rilascia il lock del contatore dell'aging
		finally {
			this.lockAging.unlock();
		}
	}

	// Azzera il contatore dell'aging 
 	private void resetContatoreAging() {
		// acquisisce il lock del contatore dell'aging
		this.lockAging.lock();
		// azzera il contatore 
		try {
			this.contatoreAging = 0;
		}
		// rilascia il lock del contatore dell'aging
		finally {
			this.lockAging.unlock();
		}
	}

	// Incrementa il contatore dell'aging 
	private void incrementaContatoreAging() {
		// acquisisce il lock del contatore dell'aging
		this.lockAging.lock();
		// incrementa il contatore
		try {
			this.contatoreAging++;
		}
		// rilascia il lock del contatore dell'aging
		finally {
			this.lockAging.unlock();
		}
	}

	// Setta il sesso corrente del bagno
	// con il sesso che gli viene passato
	private void setSessoCorrente(Persona.Sesso sesso) {
		// acquisice il lock del sesso corrente
		this.lockSesso.lock();
		// setta il sesso corrente 
		try {
			this.sessoCorrente = sesso;
		}
		// rilascia il lock del sesso corrente
		finally {
			this.lockSesso.unlock();
		}
		// azzera il contatore dell'aging 
		this.resetContatoreAging();
	}

	// Mette la persona che gli viene passata in attesa sulla coda
	// del proprio sesso 
	private void mettiPersonaInAttesa(Persona persona) {
		// acquisisce il lock delle code
		this.lockCode.lock();
		try {
			// se non è già stato fatto, inizializza la variabile
			// condizionale della persona
			if (persona.getCondition() == null) {
				persona.setCondition(this.lockCode.newCondition());	
			}
			// aggiunge la persona alla coda del suo sesso
			if (persona.sesso == Persona.Sesso.maschio) {
				this.codaUomini.add((Uomo) persona);
			}	
			else {
				this.codaDonne.add((Donna) persona);
			}
			// blocca la persona sulla sua variabile condizionale
			persona.getCondition().await();
		}
		catch(InterruptedException ex) {
			System.err.println(ex);
		}
		// rilascia il lock delle code 
		finally {
			this.lockCode.unlock();
		}
	}

	// Sveglia le persone del sesso che gli viene indicato 
	private void svegliaPersoneInAttesa(Persona.Sesso sesso) {
		// acquisisce il lock delle code 
		this.lockCode.lock();
		// seleziona la coda del sesso giusto, e finché non risulta
		// vuota, continua a estrarre le persone e a lanciargli il 
		// signal() per svegliarli dalle variabili condizionali 
		try {
			if (sesso == Persona.Sesso.maschio) {
				while(!this.codaUomini.isEmpty()) {
					this.codaUomini.remove().getCondition().signal();
				}
			}
			else {
				while(!this.codaDonne.isEmpty()) {
					this.codaDonne.remove().getCondition().signal();
				}
			}
		}
		// rilascia il lock delle code
		finally {
			this.lockCode.unlock();
		}
	}
}
