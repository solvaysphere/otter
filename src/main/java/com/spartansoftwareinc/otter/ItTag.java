package com.spartansoftwareinc.otter;

/**
 * A <code>&lt;it&gt;</code> tag.
 */
public class ItTag extends TypedTag {
    public ItTag(int x, String data) {
        super(x, data);
    }

    public ItTag(String data) {
        super(data);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) &&
                (o instanceof ItTag);
    }
}
