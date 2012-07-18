package lactao.batch;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import cc.mallet.cluster.Clustering;
import cc.mallet.cluster.clustering_scorer.ClusteringScorer;
import cc.mallet.fst.confidence.NBestViterbiConfidenceEstimator;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Metric;
import cc.mallet.types.Minkowski;
import cc.mallet.types.NormalizedDotProductMetric;

import lactao.corpuses.Corpus;
import lactao.dbio.AccessConnection;
import lactao.eureka.ArticleSplitter;
import lactao.eureka.EurekaDocs;
import lactao.filtreur.Filtreur;
import lactao.graphexport.GraphExport;
import lactao.mallet.clustering.MalletClusterer;
import lactao.mallet.clustering.SimplifiedSilhouetteEvaluator;
import lactao.stemmer.FrenchStemmer;
import lactao.stemmer.Stemmer;
import lactao.tokeniseur.Tokenizeur;

public class JF {

	private static ArrayList<Corpus> corpuses = new ArrayList<Corpus>();

	private static void initCorpuses() {
//		final String[][] ranges = { { "1988-01-01", "2006-10-31" },
//				{ "2006-11-01", "2007-07-31" }, { "2007-08-01", "2008-01-31" },
//				{ "2008-02-01", "2009-12-31" } };
//		final String[][] sources = { { "La Presse", "P" },
//				{ "Le Devoir", "D" }, { "Le Soleil", "S" } };

		final String[][] ranges = { { "1988-01-01", "2009-12-31" } };
		final String[][] sources = { { "*", "X" } };

		for (int i = 0; i < ranges.length; ++i) {
			for (String[] source : sources) {
				Date startDate = Date.valueOf(ranges[i][0]);
				Date endDate = Date.valueOf(ranges[i][1]);
				String code = source[1] + (i + 1);
				corpuses.add(new Corpus(source[0], startDate, endDate, code));
			}
		}
	}

	public static void main(String[] args) {
		try {
			AccessConnection cnn = new AccessConnection("c:/accrais/corpus.mdb");
			EurekaDocs ed = new EurekaDocs(cnn);
			ed.resetDb();
			ed.getArticlesFromPath(new File("c:/accrais/corpus"));
			ed.saveArticlesToDb();
			ed.getSentenceConcordanceForArticles(2, 2, "accommodement");
			Stemmer stemmer = new FrenchStemmer();
			(new Tokenizeur(cnn)).tokenize(stemmer);
			(new Filtreur(cnn)).filter("c:/accrais/stoplist.txt", stemmer, 1, 40);
			/*
			 * Note: il faut faire toutes les étapes AVANT de splitter le
			 * corpus! La raison en est que, si on veut comparer les classes
			 * entre elles, elles doivent avoir le même lexique.
			 */
			initCorpuses();
			ArticleSplitter.split(cnn, corpuses);
			ArticleSplitter.createLexiqueCorpus(cnn, corpuses);
			ArticleSplitter.updateTFIDF(cnn);

			MalletClusterer mc = new MalletClusterer(cnn);
			mc.resetClassesInDb();

			Metric metric = new NormalizedDotProductMetric();
			ClusteringScorer scorer = new SimplifiedSilhouetteEvaluator(metric);

			for (Corpus c : corpuses) {
				Clustering cl;
				InstanceList il;
				PrintWriter logger;

				il = mc.getInstanceListFromMatrix(c.label, "freq");
				cl = mc.cluster(il, 30);
				// logger = new PrintWriter(new FileWriter("c:/accrais/logs/"
				// + c.label + ".log"));
				// cl = mc.getOptimalClustering(il, 30, 30, scorer, logger);

				mc.addClusteringToDB(cl, c.label);
				mc.addDistanceMatrixToDB(cl, c.label);
				String baseFileName = "c:/accrais/graphs/" + c.label;
				GraphExport ge = new GraphExport(c.label, cnn);
				ge.exportToPajek(baseFileName + "_fullgraph.net", baseFileName
						+ "_mintree.net");
			}
			mc.addCentroidsToDb();
			mc.makeGlobalDistanceMatrix();
			mc.addLexiquesToDB();
			mc.motsRepresentatifs();
			cnn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
