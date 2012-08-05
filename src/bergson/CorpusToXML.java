package bergson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import lactao.dbio.AccessConnection;

public class CorpusToXML {

	public static void main(String[] args) throws SQLException,
			ClassNotFoundException, XMLStreamException, IOException {

		AccessConnection cnn = new AccessConnection("c:/bergson/bergson.mdb");

		File f = new File("c:/bergson/classes_bergson.xml");
		f.createNewFile();
		XMLStreamWriter xml = XMLOutputFactory.newInstance()
				.createXMLStreamWriter(new FileOutputStream(f), "UTF-16");

		try {
			xml.writeStartDocument("UTF-16", "1.0");
			xml.writeStartElement("corpus");

			StringBuilder sql = new StringBuilder();
			sql.append(" SELECT id_classe");
			sql.append(" FROM classes");
			sql.append(" ORDER BY id_classe");
			ResultSet rsClasses = cnn.getRS(sql.toString());
			while (rsClasses.next()) {

				String idClasse = rsClasses.getString("id_classe");
				xml.writeStartElement("classe");
				xml.writeAttribute("id_classe", idClasse);

				sql.delete(0, sql.length());
				sql.append(" SELECT d.no_domif, d.no_document, d.texte");
				sql.append(" FROM domifs_par_classes dc");
				sql.append("   INNER JOIN domifs d");
				sql.append("     ON dc.no_domif = d.no_domif");
				sql.append(" WHERE dc.id_classe = ?");
				sql.append(" ORDER BY d.no_domif;");

				PreparedStatement pst = cnn.prepareStatement(sql.toString());
				pst.setString(1, idClasse);
				ResultSet rsDomifs = pst.executeQuery();

				while (rsDomifs.next()) {
					xml.writeStartElement("segment");
					xml.writeAttribute("no_domif", rsDomifs.getString("no_domif"));
					xml.writeAttribute("no_document", rsDomifs.getString("no_document"));
					xml.writeCData(rsDomifs.getString("texte"));
					xml.writeEndElement();
				}
				xml.writeEndElement(); // </idClasse>
			}
			xml.writeEndElement(); // </BERGSON>
			xml.writeEndDocument();
		} catch (XMLStreamException e) {
			xml.flush();
			e.printStackTrace();
		}
	}

}
