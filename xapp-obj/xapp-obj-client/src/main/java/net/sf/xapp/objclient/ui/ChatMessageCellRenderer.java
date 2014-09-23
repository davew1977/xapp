/*
 *
 * Date: 2010-dec-20
 * Author: davidw
 *
 */
package net.sf.xapp.objclient.ui;

import net.sf.xapp.net.api.chatclient.to.ChatBroadcast;
import net.sf.xapp.net.client.framework.ClientContext;
import net.sf.xapp.net.common.framework.Carousel;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.uifwk.XCellRenderer;
import net.sf.xapp.uifwk.XPane;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class ChatMessageCellRenderer extends XCellRenderer<ChatBroadcast>
{
    public static int LINE_GAP = 5;
    public static int WORD_GAP = 3;
    //public static final Pattern CARD_PATTERN = Pattern.compile("[\\dATJQK][hcds]");
    //public static final Pattern HAND_PATTERN = Pattern.compile("[\\dATJQK][\\dATJQK]s?");
    private final ClientContext clientContext;
    private final int TEXT_START_Y = 18;
    String nickname;
    private UserId userId;
    MarkUpArea markUpArea;
    private Map<String, Color> nickColors = new HashMap<String, Color>();

    private Carousel<Color> colorList = new Carousel<Color>(Arrays.asList(Helper.colors), Helper.colors[0]);
    private final int textMargin = 33;

    public ChatMessageCellRenderer(int width, JScrollPane containingScrollpane, ClientContext clientContext)
    {
        super(width, containingScrollpane);
        this.clientContext = clientContext;
        setDefaultAlpha(1.0f);
        setDefaultFont(Helper.defaultFont);
    }

    @Override
    protected void newData(ChatBroadcast data)
    {
        boolean comment = data.getSenderNickname().equals("_comment_");
        nickname = data.getSenderNickname();
        userId = data.getUserId();

        markUpArea = render(data.getMessage(), getVisibleWidth() - textMargin, getFontMetrics(defaultFont));
        setSize(getWidth(), Math.max(30, markUpArea.height() + TEXT_START_Y));
    }

    private int lineSpacing()
    {
        return (getFont().getSize() + 1);
    }

    @Override
    protected void paintPane(Graphics2D g)
    {
        setAlpha(g, 0.75f * getDefaultAlpha());
        g.setPaint(new GradientPaint(0, 0, Color.black, getVisibleWidth(), 0, getColor()));
        g.fillRect(0, 0, getVisibleWidth()-2, getHeight() - 1);
        XPane.setAlpha(g, getDefaultAlpha());
        if (userId != null) {
            g.drawImage(Helper.getProfileImage(userId), 2, 2, null);
        }
        g.setColor(Color.white);
        g.setFont(defaultFont.deriveFont(Font.BOLD));
        g.drawString(nickname, 33, 11);

        /*GradientPaint gp = new GradientPaint(0, 0, Color.gray, 0, 10, getColor());
        g.setPaint(gp);
        g.fillRoundRect(0, 0, getVisibleWidth(), getHeight() - 2, 15, 15);*/

        //split into words

        g.setFont(defaultFont.deriveFont(Font.PLAIN));
        g.setColor(Color.white);
        Graphics2D graphics = (Graphics2D) g.create(33, TEXT_START_Y, markUpArea.MAX_WIDTH, markUpArea.height());
        markUpArea.paint(graphics);
    }

    public MarkUpArea render(String text, int width, FontMetrics metrics)
    {
        MarkUpArea markUpArea = new MarkUpArea(width);
        String[] words = text.split("\\s");
        for (String word : words) {
            markUpArea.add(word);
        }
        return markUpArea;
    }

    public Color getColor()
    {
        Color color = nickColors.get(nickname);
        if (color == null)
        {
            color = colorList.next();
            nickColors.put(nickname, color);
        }
        return color;
    }

    private class MarkUpArea {
        final List<Line> lines = new ArrayList<Line>();
        final int MAX_WIDTH;
        int height;

        private MarkUpArea(int width) {
            this.MAX_WIDTH = width;
        }

        void add(String word) {
            Chunk chunk;
            /*if (CARD_PATTERN.matcher(word).matches()) {
                chunk = new CardPic(Card.decode(word));
            }
            else if (HAND_PATTERN.matcher(word).matches()) {
                Card c1 = Card.decode(word.charAt(0) + "s");
                Card c2 = Card.decode(word.charAt(1) + (word.length()==3 ? "s" : "h"));
                chunk = new HandPic(c1, c2);
            }
            else {*/
                chunk = new Text(word);
//            }
            add(chunk);
        }

        private void add(Chunk chunk) {
            Line line = currentLine();
            if (line.width + chunk.width() > MAX_WIDTH) {
                height+=line.maxHeight + LINE_GAP;
                newLine();
            }
            currentLine().add(chunk);
        }

        Line currentLine() {
            if (lines.isEmpty()) {
                newLine();
            }
            return lines.get(lines.size()-1);
        }

        void newLine() {
            lines.add(new Line());
        }

        public int height() {
            return height + currentLine().maxHeight + LINE_GAP;
        }

        public void paint(Graphics2D graphics2D) {
            int cursorY = 0;
            for (Line line : lines) {
                Graphics2D g = (Graphics2D) graphics2D.create(0, cursorY, line.width, line.maxHeight);
                line.paint(g);
                cursorY += line.maxHeight + LINE_GAP;
            }
        }
    }

    private class Line {
        List<Chunk> chunks = new ArrayList<Chunk>();
        int maxHeight;
        int width;
        public void add(Chunk chunk) {
            chunks.add(chunk);
            maxHeight = Math.max(maxHeight, chunk.height());
            width += chunk.width() + WORD_GAP;
        }

        public void paint(Graphics2D graphics2D) {
            int cursorX = 0;
            for (Chunk chunk : chunks) {
                Graphics2D g = (Graphics2D) graphics2D.create(cursorX, 0, chunk.width(), chunk.height());
                chunk.paint(g);
                cursorX += chunk.width() + WORD_GAP;
            }
        }
    }

    private abstract class Chunk {

        protected Chunk() {
        }

        public abstract void paint(Graphics2D g);

        public abstract int height();

        public abstract int width();
    }

    /*private class Pic extends Chunk {
        protected Pic() {
        }

        @Override
        public void paint(Graphics2D g) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }*/

    /*private class CardPic extends Chunk {
        private Image cardImage;

        private CardPic(Card card) {
            cardImage = Images.getInstance().smallCardImages.get(card);

        }

        @Override
        public void paint(Graphics2D g) {
            g.drawImage(cardImage, 0,0, null);
        }

        @Override
        public int height() {
            return cardImage.getHeight(null);
        }

        @Override
        public int width() {
            return cardImage.getWidth(null);
        }
    }
    private class HandPic extends Chunk {
        private Image c1;
        private Image c2;

        private HandPic(Card card1, Card card2) {
            c1 = Images.getInstance().smallCardImages.get(card1);
            c2 = Images.getInstance().smallCardImages.get(card2);

        }

        @Override
        public void paint(Graphics2D g) {
            g.drawImage(c1, 0,0, null);
            g.drawImage(c2, c1.getWidth(null) + 1, 0, null);
        }

        @Override
        public int height() {
            return c1.getHeight(null);
        }

        @Override
        public int width() {
            return c1.getWidth(null) * 2 + 1;
        }
    }
*/
    private class Text extends Chunk {
        private String str;

        private Text(String str) {
            super();
            this.str = str;
        }

        @Override
        public int height() {
            return defaultFont.getSize();
        }

        @Override
        public int width() {
            return getFontMetrics(defaultFont).stringWidth(str);
        }

        @Override
        public void paint(Graphics2D g) {
            g.drawString(str, 0, height() - getFontMetrics(defaultFont).getMaxDescent());
        }
    }
}
