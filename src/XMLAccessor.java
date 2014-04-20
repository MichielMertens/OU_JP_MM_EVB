import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

/**
 * Een accessor die XML bestanden kan lezen en schrijven
 * 
 * @version $Id: XMLAccessor.java,v 1.2 2004/08/17 Sylvia Stuurman
 */

public class XMLAccessor extends Accessor {

	public void loadFile(Presentation presentation, String filename)
			throws IOException {
		try {
			SAXBuilder builder = new SAXBuilder(true); // true -> validate
			Document document = builder.build(new File(filename)); // maak een
																	// JDOM
																	// document
			Element element = document.getRootElement();
			String title = element.getChild("head").getChild("title").getText();
			presentation.setTitle(title);
			List slides = element.getChildren("slide");
			for (int slideNumber = 0; slideNumber < slides.size(); slideNumber++) {
				Element xmlSlide = (Element) slides.get(slideNumber);
				Slide slide = new Slide();
				slide.setTitle(xmlSlide.getChild("title").getText());
				presentation.append(slide);
				Element items = xmlSlide.getChild("items");
				List slideItems = items.getChildren();
				for (int itemNumber = 0; itemNumber < slideItems.size(); itemNumber++) {
					Element item = (Element) slideItems.get(itemNumber);
					// Aanpassing voor de nieuwe methode
					// loadSlideItem(slide, item);
					loadSlideItem(presentation, slide, item);
				}
			}
		} catch (JDOMException jdx) {
			System.err.println(jdx.toString());
			throw new IOException("Parse Exception");
		}
	}

	// Oude methode voor het laden van een SlideItem
	protected void loadSlideItem(Slide slide, Element item) {
		String type = item.getName();
		int level = 1; // default
		String leveltext = item.getAttributeValue("level");
		if (leveltext != null) {
			try {
				level = Integer.parseInt(leveltext);
			} catch (NumberFormatException x) {
			}
		}
		if ("text".equals(type)) {
			slide.append(new TextItem(level, item.getText()));
		} else {
			if ("image".equals(type)) {
				slide.append(new BitmapItem(level, item.getText()));
			} else {
				System.err.println("Unknown element type");
			}
		}
	}

	// Nieuwe methode voor het laden van een SlideItem.
	// TODO: Opzetten factory voor de werking van SlideItem's.
	protected void loadSlideItem(Presentation presentation, Slide slide,
			Element item) {
		String type = item.getName(); // het type wordt opgevraagd: text, image
										// of action
		Stack<Element> actions = new Stack<Element>(); // voor de action(s)
														// wordt er een stack
														// (LIFO) opgezet
		Element element = item; // item wordt opgeslagen in een methode
								// variabele voor verdere verwerking

		// Als het element een action betreft, dan moet de SlideItemDecorator
		// aangesproken worden.
		// Omdat een action kan bestaan uit meerdere actions, waarbij de laatste
		// action als eerste in de decorator
		// gebrukt moet worden, worden de actions een voor een in een stack
		// gestopt (LIFO).
		// Aan het eind van deze if-statement blijft er een element over die een
		// text- of een bitmapitem kan zijn.
		if ("action".equals(type)) {
			while (!element.getChildren().isEmpty()) {
				actions.push(element);
				element = (Element) element.getChildren().get(0);
			}
		}

		// Het type wordt opnieuw bepaald, omdat we na de vorige if-statement
		// (action) geen type weten van het basis element.
		type = element.getName();
		int level = 1; // default
		String leveltext = element.getAttributeValue("level");
		if (leveltext != null) {
			try {
				level = Integer.parseInt(leveltext);
			} catch (NumberFormatException x) {
			}
		}

		// Aan de hand van het type wordt nu de juiste SlideItem geinitieerd.
		// Maar deze wordt nog niet aan de Slide toegevoegd, omdat voor deze
		// SlideItem nog action(s) kunnen zijn.
		SlideItem slideItem;
		if ("text".equals(type)) {
			slideItem = new TextItem(level, element.getText());
		} else {
			if ("image".equals(type)) {
				slideItem = new BitmapItem(level, element.getText());
			} else {
				System.err.println("Unknown element type");
				return;
			}
		}

		// Als de Stack gevuld is, dan betekent dit dat er action(s) aan de
		// SlideItem moeten worden toegevoegd.
		// Het gebruikte patroon is het Decorator-patroon, deze zorgt voor de
		// toevoeging van extra functionaliteit
		// aan de SlideItem. De momenteel voorkomende actions zijn:
		// - next
		// - previous
		// - first
		// - last
		// - goto
		// - beep
		// - open presentation and first
		if (!actions.isEmpty()) {
			while (!actions.isEmpty()) {
				Element action = actions.pop(); // haal de laatste toegevoegde
												// action uit de Stack en
												// verwijder deze
				String name = action.getAttributeValue("name");
				if ("beep".equals(name)) {
					slideItem = new BeepDecorator(slideItem);
				} else {
					if ("open".equals(name)) {
						// TODO: Frame bij OpenPresentationDecorator -> Factory?
						// De laatste variabele is de tekst van het bestand
						slideItem = new OpenPresentationDecorator(presentation,
								slideItem, action.getAttributeValue("value"));
					} else {
						if ("next".equals(name) || "previous".equals(name)
								|| "first".equals(name) || "last".equals(name)
								|| "goto".equals(name)) {
							// De laatste variabele is het slidenummer
							// waarnaartoe gesprongen moet worden.
							slideItem = new GaNaarSlideDecorator(presentation,
									slideItem, name,
									action.getAttributeValue("value"));
						} else {
							System.err.println("Unknown action type");
						}
					}
				}
			}
		}

		// Dit is een test om te kijken welke action(s) er gekoppeld zijn aan
		// een SlideItem.
		slideItem.execute();

		// Als de eventuele action(s) gekoppeld zijn, pas dan wordt de SlideItem
		// toegevoegd aan de Slide.
		slide.append(slideItem);
	}

	public void saveFile(Presentation presentation, String filename)
			throws IOException {
		PrintWriter out = new PrintWriter(new FileWriter(filename));
		out.println("<?xml version=\"1.0\"?>");
		out.println("<!DOCTYPE slideshow SYSTEM \"jabberpoint.dtd\">");
		out.println("<presentation>");
		out.print("<head><title>");
		out.print(presentation.getTitle());
		out.println("</title></head>");
		for (int slideNumber = 0; slideNumber < presentation.getSize(); slideNumber++) {
			Slide slide = presentation.getSlide(slideNumber);
			out.println("<slide>");
			out.println("<title>" + slide.getTitle() + "</title>");
			out.println("<items>");
			Vector slideItems = slide.getSlideItems();
			for (int itemNumber = 0; itemNumber < slideItems.size(); itemNumber++) {
				SlideItem slideItem = (SlideItem) slideItems
						.elementAt(itemNumber);
				if (slideItem instanceof TextItem) {
					out.print("<text level=\"" + slideItem.getLevel() + "\">");
					out.print(((TextItem) slideItem).getText());
					out.println("</text>");
				} else {
					if (slideItem instanceof BitmapItem) {
						out.print("<image level=\"" + slideItem.getLevel()
								+ "\">");
						out.print(((BitmapItem) slideItem).getName());
						out.println("</image>");
					} else {
						System.out.println("Ignoring " + slideItem);
					}
				}
			}
			out.println("</items>");
			out.println("</slide>");
		}
		out.println("</slideshow>");
		out.close();
	}
}
