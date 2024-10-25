package buoy.widget;

import buoy.event.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.html.*;

/**
 * A BDocumentViewer is used for displaying formatted text documents. The
 * supported document types include HTML, Rich Text Format (RTF), and plain
 * text. This class is most often used for displaying help screens or
 * documentation within a program.
 * <p>
 * When the user clicks on a hyperlink inside an HTML document, the
 * BDocumentViewer generates a
 * {@link buoy.event.DocumentLinkEvent DocumentLinkEvent}. You can then pass
 * that event to
 * {@link buoy.widget.BDocumentViewer#processLinkEvent processLinkEvent()},
 * which will load the new document pointed to by the hyperlink. This can be
 * done most easily by having the BDocumentViewer listen for its own events
 * directly:
 * <p>
 * <
 * pre>
 * viewer.addEventLink(DocumentLinkEvent.class, viewer, "processLinkEvent");
 * </pre>
 * <p>
 * Alternatively, you can have another object listen for the events and then
 * pass them on to the BDocumentViewer. This would be useful, for example, if
 * you wanted to filter the events and only allow certain hyperlinks to be
 * followed.
 * <p>
 * When you tell the BDocumentViewer to load a new document, either by calling
 * {@link buoy.widget.BDocumentViewer#setDocument setDocument()} or
 * {@link buoy.widget.BDocumentViewer#processLinkEvent processLinkEvent()}, the
 * loading is usually done asynchronously. When loading is complete, it
 * generates a {@link buoy.event.ValueChangedEvent ValueChangedEvent}. If you
 * want to show a progress bar while the document is being loaded, for example,
 * you can listen for the event to know when to stop animating the progress bar.
 * <p>
 * In addition to the event types generated by all Widgets, BDocumentViewers
 * generate the following event types:
 * <ul>
 * <li>{@link buoy.event.DocumentLinkEvent DocumentLinkEvent}</li>
 * <li>{@link buoy.event.ValueChangedEvent ValueChangedEvent}</li>
 * </ul>
 *
 * @author Peter Eastman
 */
public class BDocumentViewer extends Widget<JEditorPane> {

    /**
     * Create an empty BDocumentViewer.
     */

    public BDocumentViewer() {
        component = createComponent();
        JEditorPane ep = getComponent();
        ep.setEditable(false);
        ep.addPropertyChangeListener("page", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent ev) {
                if (getComponent().isDisplayable()) {
                    updateScrollPane();
                }
                dispatchEvent(new ValueChangedEvent(BDocumentViewer.this));
            }
        });
        ep.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent ev) {
                if (getComponent().isDisplayable()) {
                    updateScrollPane();
                }
            }
        });
        ep.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    dispatchEvent(new DocumentLinkEvent(BDocumentViewer.this, e));
                }
            }
        });
    }

    /**
     * Create a new BDocumentViewer displaying the document referenced by a URL.
     * Depending on the location and type of document, it may be loaded either
     * synchronously or asynchronously. For this reason, no assumptions should
     * be made about whether the document has been loaded when this method
     * returns. This method may throw an IOException if an error occurs while
     * loading the document, but the lack of an exception cannot be taken to
     * mean that the document was loaded successfully (in the case of
     * asynchronous loading).
     *
     * @param document a URL pointing to the document to display
     */
    public BDocumentViewer(URL document) throws IOException {
        this();
        setDocument(document);
    }

    /**
     * Create the JEditorPane which serves as this Widget's Component. This
     * method is protected so that subclasses can override it.
     */
    protected JEditorPane createComponent() {
        return new JEditorPane();
    }

    /**
     * Get the URL for the document currently being displayed. If the document
     * was not specified by a URL, this returns null.
     */
    public URL getDocument() {
        return getComponent().getPage();
    }

    /**
     * Set the document to display in this BDocumentViewer. Depending on the
     * location and type of document, it may be loaded either synchronously or
     * asynchronously. For this reason, no assumptions should be made about
     * whether the document has been loaded when this method returns. This
     * method may throw an IOException if an error occurs while loading the
     * document, but the lack of an exception cannot be taken to mean that the
     * document was loaded successfully (in the case of asynchronous loading).
     *
     * @param document a URL pointing to the document to display
     */
    public void setDocument(URL document) throws IOException {
        getComponent().setPage(document);
    }

    /**
     * Set the document to display in this BDocumentViewer. So that the viewer
     * can know how to interpret the document contents, you must specify its
     * MIME type. Currently supported types include "text/plain", "text/html",
     * and "text/rtf".
     *
     * @param text the text of the document to display
     * @param type the MIME type of the document
     */
    public void setDocument(String text, String type) {
        getComponent().setContentType(type);
        getComponent().setText(text);
    }

    /**
     * Get the MIME type of the document currently being displayed.
     */
    public String getContentType() {
        return component.getContentType();
    }

    /**
     * Process a DocumentLinkEvent generated by this viewer, and handle it
     * appropriately. Depending on the event, this causes either the entire
     * document, or the contents of one frame, to be replaced with the link
     * target.
     */
    public void processLinkEvent(DocumentLinkEvent event) throws IOException {
        if (event.getEvent() instanceof HTMLFrameHyperlinkEvent) {
            HTMLFrameHyperlinkEvent ev = (HTMLFrameHyperlinkEvent) event.getEvent();
            ((HTMLDocument) getComponent().getDocument()).processHTMLFrameHyperlinkEvent(ev);
        } else {
            setDocument(event.getURL());
        }
    }

    /**
     * This method is called whenever the content of the Widget changes. If this
     * widget is contained inside a BScrollPane, we need to update its layout.
     */
    protected void updateScrollPane() {
        invalidateSize();
        if (getParent() instanceof BScrollPane) {
            getParent().layoutChildren();
        }
    }
}
