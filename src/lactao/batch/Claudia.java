package lactao.batch;

import java.io.FileWriter;
import java.io.PrintWriter;

import lactao.dbio.AccessConnection;
import lactao.filtreur.Filtreur;
import lactao.mallet.clustering.MalletClusterer;
import lactao.mallet.clustering.SimplifiedSilhouetteEvaluator;
import lactao.stemmer.FrenchStemmer;
import lactao.stemmer.Stemmer;
import lactao.tokeniseur.Tokenizeur;
import cc.mallet.cluster.Clustering;
import cc.mallet.cluster.clustering_scorer.ClusteringScorer;
import cc.mallet.types.Metric;
import cc.mallet.types.NormalizedDotProductMetric;

public class Claudia {

	public static void main(String[] args) {
		try {
			AccessConnection cnn;
			Metric metric = new NormalizedDotProductMetric();

			cnn = new AccessConnection("c:/lactao/claudia2/claudia2.mdb");
			Stemmer stemmer = new FrenchStemmer();
			(new Tokenizeur(cnn)).tokenize(stemmer);
			(new Filtreur(cnn)).filter("c:/lactao/claudia2/stoplist.txt", stemmer, 0.5, 40);
			String label = "2";
			MalletClusterer mc = new MalletClusterer(cnn);
			mc.resetClassesInDb();

			PrintWriter logger = new PrintWriter(new FileWriter("c:/lactao/claudia2/claudia2.log"));
			ClusteringScorer scorer = new SimplifiedSilhouetteEvaluator(metric);
			Clustering cl = mc.getOptimalClustering(mc.getInstanceListFromMatrix(label, "freq"), 35, 45,
					scorer, logger);
			mc.addClusteringToDB(cl, label);
			
			mc.addCentroidsToDb();
			mc.makeGlobalDistanceMatrix();
			
			cnn.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
