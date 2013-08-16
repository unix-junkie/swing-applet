/*-
 * $Id$
 */
package com.example;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;

import com.example.backport.java.util.Arrays;
import com.example.backport.java.util.Comparator;

/**
 * @author Andrew ``Bass'' Shcheglov (mailto:andrewbass@gmail.com)
 */
public final class Main extends JApplet {
	private static final long serialVersionUID = 2894495448617220305L;

	/**
	 * @see Applet#init()
	 */
	public void init() {
		final JPanel contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(newContentPane(true));
	}

	private static String[] getPropertyNames(final boolean runningAsApplet) {
		final String keys[];
		if (runningAsApplet) {
			keys = new String[] {
				"java.class.version",
				"java.vendor",
				"java.vendor.url",
				"java.version",
				"os.name",
				"os.arch",
				"os.version",
				"file.separator",
				"path.separator",
				"line.separator",
			};
		} else {
			final Properties properties = System.getProperties();
			final Enumeration it = properties.keys();
			keys = new String[properties.size()];
			int i = 0;
			while (it.hasMoreElements()) {
				keys[i++] = (String) it.nextElement();
			}
		}
		Arrays.sort(keys, new Comparator() {
			/**
			 * @see Comparator#compare(Object, Object)
			 */
			public int compare(final Object o1, final Object o2) {
				final String left = (String) o1;
				final String right = (String) o2;

				final int len1 = left.length();
				final int len2 = right.length();
				int n = Math.min(len1, len2);
				int i = 0;
				int j = 0;

				while (n-- != 0) {
				    final char c1 = left.charAt(i++);
				    final char c2 = right.charAt(j++);
				    if (c1 != c2) {
					return c1 - c2;
				    }
				}
				return len1 - len2;
			}
		});
		return keys;
	}

	private static JComponent newContentPane(final boolean runningAsApplet) {
		final String keys[] = getPropertyNames(runningAsApplet);

		final JTable table = new JTable();
		table.setModel(new AbstractTableModel() {
			private static final long serialVersionUID = 4350291017196065342L;

			/**
			 * @see AbstractTableModel#getColumnClass(int)
			 */
			public Class getColumnClass(final int columnIndex) {
				return String.class;
			}

			/**
			 * @see AbstractTableModel#getColumnName(int)
			 */
			public String getColumnName(final int column) {
				return column == 0 ? "Property" : "Value";
			}

			/**
			 * @see AbstractTableModel#getValueAt(int, int)
			 */
			public Object getValueAt(final int rowIndex, final int columnIndex) {
				final String key = keys[rowIndex];
				return columnIndex == 0 ? key : System.getProperty(key);
			}

			/**
			 * @see AbstractTableModel#getRowCount()
			 */
			public int getRowCount() {
				return keys.length;
			}

			/**
			 * @see AbstractTableModel#getColumnCount()
			 */
			public int getColumnCount() {
				return 2;
			}
		});

		final JViewport viewPort = new JViewport();
		viewPort.add(table);

		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewport(viewPort);
		scrollPane.setPreferredSize(new Dimension(800, 600));
		return scrollPane;
	}

	/**
	 * @param args
	 */
	public static void main(final String args[]) {
		final JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			/**
			 * @see WindowAdapter#windowClosing(WindowEvent)
			 */
			public void windowClosing(final WindowEvent e) {
				System.exit(0);
			}
		});
		frame.setTitle("System Properties");
		frame.setResizable(false);
		final JPanel contentPane = (JPanel) frame.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(newContentPane(false));

		frame.pack();
		frame.setVisible(true);
	}
}
