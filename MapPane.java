import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Manner
 * Date: 6/19/13
 * Time: 12:06 AM
 */
public class MapPane extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {

    private BufferedImage map = null;
    private Point mapLoc = new Point(-1547, -1605);
    private double scale = 1.0;
    private Label status = new Label("Initializing...");
    public static final ArrayList<MapTile> TILES = new ArrayList<MapTile>();
    public static boolean areaSelected = false;
    public static int dragSpacing = 5;


    public MapPane() {
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        setLayout(new BorderLayout());
        status.setAlignment(Label.CENTER);
        add(status, BorderLayout.CENTER);
        setVisible(true);
    }

    public void paint(Graphics g2) {
        Controls.setTile(getTile());
        Graphics2D g = (Graphics2D) g2;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (map != null) {
            g.scale(scale, scale);
            g.drawImage(map, mapLoc.x, mapLoc.y, null);
            getTile().draw(g, mapLoc, Color.white);

            g.scale(1 / scale, 1 / scale);
            if (areaSelected) {
                g.setColor(Color.white);
                g.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, 0.5f));
                Polygon area = new Polygon();
                for (MapTile t : TILES) {
                    area.addPoint(t.getScaledGX(mapLoc, scale), t.getScaledGY(mapLoc, scale));
                }
                g.fill(area);
                g.setColor(Color.black);
                g.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, 1.0f));
            }
            for (int i = 1; i < TILES.size(); i++) {
                MapTile.drawLineBetween(g, mapLoc, scale, TILES.get(i - 1), TILES.get(i));
            }
            for (MapTile t : TILES) {
                t.drawInScale(g, mapLoc, Color.red, scale);
            }
        }
    }


    public MapTile getTile() {
        return new MapTile((int) Math.round(2047.5 + ((-mapLoc.x + (mouse.x / scale)) / 2)), (int) Math.round(4159 - ((-mapLoc.y + (mouse.y / scale)) / 2)));
    }

    public void moveMap(int xDev, int yDev) {
        int newX = mapLoc.x - xDev;
        int newY = mapLoc.y - yDev;
        if ((xDev > 0 && map.getWidth() > (-mapLoc.x) + getWidth() / scale) || (xDev < 0 && newX <= 0))
            mapLoc.x = newX;
        if ((yDev > 0 && map.getHeight() > (-mapLoc.y) + getHeight() / scale) || (yDev < 0 && newY <= 0))
            mapLoc.y = newY;
    }

    public void scale(double change) {
        double newScale = scale + change;
        if (newScale <= 10.1 && newScale >= 1.1) {
            scale = newScale;
            moveMap((int) (((change > 0) ? (getWidth() * .05) : (-getWidth() * .05)) / scale), (int) (((change > 0) ? (getHeight() * .05) : (-getHeight() * .05)) / scale));
        }
        repaint();
    }

    public void getMap() {
        try {
            status.setText("Connect to runescape.com...");
            BufferedReader read = new BufferedReader(new InputStreamReader(
                    new URL("http://www.runescape.com/downloads.ws").openStream()));
            String line;
            while ((line = read.readLine()) != null) {
                status.setText("Parsing to runescape.com...");
                if (line.contains("http://www.runescape.com/img/main/downloads_and_media/downloads_and_wallpapers/rs_map/")) {
                    Pattern pattern = Pattern.compile("\\w{4}:(/|\\w|\\.|-)+");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find())
                        line = matcher.group();
                    else {
                        status.setText("Error getting map!");
                    }
                    status.setText("Loading Map...");
                    map = ImageIO.read(new URL(line).openStream());
                    remove(status);
                    repaint();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean left = false;
    boolean right = false;
    Point mouse = new Point(0, 0);
    Point pathCursor = new Point(0, 0);

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getPoint().x >= 0 && e.getPoint().x <= getWidth() && e.getPoint().y >= 0 && e.getPoint().y <= getHeight()) {
            GUI.showCode(false);
        }
        if (e.getButton() == MouseEvent.BUTTON1) {
            left = true;
            if (e.getPoint().x >= 0 && e.getPoint().x <= getWidth() && e.getPoint().y >= 0 && e.getPoint().y <= getHeight()) {
                TILES.add(getTile());
            }
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            ((Component) e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            right = true;
        }
        if (e.getPoint().x >= 0 && e.getPoint().x <= getWidth() && e.getPoint().y >= 0 && e.getPoint().y <= getHeight())
            repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            left = false;
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            ((Component) e.getSource()).setCursor(Cursor.getDefaultCursor());
            right = false;
        }
        if (e.getPoint().x >= 0 && e.getPoint().x <= getWidth() && e.getPoint().y >= 0 && e.getPoint().y <= getHeight())
            repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (e.getPoint().x >= 0 && e.getPoint().x <= getWidth() && e.getPoint().y >= 0 && e.getPoint().y <= getHeight()) {
            if (right) {
                moveMap((int) (Math.round((mouse.x - e.getPoint().x) / scale)), (int) (Math.round((mouse.y - e.getPoint().y) / scale)));

            }
            if (left) {
                if (e.getPoint().distance(pathCursor) >= (dragSpacing * 2 * scale)) {
                    pathCursor = e.getPoint();
                    TILES.add(getTile());
                }
            } else {
                pathCursor = e.getPoint();
            }
            mouse = e.getPoint();
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (e.getPoint().x >= 0 && e.getPoint().x <= getWidth() && e.getPoint().y >= 0 && e.getPoint().y <= getHeight()) {
            mouse = e.getPoint();
            pathCursor = e.getPoint();
            repaint();
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getWheelRotation() == -1)
            scale(.1 * scale);
        else
            scale(-.1 * scale);
    }
}
