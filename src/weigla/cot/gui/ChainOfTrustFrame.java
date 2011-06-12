package weigla.cot.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLEditorKit;

import weigla.cot.extract.Person;
import weigla.cot.extract.Retriever;
import weigla.cot.extract.Trust;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;

public class ChainOfTrustFrame extends JFrame {
    private static final long serialVersionUID = -1028248650428117778L;
    private WAction actionHelp = new HelpAction();
    private WAction actionOpen = new OpenAction();
    private WAction actionSave = new SaveAction();
    private WAction actionFetch = new FetchAction();
    private WAction actionClose = new CloseAction();

    public ChainOfTrustFrame() {
	setDefaultCloseOperation(EXIT_ON_CLOSE);
	setSize(500, 500);
	setJMenuBar(createMenubar());
	setLayout(new BorderLayout());

	addWindowListener(new WindowAdapter() {
	    @Override
	    public void windowClosed(WindowEvent e) {
		System.exit(0);
	    }
	});
	
	Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
	setLocation((d.width - getWidth()) / 2,
		      (d.height- getHeight()) / 2);
    }

    private void updateView() {
	CircleLayout<Person, Trust> layout = new CircleLayout<Person, Trust>(
		currentGraph);
	layout.setSize(getSize());
	VisualizationViewer<Person, Trust> vv = new VisualizationViewer<Person, Trust>(
		layout);
	vv.setPreferredSize(getSize());
	DefaultModalGraphMouse<Person, Trust> gm = new DefaultModalGraphMouse<Person, Trust>();
	gm.setMode(Mode.TRANSFORMING);
	vv.setGraphMouse(gm);

	vv.getRenderContext().setVertexLabelTransformer(
		new ToStringLabeller<Person>() {
		    @Override
		    public String transform(Person v) {
			return v.getName();
		    }
		});

	add(vv);
	invalidate();
	repaint();
    }

    private JMenuBar createMenubar() {
	JMenuBar bar = new JMenuBar();
	JMenu file = new JMenu("File");
	JMenu help = new JMenu("Help");

	bar.add(file);
	bar.add(help);

	file.add(actionFetch);
	file.add(actionOpen);
	file.add(actionSave);
	file.add(actionClose);
	help.add(actionHelp);
	return bar;
    }

    class CloseAction extends WAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1349113638630488289L;

	public CloseAction() {
	    setText("Close");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    ChainOfTrustFrame.this.setVisible(false);
	    System.exit(0);
	}
    }

    class SaveAction extends WAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6208574755189966577L;

	public SaveAction() {
	    setText("Save Graph");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    int c = filechooser.showSaveDialog(ChainOfTrustFrame.this);
	    if (c == JFileChooser.APPROVE_OPTION) {
		File f = filechooser.getSelectedFile();
		try {
		    XMLEncoder enc = new XMLEncoder(new FileOutputStream(f));
		    enc.writeObject(currentGraph);
		    enc.close();
		} catch (FileNotFoundException e1) {
		    e1.printStackTrace();
		}
	    }
	}
    }

    JFileChooser filechooser = new JFileChooser();
    public Graph<Person, Trust> currentGraph;

    class OpenAction extends WAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7962537893103714980L;

	public OpenAction() {
	    setText("Open…");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent e) {
	    int c = filechooser.showOpenDialog(ChainOfTrustFrame.this);
	    if (c == JFileChooser.APPROVE_OPTION) {
		File f = filechooser.getSelectedFile();
		try {
		    XMLDecoder enc = new XMLDecoder(new FileInputStream(f));
		    currentGraph = (Graph<Person, Trust>) enc.readObject();
		    enc.close();
		    updateView();
		} catch (FileNotFoundException e1) {
		    e1.printStackTrace();
		}
	    }
	}
    }

    class FetchAction extends WAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1890696055889533757L;

	public FetchAction() {
	    setText("Fetch Data…");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    FetchDialog dialog = new FetchDialog(ChainOfTrustFrame.this);
	    dialog.setVisible(true);

	    if (!dialog.getAccepted())
		return;

	    
	    FetchProgressDialog dialog2 = new FetchProgressDialog(
		    ChainOfTrustFrame.this);

	    dialog2.setLocation(dialog.getLocation());
	    dialog2.setSize(dialog.getSize());

	    Retriever retriever = new Retriever(dialog2, dialog.getKeyserver());

	    List<Thread> l = new LinkedList<Thread>();

	    for (String key : dialog.getKeys()) {
		if (key.trim().isEmpty())
		    continue;
		l.add(retriever.search(key, dialog.getDepth()));
	    }

	    dialog2.setVisible(true);

	    for (Thread thread : l) {
		try {
		    thread.join();
		} catch (InterruptedException e1) {
		    e1.printStackTrace();
		}
	    }

	    dialog2.setVisible(false);
	    currentGraph = retriever.getGraph();
	    updateView();
	}
    }

    class HelpAction extends WAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = -54785792821812646L;

	public HelpAction() {
	    setText("Help");
	    setAccelerator(KeyStroke.getKeyStroke("f1"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    JDialog dialog = new JDialog(ChainOfTrustFrame.this, "About");
	    dialog.setModal(true);
	    JTextPane editor = new JTextPane();
	    editor.setContentType("text/html");

	    try {
		System.out
			.println(getClass().getResourceAsStream("about.html"));
		editor.read(getClass().getResourceAsStream("about.html"),
			new HTMLEditorKit());
	    } catch (IOException e1) {
		editor.setText(e1.getMessage());
		e1.printStackTrace();
	    }
	    dialog.add(editor);
	    dialog.pack();
	    dialog.setVisible(true);
	}
    }

    public static void main(String[] args) {
	SwingUtilities.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		new ChainOfTrustFrame().setVisible(true);
	    }
	});
    }
}
