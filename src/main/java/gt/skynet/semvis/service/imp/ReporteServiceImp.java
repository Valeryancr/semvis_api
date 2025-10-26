package gt.skynet.semvis.service.imp;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import gt.skynet.semvis.entity.Visita;
import gt.skynet.semvis.entity.VisitaEvento;
import gt.skynet.semvis.entity.VisitaReporte;
import gt.skynet.semvis.repository.VisitaEventoRepo;
import gt.skynet.semvis.repository.VisitaReporteRepo;
import gt.skynet.semvis.security.JwtService;
import gt.skynet.semvis.service.EmailService;
import gt.skynet.semvis.service.ReporteService;
import gt.skynet.semvis.util.AuthUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ReporteServiceImp implements ReporteService {

    @Autowired
    private VisitaReporteRepo repo;

    @Autowired
    private JwtService jwt;

    @Autowired
    private VisitaEventoRepo eventoRepo;

    @Value("${app.report.base-url}")
    private String reportBaseUrl;

    @Value("${spring.jackson.time-zone}")
    private String appTimeZone;

    @Autowired
    EmailService emailService;

    @Autowired
    private AuthUtils auth;

    private static final String FONT_REGULAR = "static/fonts/Poppins-Regular.ttf";
    private static final String FONT_BOLD = "static/fonts/Poppins-Bold.ttf";

    private static final Color PRIMARY = new Color(18, 35, 89);
    private static final Color ACCENT = new Color(91, 146, 255);
    private static final Color LIGHT_BG = new Color(240, 248, 255);


    @Transactional
    @Override
    public VisitaReporte generarReporte(Visita visita) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            doc.open();

            BaseFont regular = BaseFont.createFont(FONT_REGULAR, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            BaseFont bold = BaseFont.createFont(FONT_BOLD, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

            addHeader(doc, bold);

            PdfPTable titleTable = new PdfPTable(1);
            titleTable.setWidthPercentage(100);
            PdfPCell titleCell = new PdfPCell(new Phrase("REPORTE DE VISITA", new Font(bold, 16, Font.BOLD, Color.WHITE)));
            titleCell.setBackgroundColor(PRIMARY);
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            titleCell.setPadding(10);
            titleCell.setBorder(Rectangle.NO_BORDER);
            titleTable.addCell(titleCell);
            doc.add(titleTable);

            doc.add(new Paragraph(" "));

            Font text = new Font(regular, 12);
            Font label = new Font(bold, 12);

            PdfPTable info = new PdfPTable(2);
            info.setWidthPercentage(100);
            info.setSpacingBefore(10);
            info.setWidths(new float[]{30, 70});
            info.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            addRow(info, "Cliente:", visita.getCliente().getNombre(), label, text);
            addRow(info, "Técnico:", visita.getTecnico() != null ? visita.getTecnico().getNombreCompleto() : "N/A", label, text);
            addRow(info, "Fecha programada:", visita.getFechaProgramada().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm")), label, text);
            addRow(info, "Estado:", visita.getEstado().name(), label, text);
            doc.add(info);

            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Observaciones", new Font(bold, 13, Font.NORMAL, PRIMARY)));
            doc.add(new Paragraph(visita.getObservacionesPlan() != null
                    ? visita.getObservacionesPlan()
                    : "Sin observaciones registradas.", text));

            List<VisitaEvento> eventos = eventoRepo.findByVisitaIdOrderByFechaAsc(visita.getId());
            if (!eventos.isEmpty()) {
                doc.add(new Paragraph(" "));
                doc.add(new Paragraph("Detalle de eventos", new Font(bold, 13, Font.NORMAL, PRIMARY)));

                PdfPTable eventosTable = new PdfPTable(3);
                eventosTable.setWidthPercentage(100);
                eventosTable.setSpacingBefore(10);
                eventosTable.setWidths(new float[]{20, 40, 40});

                PdfPCell h1 = new PdfPCell(new Phrase("Tipo", new Font(bold, 12, Font.BOLD, Color.WHITE)));
                PdfPCell h2 = new PdfPCell(new Phrase("Fecha y hora", new Font(bold, 12, Font.BOLD, Color.WHITE)));
                PdfPCell h3 = new PdfPCell(new Phrase("Nota", new Font(bold, 12, Font.BOLD, Color.WHITE)));
                h1.setBackgroundColor(PRIMARY);
                h2.setBackgroundColor(PRIMARY);
                h3.setBackgroundColor(PRIMARY);
                h1.setPadding(5); h2.setPadding(5); h3.setPadding(5);
                eventosTable.addCell(h1); eventosTable.addCell(h2); eventosTable.addCell(h3);

                ZoneId zone = ZoneId.of(appTimeZone);

                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm", Locale.of("es", "ES"))
                        .withZone(zone);
                for (VisitaEvento evento : eventos) {
                    eventosTable.addCell(new Phrase(evento.getTipo().name(), text));
                    eventosTable.addCell(new Phrase(evento.getFecha().atZoneSameInstant(zone).format(fmt), text));
                    eventosTable.addCell(new Phrase(evento.getNota() != null ? evento.getNota() : "-", text));
                }

                doc.add(eventosTable);
            }

            addFooter(writer, regular);

            doc.close();

            VisitaReporte visitaReporte = new VisitaReporte();
            Visita referencia = new Visita();
            referencia.setId(visita.getId());
            visitaReporte.setVisita(referencia);
            visitaReporte.setPdfBlob(out.toByteArray());
            byte[] pdfBytes = out.toByteArray();
            VisitaReporte vr = repo.save(visitaReporte);

            String email = visita.getCliente().getUser() != null ? visita.getCliente().getUser().getEmail() : null;
            if (email != null && !email.isBlank()) {
                String html = """
                        <h2>Reporte de Visita Completada</h2>
                        <p>Estimado cliente,</p>
                        <p>Adjunto encontrará el reporte de la visita realizada.</p>
                        <p style="color:#5B92FF;font-weight:bold;">SkyNet S.A. - SemVis</p>
                        """;
                String tokenPdf = jwt.generate(
                        visita.getCliente().getUser().getUsername(),
                        Map.of(
                                "rid", vr.getId(),
                                "type", "pdf",
                                "expMinutes", 2880
                        )
                );

                String enlacePdf = reportBaseUrl + tokenPdf;

                OffsetDateTime fechaFinal = visita.getCompletedAt() != null
                        ? visita.getCompletedAt()
                        : visita.getFechaProgramada();

                String fechaVisita = fechaFinal.format(
                        DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy 'a las' h:mm a", Locale.of("es", "ES"))
                );

                emailService.enviarCorreoReporte(
                        email,
                        visita.getCliente().getNombre(),
                        visita.getTecnico() != null ? visita.getTecnico().getNombreCompleto() : "No asignado",
                        fechaVisita,
                        visita.getObservacionesPlan() != null ? visita.getObservacionesPlan() : "Sin observaciones registradas.",
                        enlacePdf,
                        visita.getId()
                );
                vr.setEnviadoEmail(true);
                vr.setSentAt(java.time.OffsetDateTime.now());
                vr.setNombreArchivo("finalizacion_visita_" + visita.getId() + ".pdf");
                vr.setUsuarioCarga(auth.getAuthenticatedUser());
                repo.save(vr);
            }
            return vr;
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF: " + e.getMessage(), e);
        }
    }

    private void addHeader(Document doc, BaseFont bold) throws IOException, DocumentException {
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{25, 75});

        Image logo;
        try {
            logo = Image.getInstance(new ClassPathResource("static/img/semvis_logo.png").getInputStream().readAllBytes());
            logo.scaleToFit(50, 50);
        } catch (Exception e) {
            logo = null;
        }

        PdfPCell logoCell = new PdfPCell();
        if (logo != null) logoCell.addElement(logo);
        logoCell.setBorder(Rectangle.NO_BORDER);
        header.addCell(logoCell);

        PdfPCell textCell = new PdfPCell();
        textCell.setBorder(Rectangle.NO_BORDER);
        Paragraph name = new Paragraph("SkyNet S.A.", new Font(bold, 16, Font.BOLD, PRIMARY));
        Paragraph project = new Paragraph("Sistema de Gestión de Visitas (SemVis)", new Font(bold, 11, Font.NORMAL, Color.GRAY));
        textCell.addElement(name);
        textCell.addElement(project);
        header.addCell(textCell);

        doc.add(header);
        doc.add(new Paragraph(" "));
    }

    private void addRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, labelFont));
        c1.setBorder(Rectangle.NO_BORDER);
        c1.setPadding(4);
        PdfPCell c2 = new PdfPCell(new Phrase(value, valueFont));
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setPadding(4);
        table.addCell(c1);
        table.addCell(c2);
    }

    private void addFooter(PdfWriter writer, BaseFont font) {
        PdfContentByte cb = writer.getDirectContent();
        cb.saveState();
        cb.beginText();
        cb.setFontAndSize(font, 9);
        cb.showTextAligned(Element.ALIGN_CENTER,
                "Generado automáticamente por SemVis © SkyNet S.A.",
                (PageSize.A4.getWidth() / 2), 20, 0);
        cb.endText();
        cb.restoreState();
    }
}