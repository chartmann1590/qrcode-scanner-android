package com.charles.qrcode;

import org.junit.Test;
import static org.junit.Assert.*;

public class ScanItemTest {

    @Test
    public void testScanItemConstructorAndGetters() {
        int id = 42;
        String content = "https://example.com";
        String format = "QR_CODE";
        long timestamp = 1717320000000L;

        ScanItem item = new ScanItem(id, content, format, timestamp);

        assertEquals(id, item.getId());
        assertEquals(content, item.getContent());
        assertEquals(format, item.getFormat());
        assertEquals(timestamp, item.getTimestamp());
    }

    @Test
    public void testScanItemWithEmptyContent() {
        ScanItem item = new ScanItem(1, "", "BARCODE", 0L);
        assertEquals("", item.getContent());
    }

    @Test
    public void testScanItemWithNullContent() {
        ScanItem item = new ScanItem(2, null, null, -1L);
        assertNull(item.getContent());
        assertNull(item.getFormat());
    }
}
