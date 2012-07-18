package lactao.mallet.clustering;

import java.util.Iterator;

import cc.mallet.cluster.Clustering;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Metric;
import cc.mallet.types.SparseVector;

/*
 * Cf.
 * http://ieeexplore.ieee.org/iel5/10869/34211/01631238.pdf?isnumber=34211&prod=CNF&arnumber=1631238&arSt=+32&ared=+38&arAuthor=+Hruschka%2C+E.R.%3B++Covoes%2C+T.F.
 * Note: je ne suis pas certain qu'il soit bien implémenté. Je l'ai abandonné au profit du Simplified Silhouette
 */
public class SilhouetteEvaluator {

	InstanceList[] clusters;
	Metric metric;

	public SilhouetteEvaluator(InstanceList[] clusters, Metric metric) {
		this.clusters = clusters;
		this.metric = metric;
	}

	public SilhouetteEvaluator(Clustering clustering, Metric metric) {
		this(clustering.getClusters(), metric);
	}

	/** returns the average distance between instance i and instances in il for metric m */
	private double getAvgDist(Instance i, InstanceList il) {
		double dist = 0;
		SparseVector v1 = (SparseVector) i.getData();
		Iterator<Instance> it = il.iterator();
		while (it.hasNext()) {
			SparseVector v2 = (SparseVector) it.next().getData();
			if (v1 != v2)
				dist += metric.distance(v1, v2);
		}
		return dist /= il.size();
	}

	private double getIndexForInstance(Instance i, int clusterNo) {
		double a = getAvgDist(i, clusters[clusterNo]); // avg inner dist
		double b = Double.MAX_VALUE; // will be minimum avg outer dist
		for (int c = 0; c < clusters.length; ++c)
			if (c != clusterNo) {
				double avgOuterDist = getAvgDist(i, clusters[c]);
				if (avgOuterDist < b)
					b = avgOuterDist;
			}
		return (b - a) / Math.max(a, b);
	}

	private double getIndexForCluster(int clusterNo) {
		double sum = 0;
		Iterator<Instance> it = clusters[clusterNo].iterator();
		while (it.hasNext())
			sum += getIndexForInstance(it.next(), clusterNo);
		return sum /= clusters[clusterNo].size();
	}

	public double getIndex() {
		double sum = 0;
		for (int c = 0; c < clusters.length; ++c)
			sum += getIndexForCluster(c);
		return sum /= clusters.length;
	}

}
