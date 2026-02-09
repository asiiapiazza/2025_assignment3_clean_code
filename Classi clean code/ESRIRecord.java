package com.bbn.openmap.layer.shape;

import com.bbn.openmap.dataAccess.shape.ShapeUtils;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGeometry;
import com.bbn.openmap.omGraphics.OMGeometryList;
import com.bbn.openmap.omGraphics.OMGraphicList;

/**
 * Classe base per tutte le classi di record di forma. Memorizza il numero di
 * record e la lunghezza del contenuto di un record.
 */
public abstract class ESRIRecord extends ShapeUtils {

    private static final int RECORD_HEADER_SIZE_BYTES = 8;
    private static final int CONTENT_LENGTH_OFFSET_BYTES = 4;
    private static final int WORD_SIZE_BYTES = 2;
    private int recordNumber;
    private int contentLength;

    public ESRIRecord() {
        this.recordNumber = 0;
        this.contentLength = 0;
    }

    public ESRIRecord(byte[] buffer, int offset) {
        this.recordNumber = readBEInt(buffer, offset);
        this.contentLength = readBEInt(buffer, offset + CONTENT_LENGTH_OFFSET_BYTES);
    }

    public abstract void addOMGraphics(OMGraphicList list, DrawingAttributes drawingAttributes);

    public abstract OMGeometry addOMGeometry(OMGeometryList list);

    public abstract ESRIBoundingBox getBoundingBox();

    public abstract int getShapeType();

    public abstract int getRecordLength();

    public int getBinaryStoreSize() {
        return getRecordLength() + RECORD_HEADER_SIZE_BYTES;
    }

    public int write(byte[] buffer, int offset) {
        int bytesWritten = writeBEInt(buffer, offset, recordNumber);

        int contentLengthInWords = getRecordLength() / WORD_SIZE_BYTES;

        bytesWritten += writeBEInt(buffer, offset + bytesWritten, contentLengthInWords);
        return bytesWritten;
    }

    public int getRecordNumber() {
        return recordNumber;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setRecordNumber(int recordNumber) {
        this.recordNumber = recordNumber;
    }
}