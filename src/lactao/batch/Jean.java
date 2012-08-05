package lactao.batch;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import lactao.concordancier.Concordancier;
import lactao.concordancier.SentenceConcordancier;
import lactao.dbio.AccessConnection;
import lactao.mallet.clustering.MalletClusterer;
import lactao.stemmer.FrenchStemmer;
import lactao.stemmer.Stemmer;
import cc.mallet.types.Metric;
import cc.mallet.types.NormalizedDotProductMetric;

public class Jean {

	public static void newSentenceConcordance(AccessConnection cnn,
			int nbUnitsBefore, int nbUnitsAfter, String target)
			throws SQLException {
		cnn.createTableDomifs();
		addSentenceConcordance(cnn, nbUnitsBefore, nbUnitsAfter, target);
	}

	public static void addSentenceConcordance(AccessConnection cnn,
			int nbUnitsBefore, int nbUnitsAfter, String target)
			throws SQLException {
		Concordancier c = new SentenceConcordancier("", nbUnitsBefore,
				nbUnitsAfter);
		ResultSet rs = cnn.getRS("SELECT no_document, texte FROM documents");
		PreparedStatement ps = cnn
				.prepareStatement("INSERT INTO domifs (no_document, texte) VALUES (?, ?)");
		if (rs != null) {
			while (rs.next()) {
				String noDoc = rs.getString("no_document");
				ps.setString(1, noDoc);
				System.out.println(noDoc);
				c.setText(rs.getString("texte"));
				List<String> a = c.getContexts(target, true, true, true);
				for (String s : a) {
					ps.setString(2, s);
					ps.addBatch();
				}
			}
		}
		ps.executeBatch();
		ps.close();
	}

	public static void main(String[] args) {
		try {
			AccessConnection cnn;
			Metric metric = new NormalizedDotProductMetric();

			Stemmer stemmer = new FrenchStemmer();
			cnn = new AccessConnection("c:/bergson/K-MeansIV_22_03_10.mdb");
//			CorpusParser.parse(new File("c:/bergson/corpus/corpus.txt"), cnn);
//			newSentenceConcordance(cnn, 2, 2, "langage");
//			(new Tokenizeur(cnn)).tokenize(stemmer);
//			(new Filtreur(cnn))
//					.filter("c:/bergson/stoplist.txt", stemmer, 0.5, 40);
			String label = "BERGSON";
			MalletClusterer mc = new MalletClusterer(cnn);
//			mc.resetClassesInDb();
//
//			PrintWriter logger = new PrintWriter(new FileWriter(
//					"c:/bergson/langage_ssv.log"));
//			ClusteringScorer scorer = new SimplifiedSilhouetteEvaluator(metric);
//			Clustering cl = mc.getOptimalClustering(mc
//					.getInstanceListFromMatrix(label, "freq"), 20, 20,
//					scorer, logger);
//			mc.addClusteringToDB(cl, label);
//
//			mc.addCentroidsToDb();
			mc.makeGlobalDistanceMatrix();
			mc.motsRepresentatifs();
			
			cnn.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
