package lactao.mallet.clustering;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import cc.mallet.cluster.Clustering;
import cc.mallet.cluster.clustering_scorer.ClusteringScorer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Metric;
import cc.mallet.types.SparseVector;
import cc.mallet.util.VectorStats;

/*
 * Cf.
 * http://ieeexplore.ieee.org/iel5/10869/34211/01631238.pdf?isnumber=34211&prod=CNF&arnumber=1631238&arSt=+32&ared=+38&arAuthor=+Hruschka%2C+E.R.%3B++Covoes%2C+T.F.
 */
public class SimplifiedSilhouetteEvaluator implements ClusteringScorer {

	Metric metric;

	public SimplifiedSilhouetteEvaluator(Metric metric) {
		this.metric = metric;
	}

	@Override
	public double score(Clustering clustering) {
		int[] labels = clustering.getLabels();
		InstanceList instances = clustering.getInstances();
		int numClusters = clustering.getNumClusters();

		// calculer centroides....
		SparseVector[] centroids = new SparseVector[numClusters];
		for (int c = 0; c < numClusters; ++c)
			centroids[c] = VectorStats.mean(clustering.getCluster(c));

		// calculer taille des clusters
		int[] clustSizes = new int[numClusters];
		for (int c = 0; c < numClusters; ++c)
			clustSizes[c] = clustering.getCluster(c).size();

		double sum = 0;
		for (int i = 0; i < instances.size(); ++i) {
			if (clustSizes[labels[i]] == 1)
				continue; // by def., silhouette = 0 for singletons
			SparseVector v = (SparseVector) instances.get(i).getData();
			double a = 0;
			double b = Double.MAX_VALUE;
			for (int c = 0; c < centroids.length; ++c)
				if (c == labels[i])
					a = metric.distance(v, centroids[c]);
				else {
					double outerDist = metric.distance(v, centroids[c]);
					if (outerDist < b)
						b = outerDist;
				}
			sum += (b - a) / Math.max(a, b);
		}
		return (sum / instances.size());
	}
}
