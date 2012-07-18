package lactao.graphexport;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import lactao.dbio.AccessConnection;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.shortestpath.PrimMinimumSpanningTree;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.io.PajekNetWriter;

public class GraphExport {

	AccessConnection cnn;
	private String filteredTable;
	long nbClasses;
	DecimalFormat f = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
	UndirectedSparseGraph<String, Double> fullGraph;
	UndirectedSparseGraph<String, Double> minTree;

	public GraphExport(String classificationLabel, AccessConnection cnn) throws SQLException {
		assert !classificationLabel.contains("'") : "Apostrophe illégale dans le libellé de la classification";
		this.cnn = cnn;
		filteredTable = "(SELECT * FROM distance WHERE classification = '" + classificationLabel
				+ "')";
		nbClasses = cnn.getCount(filteredTable);
		f.applyPattern("0.000000");
		fullGraph = graphFromRS(getGraphRS());
		minTree = getMinTree(fullGraph);
	}

	public void exportToDL(String fileName) throws IOException, SQLException {
		PrintWriter out = new PrintWriter(new FileWriter(fileName));
		out.println("dl nm = 2 n = " + nbClasses + " format = edgelist1");
		out.println("RECODENA=NO");
		out.println("matrix labels:");
		out.println("distance");
		out.println("arbre_maximum");
		out.println("labels embedded");
		out.println("data:");
		for (Double e : fullGraph.getEdges()) {
			Pair<String> v = fullGraph.getEndpoints(e);
			out.println(v.getFirst() + "\t" + v.getSecond() + "\t" + f.format(e));
			out.println(v.getSecond() + "\t" + v.getFirst() + "\t" + f.format(e));
		}
		out.println("!"); // separates the two matrices
		for (Double e : minTree.getEdges()) {
			Pair<String> v = minTree.getEndpoints(e);
			out.println(v.getFirst() + "\t" + v.getSecond() + "\t" + f.format(e));
		}
		out.close();
	}

	public void exportToVNA(String fileName) throws IOException, SQLException {
		PrintWriter out = new PrintWriter(new FileWriter(fileName));
		out.println("*Tie data");
		out.println("from to distance min_tree");

		for (Double e : fullGraph.getEdges()) {
			Pair<String> v = fullGraph.getEndpoints(e);
			Double mt = minTree.containsEdge(e) ? e : 0;
			out.println(v.getFirst() + "\t" + v.getSecond() + "\t" + f.format(e) + "\t"
					+ f.format(mt));
			out.println(v.getSecond() + "\t" + v.getFirst() + "\t" + f.format(e) + "\t"
					+ f.format(mt));

		}
		out.close();
	}

	public void exportToVNA(String fileNameFullGraph, String fileNameMaxTree) throws IOException,
			SQLException {
		PrintWriter out = new PrintWriter(new FileWriter(fileNameFullGraph));
		out.println("*Tie data");
		out.println("from to distance");
		for (Double e : fullGraph.getEdges()) {
			Pair<String> v = fullGraph.getEndpoints(e);
			out.println(v.getFirst() + "\t" + v.getSecond() + "\t" + f.format(e));
			out.println(v.getSecond() + "\t" + v.getFirst() + "\t" + f.format(e));
		}
		out.close();
		out = new PrintWriter(new FileWriter(fileNameMaxTree));
		out.println("*Tie data");
		out.println("from to min_tree");
		for (Double e : minTree.getEdges()) {
			Pair<String> v = minTree.getEndpoints(e);
			out.println(v.getFirst() + "\t" + v.getSecond() + "\t" + f.format(e));
			out.println(v.getSecond() + "\t" + v.getFirst() + "\t" + f.format(e));
		}
		out.close();
	}

	public void exportToPajek(String fileNameFullGraph, String fileNameMaxTree) throws IOException {
		PajekNetWriter<String, Double> pw = new PajekNetWriter<String, Double>();
		Transformer<String, String> tV = new Transformer<String, String>() {
			public String transform(String s) {
				return s;
			}
		};
		Transformer<Double, Number> tE = new Transformer<Double, Number>() {
			public Number transform(Double d) {
				return d;
			}
		};
		pw.save(fullGraph, fileNameFullGraph, tV, tE);
		pw.save(minTree, fileNameMaxTree, tV, tE);
	}

	private ResultSet getGraphRS() throws SQLException {
		String sql = "";
		sql += " SELECT id_classe_x, id_classe_y, distance";
		sql += " FROM " + filteredTable;
		return cnn.getRS(sql);
	}

	private UndirectedSparseGraph<String, Double> graphFromRS(ResultSet rs) throws SQLException {
		UndirectedSparseGraph<String, Double> g = new UndirectedSparseGraph<String, Double>();
		while (rs.next()) {
			String v1 = rs.getString("id_classe_x");
			String v2 = rs.getString("id_classe_y");
			Double e = rs.getDouble("distance");
			try {
				g.addEdge(e, v1, v2);
			} catch (IllegalArgumentException ignore) {
				// fires for parallel edges, which shouldn't be there anyway...
			}
		}

		return g;
	}

	private UndirectedSparseGraph<String, Double> getMinTree(Graph<String, Double> g) {
		Factory<UndirectedGraph<String, Double>> factory = UndirectedSparseGraph.getFactory();
		Transformer<Double, Double> t = new Transformer<Double, Double>() {
			public Double transform(Double d) {
				return d;
			}
		};
		PrimMinimumSpanningTree<String, Double> pmst = new PrimMinimumSpanningTree<String, Double>(
				factory, t);
		return (UndirectedSparseGraph<String, Double>) pmst.transform(g);
	}
}
