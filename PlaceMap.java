import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import static javax.swing.JOptionPane.*;

public class PlaceMap extends JFrame {

	String filename = "";
	HashMap<Position, Place> places = new HashMap<Position, Place>();
	HashMap<String, ArrayList> positions = new HashMap<String, ArrayList>();
	Boolean unsaved = false;
	Boolean whatplace = false;
	Boolean newplace = false;
	Boolean unsavednew = false;
	Boolean laddatB = false;
	Color ccolor;
	Map map = new Map();
	ImageIcon mapimage;
	JLabel mapboard = new JLabel();
	JMenu menu = new JMenu("Arkiv");
	JMenuItem newmap = new JMenuItem("New map");
	JMenuItem open = new JMenuItem("Open");
	JMenuItem save = new JMenuItem("Save");
	JMenuItem exit = new JMenuItem("Exit");
	String[] data = { "New Place", "New Described Place" };
	JComboBox<String> dp = new JComboBox<String>(data);
	JTextField sf = new JTextField("Search", 10);
	JButton search = new JButton("Search");
	JButton hidep = new JButton("Hide places");
	JButton deletep = new JButton("Delete places");
	JButton what = new JButton("What is here?");
	JList<Categori> list;
	JButton hidek = new JButton("Hide category");
	JButton newk = new JButton("New category");
	JButton deletek = new JButton("Delete category");
	JScrollPane scroll;
	JScrollPane mapscroll;
	DefaultListModel<Categori> listModel = new DefaultListModel<Categori>();
	JColorChooser cc = new JColorChooser();
	JFileChooser jfc = new JFileChooser(".");

	PlaceMap() {
		super("Map-O-Tron 20XX!");
		setLayout(new BorderLayout());
		JMenuBar top = new JMenuBar();
		JPanel bottom = new JPanel();
		bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
		top.add(menu);
		top.add(Box.createHorizontalGlue());
		menu.add(newmap);
		menu.add(open);
		menu.add(save);
		menu.add(exit);
		Dimension d = new Dimension(200, 25);
		dp.setPreferredSize(d);
		dp.setMaximumSize(d);
		dp.setMinimumSize(d);
		sf.setPreferredSize(d);
		sf.setMaximumSize(d);
		sf.setMinimumSize(d);
		bottom.add(new JLabel("New:"));
		bottom.add(dp);
		bottom.add(sf);
		bottom.add(search);
		bottom.add(hidep);
		bottom.add(deletep);
		bottom.add(what);
		bottom.add(Box.createHorizontalGlue());
		JPanel both = new JPanel();
		both.setLayout(new BoxLayout(both, BoxLayout.Y_AXIS));
		both.add(top);
		both.add(bottom);
		add(both, BorderLayout.NORTH);
		JPanel right = new JPanel();
		right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

		list = new JList<Categori>(listModel);
		list.setPreferredSize(new Dimension(100, 100));
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		scroll = new JScrollPane(list);
		scroll.setPreferredSize(new Dimension(100, 100));

		right.add(Box.createVerticalGlue());
		right.add(new JLabel("Categories"));
		right.add(scroll);
		right.add(hidek);
		right.add(newk);
		right.add(deletek);
		right.add(Box.createVerticalGlue());
		add(right, BorderLayout.EAST);

		map.setLayout(null);

		mapscroll = new JScrollPane(map);
		mapscroll.setPreferredSize(new Dimension(400, 400));
		add(mapscroll, BorderLayout.CENTER);

		addWindowListener(new exitListener());
		dp.addActionListener(new NewPlace());
		newk.addActionListener(new newCategori());
		deletek.addActionListener(new deleteCategori());
		hidek.addActionListener(new hideCategori());
		what.addActionListener(new whatIsHere());
		deletep.addActionListener(new deletePlace());
		hidep.addActionListener(new hidePlace());
		search.addActionListener(new search());
		sf.addMouseListener(new searchField());
		map.addMouseListener(new mapAdapter());
		newmap.addActionListener(new newMap());
		open.addActionListener(new open());
		save.addActionListener(new save());
		exit.addActionListener(new exit());
		list.addListSelectionListener(new list());
		cc.getSelectionModel().addChangeListener(new ColorSelection());

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setSize(800, 500);
		setMinimumSize(new Dimension(800, 500));
		setVisible(true);
	}

	class NewPlace implements ActionListener {
		public void actionPerformed(ActionEvent ave) {
			newplace = true;
			unsaved = true;
		}
	}

	class newMap implements ActionListener {
		public void actionPerformed(ActionEvent ave) {
			if (unsaved) {
				int g = JOptionPane.showConfirmDialog(null,
						"You have unsaved changes, do you still want to exit?",
						"Warning!", JOptionPane.YES_NO_OPTION,
						JOptionPane.ERROR_MESSAGE);
				if (g == JOptionPane.YES_OPTION) {
					theNewMap();
				} else if (g == JOptionPane.NO_OPTION) {
					return;
				}
			} else {
				theNewMap();
			}
		}

		public void theNewMap() {
			try {
				places.clear();
				map.newMap();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	class list implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent lev) {
			for (Place p : places.values()) {
				if (!p.getVisible()
						&& p.getCategori() == list.getSelectedValue()) {
					p.setVisible();
				}
			}
		}
	}

	class open implements ActionListener {
		public void actionPerformed(ActionEvent ave) {
			if (unsaved) {
				int g = JOptionPane.showConfirmDialog(null,
						"You have unsaved changes, do you still "
								+ "want to open a new map", "Warning!",
						JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
				if (g == JOptionPane.YES_OPTION) {
					places.clear();
					int svar = jfc.showOpenDialog(PlaceMap.this);
					if (svar != JFileChooser.APPROVE_OPTION) {
						return;
					}
				} else if (g != JOptionPane.YES_OPTION) {
					return;
				}
			}
			openMap();
		}

		public void openMap() {
			map.setMapURL("");
			places.clear();
			listModel.clear();
			int answer = jfc.showOpenDialog(PlaceMap.this);
			if (answer != JFileChooser.APPROVE_OPTION) {
				return;
			}
			File file = jfc.getSelectedFile();
			String filename = file.getAbsolutePath();

			try {
				FileInputStream fis = new FileInputStream(filename);
				ObjectInputStream ois = new ObjectInputStream(fis);
				Object o = ois.readObject();
				saveStuff a = (saveStuff) o;				System.out.println("b");
				map.setMap(a.getMapURL());
				for (Object ob : a.getCategories().toArray()) {
					if (ob instanceof Categori) {
						listModel.addElement((Categori) ob);
					}
				}
				for (Position p : a.getPlaces().keySet()) {
					Place pl1 = a.getPlaces().get(p);
					Place pl2;
					for (Object ob : listModel.toArray()) {
						Categori c = (Categori) ob;

						if (c.getName().equals(pl1.getCategoriname())) {
							list.setSelectedIndex(listModel.indexOf(ob));
							System.out.println("tilldelad");
						}
					}
					if (pl1 instanceof DescribedPlace) {
						pl2 = new DescribedPlace(pl1.getName(),
								pl1.getPosition(),
								((DescribedPlace) pl1).getDescription());

					} else {
						pl2 = new Place(pl1.getName(), pl1.getPosition());
					}
					if (pl1.getCategoriname().length() > 0) {
						pl2.setCategori(list.getSelectedValue());
					}
					places.put(p, pl2);
					map.add(pl2.getArrow());
				}
				map.repaint();
				pack();
				validate();
				repaint();
				ois.close();
				fis.close();
			} catch (FileNotFoundException e) {
				showMessageDialog(PlaceMap.this, "Cant open file!");
			} catch (IOException e) {
				showMessageDialog(PlaceMap.this,
						"Error: IO exception" + e.getMessage());
			} catch (ClassNotFoundException e) {
				showMessageDialog(PlaceMap.this,
						"Cant find class:" + e.getMessage());
			}// felmeddelanden
			pack();
			validate();
			repaint();
			laddatB = true;
			unsaved = false;
			unsavednew = false;
		}
	}

	class saveStuff implements Serializable {

		private static final long serialVersionUID = 1L;
		String mapURL;
		HashMap<Position, Place> theplaces;
		DefaultListModel<Categori> theCategories;

		saveStuff() {
			mapURL = map.getMapURL();
			theplaces = places;
			theCategories = listModel;
		}

		public String getMapURL() {
			return mapURL;
		}

		public HashMap<Position, Place> getPlaces() {
			return theplaces;
		}

		public DefaultListModel<Categori> getCategories() {
			return theCategories;
		}
	}

	class save implements ActionListener {
		public void actionPerformed(ActionEvent ave) {
			if (unsaved && filename.length() > 0) {
				doSave();
			} else if (unsaved) {
				jfc.showSaveDialog(null);
				filename = jfc.getSelectedFile().getAbsolutePath();
				doSave();
			}

			unsaved = false;
			unsavednew = false;
		}

		public void doSave() {
			try {
				FileOutputStream fos = new FileOutputStream(filename);
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				saveStuff ss = new saveStuff();
				for (Position p : ss.getPlaces().keySet()) {
					ss.getPlaces().get(p).getArrow().setML();
					ss.getPlaces().get(p).removeCategori();
				}
				oos.writeObject(ss);
				oos.flush();
				oos.close();
				fos.flush();
				fos.close();
				for (Position p : ss.getPlaces().keySet()) {
					Place pl = ss.getPlaces().get(p);
					ss.getPlaces().get(p).getArrow().setML();
					for (Object ob : listModel.toArray()) {
						Categori c = (Categori) ob;

						if (c.getName().equals(pl.getCategoriname())) {
							list.setSelectedIndex(listModel.indexOf(ob));
						}
					}
					ss.getPlaces().get(p).setCategori(list.getSelectedValue());
				}
			} catch (FileNotFoundException e) {
				showMessageDialog(PlaceMap.this, "Cant open file!");
			} catch (IOException e) {
				showMessageDialog(PlaceMap.this, "Error:" + e.getMessage());
			}
		}
	}

	class exit implements ActionListener {
		public void actionPerformed(ActionEvent ave) {
			if (unsaved) {
				int g = JOptionPane.showConfirmDialog(null,
						"You have unsaved changes, do you want exit anyway?",
						"Warning!", JOptionPane.YES_NO_OPTION,
						JOptionPane.ERROR_MESSAGE);
				if (g == JOptionPane.YES_OPTION) {
					System.exit(0);
				}
				if (g != JOptionPane.YES_OPTION)
					return;
			}

			System.exit(0);
		}

	}

	class exitListener extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent Exit) {
			if (unsaved) {
				int g = JOptionPane.showConfirmDialog(null,
						"You have unsaved changes, do you still want to exit?",
						"Warning!", JOptionPane.YES_NO_OPTION,
						JOptionPane.ERROR_MESSAGE);
				if (g == JOptionPane.YES_OPTION) {
					System.exit(0);
				}
				if (g != JOptionPane.YES_OPTION) {
					return;
				}
			} else {
				System.exit(0);
			}
		}

	}

	class newCategori implements ActionListener {
		public void actionPerformed(ActionEvent ave) {
			CategoriFrame K = new CategoriFrame();
			showConfirmDialog(PlaceMap.this, K, "New Categori",
					JOptionPane.OK_CANCEL_OPTION);
			Categori k = new Categori(K.getName(), ccolor);
			listModel.addElement(k);
			unsaved = true;
		}
	}

	class deleteCategori implements ActionListener {
		public void actionPerformed(ActionEvent ave) {
			if (places.size() > 0) {
				Iterator<HashMap.Entry<Position, Place>> iter = places
						.entrySet().iterator();
				while (iter.hasNext()) {
					HashMap.Entry<Position, Place> entry = iter.next();
					if (entry.getValue().getCategori() == list
							.getSelectedValue()) {
						if (entry.getValue().getVisible()) {
							entry.getValue().setVisible();
						}
						iter.remove();
					}
				}
			}
			if (list.getSelectedIndex() != -1) {
				int index = list.getSelectedIndex();
				listModel.remove(index);
				unsaved = true;
			}
		}
	}

	class hideCategori implements ActionListener {
		public void actionPerformed(ActionEvent ave) {
			for (Place p : places.values()) {
				if (p.getCategori() == list.getSelectedValue() && p.isVisible()) {
					p.setVisible();
					if (p.getMarked()) {
						p.setMarked();
					}
				}
			}
		}
	}

	class whatIsHere implements ActionListener {
		public void actionPerformed(ActionEvent ave) {
			whatplace = true;
		}
	}

	class deletePlace implements ActionListener {
		public void actionPerformed(ActionEvent ave) {
			if (places.size() > 0) {
				Iterator<HashMap.Entry<Position, Place>> iter = places
						.entrySet().iterator();
				while (iter.hasNext()) {
					HashMap.Entry<Position, Place> entry = iter.next();
					if (entry.getValue().getMarked()) {
						entry.getValue().setVisible();
						ArrayList al = positions.get(entry.getValue().getName());
						int index=0;
//						for(index = 0; index<al.size(); index++){}
						while(al.get(index)!= entry.getKey()){
							index++;
						}
						al.remove(index);
						positions.remove(entry.getValue().getName());
						iter.remove();
						unsaved = true;
					}
				}
			}
		}
	}

	class hidePlace implements ActionListener {
		public void actionPerformed(ActionEvent ave) {
			for (Place p : places.values()) {
				if (p.getMarked()) {
					p.setVisible();
					p.setMarked();
				}
			}
		}
	}

	class search implements ActionListener {
		public void actionPerformed(ActionEvent ave) {
			String str = sf.getText();
			sf.setText("Search");
			if(positions.get(str) != null){
			ArrayList<Position> al = positions.get(str);   //
			for(Position p : al){
				Place place = places.get(p);
				if(!place.getVisible()){
				place.setVisible();
				}
				place.setMarked();
			}
			}
			}

//			Iterator i = set.iterator();
//			while(i.hasNext(){
//				Position pos = (Position)i.next();
//				if (str == get(str);
			}
//			places.get(str).setMarked();;
//			for (Place p : places.values()) {
//				if (p.getName().equals(str)) {
//					if (!p.getVisible()) {
//						p.setVisible();
//					}
//					if (!p.getMarked()) {
//						p.setMarked();
//					}
//				} else if (p.getMarked()) {
//					p.setMarked();
//				}
//
//			}

	class searchField extends MouseAdapter {
		public void mouseClicked(MouseEvent mev) {
			if (sf.getText().equals("Search")) {
				sf.setText("");
			}
		}
	}

	class mapAdapter extends MouseAdapter {
		public void mouseEntered(MouseEvent mev) {
			if (newplace || whatplace) {
				setCursor(Cursor.CROSSHAIR_CURSOR);
			}
		}

		public void mouseExited(MouseEvent mev) {
			setCursor(Cursor.DEFAULT_CURSOR);
		}

		public void mouseClicked(MouseEvent mev) {
			int x = mev.getX();
			int y = mev.getY();
			Position pos = new Position(x, y);
			if (laddatB == true) {
				if (newplace) {
					newplace = false;
					setCursor(Cursor.DEFAULT_CURSOR);
					PlaceFrame pf = new PlaceFrame();
					showConfirmDialog(PlaceMap.this, pf, "New place",
							JOptionPane.OK_CANCEL_OPTION);
					Place place;
					if (pf.korrekt()) {
						if (dp.getSelectedIndex() == 0) {
							place = new Place(pf.getName(), pos);
						} else {
							place = new DescribedPlace(pf.getName(), pos,
									pf.getDescription());
						}
						if (list.getSelectedIndex() != -1) {
							place.setCategori(list.getSelectedValue());
						}
						places.put(pos, place);
						map.add(place.getArrow());
						map.repaint();

					}
				} else if (whatplace) {
					whatplace = false;
					setCursor(Cursor.DEFAULT_CURSOR);
					for (Place p : places.values()) {
						if ((x - 15) <= p.getPosition().getX()
								&& p.getPosition().getX() <= (x + 15)
								&& (y - 15) <= p.getPosition().getY()
								&& p.getPosition().getY() <= (y + 15)) {
							if (!p.getVisible()) {
								p.setVisible();
							}
						}
					}

				}
			}
		}
	}

	class markedPlace extends MouseAdapter implements Serializable {
		public void mouseClicked(MouseEvent mev) {
			if (SwingUtilities.isLeftMouseButton(mev)) {
				if (mev.getSource() instanceof Arrow) {
					Arrow a = (Arrow) mev.getSource();
					Place p = places.get(a.getPosition());
					p.setMarked();
				}
			} else if (SwingUtilities.isRightMouseButton(mev)) {
				if (mev.getSource() instanceof Arrow) {
					Arrow a = (Arrow) mev.getSource();
					Place p = places.get(a.getPosition());
					p.setOpen();
				}
			}
		}
	}

	class PlaceDisplay extends JComponent implements Serializable {
		private boolean open = false;
		private Color color = Color.black;
		private String str;
		private int x, y;
		private int rad;

		public PlaceDisplay(Position p, String s) {
			x = p.getX();
			y = p.getY();
			str = s;
			rad = (int) (((str.length() / 10) + 1) * 13);
			setBounds(x + 20, y - 10, 60, rad);
			Dimension d = new Dimension(60, rad);
			setPreferredSize(d);
			setMaximumSize(d);
			setMinimumSize(d);
			if (list.getSelectedIndex() != -1) {
				color = (listModel.get(list.getSelectedIndex()).getColor());
			}
		}

		public void setOpen() {
			if (open) {
				open = false;
				repaint();
			} else
				open = true;
			repaint();
		}

		public boolean getOpen() {
			return open;
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (open) {
				g.setColor(color.white);
				g.fillRect(0, 0, 60, rad);
				String strt = str;
				g.setColor(color);
				g.setFont(new Font("TimesRoman", Font.PLAIN, 12));
				int index = 10;
				int yPos = 10;
				String str1 = str;
				while (index < str.length()) {
					str1 = strt.substring(index - 10, index);
					g.drawString(str1, 2, yPos);
					yPos += 13;
					index += 10;
				}
				g.drawString(str1, 2, yPos);
			}
		}
	}

	class NameDisplay extends JComponent implements Serializable {
		private boolean open = false;
		private Color color = Color.black;
		private String str;
		private int x, y;

		public NameDisplay(String name, Position p) {
			this.str = name;
			this.x = p.getX();
			this.y = p.getY();
			setBounds((x - 100), (y - 35), 200, 25);
			Dimension d = new Dimension(200, 25);
			setPreferredSize(d);
			setMaximumSize(d);
			setMinimumSize(d);
			if (list.getSelectedIndex() != -1) {
				color = (listModel.get(list.getSelectedIndex()).getColor());
			}
		}

		public void setOpen() {
			if (open) {
				open = false;
				repaint();
			} else {
				open = true;
				repaint();
			}
		}

		public boolean getOpen() {
			return open;
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (open) {
				int stringLen = (int) g.getFontMetrics()
						.getStringBounds(str, g).getWidth();
				int start = (100) - (stringLen / 2);
				g.setColor(color);
				g.setFont(new Font("TimesRoman", Font.PLAIN, 18));
				g.drawString(str, start, 20);
			}
		}
	}

	class MarkedBox extends JComponent implements Serializable {
		private Color color = Color.black;
		private int x;
		private int y;
		private int[] xPoints = { 0, 10, 10, 0, 0 };
		private int[] yPoints = { 0, 0, 10, 10, 0 };
		private boolean marked = false;

		public MarkedBox(Position p) {
			this.x = p.getX();
			this.y = p.getY();
			setBounds(x - 4, y - 10, 15, 15);
			Dimension d = new Dimension(15, 15);
			setPreferredSize(d);
			setMaximumSize(d);
			setMinimumSize(d);
			if (list.getSelectedIndex() != -1) {
				color = (listModel.get(list.getSelectedIndex()).getColor());
			}
		}

		public void setMarked() {
			if (marked) {
				marked = false;
				repaint();
			} else {
				marked = true;
				repaint();
			}
		}

		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (marked) {
				g.setColor(color);
				g.drawPolygon(xPoints, yPoints, xPoints.length);
			}
		}

		public boolean getMarked() {
			return marked;
		}
	}

	class Arrow extends JComponent implements Serializable {
		private int[] xPoints = { 0, 10, 5, 0 };
		private int[] yPoints = { 0, 0, 10, 0 };
		private Color color = Color.black;
		private int x;
		private int y;
		private Position p;
		private boolean ml = false;
		private MouseListener MouseL;

		public Arrow(Position p) {
			this.p = p;
			this.x = p.getX();
			this.y = p.getY();
			setBounds(x - 5, y - 10, 10, 10);
			Dimension d = new Dimension(10, 10);
			setPreferredSize(d);
			setMaximumSize(d);
			setMinimumSize(d);
			if (list.getSelectedIndex() != -1) {
				color = (listModel.get(list.getSelectedIndex()).getColor());
			}
			setML();
		}

		public void setML() {
			if (ml) {
				removeMouseListener(MouseL);
				ml = false;
			} else {
				addMouseListener(MouseL = new markedPlace());
				ml = true;
			}
		}

		public Position getPosition() {
			return p;
		}

		public String getCoordinate() {
			return x + ", " + y;
		}

		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(color);
			Polygon p = new Polygon(xPoints, yPoints, xPoints.length);
			g.fillPolygon(p);
		}
	}

	class PlaceFrame extends JPanel {
		private JTextField nfield = new JTextField(10);
		private JTextArea darea = new JTextArea(15, 15);

		public PlaceFrame() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			JPanel line1 = new JPanel();
			line1.add(new JLabel("Name: "));
			line1.add(nfield);
			add(line1);
			JPanel line2 = new JPanel();
			if (dp.getSelectedIndex() == 1) {
				darea.setLineWrap(true);
				darea.setWrapStyleWord(true);
				line2.setLayout(new BoxLayout(line2, BoxLayout.Y_AXIS));
				line2.add(new JLabel("Description"));
				darea.setBounds(0, 0, 150, 150);
				darea.setPreferredSize(new Dimension(150, 150));
				darea.setMinimumSize(new Dimension(150, 150));
				darea.setMaximumSize(new Dimension(150, 150));
				line2.add(darea);
				add(line2);
			}
		}

		public boolean korrekt() {
			if (dp.getSelectedIndex() == 0) {
				if (nfield.getText().isEmpty()) {
					return false;
				} else
					return true;
			} else if (nfield.getText().isEmpty() || darea.getText().isEmpty()) {
				return false;
			}
			return true;
		}

		public String getName() {
			return nfield.getText();
		}

		public String getDescription() {
			return darea.getText();
		}
	}

	class CategoriFrame extends JPanel {

		private JTextField nfield = new JTextField(10);

		public CategoriFrame() {
			AbstractColorChooserPanel[] cpanels = cc.getChooserPanels();
			for (AbstractColorChooserPanel accp : cpanels) {
				if (!accp.getDisplayName().equals("Prov")) {
					cc.removeChooserPanel(accp);
				}
			}
			cc.setPreviewPanel(new JPanel());
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			JPanel line1 = new JPanel();
			line1.add(new JLabel("Name: "));
			line1.add(nfield);
			add(line1);
			add(cc);
		}

		public String getName() {
			return nfield.getText();
		}
	}

	class ColorSelection implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			ccolor = cc.getColor();
		}
	}

	class Map extends JComponent implements Serializable {

		private static final long serialVersionUID = 1L;
		int height;
		int width;
		String mapURL = "";

		public Map() {
		}

		protected void newMap() throws IOException {
			int svar = jfc.showOpenDialog(PlaceMap.this);
			if (svar == JFileChooser.APPROVE_OPTION) {
				File file = jfc.getSelectedFile();
				String filename = file.getAbsolutePath();
				map.setMap(filename);
			}
		}

		public void setMap(String filename) {
			mapURL = filename;
			mapimage = new ImageIcon(filename);
			while (mapimage.getIconHeight() == -1) {
			}
			setWH();
			Dimension d = new Dimension(mapimage.getIconWidth(),
					mapimage.getIconHeight());
			map.setPreferredSize(d);
			map.setMinimumSize(d);
			map.setMaximumSize(d);
			map.validate();
			map.repaint();
			mapscroll.setPreferredSize(d);
			mapscroll.validate();
			mapscroll.repaint();
			pack();
			validate();
			repaint();
			unsaved = true;
			unsavednew = true;
			laddatB = true;
		}

		public String getMapURL() {
			return mapURL;
		}

		public void setMapURL(String s) {
			mapURL = s;
		}

		public void setWH() {
			height = mapimage.getIconHeight();
			width = mapimage.getIconWidth();

		}

		protected void paintComponent(Graphics g) {
			if (mapURL.length() > 0) {
				super.paintComponent(g);
				g.drawImage(mapimage.getImage(), 0, 0, width, height, this);
			}
		}

	}

	class Place extends JComponent implements Serializable {

		private static final long serialVersionUID = 1L;
		private String name;
		private Position position;
		private String categoriname = "";
		private boolean marked = false;
		private boolean open = false;
		private boolean visible = true;
		private boolean hascategori = false;
		private Categori categori;
		private Arrow arrow;
		private MarkedBox mb;
		private NameDisplay nd;

		public Place(String name, Position p) {
			arrow = new Arrow(p);
			mb = new MarkedBox(p);
			nd = new NameDisplay(name, p);
			this.name = name;
			this.position = p;
			places.put(this.position, this);
			if(positions.containsKey(this.name)){
				(positions.get(this.name)).add(this.position);
			}else{
				ArrayList<Position> al = new ArrayList<Position>();
				al.add(this.position);
			positions.put(this.name, al);}
			
		}

		public boolean getMarked() {
			return marked;
		}

		public boolean getVisible() {
			return visible;
		}

		public Arrow getArrow() {
			return arrow;
		}

		public MarkedBox getMarkedBox() {
			return mb;
		}

		public NameDisplay getNameDisplay() {
			return nd;
		}

		public Categori getCategori() {
			return categori;
		}

		public void removeCategori() {
			categori = null;
		}

		public void setCategori(Categori c) {
			categori = c;
			hascategori = true;
			categoriname = c.getName();
		}

		public String getCategoriname() {
			return categoriname;
		}

		public boolean getHasCategori() {
			return hascategori;
		}

		public String getName() {
			return name;
		}

		public Position getPosition() {
			return position;
		}

		public void setOpen() {
			if (nd.getOpen()) {
				nd.setOpen();
				nd.setVisible(false);
				map.remove(nd);
				map.repaint();
			} else {
				nd.setOpen();
				nd.setVisible(true);
				map.add(nd);
				nd.repaint();
				map.repaint();
			}
		}

		public void setMarked() {
			if (mb.getMarked()) {
				marked = false;
				mb.setVisible(false);
				mb.setMarked();
				map.remove(mb);
				map.repaint();
			} else {
				marked = true;
				mb.setMarked();
				mb.setVisible(true);
				map.add(mb);
				mb.repaint();
				map.repaint();
			}
		}

		public void setVisible() {
			if (visible) {
				visible = false;
				arrow.setVisible(false);
				mb.setVisible(false);
				nd.setVisible(false);
				map.repaint();
			} else {
				visible = true;
				arrow.setVisible(true);
				map.add(arrow);
				map.repaint();
			}
		}
	}

	class DescribedPlace extends Place {
		private String description;
		private PlaceDisplay pd;

		public DescribedPlace(String name, Position p, String description) {
			super(name, p);
			pd = new PlaceDisplay(p, description);
			this.description = description;
		}

		public String getDescription() {
			return description;
		}

		public void setOpen() {
			if (pd.open) {
				pd.setOpen();
				pd.setVisible(false);
				map.remove(pd);
				map.repaint();
			} else {
				pd.setOpen();
				pd.setVisible(true);
				map.add(pd);
				pd.repaint();
				map.repaint();
			}
			super.setOpen();
		}

		public void setVisible() {
			if (super.isVisible()) {
				pd.setVisible(false);
			} else {
				pd.setVisible(true);
			}
			super.setVisible();
		}
	}

	class PicturePlace extends Place {
		private Image image;

		public PicturePlace(String name, Position p, Image picture) {
			super(name, p);
		}

		public Image getImage() {
			return image;
		}
	}

	public static void main(String[] args) {
		new PlaceMap();

	}
}
