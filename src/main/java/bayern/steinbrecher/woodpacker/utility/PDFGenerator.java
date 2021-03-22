package bayern.steinbrecher.woodpacker.utility;

import bayern.steinbrecher.woodpacker.BuildConfig;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfDocumentInfo;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.property.AreaBreakType;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public final class PDFGenerator {

    private PDFGenerator() {
        throw new UnsupportedOperationException("The construction of instances is prohibited");
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
            final Collection<WritableImage> cuttingPlanSnapshots, final File savePath) throws IOException {
        try (Document document = new Document(new PdfDocument(new PdfWriter(savePath)))) {
            final PdfDocument pdfDocument = document.getPdfDocument();
            final PdfDocumentInfo documentInfo = pdfDocument.getDocumentInfo();
            documentInfo.setCreator(BuildConfig.APP_NAME + " " + BuildConfig.APP_VERSION);
            final PageSize pageSize = pdfDocument.getDefaultPageSize();

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
