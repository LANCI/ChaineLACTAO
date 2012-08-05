package lactao.tokeniseur;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lactao.dbio.AccessConnection;
import lactao.stemmer.Stemmer;

public class Tokenizeur {

	private AccessConnection cnn;
	
	public Tokenizeur(AccessConnection cnn) {
		this.cnn = cnn;
	}

	private void createMatrix(Stemmer stemmer) throws SQLException {
		System.out.println("createMatrix");
		cnn.createTableMatrice();
		Pattern p = Pattern.compile("[\\p{L}\\p{N}\\p{Pc}]+");
		long nbDomifs = cnn.getCount("domifs");
		ResultSet rs = cnn.getRS("SELECT no_domif, texte FROM domifs");		
		if (rs != null) {
			PreparedStatement pstLongDomifs = cnn.prepareStatement("UPDATE domifs SET longueur = ? WHERE no_domif = ?");
			while (rs.next()) {

				// donner un peu d'info à l'utilisateur pour qu'il sache où on en est
				int n = rs.getInt("no_domif");
				String t = rs.getString("texte");
				System.out.println("Creating matrix for domif: " + n + " / " + nbDomifs);

				int tokenCount = 0;

				// pour chaque mot dans le segment
				Matcher m = p.matcher(t);
				// note: dans le corpus de JF, le plus gros lexique pour un domif est de 180
				HashMap<String, Integer> lexique = new HashMap<String, Integer>(200, 0.9f);
				while (m.find()) {
					tokenCount++;
					String token = m.group();
					if (stemmer != null)
						token = stemmer.stem(token);
					token = token.toLowerCase(Locale.FRENCH);
					/*
					 * si le mot n'est pas déjà dans le lexique de la classe, on initialise sa
					 * fréquence à 1. S'il est déjà là, on va chercher sa fréquence actuelle
					 * incrémentée de 1
					 */
					Integer freq = lexique.get(token);
					freq = (freq == null ? 1 : ++freq);
					lexique.put(token, freq);
				}

				pstLongDomifs.setInt(1, tokenCount);
				pstLongDomifs.setInt(2, n);
				pstLongDomifs.addBatch();
				
				/* boucler dans le lexique et sauver les fréquences dans la bd */
				String sql = "INSERT INTO matrice (no_domif, unif, freq) VALUES (?, ?, ?)";
				PreparedStatement ps = cnn.prepareStatement(sql);
				for (Map.Entry<String, Integer> entry : lexique.entrySet()) {
					String unif = entry.getKey();
					int freq = entry.getValue();
					ps.setInt(1, n);
					ps.setString(2, unif);
					ps.setInt(3, freq);
					ps.addBatch();
				}
				ps.executeBatch();
			}
			rs.close();
			pstLongDomifs.execute();
		}

	}

	private void createLexicon() throws SQLException {
		System.out.println("createLexicon()");
		cnn.createTableUnifs();
		String sql="";
		sql += " INSERT INTO unifs (unif, nb_domifs, freq_globale, idf)";
		sql += " SELECT unif,";
		sql += "   Count(no_domif) AS nb_domifs,";
		sql += "   Sum(freq) AS freq_globale,";
		sql += "   (" + cnn.getCount("domifs") + " / nb_domifs) AS idf";
		sql += " FROM matrice GROUP BY unif";
		cnn.execute(sql);
				
	}

	public void tokenize() throws SQLException, ClassNotFoundException {
		tokenize(null);
	}
	
	public void tokenize(Stemmer stemmer) throws SQLException, ClassNotFoundException {
		createMatrix(stemmer);
		createLexicon();
		System.out.println("Tokeniseur done!");
	}
}
