package Data;

import Model.*;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ServicoDAO implements Map<String, Servico> {
    private static ServicoDAO singleton = null;

    public ServicoDAO() {}

    public static ServicoDAO getInstance() {
        if (ServicoDAO.singleton == null) {
            ServicoDAO.singleton = new ServicoDAO();
        }
        return ServicoDAO.singleton;
    }


    public int size() {
        int i = 0;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT count(*) FROM Servico")) {
            if (rs.next()) {
                i = rs.getInt(1);
            }
        } catch (Exception e) {
            // Erro a criar tabela...
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return i;
    }

    public boolean isEmpty() {
        return this.size() == 0;
    }

    public boolean containsKey(Object key) {
        boolean r;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs =
                     stm.executeQuery("SELECT idS FROM Servico WHERE idS='" + key.toString() + "'")) {
            r = rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return r;
    }

    public boolean containsValue(Object value) {
        Servico p = (Servico) value;
        return this.containsKey(p.getIdS());
    }

    @Override
    public Servico get(Object key) {
        Servico servico = null;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT * FROM Servico WHERE idS ='" + key + "'")) {
            if (rs.next()) {
                String idS = rs.getString("idS");
                String nome = rs.getString("nome");
                String matricula = rs.getString("matricula");
                String idCliente = rs.getString("idCliente");
                String idMecanico = rs.getString("idMecanico");
                String idPosto = rs.getString("idPosto");
                Timestamp timestampComeco = rs.getTimestamp("data_comeco");
                LocalDateTime data_comeco = timestampComeco.toLocalDateTime();

                int tipo = rs.getInt("tipoServico");

                if (tipo == 1) {
                    servico = new ServicoUniversal(idS, nome, matricula, idCliente, idMecanico, idPosto, data_comeco, null, Duration.ofMinutes(30));
                }else if (tipo == 2){
                    servico = new ServicoGasolina(idS, nome, matricula, idCliente, idMecanico, idPosto, data_comeco, null, Duration.ofHours(1));
                }else if (tipo == 3){
                    servico = new ServicoGasoleo(idS, nome, matricula, idCliente, idMecanico, idPosto, data_comeco, null, Duration.ofHours(1));
                }else { //tipo==4
                    servico = new ServicoEletrico(idS, nome, matricula, idCliente, idMecanico, idPosto, data_comeco, null, Duration.ofHours(1));
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return servico;
    }

    @Override
    public Servico put(String key, Servico servico) {
        int tipo;
        if (servico instanceof ServicoUniversal) tipo = 1;
        else if (servico instanceof ServicoGasolina) tipo = 2;
        else if (servico instanceof ServicoGasoleo) tipo = 3;
        else tipo =4; // if(servico instanceof  ServicoEletrico)

        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {

            ResultSet existFunc = stm.executeQuery("SELECT * FROM Servico WHERE idS='" + key + "'");
            if (existFunc.next()) {
                // Atualize o registro existente
                long seconds = servico.getDuracao_prevista().getSeconds();
                long hours = seconds / 3600;
                long minutes = (seconds % 3600) / 60;
                long remainingSeconds = seconds % 60;
                Time sqlTime = Time.valueOf(String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds));
                stm.executeUpdate("UPDATE Servico SET nome='" + servico.getNome() +
                        "', matricula='" + servico.getMatricula()+
                        "', idCliente='" + servico.getId_cliente() +
                        "', idMecanico='" + servico.getId_mecanico() +
                        "', idPosto='" + servico.getId_posto() +
                        "', data_comeco='" + servico.getData_comeco() +
                        "', duracao_prevista='" + sqlTime +
                        "' WHERE idS='" + key + "'");
            } else {

                long seconds = servico.getDuracao_prevista().getSeconds();
                long hours = seconds / 3600;
                long minutes = (seconds % 3600) / 60;
                long remainingSeconds = seconds % 60;
                Time sqlTime = Time.valueOf(String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds));


                stm.executeUpdate("INSERT INTO Servico  (idS, nome, matricula, idCliente, idMecanico, idPosto, data_comeco, duracao_prevista, tipoServico) " +
                        "VALUES ('" + key +
                        "', '" + servico.getNome()  +
                        "', '" + servico.getMatricula()  +
                        "', '" + servico.getId_cliente() +
                        "', '" + servico.getId_mecanico() +
                        "', '" + servico.getId_posto() +
                        "', '" + servico.getData_comeco() +
                        "', '" + sqlTime +
                        "', '" + tipo +
                        "')");
            }

            // Após a inserção ou atualização, recupere o func para retorná-lo
            ResultSet updatedServico = stm.executeQuery("SELECT * FROM Servico WHERE idS='" + key + "'");
            if (updatedServico.next()) {
                String nome = updatedServico.getString("nome");
                String matricula = updatedServico.getString("matricula");
                String idCliente = updatedServico.getString("idCliente");
                String idMecanico = updatedServico.getString("idMecanico");
                String idPosto = updatedServico.getString("idPosto");

                // Obter Timestamp para data_comeco e data_terminio
                Timestamp timestampComeco = updatedServico.getTimestamp("data_comeco");
                LocalDateTime data_comeco = timestampComeco.toLocalDateTime();

                Timestamp timestamp = updatedServico.getTimestamp("duracao_prevista");
                Duration duration = Duration.ofMillis(timestamp.getTime());

                return new Servico(key, nome, matricula,idCliente, idMecanico, idPosto, data_comeco, duration);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return null;
    }

    @Override
    public Servico remove(Object key) {
        throw new NullPointerException("not implemented!");
    }


    @Override
    public void putAll(Map<? extends String, ? extends Servico> m) {
        throw new NullPointerException("not implemented!");
    }


    @Override
    public void clear() {
        throw new NullPointerException("not implemented!");
    }


    public Collection<Servico> values() {
        Collection<Servico> res = new HashSet<>();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT * FROM Servico")) {
            while (rs.next()) {
                String idS = rs.getString("idS");
                String nome = rs.getString("nome");
                String matricula = rs.getString("matricula");
                String idCliente = rs.getString("idCliente");
                String idMecanico = rs.getString("idMecanico");
                String idPosto = rs.getString("idPosto");

                int tipo = rs.getInt("tipoServico");

                Servico servico;
                if (tipo == 1) {
                     servico = new ServicoUniversal(idS, nome, matricula, idCliente, idMecanico, idPosto, null, null, Duration.ofMinutes(30));
                }else if (tipo == 2){
                     servico = new ServicoGasolina(idS, nome, matricula, idCliente, idMecanico, idPosto, null, null, Duration.ofHours(1));
                }else if (tipo == 3){
                     servico = new ServicoGasoleo(idS, nome, matricula, idCliente, idMecanico, idPosto, null, null, Duration.ofHours(1));
                }else { //tipo==4
                     servico = new ServicoEletrico(idS, nome, matricula, idCliente, idMecanico, idPosto, null, null, Duration.ofHours(1));
                }

                res.add(servico);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return res;
    }

    @Override
    public Set<Entry<String, Servico>> entrySet() {
        //criar um set de entrys
        Set<Entry<String, Servico>> res = new HashSet<>();
        //criar um set de keys
        Set<String> keys = new HashSet<>(this.keySet());
        //para cada key
        for (String key : keys) {
            //criar uma entry com a key e o value
            Entry<String, Servico> entry = new AbstractMap.SimpleEntry<>(key, this.get(key));
            //adicionar a entry ao set de entrys
            res.add(entry);
        }
        return res;
    }
    @Override
    public Set<String> keySet() {
        Set<String> res = new HashSet<>();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT idS FROM Servico")) {
            while (rs.next()) {
                String idp = rs.getString("idS");
                res.add(idp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return res;
    }


    public void atualizaAgendamentoServico(String idServico, LocalDateTime data) {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement("UPDATE Servico SET data_comeco = ? WHERE idS = ?")) {
            pstmt.setTimestamp(1, Timestamp.valueOf(data));
            pstmt.setString(2, idServico);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void atualizaFimServico(String idServico, LocalDateTime dataFim) {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement("UPDATE Servico SET data_terminio = ? WHERE idS = ?")) {
            pstmt.setTimestamp(1, Timestamp.valueOf(dataFim));
            pstmt.setString(2, idServico);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
