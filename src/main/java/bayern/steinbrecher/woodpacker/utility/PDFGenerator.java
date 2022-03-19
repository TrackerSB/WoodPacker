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
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.function.Supplier;

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

    private static Image generateCuttingPlanPage(final WritableImage cuttingPlan) throws IOException {
        // Remove wood colored background
        final PixelReader pixelReader = cuttingPlan.getPixelReader();
        final PixelWriter pixelWriter = cuttingPlan.getPixelWriter();
        for (int x = 0; x < cuttingPlan.getWidth(); x++) {
            for (int y = 0; y < cuttingPlan.getHeight(); y++) {
                final Color pixelColor = pixelReader.getColor(x, y);
                if (pixelColor.equals(DrawActionGenerator.BASE_PLANK_COLOR)
                        || pixelColor.equals(DrawActionGenerator.REQUIRED_PLANK_COLOR)) {
                    pixelWriter.setColor(x, y, Color.WHITE);
                }
            }
        }

        final boolean rotateVertical = cuttingPlan.getWidth() > cuttingPlan.getHeight();

        // Create output AWT image with modifying properties (including grayscale and eventual rotation)
        final var modifiedCuttingPlan = ((Supplier<BufferedImage>) () -> {
            if (rotateVertical) {
                return new BufferedImage((int) cuttingPlan.getHeight(), (int) cuttingPlan.getWidth(),
                        BufferedImage.TYPE_USHORT_GRAY);
            }
            return new BufferedImage((int) cuttingPlan.getWidth(), (int) cuttingPlan.getHeight(),
                    BufferedImage.TYPE_USHORT_GRAY);
        }).get();

        final Graphics2D modifiedCuttingPlanDrawer = modifiedCuttingPlan.createGraphics();
        if (rotateVertical) {
            // Move origin to right upper corner
            modifiedCuttingPlanDrawer.translate(
                    modifiedCuttingPlan.getWidth() / 2d, modifiedCuttingPlan.getHeight() / 2d);

            // Rotate context by 90°
            modifiedCuttingPlanDrawer.rotate(Math.PI / 2d);

            // Move origin back to center
            modifiedCuttingPlanDrawer.translate(
                    -modifiedCuttingPlan.getHeight() / 2d, -modifiedCuttingPlan.getWidth() / 2d);
        }

        // Convert JavaFX image to AWT image
        final BufferedImage bufferedCuttingPlan = SwingFXUtils.fromFXImage(cuttingPlan, null);

        // Apply cutting plan to modifying output AWT image
        modifiedCuttingPlanDrawer.drawImage(bufferedCuttingPlan, 0, 0, null);

        // Convert AWT image to PDF image
        final ByteArrayOutputStream snapshotByteStream = new ByteArrayOutputStream();
        ImageIO.write(modifiedCuttingPlan, "png", snapshotByteStream);
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
