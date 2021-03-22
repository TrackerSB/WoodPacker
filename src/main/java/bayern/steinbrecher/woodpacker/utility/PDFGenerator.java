package bayern.steinbrecher.woodpacker.utility;

import bayern.steinbrecher.woodpacker.BuildConfig;
import bayern.steinbrecher.woodpacker.WoodPacker;
import bayern.steinbrecher.woodpacker.data.RequiredPlank;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfDocumentInfo;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.List;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.AreaBreakType;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public final class PDFGenerator {

    private PDFGenerator() {
        throw new UnsupportedOperationException("The construction of instances is prohibited");
    }

    /**
     * @param tableContent Row major content where the first entry represents the headings.
     */
    @SuppressWarnings("unused")
    private static Table generateTable(final Iterable<Iterable<String>> tableContent) {
        final Iterator<Iterable<String>> tableRowIterator = tableContent.iterator();
        if (tableRowIterator.hasNext()) {
            throw new IllegalArgumentException(
                    "The table content has to have at least a single row. This row contains the table headings");
        }

        final Table table = new Table(2);
        final Iterable<String> headerRow = tableRowIterator.next();
        for (final String headerCellContent : headerRow) {
            table.addHeaderCell(headerCellContent);
        }
        tableRowIterator.forEachRemaining(row -> {
            for (final String cellContent : row) {
                table.addCell(cellContent);
            }
            table.startNewRow();
        });
        return table;
    }

    private static List generateList(final Iterable<?> listContent) {
        List list = new List()
                .setListSymbol("\u2022")
                .setSymbolIndent(4);
        listContent.forEach(item -> list.add(item.toString()));
        return list;
    }

    private static Image generateCuttingPlanPage(final WritableImage snapshot) throws IOException {
        final BufferedImage bufferedSnapshot = SwingFXUtils.fromFXImage(snapshot, null);
        final boolean rotateVertical = bufferedSnapshot.getWidth() > bufferedSnapshot.getHeight();
        BufferedImage monochromeSnapshot;
        if (rotateVertical) {
            monochromeSnapshot = new BufferedImage(
                    bufferedSnapshot.getHeight(), bufferedSnapshot.getWidth(), BufferedImage.TYPE_BYTE_BINARY);
        } else {
            monochromeSnapshot = new BufferedImage(
                    bufferedSnapshot.getWidth(), bufferedSnapshot.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        }
        final Graphics2D monochromeSnapshotGraphics = monochromeSnapshot.createGraphics();
        if (rotateVertical) {
            // Move origin to right upper corner
            monochromeSnapshotGraphics.translate(
                    monochromeSnapshot.getWidth() / 2d, monochromeSnapshot.getHeight() / 2d);
            monochromeSnapshotGraphics.rotate(Math.PI / 2);
            monochromeSnapshotGraphics.translate(
                    -monochromeSnapshot.getHeight() / 2d, -monochromeSnapshot.getWidth() / 2d);
        }
        monochromeSnapshotGraphics.drawImage(bufferedSnapshot, 0, 0, null);
        final ByteArrayOutputStream snapshotByteStream = new ByteArrayOutputStream();
        ImageIO.write(monochromeSnapshot, "png", snapshotByteStream);
        return new Image(ImageDataFactory.create(snapshotByteStream.toByteArray()));
    }

    private static void scaleAndCenterOnPage(final Image content, final PageSize pageSize) {
        content.setAutoScale(true);
        content.scaleToFit(pageSize.getWidth(), pageSize.getHeight());
        final float leftMargin = (pageSize.getWidth() - content.getImageScaledWidth()) / 2;
        content.setMarginLeft(Math.max(0, leftMargin));
    }

    public static void generateCuttingPlanDocument(
            final Iterable<WritableImage> cuttingPlanSnapshots, final Iterable<RequiredPlank> plankDemandList,
            final File savePath) throws IOException {
        try (Document document = new Document(new PdfDocument(new PdfWriter(savePath)))) {
            final PdfDocument pdfDocument = document.getPdfDocument();
            final PdfDocumentInfo documentInfo = pdfDocument.getDocumentInfo();
            documentInfo.setCreator(BuildConfig.APP_NAME + " " + BuildConfig.APP_VERSION);
            final PageSize pageSize = pdfDocument.getDefaultPageSize();

            // Append plank demand list
            document.add(new Paragraph(WoodPacker.getResource("demandList")));
            document.add(generateList(plankDemandList));

            // Append cutting plans
            for (final WritableImage snapshot : cuttingPlanSnapshots) {
                document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
                final Image cuttingPlan = generateCuttingPlanPage(snapshot);
                scaleAndCenterOnPage(cuttingPlan, pageSize);
                document.add(cuttingPlan);
            }
        }
    }
}
