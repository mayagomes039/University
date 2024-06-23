package Data;

import Model.Cliente;
import Model.Posto;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ClienteDAO implements Map<String, Cliente> {

    private static ClienteDAO singleton = null;

    public ClienteDAO() {}

    public static ClienteDAO getInstance() {
        if (ClienteDAO.singleton == null) {
            ClienteDAO.singleton = new ClienteDAO();
        }
        return ClienteDAO.singleton;
    }


    public int size() {
        int i = 0;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT count(*) FROM Cliente")) {
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
                     stm.executeQuery("SELECT NIF FROM Cliente WHERE NIF='" + key.toString() + "'")) {
            r = rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return r;
    }

    public boolean containsValue(Object value) {
        Cliente c = (Cliente) value;
        return this.containsKey(c.getId());
    }

    @Override
    public Cliente get(Object key) {
        Cliente p = null;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT * FROM Cliente WHERE NIF='" + key + "'")) {
            if (rs.next()) {
                String idc = rs.getString("NIF");
                String nome = rs.getString("Nome");
                String senha = rs.getString("PalavraPasse");
                String email = rs.getString("Email");
                int tel = rs.getInt("Telefone");
                String morada = rs.getString("Morada");
                String estacao = rs.getString("EstacaoFreq");

                String sql1 = "SELECT idS FROM Servico WHERE idCliente = ? AND data_terminio IS NOT NULL";
                List<String> historico_servicos = new ArrayList<>();
                try (PreparedStatement stmt = conn.prepareStatement(sql1)) {
                    stmt.setString(1, idc);
                    try (ResultSet resultSet = stmt.executeQuery()) {
                        while (resultSet.next()) {
                            String idS = resultSet.getString("idS");
                            historico_servicos.add(idS);
                        }
                    }
                }

                String sql2 = "SELECT Matricula FROM Veiculo WHERE ClienteNIF = ?";
                List<String> veiculos = new ArrayList<>();
                try (PreparedStatement stmt = conn.prepareStatement(sql2)) {
                    stmt.setString(1, idc);
                    try (ResultSet resultSet = stmt.executeQuery()) {
                        while (resultSet.next()) {
                            String matricula = resultSet.getString("Matricula");
                            veiculos.add(matricula);
                        }
                    }
                }
                p = new Cliente( nome, key.toString(), senha, email, tel, morada, estacao, historico_servicos, veiculos);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return p;
    }


    public Cliente put(String key, Cliente cliente) {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {
            // Verificar se o cliente com a chave (ID) já existe
            ResultSet existCliente = stm.executeQuery("SELECT * FROM Cliente WHERE NIF='" + key + "'");
            if (existCliente.next()) { // Se o cliente já existe
                // Atualize o registro existente
                stm.executeUpdate("UPDATE Cliente SET Nome='" + cliente.getNome() +
                        "', PalavraPasse='" + cliente.getPalavraPasse() +
                        "', Email='" + cliente.getEmail() +
                        "', Telefone='" + cliente.getTelefone() +
                        "', Morada='" + cliente.getMorada() +
                        "', EstacaoFreq='" + cliente.getEstacaoFrequentada() +
                        "', Veiculos='" + cliente.getVeiculos_cliente()+
                        "' WHERE IdF='" + key + "'");
            } else { // Se o cliente não existe, add um novo registro
                stm.executeUpdate("INSERT INTO Cliente (IdF, Nome, PalavraPasse, Email, Telefone, Morada, EstacaoFreq, caracterizacaoVeiculos) " +
                        "VALUES ('" + key + "', '" + cliente.getNome() +
                        "', '" +cliente.getPalavraPasse() +
                        "', '" +  cliente.getEmail() +
                        "', '" + cliente.getTelefone() +
                        "', '"  + cliente.getMorada() +
                        "', '" + cliente.getEstacaoFrequentada() +
                        "', '" + cliente.getVeiculos_cliente() +
                        "')");
            }

            // Após a inserção ou atualização, recupere o cliente para retorná-lo
            ResultSet updatedCliente = stm.executeQuery("SELECT * FROM Cliente WHERE NIF='" + key + "'");
            if (updatedCliente.next()) {
                String nome = updatedCliente.getString("Nome");
                String palavraPasse = updatedCliente.getString("PalavraPasse");
                String email = updatedCliente.getString("Email");
                int tel = updatedCliente.getInt("Telefone");
                String morada = updatedCliente.getString("Morada");
                String estacao = updatedCliente.getString("EstacaoFreq");

                String sql1 = "SELECT idS FROM Servico WHERE idCliente = ? AND data_terminio IS NOT NULL";
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
                String sql2 = "SELECT Matricula FROM Veiculo WHERE ClienteNIF = ?";
                List<String> veiculos = new ArrayList<>();
                try (PreparedStatement stmt = conn.prepareStatement(sql2)) {
                    stmt.setString(1, key);
                    try (ResultSet resultSet = stmt.executeQuery()) {
                        while (resultSet.next()) {
                            String matricula = resultSet.getString("Matricula");
                            veiculos.add(matricula);
                        }
                    }
                }

                return new Cliente( nome, key, palavraPasse, email, tel, morada, estacao, historico_servicos, veiculos);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return null;
    }

    @Override
    public Cliente remove(Object key) {
        throw new NullPointerException("public Cliente remove(Object key) not implemented!");
    }

    @Override
    public void putAll(Map<? extends String, ? extends Cliente> m) {
        throw new NullPointerException("public putAll(Map<? extends String, ? extends Cliente> m) not implemented!");
    }

    @Override
    public void clear() {
        throw new NullPointerException("public void clear() not implemented!");
    }



    public Collection<Cliente> values() {
        Collection<Cliente> res = new HashSet<>();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT * FROM Cliente")) {
            while (rs.next()) {
                String idc = rs.getString("NIF");
                String nome = rs.getString("Nome");
                String senha = rs.getString("PalavraPasse");
                String email = rs.getString("Email");
                int tel = rs.getInt("Telefone");
                String morada = rs.getString("Morada");
                String estacao = rs.getString("EstacaoFreq");

                String sql1 = "SELECT idS FROM Servico WHERE idCliente = ? AND data_terminio IS NOT NULL";
                List<String> historico_servicos = new ArrayList<>();
                try (PreparedStatement stmt = conn.prepareStatement(sql1)) {
                    stmt.setString(1, idc);
                    try (ResultSet resultSet = stmt.executeQuery()) {
                        while (resultSet.next()) {
                            String idS = resultSet.getString("idS");
                            historico_servicos.add(idS);
                        }
                    }
                }

                String sql2 = "SELECT Matricula FROM Veiculo WHERE ClienteNIF = ?";
                List<String> veiculos = new ArrayList<>();
                try (PreparedStatement stmt = conn.prepareStatement(sql2)) {
                    stmt.setString(1, idc);
                    try (ResultSet resultSet = stmt.executeQuery()) {
                        while (resultSet.next()) {
                            String matricula = resultSet.getString("Matricula");
                            veiculos.add(matricula);
                        }
                    }
                }

                Cliente c = new Cliente(nome, idc, senha, email, tel, morada,estacao, historico_servicos, veiculos);

                c.setVeiculos_cliente(veiculos);

                res.add(c); // Adiciona o cliente ao resultado.
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return res;
    }

    @Override
    public Set<Entry<String, Cliente>> entrySet() {
        //criar um set de entrys
        Set<Entry<String, Cliente>> res = new HashSet<>();
        //criar um set de keys
        Set<String> keys = new HashSet<>(this.keySet());
        //para cada key
        for (String key : keys) {
            //criar uma entry com a key e o value
            Entry<String, Cliente> entry = new AbstractMap.SimpleEntry<>(key, this.get(key));
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
             ResultSet rs = stm.executeQuery("SELECT NIF FROM Cliente")) {
            while (rs.next()) {
                String idp = rs.getString("NIF");
                res.add(idp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return res;
    }

}
