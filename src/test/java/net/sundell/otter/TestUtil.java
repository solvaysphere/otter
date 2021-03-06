package net.sundell.otter;

import static net.sundell.otter.TMXEventType.TU;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import net.sundell.otter.Note;
import net.sundell.otter.Property;
import net.sundell.otter.TMXEvent;
import net.sundell.otter.TMXEventType;
import net.sundell.otter.TMXReader;
import net.sundell.otter.TU;
import net.sundell.otter.TUEvent;

import static org.junit.Assert.assertTrue;

public class TestUtil {
    public static void checkProperty(Property p, String propertyType, String value,
                                     String encoding, String xmlLang) {
        assertNotNull(p);
        assertEquals(propertyType, p.getType());
        assertEquals(value, p.getValue());
        assertEquals(encoding, p.getEncoding());
        assertEquals(xmlLang, p.getLang());
    }
    public static void checkNote(Note n, String note) {
        assertNotNull(n);
        assertEquals(note, n.getContent());
    }
    public static void checkEvent(TMXEvent e, TMXEventType type) {
        assertEquals(type, e.getEventType());
    }
    
    public static List<TU> readTUs(TMXReader r) {
        List<TMXEvent> events = readEvents(r);
        List<TU> tus = new ArrayList<TU>();
        for (TMXEvent e : events) {
            if (e.getEventType() == TU) {
                assertNotNull(e.getTU());
                tus.add(e.getTU());
            }
        }
        return tus;
    }
    
    public static List<TUEvent> readTUEvents(TMXReader r) {
        List<TMXEvent> events = readEvents(r);
        List<TUEvent> tus = new ArrayList<TUEvent>();
        for (TMXEvent e : events) {
            if (e.getEventType() == TU) {
                assertTrue(e instanceof TUEvent);
                tus.add((TUEvent)e);
            }
        }
        return tus;
    }
    
    public static List<TMXEvent> readEvents(TMXReader r) {
        List<TMXEvent> events = new ArrayList<TMXEvent>();
        while (r.hasNext()) {
            events.add(r.nextEvent());
        }
        return events;
    }
    
    public static TMXReader getTMXReader(String testResource) throws Exception {
        InputStream is = TestUtil.class.getResourceAsStream(testResource);
        return TMXReader.createTMXReader(
                            new InputStreamReader(is, "UTF-8"));
    }

}
