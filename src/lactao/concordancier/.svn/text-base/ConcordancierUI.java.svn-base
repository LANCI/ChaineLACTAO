package lactao.concordancier;
//import SpringUtilities;

import javax.swing.JFrame;

@SuppressWarnings("serial") //TODO: permettre de sérialiser pour garder les settings?
public class ConcordancierUI extends JFrame {
//
//	private ConcordancierIO c = new ConcordancierIO();
//
//	public void setNativeLookAndFeel() {
//		try {
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//		} catch (Exception e) {
//			displayException(e);
//		}
//	}
//
//	public void displayException(Exception e) {
//		e.printStackTrace();
//		JOptionPane.showMessageDialog(null, e.getMessage(), "Erreur",
//				JOptionPane.ERROR_MESSAGE);
//	}
//
//	private JTextArea inputTextArea;
//	private JTextArea outputTextArea;
//	private JLabel inputFileNameLabel;
//	private SpinnerNumberModel unitsBeforeSpinnerModel;
//	private SpinnerNumberModel unitsAfterSpinnerModel;
//	private JTextField targetTextField;
//	private JComboBox comboUnits;
//
//	class InputTextFileLoader implements ActionListener {
//
//		public void actionPerformed(ActionEvent ae) {
//			JFileChooser chooser = new JFileChooser();
//			int option = chooser.showOpenDialog(ConcordancierUI.this);
//
//			if (option == JFileChooser.APPROVE_OPTION) {
//				try {
//					File f = chooser.getSelectedFile();
//					c.setInputFileName(f.getPath());
//					c.loadText();
//					inputTextArea.setText(c.getText());
//					inputTextArea.setCaretPosition(0);
//					inputFileNameLabel.setText(f.getPath());
//				} catch (Exception e) {
//					displayException(e);
//				}
//			}
//		}
//	}
//	
//	class ResultSaver implements ActionListener {
//
//		public void actionPerformed(ActionEvent ae) {
//			JFileChooser chooser = new JFileChooser();
//			int option = chooser.showSaveDialog(ConcordancierUI.this);
//
//			if (option == JFileChooser.APPROVE_OPTION) {
//				//TODO:avertir si fichier existe
//				try {
//					File f = chooser.getSelectedFile();
//					c.setOutputFileName(f.getPath());
//					c.saveText();
//				} catch (Exception e) {
//					displayException(e);
//				}
//			}
//		}
//	}
//
//	class Runner implements ActionListener {
//		public void actionPerformed(ActionEvent ae) {
//			try {
//				c.setRegexBuilder((ConcordanceRegex) comboUnits.getSelectedItem());
//				c.setTarget(targetTextField.getText());
//				c.setUnitsAfter(unitsAfterSpinnerModel.getNumber().intValue());
//				c
//						.setUnitsBefore(unitsBeforeSpinnerModel.getNumber()
//								.intValue());
//				c.findTarget();
//				if (c.getFindingsString().isEmpty())
//					throw new Exception("Expression cible non trouvée!");
//				outputTextArea.setText(c.getFindingsString());
//			} catch (Exception e) {
//				displayException(e);
//			}
//		}
//	}
//
//	private JPanel optionsPanel() {
//		JPanel p = new JPanel(new SpringLayout());
//
//		p.setBorder(BorderFactory.createTitledBorder("Options"));
//
//		p.add(new JLabel("Expression cible:"));
//		targetTextField = new JTextField();
//		p.add(targetTextField);
//
//		p.add(new JLabel("Unités avant:"));
//		unitsBeforeSpinnerModel = new SpinnerNumberModel(1, // initial value
//				0, // min
//				9999, // max
//				1); // step
//
//		JSpinner s1 = new JSpinner(unitsBeforeSpinnerModel);
//		((JSpinner.DefaultEditor) s1.getEditor()).getTextField()
//				.setHorizontalAlignment(SwingConstants.LEFT);
//		p.add(s1);
//
//		p.add(new JLabel("Unités après:"));
//		unitsAfterSpinnerModel = new SpinnerNumberModel(1, // initial value
//				0, // min
//				9999, // max
//				1); // step
//
//		JSpinner s2 = new JSpinner(unitsAfterSpinnerModel);
//		((JSpinner.DefaultEditor) s2.getEditor()).getTextField()
//				.setHorizontalAlignment(SwingConstants.LEFT);
//		p.add(s2);
//
//		p.add(new JLabel("Type d'unités:"));
//		comboUnits = new JComboBox(ConcordanceRegex.values());
//		p.add(comboUnits);
//
//		SpringUtilities.makeCompactGrid(p, 4, 2, // rows, cols
//				6, 6, // initX, initY
//				6, 6); // xPad, yPad
//
//		return p;
//	}
//
//	private JPanel inputTextPanel() {
//		JPanel panel = new JPanel();
//
//		panel.setLayout(new BorderLayout());
//		panel.setBorder(BorderFactory.createTitledBorder("Texte source"));
//
//		inputTextArea = new JTextArea();
//		// inputTextArea.setLineWrap(true);
//		// inputTextArea.setWrapStyleWord(true);
//		inputTextArea.setEditable(false);
//
//		JScrollPane inputTextScrollPane = new JScrollPane(inputTextArea);
//		panel.add(inputTextScrollPane, BorderLayout.CENTER);
//
//		Box controlBox = new Box(BoxLayout.PAGE_AXIS);
//		panel.add(controlBox, BorderLayout.PAGE_START);
//
//		JButton loadButton = new JButton("Charger...");
//		controlBox.add(loadButton);
//
//		inputFileNameLabel = new JLabel();
//		controlBox.add(inputFileNameLabel);
//		loadButton.addActionListener(new InputTextFileLoader());
//
//		return panel;
//	}
//
//	private JPanel outputTextPanel() {
//
//		JPanel panel = new JPanel();
//
//		panel.setLayout(new BorderLayout());
//		panel.setBorder(BorderFactory.createTitledBorder("Concordances"));
//
//		outputTextArea = new JTextArea();
//		// outputTextArea.setLineWrap(true);
//		// outputTextArea.setWrapStyleWord(true);
//		outputTextArea.setEditable(false);
//
//		JScrollPane outputTextScrollPane = new JScrollPane(outputTextArea);
//		panel.add(outputTextScrollPane, BorderLayout.CENTER);
//
//		Box controlBox = new Box(BoxLayout.LINE_AXIS);
//		panel.add(controlBox, BorderLayout.PAGE_START);
//
//		JButton runButton = new JButton("Trouver");
//		controlBox.add(runButton);
//
//		runButton.addActionListener(new Runner());
//
//		JButton saveButton = new JButton("Sauvegarder...");
//		controlBox.add(saveButton);
//		saveButton.addActionListener(new ResultSaver());
//
//		return panel;
//	}
//
//	public ConcordancierUI() {
//		super("Concordancier");
//
//		setNativeLookAndFeel();
//		setSize(800, 600);
//		setDefaultCloseOperation(EXIT_ON_CLOSE);
//
//		Container c = getContentPane();
//		c.setLayout(new BorderLayout());
//
//		JPanel leftPanel = new JPanel(new BorderLayout());
//		leftPanel.add(inputTextPanel(), BorderLayout.CENTER);
//
//		JPanel rightPanel = new JPanel(new BorderLayout());
//		rightPanel.add(optionsPanel(), BorderLayout.PAGE_START);
//		rightPanel.add(outputTextPanel(), BorderLayout.CENTER);
//
//		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
//				leftPanel, rightPanel);
//		c.add(splitPane, BorderLayout.CENTER);
//		splitPane.setDividerLocation(400);
//		splitPane.setResizeWeight(0.5);
//
//	}
//
//	public static void main(String args[]) throws Exception {
//		ConcordancierUI ui = new ConcordancierUI();
//		ui.setVisible(true);
//	}
}