package lactao.eureka;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import lactao.dbio.AccessConnection;

public class Article {
	public String titre = "";
	public String source = "";
	public String texte = "";
	public ArrayList<String> auteurs = new ArrayList<String>();
	public Date dtPublication = new Date();
	public String noDocument = "";

	public void clean() {
		while (texte.contains("\r\n\r\n"))
			texte = texte.replace("\r\n\r\n", "\r\n");
		texte = texte.trim();
	}

	public void saveToDb(AccessConnection c) throws SQLException {
		
		System.out.println("Saving " + noDocument + " to db");

		String sql = "";
		sql += " INSERT INTO documents";
		sql += "   (titre, source, dt_publication, no_document, texte)";
		sql += " VALUES (?, ?, ?, ?, ?)";
		PreparedStatement pstt = c.prepareStatement(sql);
		pstt.setString(1, titre);
		pstt.setString(2, source);
		pstt.setDate(3, new java.sql.Date(dtPublication.getTime()));
		pstt.setString(4, noDocument);
		pstt.setString(5, texte);
		pstt.executeUpdate();
		pstt.close();

		sql = "";
		sql += " INSERT INTO auteurs";
		sql += "   (no_document, auteur)";
		sql += " VALUES (?, ?)";
		pstt = c.prepareStatement(sql);
		//TODO: pourrait être batch
		for (String auteur : auteurs) {
			pstt.setString(1, noDocument);
			pstt.setString(2, auteur);
			pstt.executeUpdate();
		}
		pstt.close();

	}
}
