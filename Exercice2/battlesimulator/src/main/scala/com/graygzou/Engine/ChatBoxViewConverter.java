package com.graygzou.Engine;

import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.chatcontrol.ChatEntryModelClass;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.ImageRenderer;
import de.lessvoid.nifty.elements.render.TextRenderer;

/**
 * Handles the displaying of the items in the ChatBox.
 * @author Mark
 * @version 0.1
 */
public class ChatBoxViewConverter implements ListBox.ListBoxViewConverter<ChatEntryModelClass> {
    private static final String CHAT_LINE_ICON = "#icon";
    private static final String CHAT_LINE_TEXT = "#entity-name";

    /**
     * Default constructor.
     */
    public ChatBoxViewConverter() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void display(final Element listBoxItem, final ChatEntryModelClass item) {
        final Element text = listBoxItem.findElementById(CHAT_LINE_TEXT);
        final TextRenderer textRenderer = text.getRenderer(TextRenderer.class);
        final Element icon = listBoxItem.findElementById(CHAT_LINE_ICON);
        final ImageRenderer iconRenderer = icon.getRenderer(ImageRenderer.class);
        if (item != null) {
            textRenderer.setText(item.getLabel());
            iconRenderer.setImage(item.getIcon());
        } else {
            textRenderer.setText("");
            iconRenderer.setImage(null);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getWidth(final Element listBoxItem, final ChatEntryModelClass item) {
        final Element text = listBoxItem.findElementById(CHAT_LINE_TEXT);
        final TextRenderer textRenderer = text.getRenderer(TextRenderer.class);
        final Element icon = listBoxItem.findElementById(CHAT_LINE_ICON);
        final ImageRenderer iconRenderer = icon.getRenderer(ImageRenderer.class);
        return 50 + ((textRenderer.getFont() == null) ? 0 : textRenderer.getFont().getWidth(item.getLabel()))
                + ((item.getIcon() == null) ? 0 : item.getIcon().getWidth());
    }
}
