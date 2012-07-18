package lactao.stoplist;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Locale;

import lactao.dbio.AccessConnection;
import lactao.stemmer.FrenchStemmer;
import lactao.stemmer.Stemmer;

public class StopList {
	HashSet<String> hs = new HashSet<String>();

	public StopList(String fileName, Stemmer stemmer) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
		String token;
		while ((token = in.readLine()) != null) {
			if (stemmer != null)
				token = stemmer.stem(token);
			hs.add(token.toLowerCase(Locale.FRENCH));
		}
		in.close();
	}

	public void saveToDb(AccessConnection cnn) throws SQLException {
		cnn.createTableStopList();
		PreparedStatement ps = cnn.prepareStatement("INSERT INTO stoplist (token) VALUES (?)");
		for (String token : hs) {
			ps.setString(1, token);
			ps.execute();
		}
	}

	public boolean contains(String word) {
		return hs.contains(word);
	}

}
