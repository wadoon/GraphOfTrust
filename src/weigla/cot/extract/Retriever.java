package weigla.cot.extract;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import net.htmlparser.jericho.Source;
import weigla.cot.gui.FetchProgressDialog;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;

public class Retriever {
    Set<String> alreadyScannedID = new HashSet<String>();
    private Graph<Person, Trust> graph;
    private ExecutorService execserv;
    private FetchProgressDialog dialog;
    private final String urlprefix;

    public Retriever(FetchProgressDialog progress, String keyserver) {
	execserv = Executors.newCachedThreadPool();
	graph = new DirectedSparseGraph<Person, Trust>();
	this.dialog = progress;
	urlprefix = "http://" + keyserver + "/pks/lookup?op=vindex&search=";
    }

    public Thread search(final String key, final int depth) {
	System.out.println("Retriever.search()");
	Thread t = new Thread(new Runnable() {
	    @Override
	    public void run() {
		builtGraph(key, depth);
	    }
	});
	t.start();
	return t;
    }

    private void builtGraph(String hash, int depth) {
	onStart(hash, depth);

	if (depth == 0)
	    return;

	synchronized (alreadyScannedID) {
	    if (alreadyScannedID.contains(hash))
		return;
	}

	try {
	    String s = urlprefix + hash;
	    Source source = new Source(new URL(s));
	    String renderedText = source.getRenderer().toString();
	    String[] lines = renderedText.split("\n");
	    int i = 0;
	    // parse header
	    for (; i < lines.length; i++) {
		if (lines[i].startsWith("pub")) {
		    break;
		}
	    }
	    String id = getHex(lines[i]);
	    i += 2;
	    String name = lines[i].substring(4).trim();
	    i++;
	    
	    System.out.println(renderedText);
	    System.out.println("\n-----------------------------------\n");

	    System.out.println("Name: " + name + "   -- " + id);

	    synchronized (alreadyScannedID) {
		alreadyScannedID.add(id);
	    }

	    Person to = new Person(name, id);

	    java.util.List<Thread> listT = new LinkedList<Thread>();
	    for (; i < lines.length; i += 2) {
		String signHex = getHex(lines[i]);
		String signName = getName(lines[i]).trim();
		Person from = new Person(signName, signHex);

		if (from.selfsigned())
		    continue;

		if (signHex == null)
		    continue;

		System.out
			.println("\t\tName: " + signName + "   -- " + signHex);
		synchronized (graph) {
		    graph.addEdge(new Trust(), from, to);
		}
		search(signHex, depth - 1);
	    }

	    for (Thread thread : listT) {
		thread.join();
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (InterruptedException e) {
	    e.printStackTrace();
	} finally {
	    onFinish(hash);
	}
    }

    private void onFinish(final String hash) {
	SwingUtilities.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		dialog.onFinish(hash);
	    }
	});
    }

    private void onStart(final String s, final int depth) {
	SwingUtilities.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		dialog.onStart(s, depth);
	    }
	});
    }

    private String getName(String s) {
	final int i = "sig  sig   DD05EBE1 </pks/lookup?op=get&search=0x5822CFFFDD05EBE1> 2011-05-31 __________ __________"
		.length();
	return s.substring(Math.min(i, s.length()));
    }

    private String getHex(String string) {
	Pattern p = Pattern.compile("0x................");
	Matcher m = p.matcher(string);
	if (m.find())
	    return m.group();
	return null;
    }

    public void join() {
	execserv.shutdown();
    }

    public Graph<Person, Trust> getGraph() {
	return graph;
    }
}
