package lactao.dbio;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AccessConnection {

	private Connection c;

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return c.prepareStatement(sql);
	}

	public Statement createStatement() throws SQLException {
		return c.createStatement();
	}

	public void close() throws SQLException {
		c.close();
	}

	public AccessConnection(String fileName) throws SQLException, ClassNotFoundException {
		Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
		String database = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";
		database += fileName.trim() + ";DriverID=22;READONLY=false;ExtendedANSISQL=1}";
		c = DriverManager.getConnection(database, "", "");
	}

	public void execute(String sql) throws SQLException {
		Statement s = c.createStatement();
		s.executeUpdate(sql);
		s.close();
		c.commit();
	}

	public ResultSet getRS(String sql) throws SQLException {
		Statement stt = c.createStatement();
		stt.execute(sql);
		return stt.getResultSet();
	}

	public void dropIfExist(String table) throws SQLException {
		try {
			execute("DROP TABLE " + table);
		} catch (SQLException e) {
			// ignore "table not found"
			if (e.getErrorCode() != -1305)
				throw e;
		}
	}

	public long getCount(String table) throws SQLException {
		ResultSet rs = getRS("SELECT Count(*) FROM " + table);
		rs.next();
		long count = rs.getLong(1);
		rs.close();
		return count;
	}

	public double getMaxDouble(String table, String field) throws SQLException {
		ResultSet rs = getRS("SELECT Max(" + field + ") FROM " + table);
		rs.next();
		// TODO: c'est bizarre de passer par un long; investiguer...
		double max = rs.getDouble(1);
		rs.close();
		return max;
	}

	public int getMaxInt(String table, String field) throws SQLException {
		ResultSet rs = getRS("SELECT Max(" + field + ") FROM " + table);
		rs.next();
		return rs.getInt(1);
	}

	public void createTableDomifs() throws SQLException {
		dropIfExist("domifs");
		String sql = "";
		sql += " CREATE TABLE domifs (";
		sql += "   no_domif COUNTER CONSTRAINT pk PRIMARY KEY,";
		sql += "   no_document TEXT(50),";
		sql += "   longueur LONG,";
		sql += "   texte MEMO)";
		execute(sql);
	}

	public void createTableMatrice() throws SQLException {
		String sql;
		dropIfExist("matrice");
		sql = "  CREATE TABLE matrice (";
		sql += "   no_domif INTEGER,";
		sql += "   unif TEXT(255),";
		sql += "   freq INTEGER,";
		sql += "   tfidf DOUBLE,";
		sql += "   exclue YESNO DEFAULT No,";
		sql += "   raison_exclusion TEXT(255))";
		execute(sql);
		execute("CREATE INDEX idx_no_domif ON matrice (no_domif ASC)");
	}

	public void createTableDistance() throws SQLException {
		String sql;
		dropIfExist("distance");
		sql = "  CREATE TABLE distance (";
		sql += "  classification TEXT(15),";
		sql += "  id_classe_x TEXT(20),";
		sql += "  id_classe_y TEXT(20),";
		sql += "  distance DOUBLE)";
		execute(sql);
	}

	public void createTableUnifs() throws SQLException {
		dropIfExist("unifs");
		String sql = "";
		sql = "  CREATE TABLE unifs (";
		sql += "   unif TEXT(255) CONSTRAINT pk PRIMARY KEY,";
		sql += "   nb_domifs LONG,";
		sql += "   freq_globale LONG,";
		sql += "   idf DOUBLE)";
		execute(sql);
	}

	public void createTableStopList() throws SQLException {
		dropIfExist("stoplist");
		execute("CREATE TABLE stoplist (token TEXT(255) CONSTRAINT pk PRIMARY KEY)");
	}

	public void createTableClasses() throws SQLException {
		dropIfExist("classes");
		String sql = "";
		sql += "CREATE TABLE classes (";
		sql += "  classification TEXT(15),";
		sql += "  id_classe TEXT(20) CONSTRAINT pk PRIMARY KEY,";
		sql += "  nb_domifs LONG,";
		sql += "  unifs_representatives MEMO)";
		execute(sql);
	}

	public void createTableDomifsParClasses() throws SQLException {
		dropIfExist("domifs_par_classes");
		String sql = "";
		sql += "CREATE TABLE domifs_par_classes (";
		sql += "  id_classe TEXT(20),";
		sql += "  no_domif LONG,";
		sql += "  distance_centroide DOUBLE)";
		execute(sql);
	}

	public void createTableLexiqueClasses() throws SQLException {
		dropIfExist("lexique_classes");
		String sql = "";
		sql += "CREATE TABLE lexique_classes (";
		sql += "  id_classe TEXT(20),";
		sql += "  unif TEXT(255),";
		sql += "  freq_dans_classe LONG)";
		execute(sql);
	}

	public void createTableLexiqueClassifications() throws SQLException {
		dropIfExist("lexique_classifications");
		String sql = "";
		sql += "CREATE TABLE lexique_classifications (";
		sql += "  classification TEXT(15),";
		sql += "  unif TEXT(255),";
		sql += "  nb_classes LONG)";
		execute(sql);
	}

	public void createTableAuteurs() throws SQLException {
		dropIfExist("auteurs");
		String sql = "";
		sql += " CREATE TABLE auteurs (";
		sql += "   no_document TEXT(50),";
		sql += "   auteur TEXT(255)";
		sql += ")";
		execute(sql);
	}

	public void createTableDocuments() throws SQLException {
		dropIfExist("documents");
		String sql = "";
		sql += " CREATE TABLE documents (";
		sql += "   no_document TEXT(50) CONSTRAINT pk PRIMARY KEY,";
		sql += "   titre MEMO,";
		sql += "   source TEXT(255),";
		sql += "   corpus TEXT(50),";
		sql += "   texte MEMO,";
		sql += "   dt_publication DATE";
		sql += " )";
		execute(sql);
	}

	public void createTableCorpuses() throws SQLException {
		dropIfExist("corpus");
		String sql = "";
		sql += " CREATE TABLE corpus (";
		sql += "   corpus TEXT(50),";
		sql += "   nb_documents LONG,";
		sql += "   nb_domifs LONG";
		sql += " )";
		execute(sql);
	}

	public void createTableLexiqueCorpus() throws SQLException {
		dropIfExist("lexique_corpus");
		String sql = "";
		sql += " CREATE TABLE lexique_corpus (";
		sql += "   corpus TEXT(50),";
		sql += "   unif TEXT(255),";
		sql += "   nb_domifs LONG";
		sql += " )";
		execute(sql);
	}

	public void createTableCentroides() throws SQLException {
		dropIfExist("centroides");
		String sql = "";
		sql += " CREATE TABLE centroides (";
		sql += "   id_classe TEXT(20),";
		sql += "   unif TEXT(255),";
		sql += "   freq_tot LONG,";
		sql += "   nb_domifs LONG,";
		sql += "   freq_moyenne DOUBLE";
		sql += " )";
		execute(sql);
	}
	
	public void createTableGlobalDistance() throws SQLException {
		dropIfExist("global_distance");
		String sql = "";
		sql += " CREATE TABLE global_distance (";
		sql += "   id_classe_x TEXT(20),";
		sql += "   id_classe_y TEXT(20),";
		sql += "   distance DOUBLE";
		sql += " )";
		execute(sql);		
	}

	protected void finalize() {
		try {
			c.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
