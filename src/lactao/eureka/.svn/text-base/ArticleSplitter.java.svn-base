package lactao.eureka;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import lactao.corpuses.Corpus;
import lactao.dbio.AccessConnection;

public class ArticleSplitter {

	public static void split(AccessConnection cnn, ArrayList<Corpus> corpuses)
			throws SQLException {
		cnn.createTableCorpuses();
		String sql = "INSERT INTO corpus (corpus, nb_documents, nb_domifs) VALUES (?, ?, ?)";
		PreparedStatement pst = cnn.prepareStatement(sql);
		for (Corpus c : corpuses) {
			ArticleSplitter.createSubset(cnn, c.label, c.source, c.startDate,
					c.endDate);
			pst.setString(1, c.label);
			pst.setBigDecimal(2, BigDecimal.valueOf(getNbDocumentsFromDb(cnn,
					c.label)));
			pst.setBigDecimal(3, BigDecimal.valueOf(getNbDomifsFromDb(cnn,
					c.label)));
			// note: j'utilise BigDecimal parce que le driver JDBC-ODBC ne
			// supporte setLong sur un
			// prepared statement (NP 2008-11-28)
			pst.addBatch();
		}
		pst.executeBatch();
	}

	static public void createSubset(AccessConnection cnn, String corpus,
			String articleSource, Date startDate, Date endDate)
			throws SQLException {
		// Note: utiliser Date.valueOf("YYYY-MM-DD") pour initialiser facilement
		// les dates...

		String sql = "";
		sql += " UPDATE documents";
		sql += " SET corpus = ?";
		sql += " WHERE dt_publication >= ?";
		sql += "   AND dt_publication <= ?";

		if (!articleSource.equals("*"))
			sql += "   AND source LIKE ?";
		
		PreparedStatement ps = cnn.prepareStatement(sql);
		ps.setString(1, corpus);
		ps.setDate(2, startDate);
		ps.setDate(3, endDate);
		
		if (!articleSource.equals("*"))
			ps.setString(4, articleSource);
		
		ps.execute();

	}

	private static long getNbDocumentsFromDb(AccessConnection cnn,
			String classificationLabel) throws SQLException {
		assert !classificationLabel.contains("'") : "Apostrophe illégale dans le libellé de la classification";
		String sql = "(SELECT no_document FROM documents WHERE corpus = '"
				+ classificationLabel + "')";
		return cnn.getCount(sql);
	}

	private static long getNbDomifsFromDb(AccessConnection cnn, String label)
			throws SQLException {
		String sql = "";
		sql += " SELECT Count(d2.no_domif) AS nb_domifs";
		sql += " FROM documents d1 INNER JOIN domifs d2 ON d1.no_document = d2.no_document";
		sql += " GROUP BY d1.corpus HAVING d1.corpus = '" + label + "'";
		ResultSet rs = cnn.getRS(sql);
		if (rs.next())
			return rs.getLong("nb_domifs");
		else
			return 0;
	}

	public static void createLexiqueCorpus(AccessConnection cnn,
			ArrayList<Corpus> corpuses) throws SQLException {
		cnn.createTableLexiqueCorpus();
		String sql = "";
		sql += " INSERT INTO lexique_corpus (corpus, unif, nb_domifs)";
		sql += " SELECT d.corpus, m.unif, Count(s.no_domif) AS nb_domifs";
		sql += " FROM (documents d";
		sql += "   INNER JOIN domifs s ON d.no_document = s.no_document)";
		sql += "   INNER JOIN matrice m ON s.no_domif = m.no_domif";
		sql += " GROUP BY d.corpus, m.unif, m.exclue";
		sql += " HAVING NOT m.exclue";
		cnn.execute(sql);
	}

	public static void updateTFIDF(AccessConnection cnn) throws SQLException {
		String sql = "";
		sql += " UPDATE (documents d";
		sql += "   INNER JOIN (matrice m";
		sql += "     INNER JOIN domifs s";
		sql += "     ON m.no_domif = s.no_domif)";
		sql += "   ON d.no_document = s.no_document)";
		sql += "   INNER JOIN (lexique_corpus l";
		sql += "     INNER JOIN corpus c";
		sql += "     ON l.corpus = c.corpus)";
		sql += "   ON (m.unif = l.unif)";
		sql += "     AND (d.corpus = c.corpus)";
		sql += " SET m.tfidf = freq * Log(c.nb_domifs/l.nb_domifs)";
		sql += " WHERE NOT m.exclue";
		cnn.execute(sql);
	}
}