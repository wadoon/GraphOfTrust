package weigla.cot.gui;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.*;

public class FetchProgressDialog extends JDialog {
    private static final long serialVersionUID = -219784938169337542L;
    private JList list;
    private HashMap<String, String> map = new HashMap<String, String>();
    private int count;

    public FetchProgressDialog(JFrame parent) {
	super(parent);
	setTitle("Fetching Progress");
	final BoxLayout manager = new BoxLayout(getContentPane(),
		BoxLayout.Y_AXIS);
	getContentPane().setLayout(manager);
	add(new JLabel("Currently running:"));
	add(list = new JList());
	list.setModel(new DefaultListModel());
	setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    public void onStart(String hash, int depth) {
	count++;
	String str = "Fetching: " + hash + " Depth:" + depth;
	map.put(hash, str);
	((DefaultListModel) list.getModel()).addElement(str);
    }

    public void onFinish(String hash) {
	count--;
	((DefaultListModel) list.getModel()).removeElement(map.get(hash));
	int size = ((DefaultListModel) list.getModel()).size();
	System.out.println("FetchProgressDialog.onFinish()" + size);
	if (count == 0) {

	    setVisible(false);
	}
    }

}
