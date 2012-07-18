package lactao.evaluateur;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import lactao.dbio.AccessConnection;

public class Evaluateur {
	private AccessConnection cnn;
	private ArrayList<String> dic = new ArrayList<String>();

	private void initDic() throws Exception {
		ResultSet rs = cnn.getRS("SELECT DISTINCT unif FROM matrice WHERE NOT exclue");
		while (rs.next())
			dic.add(rs.getString("unif"));
	}

	public ArrayList<ArrayList<Double>> getMatrixForClass(int classeNo) throws Exception {
		System.out.println("getMatrixForClass(" + classeNo + ")");
		ArrayList<ArrayList<Double>> classe = new ArrayList<ArrayList<Double>>();

		// Construire une liste contenant les vecteurs pour chaque domif dans la classe
		ResultSet rsDomifs = cnn.getRS("SELECT no_domif FROM classes WHERE no_classe =" + classeNo);
		while (rsDomifs.next()) {

			String sql = "";
			sql += " SELECT unif, freq FROM matrice";
			sql += " WHERE no_domif = " + rsDomifs.getInt("no_domif");
			sql += "   AND NOT exclue";
			ResultSet rs = cnn.getRS(sql);
			HashMap<String, Integer> bag = new HashMap<String, Integer>(dic.size());
			while (rs.next())
				bag.put(rs.getString("unif"), rs.getInt("freq"));

			ArrayList<Double> v = new ArrayList<Double>(dic.size());
			for (String unif : dic) {
				Integer freq = bag.get(unif);
				if (freq == null)
					freq = 0;
				v.add(freq.doubleValue());
			}
			classe.add(v);
		}
		return classe;
	}

	private ArrayList<Double> getCentroidForClass(ArrayList<ArrayList<Double>> classe) {
		System.out.println("getCentroidForClass");
		int vSize = classe.get(0).size();

		// Initialize centroid to a vector of 0s
		ArrayList<Double> centroid = new ArrayList<Double>(vSize);
		for (int i = 0; i < vSize; ++i)
			centroid.add(0.0);

		// sum each vector of the class in the centroid
		for (ArrayList<Double> v : classe)
			for (int i = 0; i < vSize; ++i)
				centroid.set(i, centroid.get(i) + v.get(i));

		// divide each element of the centroid by the nb of vectors
		for (int i = 0; i < vSize; ++i)
			centroid.set(i, centroid.get(i) / (double) classe.size());

		return centroid;
	}

	private double dist(ArrayList<Double> v1, ArrayList<Double> v2) {
		System.out.println("dist()");
		double d = 0;
		for (int i = 0; i < v1.size(); ++i)
			d += Math.pow(v1.get(i) - v2.get(i), 2);

		return Math.sqrt(d);
	}

	public double getMSE(int classeNo) throws Exception {
		System.out.println("getMSE(" + classeNo + ")");
		ArrayList<ArrayList<Double>> classe = getMatrixForClass(classeNo);
		ArrayList<Double> centroid = getCentroidForClass(classe);

		double mse = 0;

		for (ArrayList<Double> v : classe)
			mse += Math.pow(dist(v, centroid), 2);

		return mse / centroid.size();

	}

	public double getGlobalMSE(String dbFileName) throws Exception {

		cnn = new AccessConnection(dbFileName);
		initDic();
		double mse = 0;
		ResultSet rs = cnn.getRS("SELECT DISTINCT no_classe FROM classes");
		int count = 0;
		while (rs.next()) {
			mse += getMSE(rs.getInt("no_classe"));
			++count;
		}

		return mse / count;
	}

	public static void main(String args[]) {
		try {
			System.out.println((new Evaluateur()).getGlobalMSE("c:\\bergson\\bergson_kmeans_evolution.mdb"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
