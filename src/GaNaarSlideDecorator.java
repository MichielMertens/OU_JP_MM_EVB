import java.awt.*;
import java.awt.image.ImageObserver;

public class GaNaarSlideDecorator extends SlideItemDecorator {
	private Presentation presentation;
	private String action;
	private String slide;

	public GaNaarSlideDecorator(Presentation presentation, SlideItem slideItem,
			String action, String slide) {
		super(slideItem);
		this.presentation = presentation;
		this.action = action;
		this.slide = slide;
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
		int number = 0;
		try {
			number = Integer.parseInt(slide);
		} catch (NumberFormatException x) {
		}

		if ("next".equals(action)) {
			presentation.nextSlide();
		} else {
			if ("previous".equals(action)) {
				presentation.prevSlide();
			} else {
				if ("first".equals(action)) {
					presentation.setSlideNumber(0);
				} else {
					if ("last".equals(action)) {
						presentation.setSlideNumber(presentation.getSize() - 1);
					} else {
						if ("goto".equals(action)) {
							presentation.setSlideNumber(number);
						}
					}
				}
			}
		}
		System.out.println("GaNaarSlideDecorator.execute() action: " + action
				+ " slide: " + number);
		decoratedSlideItem.execute();
	}
}
