package lactao.eureka;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

public class EurekaParser extends HTMLEditorKit.ParserCallback {

	private enum Destination {
		TITRE, SOURCE, TEXTE, AUCUN
	};

	private Destination dest = Destination.TEXTE;
	private Article a; // current working article
	private final List<Article> articles = new ArrayList<Article>();
	private final HTMLEditorKit.Parser parser = new ParserDelegator();

	private final String[] ignoreList = { "Réplique", "Dialogue", "Éditoriaux",
			"Éditorial", "Précision", "Appel à tous", "Lettre de la semaine",
			"Encadré(s) :", "Illustration(s) :", "La boîte aux lettres",
			"Chronique", "Rencontre", "Critique", "Opinion", "Télévision",
			"Radio", "Analyse", "Sondage CROP - La Presse", "Post-scriptum",
			"Entracte", "Éducation", "Libre opinion", "En bref...", "Lettres",
			"En bref", "Société", "Perspectives", "Livres", "Médias",
			"Technologie", "Essais québécois", "Théâtre", "Tête-à-tête",
			"Essais", "En couverture", "Humour", "PC", "Presse Canadienne",
			"AFP", "Reuters", "France-Presse, Agence", "Le, Devoir" };

	// ignore list must all be uppercase
	private final String[] ignoreAtStartList = { "©", "(C)", "PHOTO" };

	private int spanLevel = 0;

	private final Pattern noDocPattern = Pattern
			.compile("Numéro de document\\s:\\s(.*(\\d{8})·([A-Z][A-Z]).*)");

	private final Pattern auteurPattern = Pattern
			.compile("^(\\p{Lu}[\\p{L}\\p{Pd}']*\\b,\\s*\\b\\p{Lu}[\\p{L}\\p{Pd}']*(?:;\\s)?)+$");

	private final Pattern emailPattern = Pattern
			.compile("(?i)^[A-Z0-9._%+-]+@[A-Z0-9.-]+(\\.[A-Z]{2,4})?$");

	public List<Article> getArticles() {
		return articles;
	}

	@Override
	public void handleEndTag(final HTML.Tag tag, final int position) {
		if (a != null)
			if (tag == HTML.Tag.P)
				a.texte += "\r\n";
			else if (tag == HTML.Tag.SPAN) {
				spanLevel--;
				if (spanLevel == 0)
					dest = Destination.TEXTE;
			}
	}

	@Override
	public void handleSimpleTag(final HTML.Tag tag,
			final MutableAttributeSet attributes, final int position) {

		if (a != null && tag == HTML.Tag.BR && dest == Destination.TEXTE)
			a.texte += "\r\n";

	}

	@Override
	public void handleStartTag(final HTML.Tag tag,
			final MutableAttributeSet attributes, final int position) {

		if (tag == HTML.Tag.SPAN) {

			spanLevel++;

			if (spanLevel == 1)
				if (attributes.containsAttribute(HTML.Attribute.CLASS,
						"DocPublicationName")) {
					if (a == null || a.source.isEmpty()) {
						a = new Article();
						dest = Destination.SOURCE;
					} else
						// sert au cas d'exception news·20060413·LE·106702 où un
						// second span
						// contient "2"
						dest = Destination.AUCUN;
				} else if (a != null
						&& attributes.containsAttribute(HTML.Attribute.CLASS,
								"TitreArticleVisu"))
					dest = Destination.TITRE;
				else if (a != null
						&& attributes.containsAttribute(HTML.Attribute.CLASS,
								"DocHeader"))
					dest = Destination.AUCUN;
		}

	}

	@Override
	public void handleText(final char[] text, final int position) {

		if (a == null)
			return; // ------------------>> EXIT

		final String s = new String(text);

		Matcher m = noDocPattern.matcher(s);
		if (m.matches()) {
			a.noDocument = m.group(1);
			System.out.println("  doc no: " + a.noDocument);
			try {
				a.dtPublication = (new SimpleDateFormat("yyyyMMdd")).parse(m
						.group(2));
			} catch (final ParseException e) {
				e.printStackTrace();
			}
			a.clean();
			articles.add(a);
			a = null;
			return; // ------------------>> EXIT
		}

		if (emailPattern.matcher(s).matches())
			return; // ------------------>> EXIT

		if (inIgnoreAtStartList(s) || inIgnoreList(s))
			return; // ------------------>> EXIT

		m = auteurPattern.matcher(s);
		if (a.auteurs.isEmpty() && m.matches()) {
			final String[] auteurs = s.split(";\\s*");
			for (final String auteur : auteurs)
				a.auteurs.add(auteur);
			return; // ------------------>> EXIT
		}

		// exception:
		if (s.equalsIgnoreCase("des Rivières, Paule")) {
			a.auteurs.add(s);
			return; // ------------------>> EXIT
		}

		switch (dest) {
		case TITRE:
			a.titre += s;
			a.texte += s;
			break;
		case SOURCE:
			// if (!s.startsWith("2")) // cas d'exception, article
			// news·20060413·LE·106702
			a.source += s;
			break;
		case TEXTE:
			a.texte += s;
		}

	}

	private boolean inIgnoreAtStartList(String s) {
		boolean found = false;
		s = s.trim().toUpperCase();
		for (final String t : ignoreAtStartList)
			if (s.startsWith(t)) {
				found = true;
				break;
			}
		return found;
	}

	private boolean inIgnoreList(String s) {
		boolean found = false;
		s = s.trim();
		for (final String t : ignoreList)
			if (s.equalsIgnoreCase(t)) {
				found = true;
				break;
			}
		return found;
	}

	public void parse(final File f) throws FileNotFoundException, IOException {
		System.out.println("Parsing " + f.getPath());
		parser.parse(new InputStreamReader(new FileInputStream(f)), this, true);
	}

}