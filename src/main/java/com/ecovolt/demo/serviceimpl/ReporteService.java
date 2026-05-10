package com.ecovolt.demo.serviceimpl;

import com.ecovolt.demo.dtos.ItemComparacionConsumoDto;
import com.ecovolt.demo.dtos.ReporteRespuestaDto;
import com.ecovolt.demo.entities.Historico;
import com.ecovolt.demo.entities.DispositivoVirtual;
import com.ecovolt.demo.repositories.AlertaRepositorio;
import com.ecovolt.demo.repositories.HistoricoRepositorio;
import com.ecovolt.demo.repositories.DispositivoVirtualRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReporteService {

    private final HistoricoRepositorio historicoRepositorio;
    private final DispositivoVirtualRepositorio dispositivoVirtualRepositorio;
    private final AlertaRepositorio alertaRepositorio;

    public ReporteService(HistoricoRepositorio historicoRepositorio,
                          DispositivoVirtualRepositorio dispositivoVirtualRepositorio,
                          AlertaRepositorio alertaRepositorio) {
        this.historicoRepositorio = historicoRepositorio;
        this.dispositivoVirtualRepositorio = dispositivoVirtualRepositorio;
        this.alertaRepositorio = alertaRepositorio;
    }

    @Transactional(readOnly = true)
    public ReporteRespuestaDto getReport(Long userId) {
        List<Historico> history = historicoRepositorio
                .findByDispositivoHabitacionCasaUsuarioIdOrderByFechaRegistroDesc(userId);
        List<ItemComparacionConsumoDto> topConsumers = buildTopConsumers(history);

        return ReporteRespuestaDto.builder()
                .totalKwh(round(history.stream().map(Historico::getKwhConsumidos).mapToDouble(Double::doubleValue).sum()))
                .totalDurationMinutes(history.stream()
                        .map(Historico::getDuracionMinutos)
                        .filter(minutes -> minutes != null)
                        .mapToInt(Integer::intValue)
                        .sum())
                .deviceCount(dispositivoVirtualRepositorio.findByHabitacionCasaUsuarioId(userId).size())
                .alertCount(alertaRepositorio.findByDispositivoHabitacionCasaUsuarioIdOrderByFechaCreacionDesc(userId).size())
                .topConsumers(topConsumers)
                .build();
    }

    @Transactional(readOnly = true)
    public byte[] exportPdf(Long userId) {
        ReporteRespuestaDto report = getReport(userId);
        StringBuilder text = new StringBuilder();
        text.append("EcoVolt - Reporte de consumo energetico\n");
        text.append("Consumo total: ").append(report.getTotalKwh()).append(" kWh\n");
        text.append("Duracion total: ").append(report.getTotalDurationMinutes()).append(" minutos\n");
        text.append("Dispositivos: ").append(report.getDeviceCount()).append("\n");
        text.append("Alertas: ").append(report.getAlertCount()).append("\n\n");
        text.append("Mayores consumos:\n");

        for (ItemComparacionConsumoDto item : report.getTopConsumers()) {
            text.append("- ")
                    .append(item.getDeviceName())
                    .append(" (")
                    .append(item.getRoomName())
                    .append("): ")
                    .append(item.getTotalKwh())
                    .append(" kWh\n");
        }

        return buildSimplePdf(text.toString());
    }

    private List<ItemComparacionConsumoDto> buildTopConsumers(List<Historico> history) {
        Map<Long, DispositivoVirtual> devicesById = history.stream()
                .map(Historico::getDispositivo)
                .collect(Collectors.toMap(
                        DispositivoVirtual::getId,
                        device -> device,
                        (current, ignored) -> current,
                        LinkedHashMap::new
                ));

        Map<Long, Double> totals = history.stream()
                .collect(Collectors.groupingBy(
                        historyItem -> historyItem.getDispositivo().getId(),
                        LinkedHashMap::new,
                        Collectors.summingDouble(Historico::getKwhConsumidos)
                ));

        double totalKwh = totals.values().stream().mapToDouble(Double::doubleValue).sum();

        return totals.entrySet()
                .stream()
                .map(entry -> toItem(devicesById.get(entry.getKey()), entry.getValue(), totalKwh))
                .sorted(Comparator.comparing(ItemComparacionConsumoDto::getTotalKwh).reversed())
                .limit(5)
                .toList();
    }

    private ItemComparacionConsumoDto toItem(DispositivoVirtual device, Double deviceTotal, double totalKwh) {
        double percentage = totalKwh == 0 ? 0 : (deviceTotal / totalKwh) * 100;

        return ItemComparacionConsumoDto.builder()
                .deviceId(device.getId())
                .deviceName(device.getNombre())
                .roomId(device.getHabitacion().getId())
                .roomName(device.getHabitacion().getNombre())
                .totalKwh(round(deviceTotal))
                .percentage(round(percentage))
                .build();
    }

    private byte[] buildSimplePdf(String content) {
        String escapedContent = escapePdf(content).replace("\n", ") Tj T* (");
        String stream = "BT /F1 12 Tf 50 780 Td 14 TL (" + escapedContent + ") Tj ET";
        byte[] streamBytes = stream.getBytes(StandardCharsets.UTF_8);

        String pdf = "%PDF-1.4\n"
                + "1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n"
                + "2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n"
                + "3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] "
                + "/Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >> endobj\n"
                + "4 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj\n"
                + "5 0 obj << /Length " + streamBytes.length + " >> stream\n"
                + stream
                + "\nendstream endobj\n"
                + "xref\n0 6\n0000000000 65535 f \n"
                + "trailer << /Root 1 0 R /Size 6 >>\nstartxref\n0\n%%EOF";

        return pdf.getBytes(StandardCharsets.UTF_8);
    }

    private String escapePdf(String value) {
        return value.replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
