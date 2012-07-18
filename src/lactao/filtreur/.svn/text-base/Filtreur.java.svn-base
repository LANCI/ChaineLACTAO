package lactao.filtreur;

import java.io.IOException;
import java.sql.SQLException;

import lactao.dbio.AccessConnection;
import lactao.stemmer.Stemmer;
import lactao.stoplist.StopList;

public class Filtreur {

	private final AccessConnection cnn;

	public Filtreur(final AccessConnection cnn) {
		this.cnn = cnn;
	}

	private void applyStopList(final String stopListFileName,
			final Stemmer stemmer) throws IOException, SQLException {
		System.out.println("applyStopList()");
		final StopList stoplist = new StopList(stopListFileName, stemmer);
		stoplist.saveToDb(cnn);
		String sql = "";
		sql += " UPDATE matrice m";
		sql += " INNER JOIN stoplist s ON m.unif = s.token";
		sql += " SET exclue = True, raison_exclusion = 'stoplist'";
		sql += " WHERE NOT exclue";
		cnn.execute(sql);
	}

	private void excludeIfInLessThenPctDomifs(final double pct)
			throws SQLException {
		System.out.println("excludeIfInLessThenPctDomifs(" + pct + ")");

		final long nbDomifs = cnn.getCount("domifs");
		final double freq = (pct / 100) * nbDomifs;

		String sql = "";
		sql = "  UPDATE matrice m";
		sql += "   INNER JOIN unifs u ON m.unif = u.unif";
		sql += " SET exclue = True, raison_exclusion = 'low DF (<" + freq
				+ ")'";
		sql += " WHERE u.nb_domifs < " + freq + " AND NOT exclue";
		cnn.execute(sql);
	}

	private void excludeIfInMoreThenPctDomifs(final double pct)
			throws SQLException {
		System.out.println("excludeIfInMoreThenPctDomifs(" + pct + ")");

		final long nbDomifs = cnn.getCount("domifs");
		final double freq = (pct / 100) * nbDomifs;

		String sql = "";
		sql += " UPDATE matrice m";
		sql += "   INNER JOIN unifs u ON m.unif = u.unif";
		sql += " SET exclue = True, raison_exclusion = 'high DF (>" + freq
				+ ")'";
		sql += " WHERE u.nb_domifs > " + freq + " AND NOT exclue";
		cnn.execute(sql);
	}

	private void excludeNumbers() throws SQLException {
		System.out.println("excludeNumbers()");
		String sql = "";
		sql += " UPDATE matrice";
		sql += " SET exclue = True, raison_exclusion = 'number'";
		sql += " WHERE Val(unif) OR Left(unif, 1) = '0' AND NOT exclue";
		cnn.execute(sql);
	}

	private void excludeSingleLetterUnifs() throws SQLException {
		System.out.println("excludeSingleLetterUnifs()");
		String sql = "";
		sql += " UPDATE matrice";
		sql += " SET exclue = True, raison_exclusion = 'single letter'";
		sql += " WHERE Len(unif) = 1 AND NOT exclue";
		cnn.execute(sql);
	}

	public void filter(final String stopListFileName, final Stemmer stemmer,
			final double lowerBound, final double upperBound) throws Exception {

		resetMatriceFilter();
		printNbUnifsLeft();

		applyStopList(stopListFileName, stemmer); // exclure les stems contenus
													// dans la stoplist
		printNbUnifsLeft();
		excludeSingleLetterUnifs(); // exclure les unifs d'un seul caractère
		printNbUnifsLeft();
		excludeNumbers(); // exclure les unifs qui commencent par un chiffre
		printNbUnifsLeft();

		excludeIfInLessThenPctDomifs(lowerBound);
		printNbUnifsLeft();
		excludeIfInMoreThenPctDomifs(upperBound);
		printNbUnifsLeft();

		System.out.println("Filtreur done!");
	}

	private void printNbUnifsLeft() throws SQLException {
		System.out
				.println("  Unifs left: "
						+ cnn
								.getCount("(SELECT DISTINCT unif FROM matrice WHERE NOT exclue)"));
	}

	private void resetMatriceFilter() throws SQLException {
		System.out.println("resetMatriceFilter()");
		cnn.execute("UPDATE matrice SET exclue = False, raison_exclusion = ''");
	}
}