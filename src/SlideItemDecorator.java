import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.text.AttributedString;
import java.util.*;
import java.util.List;

/**
 * Created by localadmin on 18-04-14.
 */
public abstract class SlideItemDecorator extends SlideItem {
	protected SlideItem decoratedSlideItem;
	// TODO: moet naar view, maar voor het testen is deze string gevuld.
	protected String text = "hallo";

	public SlideItemDecorator(SlideItem slideItem) {
		super(slideItem.getLevel());
		decoratedSlideItem = slideItem;
	}

	// TODO: moet naar view geldt nu alleen voor een textitem voorbeeld.
	// teken het item
	public void draw(int x, int y, float scale, Graphics g, Style myStyle,
			ImageObserver o) {
		if (text == null || text.length() == 0) {
			return;
		}
		java.util.List layouts = getLayouts(g, myStyle, scale);
		Point pen = new Point(x + (int) (myStyle.indent * scale), y
				+ (int) (myStyle.leading * scale));
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(myStyle.color);
		Iterator it = layouts.iterator();
		while (it.hasNext()) {
			TextLayout layout = (TextLayout) it.next();
			pen.y += layout.getAscent();
			layout.draw(g2d, pen.x, pen.y);
			pen.y += layout.getDescent();
		}
	}

	private List getLayouts(Graphics g, Style s, float scale) {
		List<TextLayout> layouts = new ArrayList<TextLayout>();
		AttributedString attrStr = getAttributedString(s, scale);
		Graphics2D g2d = (Graphics2D) g;
		FontRenderContext frc = g2d.getFontRenderContext();
		LineBreakMeasurer measurer = new LineBreakMeasurer(
				attrStr.getIterator(), frc);
		float wrappingWidth = (Slide.referenceWidth - s.indent) * scale;
		while (measurer.getPosition() < getText().length()) {
			TextLayout layout = measurer.nextLayout(wrappingWidth);
			layouts.add(layout);
		}
		return layouts;
	}

	// geef de AttributedString voor het item
	public AttributedString getAttributedString(Style style, float scale) {
		AttributedString attrStr = new AttributedString(getText());
		attrStr.addAttribute(TextAttribute.FONT, style.getFont(scale), 0,
				text.length());
		return attrStr;
	}

	// Geef de tekst
	public String getText() {
		return text == null ? "" : text;
	}

	// geef de bounding box van het item
	public Rectangle getBoundingBox(Graphics g, ImageObserver observer,
			float scale, Style myStyle) {
		List layouts = getLayouts(g, myStyle, scale);
		int xsize = 0, ysize = (int) (myStyle.leading * scale);
		Iterator iterator = layouts.iterator();
		while (iterator.hasNext()) {
			TextLayout layout = (TextLayout) iterator.next();
			Rectangle2D bounds = layout.getBounds();
			if (bounds.getWidth() > xsize) {
				xsize = (int) bounds.getWidth();
			}
			if (bounds.getHeight() > 0) {
				ysize += bounds.getHeight();
			}
			ysize += layout.getLeading() + layout.getDescent();
		}
		return new Rectangle((int) (myStyle.indent * scale), 0, xsize, ysize);
	}
}
