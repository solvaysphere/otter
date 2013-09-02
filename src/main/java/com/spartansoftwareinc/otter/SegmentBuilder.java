package com.spartansoftwareinc.otter;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import static com.spartansoftwareinc.otter.Util.*;

class SegmentBuilder {
    private TU tu;
    private TUV tuv;
    
    TU getTu() {
        return tu;
    }

    void startTu(StartElement el) {
        tu = new TU();
    }
    
    static final QName XMLLANG = new QName("http://www.w3.org/XML/1998/namespace", "lang");
    
    void startTuv(StartElement el) {
        String locale = attrVal(el, XMLLANG);
        require(locale != null, el.getLocation(), "TUV has no xml:lang");
        tuv = new TUV(locale);
        // TODO: test for duplicates
        tu.addTUV(tuv);
    }
    
    void endTuv() {
    }
    
    void addSegmentText(String content) {
        tuv.addContent(new TextContent(content));
    }
    
    void addTUVContent(TUVContent content) {
        tuv.addContent(content);
    }
}