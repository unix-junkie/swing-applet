/*-
 *
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
		sort(keys);
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

	private static void swap(final String x[], final int a, final int b) {
		final String t = x[a];
		x[a] = x[b];
		x[b] = t;
	}

	private static void sort(final String[] a) {
		final String aux[] = (String[]) a.clone();
		mergeSort(aux, a, 0, a.length);
	}

	private static void mergeSort(final String src[], final String dest[], final int low, final int high) {
		final int length = high - low;

		// Insertion sort on smallest arrays
		if (length < 7) {
			for (int i = low; i < high; i++) {
				for (int j = i; j > low && compare(dest[j - 1], dest[j]) > 0; j--) {
					swap(dest, j, j - 1);
				}
			}
			return;
		}

		// Recursively sort halves of dest into src
		final int mid = (low + high) / 2;
		mergeSort(dest, src, low, mid);
		mergeSort(dest, src, mid, high);

		// If list is already sorted, just copy from src to dest. This
		// is an
		// optimization that results in faster sorts for nearly ordered
		// lists.
		if (compare(src[mid - 1], src[mid]) <= 0) {
			System.arraycopy(src, low, dest, low, length);
			return;
		}

		// Merge sorted halves (now in src) into dest
		for (int i = low, p = low, q = mid; i < high; i++) {
			if (q >= high || p < mid && compare(src[p], src[q]) <= 0) {
				dest[i] = src[p++];
			} else {
				dest[i] = src[q++];
			}
		}
	}

	private static int compare(final String left, final String right) {
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
}
