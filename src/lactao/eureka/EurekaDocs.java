package lactao.eureka;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import lactao.concordancier.Concordancier;
import lactao.concordancier.SentenceConcordancier;
import lactao.dbio.AccessConnection;

public class EurekaDocs {

	private AccessConnection cnn;
	private EurekaParser ep = new EurekaParser();

	public EurekaDocs(AccessConnection c) {
		this.cnn = c;
	}

	public void resetDb() throws SQLException {
		cnn.createTableDocuments();
		cnn.createTableAuteurs();
	}

	public void saveArticlesToDb() throws SQLException {
		for (Article a : ep.getArticles())
			a.saveToDb(cnn);
	}

	public void getArticlesFromPath(File path) throws FileNotFoundException, IOException {
		if (path.isDirectory()) {
			File[] a = path.listFiles();
			for (File f : a)
				getArticlesFromPath(f);
		} else {
			String name = path.getName().toLowerCase();
			if (name.endsWith("html") || name.endsWith("htm"))
				ep.parse(path);
		}
	}

	public void getSentenceConcordanceForArticles(int nbUnitsBefore, int nbUnitsAfter, String target) throws SQLException {
		cnn.createTableDomifs();
		Concordancier c = new SentenceConcordancier("", nbUnitsBefore, nbUnitsAfter);
		ResultSet rs = cnn.getRS("SELECT no_document, texte FROM documents");
		PreparedStatement ps = cnn.prepareStatement("INSERT INTO domifs (no_document, texte) VALUES (?, ?)");
		if (rs != null) {
			while (rs.next()) {
				String noDoc = rs.getString("no_document");
				ps.setString(1, noDoc);
				System.out.println("Getting concordance for " + noDoc);
				c.setText(rs.getString("texte"));
				List<String> a = c.getContexts(target, true, true, false);
				for (String s: a) {
					ps.setString(2, s);
					ps.addBatch();
				}
			}
		}
		ps.executeBatch(); 
		ps.close();
	}
}
