package lactao.segmenteur;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;

import lactao.dbio.AccessConnection;

public class Segmenteur {

	private BufferedReader r;
	private AccessConnection cnn;

	private void initReader(String fileName) throws Exception {
		r = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "Cp1252"));
	}

	public void segment(String inputFileName, String dbFileName) throws Exception {
		initReader(inputFileName);
		cnn = new AccessConnection(dbFileName);
		cnn.createTableDomifs();
		String sql = "INSERT INTO domifs (texte) VALUES (?)";
		PreparedStatement p = cnn.prepareStatement(sql);
		String s;
		while ((s = r.readLine()) != null) {
			p.setString(2, s);
			p.addBatch();
		}
		p.executeBatch();
		r.close();
		cnn.close();
	}

	public static void main(String args[]) {
		try {
			(new Segmenteur()).segment("c:\\Bergson\\concordance.txt", "c:\\Bergson\\bergson.mdb");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
