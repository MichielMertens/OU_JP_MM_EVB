import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.awt.Graphics;
import java.awt.Rectangle;

public class BeepDecorator extends SlideItemDecorator {

	public BeepDecorator(SlideItem slideItem) {
		super(slideItem);
	}

	// TODO: moet naar view
	@Override
	public Rectangle getBoundingBox(Graphics g, ImageObserver observer,
			float scale, Style style) {
		return super.getBoundingBox(g, observer, scale, style);
	}

	// TODO: moet naar view
	@Override
	public void draw(int x, int y, float scale, Graphics g, Style style,
			ImageObserver observer) {
		super.draw(x, y, scale, g, style, observer);
	}

	// TODO: String gebruikt om eea te testen.
	@Override
	public void execute() {
		Toolkit.getDefaultToolkit().beep();
		System.out.println("BeepDecorator.execute()");
		decoratedSlideItem.execute();
	}
}
