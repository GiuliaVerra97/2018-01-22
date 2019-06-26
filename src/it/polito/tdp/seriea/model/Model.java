package it.polito.tdp.seriea.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleGraph;

import com.mysql.cj.exceptions.DeadlockTimeoutRollbackMarker;

import it.polito.tdp.seriea.db.SerieADAO;

public class Model {

	private SerieADAO dao;
	private Team squadraSelezionata;
	
	private Map<Season,Integer> punteggi;			//punteggio relativo a una squadra in una stagione
	private Map<Integer, Season> mappaStagioni;
	private List<Season> stagioni;

	private List<Team> squadre;
	private Map<String, Team> mappaSquadre;
	private List<Season> stagioniConsecutive;
	
	private Graph<Season, DefaultWeightedEdge> grafo;
	
	private List<Season> percorsoBest;
	
	
	public Model() {
		
		dao=new SerieADAO();
		
		punteggi=new HashMap<Season, Integer>();
		mappaStagioni=new HashMap<Integer, Season>();		//utile per il DB, così da vedere a che numero corrisponde la stagione
		mappaSquadre=new HashMap<String, Team>();
		
		this.squadre=dao.listTeams();
		this.stagioni=dao.listAllSeasons();
		
		for(Season s: this.stagioni) {
			this.mappaStagioni.put(s.getSeason(), s);
		}
	
		
		for(Team t: this.squadre) {
			this.mappaSquadre.put(t.getTeam(), t);
		}
		
	}
	
	
	
	
	public Map<Season, Integer> calcolaPunteggi(Team squadra) {
		
		this.squadraSelezionata=squadra;
		
		this.punteggi=new HashMap<Season, Integer>();
		List<Match> partite=dao.listaMatches(squadra, mappaStagioni, mappaSquadre);
		
		for(Match m: partite) {
		
			Season stagione=m.getSeason();
			int punti=0;
			
			if(m.getFtr().equals("D")) {
				punti=1;
			}else {
				if(m.getHomeTeam().equals(squadra) && m.getFtr().equals("H") || (m.getAwayTeam().equals(squadra) && m.getFtr().equals("A"))) {		//se la squadra giocava in casa e ha vinto o al contrario(giocava fuori e ha vinto)
					punti=3;
				}
			}
			
			Integer attuale=punteggi.get(stagione);		//devo mettere integer e non int se no non posso inserirlo nella mappa
			
			if(attuale==null) {
				attuale=0;
			}
			
			punteggi.put(stagione, attuale+punti);		//senza rimuoverlo, lo sovrascrive
			
		}
		
		return punteggi;
	}
	

	
	

	
	
	
	//get per menu tendina
	public List<Team> getTeams() {
		return this.squadre;
	}
	
	
	public Season calcolaAnnataOro() {
		
		this.grafo=new SimpleDirectedWeightedGraph<Season, DefaultWeightedEdge>(DefaultWeightedEdge.class);		//vertici: tutte le stagioni in cui ha giocato la squadra
		
		Graphs.addAllVertices(grafo, punteggi.keySet());	//punteggi.keyset(); mi da le stagioni in cui ha giocato la squadra
		
		for(Season s1: punteggi.keySet()) {
			for(Season s2: punteggi.keySet()) {
				if(!s1.equals(s2)) {
					
					int punti1=punteggi.get(s1);
					int punti2=punteggi.get(s2);
					
					if(punti1>punti2) {
						Graphs.addEdge(this.grafo, s2 , s1, punti1-punti2);
					}else{
						Graphs.addEdge(this.grafo, s1 , s2, punti2-punti1);
					}
					
				}
			}
		}
		
		Season migliore=null;
		int max=0;
		
		for(Season s: grafo.vertexSet()) {
			int valore=pesoStagione(s);
			if(valore>max) {
				max=valore;
				migliore=s;
			}
		}
		
		
		return migliore;
	}




	private int pesoStagione(Season s) {
		int somma=0;
		
		for(DefaultWeightedEdge e: grafo.incomingEdgesOf(s)) {
			somma=somma+(int)grafo.getEdgeWeight(e);
		}
		
		for(DefaultWeightedEdge e:grafo.outgoingEdgesOf(s)) {
			somma=somma-(int)grafo.getEdgeWeight(e);
		}
		
		return somma;
	}
	
	
	
	
	
	
	
	
	
	
	public List<Season> camminoVirtuoso() {
		
		//trova le stagioni consecutive
		this.stagioniConsecutive=new ArrayList<Season>(punteggi.keySet());
		Collections.sort(stagioniConsecutive);
		
		List<Season> parziale=new ArrayList<Season>();
		this.percorsoBest=new ArrayList<>();
		
		
		//itera all livello zero
		for(Season s: grafo.vertexSet()) {
			parziale.add(s);
			cerca(1, parziale);
			parziale.remove(0);		//tolgo la stagione per ripartire da 0 ogni volta che considero un nuovo vertice
		}
		
		
		return percorsoBest;
		
	}
	
	
	
	
	/*RICORSIONE;
	 * 
	 * Soluzione pariziale: lista Season(lista vertici)
	 * Livello ricorsione: lunghezza della lista
	 * Casi terminali: non trovo altri vertici da aggiungere->verifica che il cammino ha lunghezza massima tra quelli visti fino ad ora
	 * Generazione delle soluzioni: vertici connessi all'ultimo vertice del percorso(con arco orientato nel verso giusto), vertici non ancora considerati, che siano stagioni consecutive
	 *
	*/
	
	public void cerca(int livello, List<Season> parziale) {
		
		//genera nuove soluzioni
		
			boolean trovato=false;
		
			Season ultimo=parziale.get(livello-1);
			
			for(Season prossimo: Graphs.successorListOf(this.grafo, ultimo)) {
				
				if(!parziale.contains(prossimo)) {
					if(stagioniConsecutive.indexOf(ultimo)+1==stagioniConsecutive.indexOf(prossimo)) {		//mi ricava l'indice, la posizione dell'oggetto
						//candidato accettabile->fai ricorsione
						trovato=true;
						parziale.add(prossimo);
						cerca(livello+1, parziale);
						parziale.remove(livello);
					}
				}
				
			}
		
			
		//valuta caso terminale
		if(!trovato) {
			if(parziale.size()>percorsoBest.size()) {
				percorsoBest=new ArrayList<Season>(parziale);		//clono
			}
		}
		
		
	}
	
	
	
	
}
