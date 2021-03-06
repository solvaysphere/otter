package net.sundell.otter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Base class for all inline tags in TUV content that can contain 
 * native code data. 
 * <br>
 * The {@link #addContent(TUVContent)} method for InlineTags will throw
 * an exception if it is passed anything other than {@link CodeContent} 
 * or {@link Subflow} objects.
 */
public abstract class InlineTag implements TagContent {
    static final int NO_VALUE = 0;
    private List<TUVContent> contents = new ArrayList<TUVContent>();
    
    protected InlineTag() {
    }
    protected InlineTag(String initialCodeContent) {
        contents.add(new CodeContent(initialCodeContent));
    }
    protected InlineTag(ComplexContent content) {
        contents.addAll(content.getContent());
    }

    /**
     * Returns true if this tag has non-null values for all
     * required attributes.
     * @return true if all required attributes have values
     */
    boolean hasRequiredAttributes() {
        return true;
    }
    
    /**
     * Add an item to the contents of this tag.  InlineTag objects
     * are restricted to {@link CodeContent} and {@link Subflow} content
     * items.
     * 
     * @param content content item to add to this tag
     * @throws IllegalArgumentException if an invalid content item is added 
     */
    @Override
    public InlineTag addContent(TUVContent content) {
        if (!(content instanceof CodeContent) && !(content instanceof Subflow)) {
            throw new IllegalArgumentException("Illegal paired tag content: " + content);
        }
        contents.add(content);
        return this;
    }

    /**
     * Add multiple items to the contents of this tag.  TUV objects
     * are restricted to {@link CodeContent} and {@link Subflow} content
     * items.
     * 
     * @param contents
     */
    public InlineTag addContents(List<TUVContent> contents) {
        for (TUVContent content : contents) {
            addContent(content);
        }
        return this;
    }

    /**
     * Get the contents of this object.
     * @return list of {@link TUVContent} items
     */
    public List<TUVContent> getContents() {
        return contents;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(contents);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || !(o instanceof InlineTag)) return false;
        return contents.equals(((InlineTag)o).contents);
    }
}
