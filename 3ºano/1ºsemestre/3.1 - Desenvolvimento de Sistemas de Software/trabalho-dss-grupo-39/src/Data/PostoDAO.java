package Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import Model.Posto;
import java.sql.*;
import java.util.*;

public class PostoDAO implements Map<String, Posto> {
    private static PostoDAO singleton = null;

    public PostoDAO() {}

    public static PostoDAO getInstance() {
        if (PostoDAO.singleton == null) {
            PostoDAO.singleton = new PostoDAO();
        }
        return PostoDAO.singleton;
    }


    public int size() {
        int i = 0;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT count(*) FROM Posto")) {
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
                     stm.executeQuery("SELECT idP FROM Posto WHERE idP='" + key.toString() + "'")) {
            r = rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return r;
    }

    public boolean containsValue(Object value) {
        Posto p = (Posto) value;
        return this.containsKey(p.getIdP());
    }

    @Override
    public Posto get(Object key) {
        Posto p = null;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT * FROM Posto WHERE idP ='" + key + "'")) {
            if (rs.next()) {
                String idp = rs.getString("idP");
                String nome = rs.getString("nome");
                String mecanico = rs.getString("mecanico");

                String sql = "SELECT data FROM Occupacao WHERE idPosto = ? AND data > NOW()";
                List<LocalDateTime> disponibilidades = new ArrayList<>();
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, idp);
                    try (ResultSet resultSet = stmt.executeQuery()) {
                        while (resultSet.next()) {
                            String data = resultSet.getString("data");
                            LocalDateTime horario = LocalDateTime.parse(data, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                            disponibilidades.add(horario);
                        }
                    }
                }

                String sql1 = "SELECT competencia FROM Competencia_Necessaria WHERE idPosto = ?";
                List<String> competencias = new ArrayList<>();
                try (PreparedStatement stmt = conn.prepareStatement(sql1)) {
                    stmt.setString(1, idp);
                    try (ResultSet resultSet = stmt.executeQuery()) {
                        while (resultSet.next()) {
                            String competencia = resultSet.getString("competencia");
                            competencias.add(competencia);
                        }
                    }
                }
                p = new Posto(idp,nome,mecanico,disponibilidades,competencias);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return p;
    }





    public Posto put(String key, Posto posto) {

        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {
            // Verificar se o posto com a chave (ID) já existe
            ResultSet existFunc = stm.executeQuery("SELECT * FROM Posto WHERE idP='" + key + "'");
            if (existFunc.next()) { // Se o posto já existe
                // Atualize o registro existente
                stm.executeUpdate("UPDATE Posto SET mecanico='" + posto.getMecanico() +
                        "', nome='" + posto.getNome() +
                        "' WHERE isP='" + key + "'");

            } else { // Se o posto não existe, add um novo registro
                stm.executeUpdate("INSERT INTO Posto  (nome, mecanico) " +
                        "VALUES ('" + key + "', '" + posto.getNome()  +
                        "', '" + posto.getMecanico() +
                        "')");
            }

            // Após a inserção ou atualização, recupere o func para retorná-lo
            ResultSet updatedPosto = stm.executeQuery("SELECT * FROM Posto WHERE idP='" + key + "'");
            List<LocalDateTime> disponibilidades = new ArrayList<>();
            if (updatedPosto.next()) {
                String nome = updatedPosto.getString("nome");
                String mecanico = updatedPosto.getString("mecanico");

                //ir buscar os valores dos horarios deste idP
                String sql = "SELECT data FROM Occupacao WHERE idPosto = ? AND data > NOW()";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, key);
                    try (ResultSet resultSet = stmt.executeQuery()) {
                        while (resultSet.next()) {
                            String data = resultSet.getString("data");
                            LocalDateTime horario = LocalDateTime.parse(data);
                            disponibilidades.add(horario);
                        }
                    }
                }
                String sql1 = "SELECT competencia FROM Competencia_Necessaria WHERE idPosto = ?";
                List<String> competencias = new ArrayList<>();
                try (PreparedStatement stmt = conn.prepareStatement(sql1)) {
                    stmt.setString(1, key);
                    try (ResultSet resultSet = stmt.executeQuery()) {
                        while (resultSet.next()) {
                            String competencia = resultSet.getString("competencia");
                            competencias.add(competencia);
                        }
                    }
                }

                return new Posto(key, nome, mecanico, disponibilidades, competencias);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return null;
    }

    @Override
    public Posto remove(Object key) {
        throw new NullPointerException("not implemented!");
    }

    @Override
    public void putAll(Map<? extends String, ? extends Posto> m) {
        throw new NullPointerException("not implemented!");
    }

    @Override
    public void clear() {
        throw new NullPointerException("not implemented!");
    }

    @Override
    public Set<String> keySet() {
        Set<String> res = new HashSet<>();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT idP FROM Posto")) {
            while (rs.next()) {
                String idp = rs.getString("idP");
                res.add(idp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return res;
    }

    public Collection<Posto> values() {
        Collection<Posto> res = new HashSet<>();

        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT * FROM Posto")) {
            while (rs.next()) {
                String idp = rs.getString("idP");
                String nome = rs.getString("nome");
                String mecanico = rs.getString("mecanico");

                String sql = "SELECT data FROM Occupacao WHERE idPosto = ? AND data > NOW()";
                List<LocalDateTime> disponibilidades = new ArrayList<>();
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, idp);
                    try (ResultSet resultSet = stmt.executeQuery()) {
                        while (resultSet.next()) {
                            String data = resultSet.getString("data");
                            LocalDateTime horario = LocalDateTime.parse(data, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                            disponibilidades.add(horario);
                        }
                    }
                }

                String sql1 = "SELECT competencia FROM Competencia_Necessaria WHERE idPosto = ?";
                List<String> competencias = new ArrayList<>();
                try (PreparedStatement stmt = conn.prepareStatement(sql1)) {
                    stmt.setString(1, idp);
                    try (ResultSet resultSet = stmt.executeQuery()) {
                        while (resultSet.next()) {
                            String competencia = resultSet.getString("competencia");
                            competencias.add(competencia);
                        }
                    }
                }

                Posto p = new Posto(idp,nome,mecanico,disponibilidades,competencias);

                res.add(p); // Adiciona o posto ao resultado.
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return res;
    }

    @Override
    public Set<Entry<String, Posto>> entrySet() {
        //criar um set de entrys
        Set<Entry<String, Posto>> res = new HashSet<>();
        //criar um set de keys
        Set<String> keys = new HashSet<>(this.keySet());
        //para cada key
        for (String key : keys) {
            //criar uma entry com a key e o value
            Entry<String, Posto> entry = new AbstractMap.SimpleEntry<>(key, this.get(key));
            //adicionar a entry ao set de entrys
            res.add(entry);
        }
        return res;
    }

    public static void removerHorarioDisponibilidades(Posto posto, LocalDateTime horarioSugerido) {
        // Remover o horário escolhido das disponibilidades
        List<LocalDateTime> disponibilidades = posto.getDisponibilidades();
        disponibilidades.remove(horarioSugerido);

        // Remover o horário da tabela Occupacao
        String idPosto = posto.getIdP();
        String sql = "DELETE FROM Occupacao WHERE idPosto = ? AND data = ?";
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, idPosto);
            stmt.setString(2, horarioSugerido.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao remover horário e ocupação: " + e.getMessage());
        }
    }
}
