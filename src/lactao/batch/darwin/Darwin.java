package lactao.batch.darwin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lactao.concordancier.Concordancier;
import lactao.concordancier.SentenceConcordancier;
import lactao.dbio.AccessConnection;
import lactao.filtreur.Filtreur;
import lactao.graphexport.EmptyEdge;
import lactao.mallet.clustering.MalletClusterer;
import lactao.tokeniseur.Tokenizeur;
import lactao.util.file.FileUtils;
import lactao.util.log.Logger;

import org.apache.commons.collections15.Transformer;

import cc.mallet.cluster.Clustering;
import cc.mallet.types.InstanceList;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.io.PajekNetWriter;

public class Darwin {

	private static Logger l;

	private static final String path = "c:/lactao/darwin/darwin_";
	private static final String baseFileName = path + "base.mdb";

	private static final Set<String> triedWords = new HashSet<String>();

	private static final Graph<String, EmptyEdge> wordGraph = new DirectedSparseGraph<String, EmptyEdge>();
	private static final Map<String, Long> domifCounts = new HashMap<String, Long>();

	private static void addAllParagraphsAsDomifs(final AccessConnection cnn)
			throws SQLException {
		cnn.createTableDomifs();
		String sql = "";
		sql += "INSERT INTO domifs (no_document, texte)";
		sql += "SELECT no_document, texte FROM documents";
		cnn.execute(sql);
		domifCounts.put("*", cnn.getCount("domifs"));
	}

	public static void addSentenceConcordance(final AccessConnection cnn,
			final int nbUnitsBefore, final int nbUnitsAfter, final String target)
			throws SQLException {
		l.log("Adding concordance for " + target);
		final SentenceConcordancier c = new SentenceConcordancier("", nbUnitsBefore,
				nbUnitsAfter);
		final ResultSet rs = cnn
				.getRS("SELECT no_document, texte FROM documents");
		final PreparedStatement ps = cnn
				.prepareStatement("INSERT INTO domifs (no_document, texte) VALUES (?, ?)");
		l.log("  finding contexts");
		if (rs != null) {
			
			final String p = "([^.!?\\r\\n]*)(\\b" + Pattern.quote(target) 
					+ "\\b)([^.!?\\r\\n]*[.!?\\r\\n]+\\s*)";
			
			while (rs.next()) {
				final String noDoc = rs.getString("no_document");
				ps.setString(1, noDoc);
				final Matcher m = Pattern.compile(p,
						Pattern.MULTILINE + Pattern.UNICODE_CASE + Pattern.CASE_INSENSITIVE).matcher(rs.getString("texte"));
				final List<String> a = c.getContexts(m);
				for (final String s : a) {
					ps.setString(2, s);
					ps.addBatch();
				}
			}
		}
		l.log("  saving contexts");
		ps.executeBatch();
		ps.close();
	}

	public static void diggForWords(final Set<String> wordList)
			throws Exception {
		final Set<String> nextLevelWords = new HashSet<String>();
		for (final String w : wordList) {
			l.log("Digging for word: " + w
					+ "---------------------------------");
			final String targetFile = path + w + ".mdb";
			FileUtils.copyFile(baseFileName, targetFile);
			final AccessConnection cnn = new AccessConnection(targetFile);
			newSentenceConcordance(cnn, 2, 2, w);
			doClustering(cnn);
			nextLevelWords.addAll(getNextLevelWords(cnn, w));
			cnn.close();
		}
		if (!nextLevelWords.isEmpty())
			diggForWords(nextLevelWords);
	}

	private static void doClustering(final AccessConnection cnn)
			throws Exception {

		(new Tokenizeur(cnn)).tokenize(null);
		(new Filtreur(cnn)).filter("c:/lactao/darwin/stoplist.txt", null, 5.0,
				60);

		// s'assurer que "evolution" n'est pas filtré
		cnn.execute("UPDATE matrice SET exclue = false"
				+ " WHERE unif = 'evolution'");

		final String label = "OS6";
		final MalletClusterer mc = new MalletClusterer(cnn);
		mc.resetClassesInDb();

		final InstanceList il = mc.getInstanceListFromMatrix(label, "freq");
		final int nbClusters = roundUp(il.size() / 10.0); // utiliser plutôt sqrt?
		l.log("Expected number of clusters:" + nbClusters);
		if (nbClusters > 0) {
			final Clustering cl = mc.cluster(il, nbClusters);
			mc.addClusteringToDB(cl, label);
			mc.addCentroidsToDb();
			mc.makeGlobalDistanceMatrix();
			mc.addLexiquesToDB();
			mc.motsRepresentatifs();
		}
	}

	private static void exportWordGraph() throws Exception {

		final PajekNetWriter<String, EmptyEdge> pw = new PajekNetWriter<String, EmptyEdge>();
		final Transformer<String, String> tV = new Transformer<String, String>() {
			public String transform(final String w) {
				return w + " (" + domifCounts.get(w) + ")";
			}
		};
		final Transformer<EmptyEdge, Number> tE = new Transformer<EmptyEdge, Number>() {
			public Number transform(final EmptyEdge e) {
				return 1;
			}
		};

		pw.save(wordGraph, path + "graph.net", tV, tE);
	}

	/**
	 * Va chercher toutes les classes où les mots "evolution", "evolve" ou
	 * "evolved" apparaissent et retourne un ensemble constitué des mots les
	 * plus représentatifs de ces classes qui n'ont pas déjà été essayés.
	 */
	private static Set<String> getNextLevelWords(final AccessConnection cnn,
			final String parentWord) throws SQLException {
		final Set<String> words = new HashSet<String>();
		final StringBuilder sql = new StringBuilder();

		sql.append(" SELECT DISTINCT c.unifs_representatives");
		sql.append(" FROM classes c INNER JOIN lexique_classes l");
		sql.append("   ON c.id_classe = l.id_classe");
		sql.append(" WHERE l.unif='evolution'");
		sql.append("   OR l.unif LIKE 'evolve?'");
		// sql.append("   OR l.unif = 'evolved'");
		// sql.append("   OR l.unif = 'evolve'");
		final ResultSet rs = cnn.getRS(sql.toString());
		List<String> unifsRepr = new ArrayList<String>();
		while (rs.next())
			unifsRepr.add(rs.getString(1));

		l.log("Getting words for next level");
		for (String u : unifsRepr) {
			final String w = u.split(" ")[0];

			wordGraph.addEdge(new EmptyEdge(), parentWord, w);

			if (triedWords.contains(w)) {
				l.log("'" + w + "' already tried");
			} else {
				l.log("Adding " + parentWord + " -> " + w);
				words.add(w);
				triedWords.add(w);
			}
		}
		return words;
	}

	public static void main(final String[] args) {

		AccessConnection cnn = null;
		try {
			final String targetFile = path + "global.mdb";
			// FileUtils.copyFile(baseFileName, targetFile);

			l = new Logger("c:/lactao/darwin.log");

			cnn = new AccessConnection(targetFile);
			addAllParagraphsAsDomifs(cnn);
			doClustering(cnn);

			final Set<String> nextLevelWords = getNextLevelWords(cnn, "*");
			cnn.close();

			diggForWords(nextLevelWords);

			exportWordGraph();

			l.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public static void newSentenceConcordance(final AccessConnection cnn,
			final int nbUnitsBefore, final int nbUnitsAfter, final String target)
			throws SQLException {
		cnn.createTableDomifs();
		addSentenceConcordance(cnn, nbUnitsBefore, nbUnitsAfter, target);
		domifCounts.put(target, cnn.getCount("domifs"));

	}

	private static int roundUp(double d) {
		final double f = Math.floor(d);
		if (f < d)
			d = f + 1;
		return (int) d;
	}
}
