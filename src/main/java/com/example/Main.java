/*-
 * $Id$
 */
package com.example;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;
import javax.swing.table.AbstractTableModel;

import com.example.backport.java.util.ArrayList;
import com.example.backport.java.util.Arrays;
import com.example.backport.java.util.Collections;
import com.example.backport.java.util.Comparator;
import com.example.backport.java.util.Iterator;
import com.example.backport.java.util.List;
import com.example.backport.java.util.SortedSet;
import com.example.backport.java.util.TreeSet;

/**
 * @author Andrew ``Bass'' Shcheglov (mailto:andrewbass@gmail.com)
 */
public final class Main extends JApplet {
	private static final long serialVersionUID = 2894495448617220305L;

	/**
	 * @see "<em>The Java Virtual Machine Specification</em>, table 4.1"
	 */
	private static short ACC_PUBLIC = 0x0001;

	/**
	 * @see "<em>The Java Virtual Machine Specification</em>, table 4.1"
	 */
	private static short ACC_ABSTRACT = 0x0400;

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
	 * @param path
	 */
	private static String[] split(final String path) {
		if (path == null || path.length() == 0) {
			return new String[0];
		}
		final String pathSeparator = System.getProperty("path.separator");
		int fromIndex = 0;
		int toIndex;
		final List pathElements = new ArrayList();
		while ((toIndex = path.indexOf(pathSeparator, fromIndex)) != -1) {
			final String pathElement = path.substring(fromIndex, toIndex);
			pathElements.add(pathElement);
			fromIndex = toIndex + 1;
		}
		pathElements.add(pathElements.isEmpty() ? path : path.substring(fromIndex));
		return (String[]) pathElements.toArray(new String[0]);
	}

	/**
	 * @param baseClass
	 * @param skipInnerClasses
	 * @param skipAnonymousClasses
	 * @param skipNonPublic
	 * @param skipDeprecated
	 * @param packagesToSkip
	 */
	private static SortedSet listDescendants(final Class baseClass,
			final boolean skipInnerClasses,
			final boolean skipAnonymousClasses,
			final boolean skipNonPublic,
			final boolean skipAbstract,
			final List packagesToSkip) {
		if (packagesToSkip == null) {
			throw new IllegalArgumentException();
		}

		final String javaClassPath = System.getProperty("java.class.path");
		final String sunBootClassPath = System.getProperty("sun.boot.class.path");
		final List pathEntries = new ArrayList();
		pathEntries.addAll(Arrays.asList(split(javaClassPath)));
		pathEntries.addAll(Arrays.asList(split(sunBootClassPath)));
		final SortedSet classes = new TreeSet(new Comparator() {
			/**
			 * @see Comparator#compare
			 */
			public int compare(final Object class0, final Object class1) {
				if (class0 == null || class1 == null) {
					throw new IllegalArgumentException();
				}
				return ((Class) class0).getName().compareTo(((Class) class1).getName());
			}
		});
		for (final Iterator it = pathEntries.iterator(); it.hasNext(); ) {
			final String path = (String) it.next();
			final File file = new File(path);
			if (!file.exists() || file.isDirectory() || !file.isFile()) {
				continue;
			}
			try {
				final FileInputStream in = new FileInputStream(file);
				try {
					final ZipInputStream jis = new ZipInputStream(in);
					try {
						ZipEntry entry;
						nextClass:
						while ((entry = jis.getNextEntry()) != null) {
							if (entry.isDirectory()) {
								/*
								 * Skip directories.
								 */
								continue;
							}
							final String entryName = entry.getName();
							final int indexOfDotClass = entryName.indexOf(".class");
							if (indexOfDotClass == -1) {
								/*
								 * Skip resources.
								 */
								continue;
							}
							final String className = entryName.substring(0, indexOfDotClass).replace('/', '.');
							if (skipInnerClasses && className.indexOf('$') != -1) {
								continue;
							}

							for (final Iterator it2 = packagesToSkip.iterator(); it2.hasNext(); ) {
								final String packageToSkip = (String) it2.next();
								if (className.startsWith(packageToSkip + '.')) {
									continue nextClass;
								}
							}

							try {
								final Class clazz = Class.forName(className);
								if (!baseClass.isAssignableFrom(clazz)) {
									continue;
								}

								final int modifiers = clazz.getModifiers();
								if (skipNonPublic && (modifiers & ACC_PUBLIC) == 0) {
									continue;
								}

								if (skipAbstract && (clazz.getModifiers() & ACC_ABSTRACT) != 0) {
									continue;
								}

								classes.add(clazz);
							} catch (final ClassNotFoundException cnfe) {
								// ignore
							} catch (final UnsatisfiedLinkError ule) {
								// ignore
							} catch (final ExceptionInInitializerError eiie) {
								// ignore
							} catch (final NoClassDefFoundError ncdfe) {
								// ignore
							} catch (final OutOfMemoryError oome) {
								throw oome;
							} catch (final Throwable t) {
								// ignore
							}
						}
					} finally {
						jis.close();
					}
				} finally {
					in.close();
				}
			} catch (final IOException ioe) {
				// ignore
			}
		}
		return classes;
	}

	/**
	 * @param lookAndFeel
	 * @param themeMenu
	 * @param c
	 */
	private static JRadioButtonMenuItem fromLookAndFeel(final LookAndFeel lookAndFeel,
			final JMenu themeMenu,
			final JFrame frame) {
		if (lookAndFeel == null) {
			throw new IllegalArgumentException();
		}

		final JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem();
		menuItem.setText(lookAndFeel.getName());
		menuItem.setToolTipText(lookAndFeel.getClass().getName());
		menuItem.setEnabled(lookAndFeel.isSupportedLookAndFeel());
		menuItem.setSelected(UIManager.getLookAndFeel().getClass() == lookAndFeel.getClass());
		menuItem.addActionListener(new ActionListener() {
			/**
			 * @see ActionListener#actionPerformed(ActionEvent)
			 */
			public void actionPerformed(final ActionEvent e) {
				try {
					UIManager.setLookAndFeel(lookAndFeel);
					themeMenu.setEnabled(lookAndFeel instanceof MetalLookAndFeel);
					SwingUtilities.updateComponentTreeUI(frame);
				} catch (final Exception e1) {
					menuItem.setEnabled(false);
				}
			}
		});
		return menuItem;
	}

	/**
	 * @param metalTheme
	 * @param c
	 */
	private static JRadioButtonMenuItem fromMetalTheme(final MetalTheme metalTheme, final Component c) {
		if (metalTheme == null) {
			throw new IllegalArgumentException();
		}

		final JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem();
		menuItem.setText(metalTheme.getName());
		final String className = metalTheme.getClass().getName();
		menuItem.setToolTipText(className);
		menuItem.setName(className);
		menuItem.addActionListener(new ActionListener() {
			/**
			 * @see ActionListener#actionPerformed(ActionEvent)
			 */
			public void actionPerformed(final ActionEvent e) {
				try {
					MetalLookAndFeel.setCurrentTheme(metalTheme);
					UIManager.setLookAndFeel(UIManager.getLookAndFeel());

					SwingUtilities.updateComponentTreeUI(c);

					menuItem.setSelected(true);
				} catch (final Exception e1) {
					menuItem.setEnabled(false);
				}
			}
		});
		return menuItem;
	}

	/**
	 * @param args
	 */
	public static void main(final String args[]) {
		final List packagesToSkip = Arrays.asList(new String[] {
				"com.apple.crypto",
				"com.oracle",
				"com.sun.crypto",
				"com.sun.management",
				"com.sun.net.ssl",
				"com.sun.org.apache.xml.internal.security.utils",
				"com.sun.security",
				"java",
				"javax.crypto",
				"javax.net.ssl",
				"javax.swing.text",
				"oracle",
				"org.apache.xalan.extensions",
				"sun",
				"sunw"
		});

		final JFrame frame = new JFrame();

		final JMenu themeMenu = new JMenu();
		final ButtonGroup themeMenuGroup = new ButtonGroup();
		themeMenu.setText("Themes");
		themeMenu.setMnemonic('T');
		themeMenu.setEnabled(UIManager.getLookAndFeel() instanceof MetalLookAndFeel);
		for (final Iterator it = listDescendants(MetalTheme.class, false, false, true, true, packagesToSkip).iterator(); it.hasNext(); ) {
			try {
				final Class clazz = (Class) it.next();
				final MetalTheme metalTheme = (MetalTheme) clazz.newInstance();
				final JRadioButtonMenuItem menuItem = fromMetalTheme(metalTheme, frame);
				themeMenu.add(menuItem);
				themeMenuGroup.add(menuItem);
			} catch (final InstantiationException ie) {
				// ignore
			} catch (final IllegalAccessException iae) {
				// ignore
			}
		}

		final JMenu lookAndFeelMenu = new JMenu();
		final ButtonGroup lookAndFeelMenuGroup = new ButtonGroup();
		lookAndFeelMenu.setText("Look & Feel");
		lookAndFeelMenu.setMnemonic('L');
		final SortedSet descendants = listDescendants(LookAndFeel.class, false, false, true, true, packagesToSkip);
		for (final Iterator it = descendants.iterator(); it.hasNext(); ) {
			try {
				final Class clazz = (Class) it.next();
				final JRadioButtonMenuItem menuItem = fromLookAndFeel((LookAndFeel) clazz.newInstance(), themeMenu, frame);
				lookAndFeelMenu.add(menuItem);
				lookAndFeelMenuGroup.add(menuItem);
			} catch (final InstantiationException ie) {
				// ignore
			} catch (final IllegalAccessException iae) {
				// ignore
			}
		}

		final JMenuBar menuBar = new JMenuBar();
		menuBar.add(lookAndFeelMenu);
		menuBar.add(themeMenu);

		frame.setJMenuBar(menuBar);
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

		/*-
		 * In 1.4 and earlier versions, we have no mechanism
		 * to determine currently selected metal theme.
		 *
		 * So just select the first menu item
		 * and set the appropriate theme.
		 */
		SwingUtilities.invokeLater(new Runnable() {
			/**
			 * @see Runnable#run()
			 */
			public void run() {
				final Iterator it = Collections.list(themeMenuGroup.getElements()).iterator();
				if (it.hasNext()) {
					((AbstractButton) it.next()).doClick();
				}
			}
		});
	}
}
