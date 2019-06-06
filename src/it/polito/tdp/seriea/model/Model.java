package it.polito.tdp.seriea.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.polito.tdp.seriea.db.SerieADAO;

public class Model {

	private SerieADAO dao;
	private Team squadraSelezionata;
	
	private Map<Season,Integer> punteggi;			//punteggio relativo a una squadra in una stagione
	private Map<Integer, Season> mappaStagioni;
	private List<Season> stagioni;

	private List<Team> squadre;
	private Map<String, Team> mappaSquadre;
	
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
	
	
	
	
	
}
