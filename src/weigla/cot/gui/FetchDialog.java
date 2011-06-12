package weigla.cot.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class FetchDialog extends JDialog implements ActionListener {
    private static final long serialVersionUID = -8920534695191639912L;
    private JLabel lblKeys;
    private JLabel lblRecur;
    private JLabel lblKeyserver;
    private JScrollPane scrollPane1;
    private JSlider sliRecur;
    private JTextArea txtKeys;
    private JTextField txtKeyserver;
    private JButton btnOk = new JButton("Fetch!");
    private JButton btnCancel = new JButton("Abort");

    private boolean accepted = false;

    public FetchDialog(Frame owner) {
	super(owner);
	setModal(true);
	setTitle("Fetch Dialog");
	setSize(500, 500);
	setResizable(false);
	init();
	Dimension d = owner.getSize();
	setLocation(owner.getX() + (d.width - getWidth()) / 2, owner.getY());
    }

    private void init() {
	lblKeys = new javax.swing.JLabel();
	scrollPane1 = new javax.swing.JScrollPane();
	txtKeys = new javax.swing.JTextArea();
	lblRecur = new javax.swing.JLabel();
	sliRecur = new javax.swing.JSlider();
	lblKeyserver = new javax.swing.JLabel();
	txtKeyserver = new javax.swing.JTextField();

	sliRecur.setMinimum(1);
	sliRecur.setMaximum(1000);

	btnOk.addActionListener(this);
	btnCancel.addActionListener(this);

	setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
	setLayout(new javax.swing.BoxLayout(getContentPane(),
		javax.swing.BoxLayout.PAGE_AXIS));

	lblKeys.setText("Start with keys:");
	add(lblKeys);
	txtKeys.setRows(5);
	scrollPane1.setViewportView(txtKeys);
	add(scrollPane1);

	add(new JSeparator());
	lblRecur.setText("Recurrence depth:");
	add(lblRecur);
	add(sliRecur);

	add(new JSeparator());
	lblKeyserver.setText("Keyserver:");
	add(lblKeyserver);
	add(txtKeyserver);

	add(new JSeparator());

	JPanel p = new JPanel();
	p.add(btnCancel);
	p.add(btnOk);
	add(p);

	txtKeys.setText("0x0FA9E9F0CC4E48FF");
	txtKeyserver.setText("keyserver.ubuntu.com:11371");
	sliRecur.setValue(10);

	pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	setVisible(false);
	if (e.getSource() == btnOk)
	    accepted = true;
    }

    public boolean getAccepted() {
	return accepted;
    }

    public String[] getKeys() {
	return txtKeys.getText().split("\n");
    }

    public String getKeyserver() {
	return txtKeyserver.getText();
    }

    public int getDepth() {
	return sliRecur.getValue();
    }

}
