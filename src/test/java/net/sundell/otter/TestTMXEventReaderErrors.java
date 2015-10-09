package net.sundell.otter;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.junit.Test;

import net.sundell.otter.ErrorHandler;
import net.sundell.otter.OtterException;
import net.sundell.otter.OtterInputException;
import net.sundell.otter.TMXReader;
import net.sundell.otter.TU;
import net.sundell.otter.TUEvent;
import net.sundell.otter.TUV;

import static net.sundell.otter.TestUtil.readEvents;
import static net.sundell.otter.TestUtil.readTUEvents;
import static org.junit.Assert.*;

public class TestTMXEventReaderErrors {

    @Test
    public void testXMLError() throws Exception {
        TMXReader reader = TMXReader.createTMXEventReader(
                            new StringReader("asldjalksdjalsdj"));
        TestErrorHandler handler = new TestErrorHandler();
        reader.setErrorHandler(handler);
        readWithErrors(reader);
        assertNotNull(handler.xmlError);
    }
    
    // XXX It would be nice to have checkable error typing
    // and an easier way to verify locations
    
    @Test
    public void testDateFormatError() throws Exception {
        expectNonfatalError("/error_date_format.tmx", 1);
    }

    @Test
    public void testUnsupportedVersionError() throws Exception {
        expectNonfatalError("/error_tmx_version.tmx", 1);
    }

    @Test
    public void testMissingVersionError() throws Exception {
        expectFatalError("/error_missing_tmx_version.tmx");
    }

    @Test
    public void testMultipleHeadersError() throws Exception {
        expectFatalError("/error_multiple_headers.tmx");
    }

    @Test
    public void testMultipleBodyError() throws Exception {
        expectFatalError("/error_multiple_body.tmx");
    }

    @Test
    public void testMissingBptIAttrError() throws Exception {
        TMXReader reader = TestUtil.getTMXReader("/error_missing_bpt_i.tmx");
        TestErrorHandler handler = new TestErrorHandler();
        reader.setErrorHandler(handler);
        readWithErrors(reader);
        // This reports multiple errors:
        // - [0] is the missing bpt/@i
        // - [1] is because the subsequent ept now how an @i that doesn't match 
        assertEquals(2, handler.tuErrors.size());
        assertEquals(0, handler.tuErrors.get(0).sequence);
        assertEquals(0, handler.tuErrors.get(1).sequence);
        assertEquals(0, handler.errors.size());
        assertNull(handler.fatalError);
        assertNull(handler.xmlError);
    }

    @Test
    public void testDuplicateBptIAttrError() throws Exception {
        TMXReader reader = TestUtil.getTMXReader("/error_duplicate_bpt_i.tmx");
        TestErrorHandler handler = new TestErrorHandler();
        reader.setErrorHandler(handler);
        readWithErrors(reader);
        // This reports multiple errors
        // - [0] is because the second bpt the same ID as the first
        // - [1] is because the second ept doesn't find a bpt with a matching ID 
        //       (because its matching bpt was rejected)
        assertEquals(2, handler.tuErrors.size());
        assertEquals(0, handler.tuErrors.get(0).sequence);
        assertEquals(0, handler.tuErrors.get(1).sequence);
        assertEquals(0, handler.errors.size());
        assertNull(handler.fatalError);
        assertNull(handler.xmlError);
    }
    
    @Test
    public void testMissingEptIAttrError() throws Exception {
        TMXReader reader = TestUtil.getTMXReader("/error_missing_ept_i.tmx");
        TestErrorHandler handler = new TestErrorHandler();
        reader.setErrorHandler(handler);
        readWithErrors(reader);
        // This reports multiple errors:
        // - [0] is because the preceding bpt now how an @i that doesn't match 
        // - [1] is the missing ept/@i
        assertEquals(2, handler.tuErrors.size());
        assertEquals(0, handler.tuErrors.get(0).sequence);
        assertEquals(0, handler.errors.size());
        assertNull(handler.fatalError);
        assertNull(handler.xmlError);
    }
    
    @Test
    public void testMissingItAttrError() throws Exception {
        TMXReader reader = TestUtil.getTMXReader("/error_it_tag_missing_pos.tmx");
        TestErrorHandler handler = new TestErrorHandler();
        reader.setErrorHandler(handler);
        readWithErrors(reader);
        assertEquals(1, handler.tuErrors.size());
        assertEquals(0, handler.tuErrors.get(0).sequence);
        assertEquals(0, handler.errors.size());
        assertNull(handler.fatalError);
        assertNull(handler.xmlError);
    }
    
    // TODO test subflow and hi cases 
    // Also, need to test multiple <bpt> with the same @i, etc
    @Test
    public void testOutOfOrderPairedTagsError() throws Exception {
        TMXReader reader = TestUtil.getTMXReader("/error_out_of_order_pair.tmx");
        TestErrorHandler handler = new TestErrorHandler();
        reader.setErrorHandler(handler);
        readWithErrors(reader);
        assertEquals(1, handler.tuErrors.size());
        assertEquals(0, handler.tuErrors.get(0).sequence);
        assertEquals(0, handler.errors.size());
        assertNull(handler.fatalError);
        assertNull(handler.xmlError);
    }

    // Make sure that when we report a TU error, we don't also produce
    // that TU as an event.
    @Test
    public void testTuErrorMeansTuIsSkipped() throws Exception {
        // This file contained only one TU, so when it is reported as an
        // error, there are no TUs produced as events.
        TMXReader reader = TestUtil.getTMXReader("/error_out_of_order_pair.tmx");
        List<TUEvent> tus = readTUEvents(reader);
        assertEquals(0, tus.size());
        // This file contains two TUs, so when the first (sequence 0) has an
        // error, the second (sequence 1) is still exposed as an event.
        reader = TestUtil.getTMXReader("/error_it_tag_missing_pos.tmx");
        tus = readTUEvents(reader);
        assertEquals(1, tus.size());
        assertEquals(1, tus.get(0).getSequence());
    }
    
    // TUs that don't contain a TUV that matches the srclang value in the
    // the TMX header should trigger TU errors.  However, if the TU contains
    // its own srcLang attribute, it's ok.
    @Test
    public void testSrcLangMismatchInTu() throws Exception {
    	TMXReader reader = TestUtil.getTMXReader("/error_srclang_mismatch.tmx");
    	TestErrorHandler handler = new TestErrorHandler();
    	reader.setErrorHandler(handler);
    	List<TUEvent> tus = readTUEvents(reader);
    	// Second TU shouldn't be counted
    	assertEquals(2, tus.size());
    	// Verify the first (correct) TU
    	TU tu = tus.get(0).getTU();
    	assertNotNull(tu);
    	// Inherited source locale value
    	assertEquals("EN-US", tu.getSrcLang());
    	TUV tuv = tu.getTuvs().get(tu.getSrcLang());
    	assertNotNull(tuv);
    	assertEquals("Hello world!", tuv.getContents().get(0).toString());
    	// Verify the third TU is also present, picking up the local override
    	tu = tus.get(1).getTU();
    	assertNotNull(tu);
    	assertEquals("DE-DE", tu.getSrcLang());
    	tuv = tu.getTuvs().get(tu.getSrcLang());
    	assertNotNull(tuv);
    	assertEquals("This matches the locally-defined source locale.", tuv.getContents().get(0).toString());
    	// Make sure the error for the second TU happened
    	assertEquals(1, handler.tuErrors.size());
    	assertEquals(1, handler.tuErrors.get(0).sequence);
    }
    
    void expectNonfatalError(String resource, int errorCount) throws Exception {
        TMXReader reader = TestUtil.getTMXReader(resource);
        TestErrorHandler handler = new TestErrorHandler();
        reader.setErrorHandler(handler);
        readWithErrors(reader);
        assertEquals(errorCount, handler.errors.size());
        assertNull(handler.fatalError);
        assertNull(handler.xmlError);
    }
    
    void expectFatalError(String resource) throws Exception {
        TMXReader reader = TestUtil.getTMXReader(resource);
        TestErrorHandler handler = new TestErrorHandler();
        reader.setErrorHandler(handler);
        readWithErrors(reader);
        assertEquals(0, handler.errors.size());
        assertNotNull(handler.fatalError);
        assertNull(handler.xmlError);
    }
    
    void readWithErrors(TMXReader reader) {
        try {
            readEvents(reader);
        }
        catch (OtterException e) {
            // Make sure all thrown exceptions originated from 
            // our error framework
            assertEquals("Halting on error", e.getMessage());
        }
    }
    
    class TestErrorHandler implements ErrorHandler {
        class TUError {
            int sequence;
            OtterException e;
        }
        List<TUError> tuErrors = new ArrayList<TUError>();
        List<OtterException> errors = new ArrayList<OtterException>();
        OtterException fatalError = null;
        XMLStreamException xmlError = null;
        
        @Override
        public void tuError(int tuSequence, OtterInputException e) {
            TUError error = new TUError();
            error.sequence = tuSequence;
            error.e = e;
            tuErrors.add(error);
        }
        
        @Override
        public void error(OtterInputException e) {
            errors.add(e);
        }

        @Override
        public void fatalError(OtterInputException e) {
            fatalError = e;
            throw new OtterInputException("Halting on error", e.getLocation());
        }

        @Override
        public void xmlError(XMLStreamException e) {
            xmlError = e;
            throw new OtterInputException("Halting on error", e.getLocation());
        }

       
    }
}
