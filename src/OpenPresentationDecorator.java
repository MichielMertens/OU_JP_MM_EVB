import javax.swing.JOptionPane;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;
import java.io.IOException;

public class OpenPresentationDecorator extends SlideItemDecorator {
	Presentation presentation;
	String file;

	public OpenPresentationDecorator(Presentation presentation,
			SlideItem slideItem, String file) {
		super(slideItem);
		this.presentation = presentation;
		this.file = file;
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
		presentation.clear();
		Accessor xmlAccessor = new XMLAccessor();
		try {
			xmlAccessor.loadFile(presentation, file);
			presentation.setSlideNumber(0);
		} catch (IOException exc) {
			JOptionPane.showMessageDialog(null, "IOException: " + exc,
					"Load Error", JOptionPane.ERROR_MESSAGE);
		}
		System.out.println("OpenPresentation.execute()");
		decoratedSlideItem.execute();
	}
}
