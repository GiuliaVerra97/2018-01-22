package it.polito.tdp.seriea.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.polito.tdp.seriea.model.Match;
import it.polito.tdp.seriea.model.Season;
import it.polito.tdp.seriea.model.Team;

public class SerieADAO {

	public List<Season> listAllSeasons() {
		String sql = "SELECT season, description FROM seasons";
		List<Season> result = new ArrayList<>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();

			while (res.next()) {
				result.add(new Season(res.getInt("season"), res.getString("description")));
			}

			conn.close();
			return result;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	
	
	
	public List<Team> listTeams() {
		String sql = "SELECT team FROM teams";
		List<Team> result = new ArrayList<>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();

			while (res.next()) {
				result.add(new Team(res.getString("team")));
			}

			conn.close();
			return result;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	
	
	
	
	
	
	public List<Match> listaMatches(Team team, Map<Integer, Season> mappaStagioni, Map<String, Team> mappaSquadre){
		String sql="SELECT match_id, Season, `Div`, DATE, HomeTeam, AwayTeam, FTHG, FTAG, FTR " + 
				"FROM matches " + 
				"WHERE HomeTeam=? OR AwayTeam=? ";
		
		
		List<Match> result = new ArrayList<>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			st.setString(1, team.getTeam());		//devo passare la stringa
			st.setString(2, team.getTeam());

			ResultSet res = st.executeQuery();

			while (res.next()) {
				result.add(new Match(res.getInt("match_id"), mappaStagioni.get(res.getInt("Season")), res.getString("div"), res.getDate("date").toLocalDate(), mappaSquadre.get(res.getString("hometeam")), mappaSquadre.get(res.getString("awayteam")), res.getInt("fthg"), res.getInt("ftag"), res.getString("ftr")));
			}

			conn.close();
			return result;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		
	}
	
	
	
}
