package lactao.mallet.clustering;

import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import cc.mallet.cluster.Clustering;
import cc.mallet.cluster.KMeans;
import cc.mallet.cluster.clustering_scorer.ClusteringScorer;
import cc.mallet.pipe.Noop;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureVector;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Metric;
import cc.mallet.types.NormalizedDotProductMetric;
import cc.mallet.types.SparseVector;
import cc.mallet.util.VectorStats;

import lactao.dbio.AccessConnection;

public class MalletClusterer {

	Noop pipe;
	Metric metric = new NormalizedDotProductMetric();
	private Alphabet alphabet;
	private AccessConnection cnn;

	@SuppressWarnings("serial")
	public MalletClusterer(AccessConnection cnn) throws SQLException {
		this.cnn = cnn;
		alphabet = new Alphabet("".getClass());
		pipe = new Noop(alphabet, null);
	}

	private int[] arrayList2RawIntArray(ArrayList<Integer> al) {
		int[] a = new int[al.size()];
		for (int i = 0; i < a.length; ++i)
			a[i] = al.get(i);
		return a;
	}

	private double[] arrayList2RawDoubleArray(ArrayList<Double> al) {
		double[] a = new double[al.size()];
		for (int i = 0; i < a.length; ++i)
			a[i] = al.get(i);
		return a;
	}

	@SuppressWarnings("deprecation")
	private InstanceList getInstanceListFromResulSet(ResultSet rs, String valueField)
			throws SQLException {
		InstanceList il = new InstanceList(pipe);
		int currentDomif = 0;
		ArrayList<Integer> indices = null;
		ArrayList<Double> values = null;
		if (rs != null) {
			while (rs.next()) {
				int noDomif = rs.getInt("no_domif");
				if (noDomif != currentDomif) {
					if (currentDomif != 0)
						// si on a déjà lu un domif, on l'ajoute à la liste
						il.add(new FeatureVector(alphabet, arrayList2RawIntArray(indices),
								arrayList2RawDoubleArray(values)), null, currentDomif, null);

					// on réinitialise les tableaux pour un nouveau domif
					indices = new ArrayList<Integer>();
					values = new ArrayList<Double>();
					currentDomif = noDomif;
				}
				indices.add(alphabet.lookupIndex(rs.getString("unif"), true));
				values.add(rs.getDouble(valueField));
			}
			// ajouter le dernier domif...
			il.add(new FeatureVector(alphabet, arrayList2RawIntArray(indices),
					arrayList2RawDoubleArray(values)), null, currentDomif, null);
		}
		System.out.println("Size of instance list: " + il.size());
		return il;
	}

	public InstanceList getInstanceListFromMatrix(String corpus, String valueField)
			throws SQLException {

		System.out.println("Reading matrix for corpus " + corpus + "...");
		assert !corpus.contains("'") : "Apostrophe illégale dans le nom du corpus";

		String sql = "";
		sql += " SELECT m.no_domif, m.unif, m." + valueField;
		sql += " FROM (documents doc";
		sql += "   INNER JOIN domifs dom ON doc.no_document = dom.no_document)";
		sql += "   INNER JOIN matrice m ON dom.no_domif = m.no_domif";
		sql += " WHERE NOT exclue AND corpus = '" + corpus + "'";
		sql += " ORDER BY m.no_domif";

		ResultSet rs = cnn.getRS(sql);
		return getInstanceListFromResulSet(rs, valueField);
	}

	public Clustering cluster(InstanceList il, int numClusters) throws SQLException {
		Clustering c = (new KMeans(pipe, numClusters, metric, KMeans.EMPTY_DROP)).cluster(il);
		return c;
	}

	public Clustering getOptimalClustering(InstanceList il, int minClusters, int maxClusters,
			ClusteringScorer scorer, PrintWriter logger) throws SQLException {
		assert minClusters >= 2 : "minClusters must be >= 2";
		assert maxClusters >= minClusters : "maxClusters must be >= minClusters";
		assert maxClusters < il.size() : "maxClusters must be < il.size()";

		Clustering bestClustering = null;
		double bestScore = Double.NEGATIVE_INFINITY;
		for (int k = minClusters; k <= maxClusters; ++k) {
			Clustering c = cluster(il, k);
			double score = scorer.score(c);
			if (logger != null) {
				logger.println(score);
				logger.flush();
			} else
				System.out.println("Score for " + k + " clusters: " + score);
			if (score > bestScore) {
				bestScore = score;
				bestClustering = c;
			}
		}
		return bestClustering;
	}

	public void addClusteringToDB(Clustering c, String classificationLabel) throws SQLException {
		String sql = "";
		sql += " INSERT INTO classes";
		sql += "   (classification, id_classe, nb_domifs)";
		sql += " VALUES";
		sql += "   (?, ?, ?)";
		PreparedStatement psAddClasse = cnn.prepareStatement(sql);

		sql = "";
		sql += " INSERT INTO domifs_par_classes";
		sql += "   (id_classe, no_domif, distance_centroide)";
		sql += " VALUES";
		sql += "   (?, ?, ?)";
		PreparedStatement psAddDomif = cnn.prepareStatement(sql);

		psAddClasse.setString(1, classificationLabel);

		// pour chaque classe dans le clustering
		int classNo = 0;
		for (InstanceList il : c.getClusters()) {
			++classNo;
			int nbDomifs = 0;
			String classId = getClassId(classificationLabel, classNo);
			psAddClasse.setString(2, classId);
			psAddDomif.setString(1, classId);

			SparseVector centroid = VectorStats.mean(il);

			// pour chq domif appartenant à la classe
			Iterator<Instance> it = il.iterator();
			while (it.hasNext()) {
				Instance domif = it.next();
				++nbDomifs;
				psAddDomif.setInt(2, (Integer) (domif.getName()));
				psAddDomif.setDouble(3, metric.distance((SparseVector) domif.getData(), centroid));
				psAddDomif.addBatch();
			}
			psAddClasse.setInt(3, nbDomifs);
			psAddClasse.addBatch();
		}
		psAddClasse.executeBatch();
		psAddClasse.close();
		psAddDomif.executeBatch();
		psAddDomif.close();
	}

	private String getClassId(String classificationLabel, int classNo) {
		DecimalFormat f = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
		f.applyPattern("00");
		return classificationLabel + "-" + f.format(classNo);
	}

	public void addLexiquesToDB() throws SQLException {

		String sql = "";
		sql += " INSERT INTO lexique_classes (id_classe, unif, freq_dans_classe)";
		sql += " SELECT d.id_classe, m.unif, Sum(m.freq) AS freq_dans_classe";
		sql += " FROM domifs_par_classes d";
		sql += "   INNER JOIN matrice m ON d.no_domif = m.no_domif";
		sql += " GROUP BY d.id_classe, m.unif, m.exclue";
		sql += " HAVING (NOT m.exclue)";
		cnn.execute(sql);

		sql = "";
		sql += " INSERT INTO lexique_classifications (classification, unif, nb_classes)";
		sql += " SELECT classification, unif, Count(id_classe) AS nb_classes FROM (";
		sql += "   SELECT DISTINCT c.classification, m.unif, d.id_classe";
		sql += "   FROM (classes AS c";
		sql += "	 INNER JOIN domifs_par_classes AS d ON c.id_classe = d.id_classe)";
		sql += "     INNER JOIN matrice AS m ON d.no_domif = m.no_domif";
		sql += "   WHERE NOT m.exclue";
		sql += " ) GROUP BY classification, unif";
		cnn.execute(sql);
	}

	public void motsRepresentatifs() throws SQLException {

		String sql = "";
		sql += " SELECT TOP 15 l2.unif, freq_dans_classe * Log(c1.nb_classes/l1.nb_classes) AS tfidf";
		sql += " FROM [";
		sql += "     SELECT classification, COUNT(id_classe) AS nb_classes";
		sql += "     FROM classes GROUP BY classification";
		sql += "   ]. AS c1";
		sql += "   INNER JOIN (lexique_classifications AS l1";
		sql += "     INNER JOIN (classes AS c2";
		sql += "       INNER JOIN lexique_classes AS l2";
		sql += "       ON c2.id_classe = l2.id_classe)";
		sql += "     ON (l1.classification = c2.classification) AND (l1.unif = l2.unif))";
		sql += "   ON c1.classification = l1.classification";
		sql += " WHERE l2.id_classe = ?";
		sql += " ORDER BY freq_dans_classe * Log(c1.nb_classes/l1.nb_classes) DESC";
		PreparedStatement pstGet = cnn.prepareStatement(sql);

		sql = "";
		sql += " UPDATE classes";
		sql += " SET unifs_representatives = ?";
		sql += " WHERE id_classe = ?";
		PreparedStatement pstUpdate = cnn.prepareStatement(sql);

		ResultSet rsClasses = cnn.getRS("SELECT id_classe FROM CLASSES");
		if (rsClasses != null)
			while (rsClasses.next()) {
				String mots = "";
				String idClasse = rsClasses.getString("id_classe");
				pstGet.setString(1, idClasse);
				ResultSet rs = pstGet.executeQuery();
				if (rs != null)
					while (rs.next()) {
						mots += rs.getString("unif") + " (" + String.format("%.2f", rs.getDouble("tfidf")) + ") ";
					}
				pstUpdate.setString(1, mots);
				pstUpdate.setString(2, idClasse);
				pstUpdate.addBatch();
				System.out.println(idClasse + " : " + mots);
			}
		pstUpdate.executeBatch();
		pstUpdate.close();
		pstGet.close();
	}

	public void addDistanceMatrixToDB(Clustering c, String classificationLabel) throws SQLException {
		System.out.println("addDistanceMatrixToDB");
		String sql = "";
		sql += " INSERT INTO distance";
		sql += "   (classification, id_classe_x, id_classe_y, distance)";
		sql += " VALUES";
		sql += "   (?, ?, ?, ?)";

		PreparedStatement ps = cnn.prepareStatement(sql);
		ps.setString(1, classificationLabel);
		InstanceList[] clusters = c.getClusters();

		// pour chaque classe
		for (int x = 0; x < clusters.length; ++x) {
			InstanceList ilX = clusters[x];
			ps.setString(2, getClassId(classificationLabel, x + 1));
			// on calcule la distance avec toutes les autres classes pour lesquelles la distance n'a
			// pas été calculée précédement (ça nous donne quelque chose comme: 1-2, 1-3, 1-4, 2-3,
			// 2-4, 3-4)
			for (int y = x + 1; y < clusters.length; ++y) {
				InstanceList ilY = clusters[y];
				ps.setString(3, getClassId(classificationLabel, y + 1));
				double dist = metric.distance(VectorStats.mean(ilX), VectorStats.mean(ilY));
				ps.setDouble(4, dist);
				ps.addBatch();
			}
		}
		ps.executeBatch();
		ps.close();
	}

	public void addCentroidsToDb() throws SQLException {
		System.out.println("addCentroidsToDb");
		cnn.createTableCentroides();
		String sql = "";
		sql += " INSERT INTO centroides (id_classe, unif, freq_tot, nb_domifs, freq_moyenne)";
		sql += " SELECT c.id_classe, m.unif, Sum(m.freq) AS freq_tot, c.nb_domifs, freq_tot/nb_domifs AS freq_moyenne";
		sql += " FROM (classes c INNER JOIN domifs_par_classes d";
		sql += "   ON c.id_classe = d.id_classe)";
		sql += "   INNER JOIN matrice m ON d.no_domif = m.no_domif";
		sql += " GROUP BY c.id_classe, m.unif, c.nb_domifs, m.exclue";
		sql += " HAVING NOT m.exclue";
		cnn.execute(sql);
	}

	public void makeGlobalDistanceMatrix() throws SQLException {
		System.out.println("mc.makeGlobalDistanceMatrix();");
		cnn.createTableGlobalDistance();

		// lire les centroides dans bd et en faire des vecteurs
		String sql = "";
		sql += " SELECT id_classe, unif, freq_moyenne";
		sql += " FROM centroides";
		sql += " ORDER BY id_classe, unif";
		ResultSet rs = cnn.getRS(sql);
		AbstractMap<String, FeatureVector> centroids = new HashMap<String, FeatureVector>();
		Map<String, ArrayList<Integer>> indices = new HashMap<String, ArrayList<Integer>>();
		Map<String, ArrayList<Double>> values = new HashMap<String, ArrayList<Double>>();
		String currentClass = "";
		Alphabet a = new Alphabet("".getClass());
		while (rs.next()) {
			String idClass = rs.getString("id_classe");
			if (!idClass.equals(currentClass)) {
				System.out.println(idClass);
				indices.put(idClass, new ArrayList<Integer>());
				values.put(idClass, new ArrayList<Double>());
				currentClass = idClass;
			}
			indices.get(idClass).add(alphabet.lookupIndex(rs.getString("unif"), true));
			values.get(idClass).add(rs.getDouble("freq_moyenne"));
		}
		for (String k: indices.keySet()) {
			int[] indicesArray = arrayList2RawIntArray(indices.get(k));
			double[] valuesArray = arrayList2RawDoubleArray(values.get(k));
			FeatureVector fv = new FeatureVector(a, indicesArray, valuesArray);
			centroids.put(k, fv);
		}

		sql = "";
		sql += " INSERT INTO global_distance (id_classe_x, id_classe_y, distance)";
		sql += " VALUES (?, ?, ?)";
		PreparedStatement pst = cnn.prepareStatement(sql);
		for (Map.Entry<String, FeatureVector> x : centroids.entrySet()) {
			pst.setString(1, x.getKey());
			for (Map.Entry<String, FeatureVector> y : centroids.entrySet()) {
//				System.out.println(x.getKey() + ":" + y.getKey());
				if (!x.getKey().equals(y.getKey())) {
					pst.setString(2, y.getKey());
					pst.setDouble(3, metric.distance(x.getValue(), y.getValue()));
					pst.addBatch();
				}
			}
		}
		pst.executeBatch();
	}

	public void resetClassesInDb() throws SQLException {
		cnn.createTableClasses();
		cnn.createTableDomifsParClasses();
		cnn.createTableLexiqueClasses();
		cnn.createTableLexiqueClassifications();
		cnn.createTableDistance();
	}
}
