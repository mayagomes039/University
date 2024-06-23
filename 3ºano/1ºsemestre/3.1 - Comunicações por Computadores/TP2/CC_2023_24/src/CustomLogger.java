import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomLogger {
    // Códigos ANSI para cores
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";

    public static void logError(String mensagem) {
        log(TipoLog.ERRO, mensagem);
    }

    public static void logInfo(String mensagem) {
        log(TipoLog.INFO, mensagem);
    }

    public static void logWarning(String mensagem) {
        log(TipoLog.WARNING, mensagem);
    }

    private static void log(TipoLog tipoLog, String mensagem) {
        String horaAtual = obterHoraAtual();
        String tipoLogFormatado = formatarTipoLog(tipoLog);

        String corTipoLog = obterCorTipoLog(tipoLog);

        String logFormatado = String.format("%s - %s%s%s - %s", horaAtual, corTipoLog, tipoLogFormatado, ANSI_RESET, mensagem);

        System.out.println(logFormatado);
    }

    private static String obterHoraAtual() {
        SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");
        return formatoHora.format(new Date());
    }

    private static String formatarTipoLog(TipoLog tipoLog) {
        switch (tipoLog) {
            case INFO:
                return "INFO";
            case ERRO:
                return "ERRO";
            case WARNING:
                return "WARNING";
            default:
                return "DESCONHECIDO";
        }
    }

    private static String obterCorTipoLog(TipoLog tipoLog) {
        switch (tipoLog) {
            case INFO:
                return ANSI_GREEN;
            case ERRO:
                return ANSI_RED;
            case WARNING:
                return ANSI_YELLOW;
            default:
                return ANSI_RESET;
        }
    }

    // Enum para os tipos de log
    public enum TipoLog {
        INFO,
        ERRO,
        WARNING
    }
}
