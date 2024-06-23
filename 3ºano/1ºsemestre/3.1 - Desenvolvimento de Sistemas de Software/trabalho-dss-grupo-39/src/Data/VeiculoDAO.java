package Data;
import Model.*;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class VeiculoDAO implements Map<String, Veiculo> {
    private static VeiculoDAO singleton = null;

    public VeiculoDAO() {
    }

    public static VeiculoDAO getInstance() {
        if (VeiculoDAO.singleton == null) {
            VeiculoDAO.singleton = new VeiculoDAO();
        }
        return VeiculoDAO.singleton;
    }


    public int size() {
        int i = 0;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT count(*) FROM Veiculo")) {
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
                     stm.executeQuery("SELECT Matricula FROM Veiculo WHERE Matricula='" + key.toString() + "'")) {
            r = rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return r;
    }

    public boolean containsValue(Object value) {
        Veiculo v = (Veiculo) value;
        return this.containsKey(v.getMatricula());
    }


    public Veiculo get(Object key) {
        List<ServicoUniversal> servicosUniversais = new ArrayList<>();
        Veiculo veiculo = null;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT * FROM Veiculo WHERE Matricula ='" + key + "'")) {
            if (rs.next()) {
                int tipo = rs.getInt("Tipo");
                String sql1 = "SELECT * FROM Servico WHERE matricula = ? AND tipoServico = 1";
                try (PreparedStatement stmt = conn.prepareStatement(sql1)) {
                    stmt.setString(1, key.toString());
                    try (ResultSet resultSet = stmt.executeQuery()) {
                        while (resultSet.next()) {
                            String idS = resultSet.getString("idS");
                            String nome = resultSet.getString("nome");
                            String matricula = resultSet.getString("matricula");
                            String idCliente = resultSet.getString("idCliente");
                            String idMecanico = resultSet.getString("idMecanico");
                            String idPosto = resultSet.getString("idPosto");

                            // Obter Timestamp para data_comeco e data_terminio
                            Timestamp timestampComeco = resultSet.getTimestamp("data_comeco");


                            // Converter Timestamp para LocalDateTime
                            LocalDateTime data_comeco = timestampComeco.toLocalDateTime();


                            ServicoUniversal s = new ServicoUniversal(idS, nome, matricula, idCliente, idMecanico, idPosto, data_comeco,null, Duration.ofMinutes(30));

                            servicosUniversais.add(s);
                        }
                    }
                }
                String matricula = key.toString();
                if (tipo == 1) {
                    List<ServicoGasolina> servicosGasolina = new ArrayList<>();
                    String sql2 = "SELECT * FROM Servico WHERE matricula = ? AND tipoServico = 2";
                    try (PreparedStatement stmt = conn.prepareStatement(sql2)) {
                        stmt.setString(1, matricula);
                        try (ResultSet resultSet = stmt.executeQuery()) {
                            while (resultSet.next()) {
                                String idS = resultSet.getString("idS");
                                String nome = resultSet.getString("nome");
                                String idCliente = resultSet.getString("idCliente");
                                String idMecanico = resultSet.getString("idMecanico");
                                String idPosto = resultSet.getString("idPosto");
                                ServicoGasolina s = new ServicoGasolina(idS, nome, matricula, idCliente, idMecanico, idPosto, null, null, Duration.ofHours(1));
                                servicosGasolina.add(s);
                            }
                        }
                    }
                    veiculo = new VeiculoGasolina(matricula, servicosUniversais, servicosGasolina);
                } else if (tipo == 2) {
                    List<ServicoGasoleo> servicosGasoleo = new ArrayList<>();
                    String sql2 = "SELECT * FROM Servico WHERE matricula = ? AND tipoServico = 3";
                    try (PreparedStatement stmt = conn.prepareStatement(sql2)) {
                        stmt.setString(1, matricula);
                        try (ResultSet resultSet = stmt.executeQuery()) {
                            while (resultSet.next()) {
                                String idS = resultSet.getString("idS");
                                String nome = resultSet.getString("nome");
                                String idCliente = resultSet.getString("idCliente");
                                String idMecanico = resultSet.getString("idMecanico");
                                String idPosto = resultSet.getString("idPosto");
                                ServicoGasoleo s = new ServicoGasoleo(idS, nome, matricula, idCliente, idMecanico, idPosto, null, null, Duration.ofHours(1));
                                servicosGasoleo.add(s);
                            }
                        }
                    }
                    veiculo = new VeiculoGasoleo(matricula, servicosUniversais, servicosGasoleo);
                } else if (tipo == 3) {
                    List<ServicoEletrico> servicoEletricos = new ArrayList<>();
                    String sql2 = "SELECT * FROM Servico WHERE matricula = ? AND tipoServico = 4";
                    try (PreparedStatement stmt = conn.prepareStatement(sql2)) {
                        stmt.setString(1, matricula);
                        try (ResultSet resultSet = stmt.executeQuery()) {
                            while (resultSet.next()) {
                                String idS = resultSet.getString("idS");
                                String nome = resultSet.getString("nome");
                                String idCliente = resultSet.getString("idCliente");
                                String idMecanico = resultSet.getString("idMecanico");
                                String idPosto = resultSet.getString("idPosto");
                                ServicoEletrico s = new ServicoEletrico(idS, nome, matricula, idCliente, idMecanico, idPosto, null, null, Duration.ofHours(1));
                                servicoEletricos.add(s);
                            }
                        }
                    }
                    veiculo = new VeiculoEletrico(matricula, servicosUniversais, servicoEletricos);
                } else { //if (tipo == 4)
                    List<ServicoEletrico> servicoEletricos = new ArrayList<>();
                    String sql2 = "SELECT * FROM Servico WHERE matricula = ? AND tipoServico = 4";
                    try (PreparedStatement stmt = conn.prepareStatement(sql2)) {
                        stmt.setString(1, matricula);
                        try (ResultSet resultSet = stmt.executeQuery()) {
                            while (resultSet.next()) {
                                String idS = resultSet.getString("idS");
                                String nome = resultSet.getString("nome");
                                String idCliente = resultSet.getString("idCliente");
                                String idMecanico = resultSet.getString("idMecanico");
                                String idPosto = resultSet.getString("idPosto");
                                ServicoEletrico s = new ServicoEletrico(idS, nome, matricula, idCliente, idMecanico, idPosto, null, null, Duration.ofHours(1));
                                servicoEletricos.add(s);
                            }
                        }
                    }
                    List<ServicoGasoleo> servicosGasoleo = new ArrayList<>();
                    String sql3 = "SELECT * FROM Servico WHERE matricula = ? AND tipoServico = 3";
                    try (PreparedStatement stmt = conn.prepareStatement(sql3)) {
                        stmt.setString(1, matricula);
                        try (ResultSet resultSet = stmt.executeQuery()) {
                            while (resultSet.next()) {
                                String idS = resultSet.getString("idS");
                                String nome = resultSet.getString("nome");
                                String idCliente = resultSet.getString("idCliente");
                                String idMecanico = resultSet.getString("idMecanico");
                                String idPosto = resultSet.getString("idPosto");
                                ServicoGasoleo s = new ServicoGasoleo(idS, nome, matricula, idCliente, idMecanico, idPosto, null, null, Duration.ofHours(1));
                                servicosGasoleo.add(s);
                            }
                        }
                    }
                    List<ServicoGasolina> servicosGasolina = new ArrayList<>();
                    String sql4 = "SELECT * FROM Servico WHERE matricula = ? AND tipoServico = 2";
                    try (PreparedStatement stmt = conn.prepareStatement(sql4)) {
                        stmt.setString(1, matricula);
                        try (ResultSet resultSet = stmt.executeQuery()) {
                            while (resultSet.next()) {
                                String idS = resultSet.getString("idS");
                                String nome = resultSet.getString("nome");
                                String idCliente = resultSet.getString("idCliente");
                                String idMecanico = resultSet.getString("idMecanico");
                                String idPosto = resultSet.getString("idPosto");
                                ServicoGasolina s = new ServicoGasolina(idS, nome, matricula, idCliente, idMecanico, idPosto, null, null, Duration.ofHours(1));
                                servicosGasolina.add(s);
                            }
                        }
                    }
                    veiculo = new VeiculoHibrido(matricula, servicosUniversais, servicosGasoleo, servicosGasolina, servicoEletricos);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return veiculo;
    }

    @Override
    public Veiculo put(String key, Veiculo value) {
        List<ServicoUniversal> servicosUniversais = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {
            // Verificar se o veiculo com a chave (ID) já existe
            ResultSet existVeiculo = stm.executeQuery("SELECT * FROM Veiculo WHERE Matricula='" + key + "'");
            if (!existVeiculo.next()) { // Se o veiculo não existe, add um novo registro
                stm.executeUpdate("INSERT INTO Veiculo (Matricula) " +
                        "VALUES ('" + key + "')");
            }
            // Após a inserção ou atualização, recupere o veiculo para retorná-lo
            ResultSet updatedVeiculo = stm.executeQuery("SELECT * FROM Veiculo WHERE Matricula='" + key + "'");
            if (updatedVeiculo.next()) {
                String cliente = updatedVeiculo.getString("ClienteNIF");

                String sql1 = "SELECT * FROM Servico WHERE matricula = ? AND tipoServico = 1";
                try (PreparedStatement stmt = conn.prepareStatement(sql1)) {
                    stmt.setString(1, key);
                    try (ResultSet resultSet = stmt.executeQuery()) {
                        while (resultSet.next()) {
                            String idS = resultSet.getString("idS");
                            String nome = resultSet.getString("nome");
                            String matricula = resultSet.getString("matricula");
                            String idCliente = resultSet.getString("idCliente");
                            String idMecanico = resultSet.getString("idMecanico");
                            String idPosto = resultSet.getString("idPosto");

                            // Obter Timestamp para data_comeco e data_terminio
                            Timestamp timestampComeco = resultSet.getTimestamp("data_comeco");
                            Timestamp timestampTerminio = resultSet.getTimestamp("data_terminio");


                            // Converter Timestamp para LocalDateTime
                            LocalDateTime data_comeco = timestampComeco.toLocalDateTime();
                            LocalDateTime data_terminio = timestampTerminio.toLocalDateTime();


                            ServicoUniversal s = new ServicoUniversal(idS, nome, matricula, idCliente, idMecanico, idPosto, data_comeco, data_terminio, Duration.ofMinutes(30));

                            servicosUniversais.add(s);
                        }
                    }
                }

                return new Veiculo(key, servicosUniversais);
            }


        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return null;
    }

    public Veiculo remove(Object key) {
        Veiculo veiculoRemovido = null;
        List<ServicoUniversal> servicosUniversais = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {
            // Verificar se o veiculo com a chave (ID) existe
            ResultSet existVeiculo = stm.executeQuery("SELECT * FROM Veiculo WHERE Matricula='" + key + "'");
            if (existVeiculo.next()) {
                // Se o cli existe, recupere os dados antes de excluir
                String matricula = existVeiculo.getString("Matricula");


                String sql1 = "SELECT * FROM Servico WHERE matricula = ? AND tipoServico = 1";
                try (PreparedStatement stmt = conn.prepareStatement(sql1)) {
                    stmt.setString(1, matricula);
                    try (ResultSet resultSet = stmt.executeQuery()) {
                        while (resultSet.next()) {
                            String idS = resultSet.getString("idS");
                            String nome = resultSet.getString("nome");
                            String idCliente = resultSet.getString("idCliente");
                            String idMecanico = resultSet.getString("idMecanico");
                            String idPosto = resultSet.getString("idPosto");

                            // Obter Timestamp para data_comeco e data_terminio
                            Timestamp timestampComeco = resultSet.getTimestamp("data_comeco");
                            Timestamp timestampTerminio = resultSet.getTimestamp("data_terminio");


                            // Converter Timestamp para LocalDateTime
                            LocalDateTime data_comeco = timestampComeco.toLocalDateTime();
                            LocalDateTime data_terminio = timestampTerminio.toLocalDateTime();


                            ServicoUniversal s = new ServicoUniversal(idS, nome, matricula, idCliente, idMecanico, idPosto, data_comeco, data_terminio, Duration.ofMinutes(30));

                            servicosUniversais.add(s);
                        }
                    }
                }

                veiculoRemovido = new Veiculo(matricula, servicosUniversais);

                // Execute a exclusão
                stm.executeUpdate("DELETE FROM Veiculo WHERE Matricula='" + key + "'");


            }
        } catch (SQLException e) {
            // Lidar com exceções SQL, se necessário
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }

        return veiculoRemovido;
    }

    public void putAll(Map<? extends String, ? extends Veiculo> veiculos) {
        for (Veiculo veiculo : veiculos.values()) {
            this.put(veiculo.getMatricula(), veiculo);
        }
    }

    public void clear() {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {
            // Execute uma instrução SQL para apagar todos os alunos da tabela
            stm.executeUpdate("DELETE FROM Veiculo");
        } catch (SQLException e) {
            // Lidar com exceções SQL, se necessário
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    public Set<String> keySet() {
        Set<String> ids = new HashSet<>();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {
            // Execute uma instrução SQL para obter todos os IDs dos veiculos da tabela
            ResultSet result = stm.executeQuery("SELECT Matricula FROM Veiculo");
            while (result.next()) {
                // Recupere o ID de cada um e adicione-o ao conjunto
                String numero = result.getString("Matricula");
                ids.add(numero);
            }
        } catch (SQLException e) {
            // Lidar com exceções SQL, se necessário
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
        return ids;
    }

    public Collection<Veiculo> values() {
        Collection<Veiculo> res = new HashSet<>();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT * FROM Veiculo")) {
            while (rs.next()) {
                String matricula = rs.getString("Matricula");
                int tipo = rs.getInt("Tipo");

                String sql1 = "SELECT * FROM Servico WHERE matricula = ? AND tipoServico = 1";
                List<ServicoUniversal> servicosUniversais = new ArrayList<>();
                try (PreparedStatement stmt = conn.prepareStatement(sql1)) {
                    stmt.setString(1, matricula);
                    try (ResultSet resultSet = stmt.executeQuery()) {
                        while (resultSet.next()) {
                            String idS = resultSet.getString("idS");
                            String nome = resultSet.getString("nome");
                            String idCliente = resultSet.getString("idCliente");
                            String idMecanico = resultSet.getString("idMecanico");
                            String idPosto = resultSet.getString("idPosto");

                            ServicoUniversal s = new ServicoUniversal(idS, nome, matricula, idCliente, idMecanico, idPosto, null, null, Duration.ofMinutes(30));

                            servicosUniversais.add(s);
                        }
                    }
                }
                Veiculo veiculo;
                if (tipo == 1) {
                    List<ServicoGasolina> servicosGasolina = new ArrayList<>();
                    String sql2 = "SELECT * FROM Servico WHERE matricula = ? AND tipoServico = 2";
                    try (PreparedStatement stmt = conn.prepareStatement(sql2)) {
                        stmt.setString(1, matricula);
                        try (ResultSet resultSet = stmt.executeQuery()) {
                            while (resultSet.next()) {
                                String idS = resultSet.getString("idS");
                                String nome = resultSet.getString("nome");
                                String idCliente = resultSet.getString("idCliente");
                                String idMecanico = resultSet.getString("idMecanico");
                                String idPosto = resultSet.getString("idPosto");
                                ServicoGasolina s = new ServicoGasolina(idS, nome, matricula, idCliente, idMecanico, idPosto, null, null, Duration.ofHours(1));
                                servicosGasolina.add(s);
                            }
                        }
                    }
                    veiculo = new VeiculoGasolina(matricula, servicosUniversais, servicosGasolina);
                } else if (tipo == 2) {
                    List<ServicoGasoleo> servicosGasoleo = new ArrayList<>();
                    String sql2 = "SELECT * FROM Servico WHERE matricula = ? AND tipoServico = 3";
                    try (PreparedStatement stmt = conn.prepareStatement(sql2)) {
                        stmt.setString(1, matricula);
                        try (ResultSet resultSet = stmt.executeQuery()) {
                            while (resultSet.next()) {
                                String idS = resultSet.getString("idS");
                                String nome = resultSet.getString("nome");
                                String idCliente = resultSet.getString("idCliente");
                                String idMecanico = resultSet.getString("idMecanico");
                                String idPosto = resultSet.getString("idPosto");
                                ServicoGasoleo s = new ServicoGasoleo(idS, nome, matricula, idCliente, idMecanico, idPosto, null, null, Duration.ofHours(1));
                                servicosGasoleo.add(s);
                            }
                        }
                    }
                    veiculo = new VeiculoGasoleo(matricula, servicosUniversais, servicosGasoleo);
                } else if (tipo == 3) {
                    List<ServicoEletrico> servicoEletricos = new ArrayList<>();
                    String sql2 = "SELECT * FROM Servico WHERE matricula = ? AND tipoServico = 4";
                    try (PreparedStatement stmt = conn.prepareStatement(sql2)) {
                        stmt.setString(1, matricula);
                        try (ResultSet resultSet = stmt.executeQuery()) {
                            while (resultSet.next()) {
                                String idS = resultSet.getString("idS");
                                String nome = resultSet.getString("nome");
                                String idCliente = resultSet.getString("idCliente");
                                String idMecanico = resultSet.getString("idMecanico");
                                String idPosto = resultSet.getString("idPosto");
                                ServicoEletrico s = new ServicoEletrico(idS, nome, matricula, idCliente, idMecanico, idPosto, null, null, Duration.ofHours(1));
                                servicoEletricos.add(s);
                            }
                        }
                    }
                    veiculo = new VeiculoEletrico(matricula, servicosUniversais, servicoEletricos);
                } else { //if (tipo == 4)
                    List<ServicoEletrico> servicoEletricos = new ArrayList<>();
                    String sql2 = "SELECT * FROM Servico WHERE matricula = ? AND tipoServico = 4";
                    try (PreparedStatement stmt = conn.prepareStatement(sql2)) {
                        stmt.setString(1, matricula);
                        try (ResultSet resultSet = stmt.executeQuery()) {
                            while (resultSet.next()) {
                                String idS = resultSet.getString("idS");
                                String nome = resultSet.getString("nome");
                                String idCliente = resultSet.getString("idCliente");
                                String idMecanico = resultSet.getString("idMecanico");
                                String idPosto = resultSet.getString("idPosto");
                                ServicoEletrico s = new ServicoEletrico(idS, nome, matricula, idCliente, idMecanico, idPosto, null, null, Duration.ofHours(1));
                                servicoEletricos.add(s);
                            }
                        }
                    }
                    List<ServicoGasoleo> servicosGasoleo = new ArrayList<>();
                    String sql3 = "SELECT * FROM Servico WHERE matricula = ? AND tipoServico = 3";
                    try (PreparedStatement stmt = conn.prepareStatement(sql3)) {
                        stmt.setString(1, matricula);
                        try (ResultSet resultSet = stmt.executeQuery()) {
                            while (resultSet.next()) {
                                String idS = resultSet.getString("idS");
                                String nome = resultSet.getString("nome");
                                String idCliente = resultSet.getString("idCliente");
                                String idMecanico = resultSet.getString("idMecanico");
                                String idPosto = resultSet.getString("idPosto");
                                ServicoGasoleo s = new ServicoGasoleo(idS, nome, matricula, idCliente, idMecanico, idPosto, null, null, Duration.ofHours(1));
                                servicosGasoleo.add(s);
                            }
                        }
                    }
                    List<ServicoGasolina> servicosGasolina = new ArrayList<>();
                    String sql4 = "SELECT * FROM Servico WHERE matricula = ? AND tipoServico = 2";
                    try (PreparedStatement stmt = conn.prepareStatement(sql4)) {
                        stmt.setString(1, matricula);
                        try (ResultSet resultSet = stmt.executeQuery()) {
                            while (resultSet.next()) {
                                String idS = resultSet.getString("idS");
                                String nome = resultSet.getString("nome");
                                String idCliente = resultSet.getString("idCliente");
                                String idMecanico = resultSet.getString("idMecanico");
                                String idPosto = resultSet.getString("idPosto");
                                ServicoGasolina s = new ServicoGasolina(idS, nome, matricula, idCliente, idMecanico, idPosto, null, null, Duration.ofHours(1));
                                servicosGasolina.add(s);
                            }
                        }
                    }
                    veiculo = new VeiculoHibrido(matricula, servicosUniversais, servicosGasoleo, servicosGasolina, servicoEletricos);
                }

                res.add(veiculo); // Adiciona o veiculo ao resultado.
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return res;
    }

    public Set<Map.Entry<String, Veiculo>> entrySet() {
        List<ServicoUniversal> servicosUniversais = new ArrayList<>();
        Set<Map.Entry<String, Veiculo>> entrySet = new HashSet<>();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT Matricula, ClienteNIF FROM Veiculo")) {
            while (rs.next()) {
                String matricula = rs.getString("Matricula");

                String sql1 = "SELECT * FROM Servico WHERE matricula = ? AND tipoServico = 1";
                try (PreparedStatement stmt = conn.prepareStatement(sql1)) {
                    stmt.setString(1, matricula);
                    try (ResultSet resultSet = stmt.executeQuery()) {
                        while (resultSet.next()) {
                            String idS = resultSet.getString("idS");
                            String nome = resultSet.getString("nome");
                            String idCliente = resultSet.getString("idCliente");
                            String idMecanico = resultSet.getString("idMecanico");
                            String idPosto = resultSet.getString("idPosto");

                            // Obter Timestamp para data_comeco e data_terminio
                            Timestamp timestampComeco = resultSet.getTimestamp("data_comeco");
                            Timestamp timestampTerminio = resultSet.getTimestamp("data_terminio");

                            // Converter Timestamp para LocalDateTime
                            LocalDateTime data_comeco = timestampComeco.toLocalDateTime();
                            LocalDateTime data_terminio = timestampTerminio.toLocalDateTime();

                            ServicoUniversal s = new ServicoUniversal(idS, nome, matricula, idCliente, idMecanico, idPosto, data_comeco, data_terminio, Duration.ofMinutes(30));

                            servicosUniversais.add(s);
                        }
                    }
                }
                Veiculo veiculo = new Veiculo(matricula, servicosUniversais);

                // Criar uma entrada (chave-valor) e adicioná-la ao conjunto de entrada
                Map.Entry<String, Veiculo> entry = new AbstractMap.SimpleEntry<>(matricula, veiculo);
                entrySet.add(entry);
            }
        } catch (SQLException e) {
            // Lidar com exceções SQL, se necessário
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return entrySet;

    }


}
