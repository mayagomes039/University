package Data;

import Model.Cliente;
import Model.Funcionario;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FuncionarioDAO implements Map<String, Funcionario> {

    private static FuncionarioDAO singleton = null;


    public FuncionarioDAO() {}

    public static FuncionarioDAO getInstance() {
        if (FuncionarioDAO.singleton == null) {
            FuncionarioDAO.singleton = new FuncionarioDAO();
        }
        return FuncionarioDAO.singleton;
    }


    public int size() {
        int i = 0;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT count(*) FROM Funcionario")) {
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
                     stm.executeQuery("SELECT IdF FROM Funcionario WHERE IdF='" + key.toString() + "'")) {
            r = rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return r;
    }

    public boolean containsValue(Object value) {
        Funcionario f = (Funcionario) value;
        return this.containsKey(f.getId());
    }

    @Override

    public Funcionario get(Object key) {
        LocalDateTime dataAtual = LocalDateTime.now();
        List<String> competencias = new ArrayList<>();
        List<String> historico_servicos = new ArrayList<>();
        Funcionario mecanico = null;
        HashMap<String,LocalDateTime> agendaTarefas = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT * FROM Funcionario WHERE IdF ='" + key + "'")) {
            if (rs.next()) {
                String nome = rs.getString("Nome");
                String palavrapasse = rs.getString("PalavraPasse");   // TODO agendaTarefas

                LocalDateTime inicioTurno = rs.getTimestamp("InicioTurno").toLocalDateTime();
                LocalDateTime fimTurno = rs.getTimestamp("FimTurno").toLocalDateTime();

                String sql = "SELECT competencia FROM Competencia_Mecanico WHERE idMecanico = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, key.toString());
                    try (ResultSet resultSet = stmt.executeQuery()) {
                        while (resultSet.next()) {
                            String competencia = resultSet.getString("competencia");
                            competencias.add(competencia);
                        }
                    }
                }

                String sql1 = "SELECT idS FROM Servico WHERE idMecanico = ? AND data_terminio IS NOT NULL";
                try (PreparedStatement stmt = conn.prepareStatement(sql1)) {
                    stmt.setString(1, key.toString());
                    try (ResultSet resultSet = stmt.executeQuery()) {
                        while (resultSet.next()) {
                            String idS = resultSet.getString("idS");
                            historico_servicos.add(idS);
                        }
                    }
                }

                String sql2 = "SELECT idS, data_comeco FROM Servico WHERE idMecanico = ? AND data_comeco >= ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql2)) {
                    stmt.setString(1, key.toString());
                    stmt.setTimestamp(2, Timestamp.valueOf(dataAtual));
                    try (ResultSet resultSet = stmt.executeQuery()) {
                        while (resultSet.next()) {
                            String idS = resultSet.getString("idS");
                            LocalDateTime dataComeco = resultSet.getTimestamp("data_comeco").toLocalDateTime();
                            agendaTarefas.put(idS, dataComeco);
                        }
                    }
                }

                mecanico = new Funcionario(nome, key.toString(), palavrapasse,competencias,inicioTurno,fimTurno, agendaTarefas, historico_servicos);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return mecanico;
    }

    public Funcionario put(String key, Funcionario funcionario) {
        LocalDateTime dataAtual = LocalDateTime.now();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {
            // Verificar se o funcionario com a chave (ID) já existe
            ResultSet existFunc = stm.executeQuery("SELECT * FROM Funcionario WHERE IdF='" + key + "'");
            if (existFunc.next()) { // Se o funcionario já existe
                // Atualize o registro existente
                stm.executeUpdate("UPDATE Funcionario SET Nome='" + funcionario.getNome() +
                        "', PalavraPasse='" + funcionario.getPalavraPasse() +
                        "', Competencias='" + funcionario.getCompetencias_mecanico() +
                        "', InicioTurno='" + funcionario.getInicioTurno() +
                        "', FimTurno='" + funcionario.getFimTurno() +
                        "', AgendaTarefas='" + funcionario.getAgendaTarefas() + //TODO
                        "' WHERE IdF='" + key + "'");
            } else { // Se o funcionario não existe, add um novo registro
                stm.executeUpdate("INSERT INTO Funcionario (IdF, Nome, PalavraPasse, Competencias, InicioTurno, FimTurno, AgendaTarefas) " +
                        "VALUES ('" + key + "', '" + funcionario.getNome()  +
                                "', '" + funcionario.getPalavraPasse() +
                                "', '" + funcionario.getCompetencias_mecanico() +
                                "', '" + funcionario.getInicioTurno() +
                                "', '" + funcionario.getFimTurno() +
                                "', '" + funcionario.getAgendaTarefas() + //TODO
                        "')");
            }

            // Após a inserção ou atualização, recupere o func para retorná-lo
            ResultSet updatedfunc = stm.executeQuery("SELECT * FROM Funcionario WHERE IdF='" + key + "'");
            if (updatedfunc.next()) {
                String nome = updatedfunc.getString("Nome");
                String palavraPasse = updatedfunc.getString("PalavraPasse");

                String inicio = updatedfunc.getString("InicioTurno");
                LocalDateTime inicioTurno = LocalDateTime.parse(inicio, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String fim = updatedfunc.getString("FimTurno");
                LocalDateTime fimTurno = LocalDateTime.parse(fim, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));


                String sql = "SELECT competencia FROM Competencia_Mecanico WHERE idMecanico = ?";
                List<String> competencias = new ArrayList<>();
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, key);
                    try (ResultSet resultSet = stmt.executeQuery()) {
                        while (resultSet.next()) {
                            String competencia = resultSet.getString("competencia");
                            competencias.add(competencia);
                        }
                    }
                }

                String sql1 = "SELECT idS FROM Servico WHERE idMecanico = ? AND data_terminio IS NOT NULL";
                List<String> historico_servicos = new ArrayList<>();
                try (PreparedStatement stmt = conn.prepareStatement(sql1)) {
                    stmt.setString(1, key);
                    try (ResultSet resultSet = stmt.executeQuery()) {
                        while (resultSet.next()) {
                            String idS = resultSet.getString("idS");
                            historico_servicos.add(idS);
                        }
                    }
                }

                String sql2 = "SELECT idS, data_comeco FROM Servico WHERE idMecanico = ? AND data_comeco >= ?";
                HashMap<String,LocalDateTime> agendaTarefas = new HashMap<>();
                try (PreparedStatement stmt = conn.prepareStatement(sql2)) {
                    stmt.setString(1, key);
                    stmt.setTimestamp(2, Timestamp.valueOf(dataAtual));
                    try (ResultSet resultSet = stmt.executeQuery()) {
                        while (resultSet.next()) {
                            String idS = resultSet.getString("idS");
                            LocalDateTime dataComeco = resultSet.getTimestamp("data_comeco").toLocalDateTime();
                            agendaTarefas.put(idS, dataComeco);
                        }
                    }
                }

                return new Funcionario( nome, key, palavraPasse, competencias, inicioTurno, fimTurno,agendaTarefas, historico_servicos);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return null;
    }

    @Override
    public Funcionario remove(Object key) {
        throw new NullPointerException("public Funcionario remove(Object key) not implemented!");
    }

    @Override
    public void putAll(Map<? extends String, ? extends Funcionario> m) {
        throw new NullPointerException("public void putAll(Map<? extends String, ? extends Funcionario> m) not implemented!");
    }

    @Override
    public void clear() {
        throw new NullPointerException("public void clear() not implemented!");
    }

    public Collection<Funcionario> values() {
        Collection<Funcionario> res = new HashSet<>();
        LocalDateTime dataAtual = LocalDateTime.now();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT * FROM Funcionario")) {
            while (rs.next()) {
                String idf = rs.getString("IdF");
                String nome = rs.getString("Nome");
                String senha = rs.getString("PalavraPasse");

                String sql = "SELECT competencia FROM Competencia_Mecanico WHERE idMecanico = ?";
                List<String> competencias = new ArrayList<>();
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, idf);
                    try (ResultSet resultSet = stmt.executeQuery()) {
                        while (resultSet.next()) {
                            String competencia = resultSet.getString("competencia");
                            competencias.add(competencia);
                        }
                    }
                }
                LocalDateTime inicioTurno = rs.getTimestamp("InicioTurno").toLocalDateTime();
                LocalDateTime fimTurno = rs.getTimestamp("FimTurno").toLocalDateTime();

                String sql1 = "SELECT idS FROM Servico WHERE idMecanico = ? AND data_terminio IS NOT NULL";
                List<String> historico_servicos = new ArrayList<>();
                try (PreparedStatement stmt = conn.prepareStatement(sql1)) {
                    stmt.setString(1, idf);
                    try (ResultSet resultSet = stmt.executeQuery()) {
                        while (resultSet.next()) {
                            String idS = resultSet.getString("idS");
                            historico_servicos.add(idS);
                        }
                    }
                }

                String sql2 = "SELECT idS, data_comeco FROM Servico WHERE idMecanico = ? AND data_comeco >= ?";
                HashMap<String,LocalDateTime> agendaTarefas = new HashMap<>();
                try (PreparedStatement stmt = conn.prepareStatement(sql2)) {
                    stmt.setString(1, idf);
                    stmt.setTimestamp(2, Timestamp.valueOf(dataAtual));
                    try (ResultSet resultSet = stmt.executeQuery()) {
                        while (resultSet.next()) {
                            String idS = resultSet.getString("idS");
                            LocalDateTime dataComeco = resultSet.getTimestamp("data_comeco").toLocalDateTime();
                            agendaTarefas.put(idS, dataComeco);
                        }
                    }
                }


                Funcionario f = new Funcionario(nome, idf, senha,competencias, inicioTurno, fimTurno, agendaTarefas, historico_servicos);

                res.add(f); // Adiciona o funcionário ao resultado.
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return res;
    }

    @Override
    public Set<Entry<String, Funcionario>> entrySet() {
        //criar um set de entrys
        Set<Entry<String, Funcionario>> res = new HashSet<>();
        //criar um set de keys
        Set<String> keys = new HashSet<>(this.keySet());
        //para cada key
        for (String key : keys) {
            //criar uma entry com a key e o value
            Entry<String, Funcionario> entry = new AbstractMap.SimpleEntry<>(key, this.get(key));
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
             ResultSet rs = stm.executeQuery("SELECT IdF FROM Funcionario")) {
            while (rs.next()) {
                String idp = rs.getString("IdF");
                res.add(idp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return res;
    }

    public static void atualizarDataComeco(String idFuncionario, LocalDateTime novaDataComeco) {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement("UPDATE Funcionario SET InicioTurno = ? WHERE IdF = ?")) {
            pstmt.setTimestamp(1, Timestamp.valueOf(novaDataComeco));
            pstmt.setString(2, idFuncionario);

            pstmt.executeUpdate();


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void atualizarDataTerminio(String idFuncionario, LocalDateTime novaDataTerminio) {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement("UPDATE Funcionario SET FimTurno = ? WHERE IdF = ?")) {
            pstmt.setTimestamp(1, Timestamp.valueOf(novaDataTerminio));
            pstmt.setString(2, idFuncionario);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
