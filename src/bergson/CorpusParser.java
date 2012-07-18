package bergson;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import lactao.dbio.AccessConnection;
import lactao.util.file.FileUtils;

public class CorpusParser {
	public static void parse(File file, AccessConnection cnn) throws IOException, SQLException {

		String[][] tdm = { 
				{ "DI1888", "Essai sur les donn�es imm�diates de la conscience" },
				{ "RI1900", "Le rire. Essai sur la signification du comique" },
				{ "CB1902", "Camille Bos. Psychologie de la croyance" },
				{ "EC1907", "L��volution cr�atrice" }, 
				{ "PF1915", "La philosophie fran�aise" },
				{ "ES1919", "L��nergie spirituelle. Essais et conf�rences" },
				{ "DS1922", "Dur�e et simultan�it�. A propos de la th�orie d�Einstein" },
				{ "MR1932", "Deux sources de la morale et de la religion" },
				{ "MM1899", "Mati�re et m�moire" },
				{ "PM1924", "La pens�e et le mouvant" }
				};

		String corpus = FileUtils.read(file);
		String[] texts = corpus.split("B\\d{4}");
		cnn.createTableDocuments();
		String sql = "";
		sql += " INSERT INTO documents (no_document, titre, texte)";
		sql += " VALUES (?, ?, ?)";
		PreparedStatement ps = cnn.prepareStatement(sql);
		for (int i = 0; i < 10; i++) {
			ps.setString(1, tdm[i][0]);
			ps.setString(2, tdm[i][1]);
			ps.setString(3, texts[i]);
			ps.addBatch();			
		}
		ps.executeBatch();
		ps.close();
	}

	public static void main(String[] args) {
		try {
			CorpusParser.parse(new File("c:/bergson/corpus/corpus.txt"), new AccessConnection(
					"c:/bergson/bergson.mdb"));
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
