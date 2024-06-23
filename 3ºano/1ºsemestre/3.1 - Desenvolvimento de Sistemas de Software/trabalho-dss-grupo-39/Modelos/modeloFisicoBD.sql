
CREATE DATABASE gestOficina;
CREATE USER IF NOT EXISTS 'jfc'@'localhost' 
IDENTIFIED BY 'jfc';
GRANT ALL PRIVILEGES ON *.* TO  'jfc'@'localhost';
-- IDENTIFIED BY 'jfc';
FLUSH PRIVILEGES;

-- Criação de tabelas:

Use gestOficina ;

CREATE TABLE Cliente (
    NIF INT PRIMARY KEY AUTO_INCREMENT, 
    Nome VARCHAR(100) NOT NULL,
    PalavraPasse VARCHAR(100) NOT NULL,
    Email VARCHAR(255) UNIQUE NOT NULL,
    Telefone INT,
    Morada VARCHAR(255),
    EstacaoFreq VARCHAR(255) UNIQUE NOT NULL,
    Veiculos JSON
);
CREATE TABLE Funcionario (
    IdF INT PRIMARY KEY AUTO_INCREMENT,
    Nome VARCHAR(100) NOT NULL,
    PalavraPasse VARCHAR(255) NOT NULL,
    InicioTurno DATETIME,
    FimTurno DATETIME
);

CREATE TABLE Veiculo (
    Matricula VARCHAR(255) PRIMARY KEY, 
    ClienteNIF INT, 
	Tipo INT,
    FOREIGN KEY (ClienteNIF)
        REFERENCES Cliente(NIF)
);

CREATE TABLE Posto (
    idP VARCHAR(255)PRIMARY KEY,
    nome VARCHAR(255),
    mecanico INT,
    FOREIGN KEY (mecanico) REFERENCES Funcionario(idF)
);


CREATE TABLE Servico (
    idS VARCHAR(255) PRIMARY KEY,
    nome VARCHAR(255),
    matricula VARCHAR(255), 
    FOREIGN KEY (matricula) REFERENCES Veiculo(Matricula),
    idCliente INT,
    FOREIGN KEY (idCliente) REFERENCES Cliente(NIF),
    idMecanico INT,
    FOREIGN KEY (idMecanico) REFERENCES Funcionario(idF),
    idPosto VARCHAR(255),
    FOREIGN KEY (idPosto) REFERENCES Posto(idP),
    data_comeco DATETIME,
    data_terminio DATETIME,
    duracao_prevista TIME,
    tipoServico INT
);


CREATE TABLE Occupacao (
	idOccupacao INT PRIMARY KEY AUTO_INCREMENT,
	idPosto VARCHAR(255),
    data DATETIME,
    FOREIGN KEY (idPosto) REFERENCES Posto(idP)
);

CREATE TABLE Competencia (
	nome VARCHAR(255) PRIMARY KEY
);

CREATE TABLE Competencia_Necessaria (
    idPosto VARCHAR(255),
    FOREIGN KEY (idPosto) REFERENCES Posto(idP),
    competencia VARCHAR(255),
    FOREIGN KEY (competencia) REFERENCES Competencia(nome)
);

CREATE TABLE Competencia_Mecanico (
    idMecanico INT,
    FOREIGN KEY (idMecanico) REFERENCES Funcionario(IdF),
    competencia VARCHAR(255),
    FOREIGN KEY (competencia) REFERENCES Competencia(nome)
);

-- povoamento:

-- tipos veiculos: 1-gasolina; 2-gasoleo; 3-eletrico; 4-hibrido
-- tipos serviços: 1-universal; 2-gasolina; 3-gasoleo; 4-eletrico

Insert into Veiculo values ('1','3','1');
Insert into Veiculo values ('2','3','3');
Insert into Veiculo values ('3','3','4');

Insert into Cliente values ('3','João Silva', 'joao1234', 'joao@email.com', 963354123, ' Estrada São Jorge 34 Braga', 'Estação de Gualtar');

-- funcionarios
INSERT INTO Funcionario (IdF, Nome, PalavraPasse)
VALUES ('1','Pedro Costa','2');

INSERT INTO Funcionario (IdF, Nome, PalavraPasse)
VALUES ('2','João Gomes','2');

INSERT INTO Funcionario (IdF, Nome, PalavraPasse)
VALUES ('3','Pedro Henriques Alvelos','3');

INSERT INTO Funcionario (IdF, Nome, PalavraPasse)
VALUES ('4','Gonçalo Vieria','4');

INSERT INTO Funcionario (IdF, Nome, PalavraPasse)
VALUES ('5','Fabio João Morais','5');

-- serviço

INSERT INTO Servico (idS, nome, matricula, idCliente, idMecanico, idPosto, tipoServico, data_comeco,
    duracao_prevista)
VALUES ('4', 'Substituição dos pneus','1','3','1','1','1', '2024-01-06 23:59:30', '00:30:00');

INSERT INTO Servico (idS, nome, matricula, idCliente, idMecanico, idPosto, tipoServico)
VALUES ('2', 'Mudança de óleo do motor','1','3','2','2','2');

INSERT INTO Servico (idS, nome, matricula, idCliente, idMecanico, idPosto, tipoServico)
VALUES ('3', 'Avaliação do desempenho da bateria','2','3','4','4','4');

-- posto  
INSERT INTO Posto (nome, mecanico)
VALUES ('Universal','1');
INSERT INTO Posto (nome, mecanico)
VALUES ('Combustao_Gasolina','2');
INSERT INTO Posto (nome, mecanico)
VALUES ('Combustao_Gasoleo','3');
INSERT INTO Posto (nome, mecanico)
VALUES ('Eletrico','4');

-- occupação 

INSERT INTO Occupacao (idPosto, data)
VALUES ('1','2024-01-22 00:00:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('1','2024-01-22 09:00:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('1','2024-01-22 09:30:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('1','2024-01-22 10:00:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('1','2024-01-22 11:00:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('1','2024-01-22 11:30:00');

INSERT INTO Occupacao (idPosto, data)
VALUES ('2','2024-01-23 08:00:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('2','2024-01-23 08:30:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('2','2024-01-23 09:00:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('2','2024-01-23 10:00:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('2','2024-01-23 11:30:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('2','2024-01-23 12:30:00');


INSERT INTO Occupacao (idPosto, data)
VALUES ('1','2024-01-17 00:00:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('1','2024-01-17 09:00:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('1','2024-01-17 09:30:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('1','2024-01-17 10:00:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('1','2024-01-17 11:00:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('1','2024-01-17 11:30:00');

INSERT INTO Occupacao (idPosto, data)
VALUES ('2','2024-01-17 08:00:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('2','2024-01-17 08:30:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('2','2024-01-17 09:00:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('2','2024-01-17 10:00:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('2','2024-01-17 11:30:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('2','2024-01-17 12:30:00');

INSERT INTO Occupacao (idPosto, data)
VALUES ('1','2024-01-24 00:00:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('1','2024-01-24 09:00:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('1','2024-01-24 09:30:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('1','2024-01-24 10:00:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('1','2024-01-24 11:00:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('1','2024-01-24 11:30:00');

INSERT INTO Occupacao (idPosto, data)
VALUES ('2','2024-01-24 08:00:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('2','2024-01-24 08:30:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('2','2024-01-24 09:00:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('2','2024-01-24 10:00:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('2','2024-01-24 11:30:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('2','2024-01-24 12:30:00');

INSERT INTO Occupacao (idPosto, data)
VALUES ('3','2024-01-21 08:00:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('3','2024-01-21 09:00:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('3','2024-01-21 09:30:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('3','2024-01-21 10:00:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('3','2024-01-21 11:00:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('3','2024-01-21 11:30:00');

INSERT INTO Occupacao (idPosto, data)
VALUES ('4','2024-01-21 08:00:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('4','2024-01-21 09:00:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('4','2024-01-21 09:30:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('4','2024-01-21 10:00:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('4','2024-01-21 11:00:00');
INSERT INTO Occupacao (idPosto, data)
VALUES ('4','2024-01-21 11:30:00');


-- competencia 
INSERT INTO Competencia (nome)
VALUES ('Substituir os pneus');
INSERT INTO Competencia (nome)
VALUES ('Calibrar as rodas');
INSERT INTO Competencia (nome)
VALUES ('Alinhar a direção');
INSERT INTO Competencia (nome)
VALUES ('Substituir os injetores');
INSERT INTO Competencia (nome)
VALUES ('Substituir os calços dos travões');
INSERT INTO Competencia (nome)
VALUES ('Mudar o óleo dos travões');
INSERT INTO Competencia (nome)
VALUES ('Limpar o interior e o exterior');
INSERT INTO Competencia (nome)
VALUES ('Substituir o filtro de ar da cabina');
INSERT INTO Competencia (nome)
VALUES ('Mudança de óleo do motor');
INSERT INTO Competencia (nome)
VALUES ('Substituição dos filtros de óleo, combustível e ar do motor');
INSERT INTO Competencia (nome)
VALUES ('Substituição do conversor catalítico');
INSERT INTO Competencia (nome)
VALUES ('Substituição da bateria de arranque');
INSERT INTO Competencia (nome)
VALUES ('Substituição das velas de incandescência');
INSERT INTO Competencia (nome)
VALUES ('Regeneração ou substituição do filtro de partículas');
INSERT INTO Competencia (nome)
VALUES ('Substituição da válvula do acelerador (borboleta)');
INSERT INTO Competencia (nome)
VALUES ('Substituição das velas de ignição');
INSERT INTO Competencia (nome)
VALUES ('Avaliação do desempenho da bateria');
INSERT INTO Competencia (nome)
VALUES ('Substituição da bateria');

-- Competencia_Necessaria (posto)
INSERT INTO Competencia_Necessaria (idPosto, competencia)
VALUES ('1','Substituir os pneus');
INSERT INTO Competencia_Necessaria (idPosto, competencia)
VALUES ('1','Calibrar as rodas');
INSERT INTO Competencia_Necessaria (idPosto, competencia)
VALUES ('1','Alinhar a direção');
INSERT INTO Competencia_Necessaria (idPosto, competencia)
VALUES ('1','Substituir os injetores');
INSERT INTO Competencia_Necessaria (idPosto, competencia)
VALUES ('1','Substituir os calços dos travões');
INSERT INTO Competencia_Necessaria (idPosto, competencia)
VALUES ('1','Mudar o óleo dos travões');
INSERT INTO Competencia_Necessaria (idPosto, competencia)
VALUES ('1','Limpar o interior e o exterior');
INSERT INTO Competencia_Necessaria (idPosto, competencia)
VALUES ('1','Substituir o filtro de ar da cabina');

-- posto 2
INSERT INTO Competencia_Necessaria (idPosto, competencia)
VALUES ('2','Mudança de óleo do motor');
INSERT INTO Competencia_Necessaria (idPosto, competencia)
VALUES ('2','Substituição dos filtros de óleo, combustível e ar do motor');
INSERT INTO Competencia_Necessaria (idPosto, competencia)
VALUES ('2','Substituição do conversor catalítico');
INSERT INTO Competencia_Necessaria (idPosto, competencia)
VALUES ('2','Substituição da bateria de arranque');
INSERT INTO Competencia_Necessaria (idPosto, competencia)
VALUES ('2','Substituição da válvula do acelerador (borboleta)');
INSERT INTO Competencia_Necessaria (idPosto, competencia)
VALUES ('2','Substituição das velas de ignição');

-- posto 3
INSERT INTO Competencia_Necessaria (idPosto, competencia)
VALUES ('3','Mudança de óleo do motor');
INSERT INTO Competencia_Necessaria (idPosto, competencia)
VALUES ('3','Substituição dos filtros de óleo, combustível e ar do motor');
INSERT INTO Competencia_Necessaria (idPosto, competencia)
VALUES ('3','Substituição do conversor catalítico');
INSERT INTO Competencia_Necessaria (idPosto, competencia)
VALUES ('3','Substituição da bateria de arranque');
INSERT INTO Competencia_Necessaria (idPosto, competencia)
VALUES ('3','Substituição das velas de incandescência');
INSERT INTO Competencia_Necessaria (idPosto, competencia)
VALUES ('3','Regeneração ou substituição do filtro de partículas');


-- posto 4
INSERT INTO Competencia_Necessaria (idPosto, competencia)
VALUES ('4','Avaliação do desempenho da bateria');
INSERT INTO Competencia_Necessaria (idPosto, competencia)
VALUES ('4','Substituição da bateria');



-- Competencia_Mecanico

INSERT INTO Competencia_Mecanico (idMecanico, competencia)
VALUES ('1','Substituir os pneus');
INSERT INTO Competencia_Mecanico (idMecanico, competencia)
VALUES ('1','Calibrar as rodas');
INSERT INTO Competencia_Mecanico (idMecanico, competencia)
VALUES ('1','Alinhar a direção');
INSERT INTO Competencia_Mecanico (idMecanico, competencia)
VALUES ('1','Substituir os injetores');
INSERT INTO Competencia_Mecanico (idMecanico, competencia)
VALUES ('1','Substituir os calços dos travões');
INSERT INTO Competencia_Mecanico (idMecanico, competencia)
VALUES ('1','Mudar o óleo dos travões');
INSERT INTO Competencia_Mecanico (idMecanico, competencia)
VALUES ('1','Limpar o interior e o exterior');
INSERT INTO Competencia_Mecanico (idMecanico, competencia)
VALUES ('1','Substituir o filtro de ar da cabina');


INSERT INTO Competencia_Mecanico (idMecanico, competencia)
VALUES ('2','Mudança de óleo do motor');
INSERT INTO Competencia_Mecanico (idMecanico, competencia)
VALUES ('2','Substituição dos filtros de óleo, combustível e ar do motor');
INSERT INTO Competencia_Mecanico (idMecanico, competencia)
VALUES ('2','Substituição do conversor catalítico');
INSERT INTO Competencia_Mecanico (idMecanico, competencia)
VALUES ('2','Substituição da bateria de arranque');
INSERT INTO Competencia_Mecanico (idMecanico, competencia)
VALUES ('2','Substituição da válvula do acelerador (borboleta)');
INSERT INTO Competencia_Mecanico (idMecanico, competencia)
VALUES ('2','Substituição das velas de ignição');

INSERT INTO Competencia_Mecanico (idMecanico, competencia)
VALUES ('3','Mudança de óleo do motor');
INSERT INTO Competencia_Mecanico (idMecanico, competencia)
VALUES ('3','Substituição dos filtros de óleo, combustível e ar do motor');
INSERT INTO Competencia_Mecanico (idMecanico, competencia)
VALUES ('3','Substituição do conversor catalítico');
INSERT INTO Competencia_Mecanico (idMecanico, competencia)
VALUES ('3','Substituição da bateria de arranque');
INSERT INTO Competencia_Mecanico (idMecanico, competencia)
VALUES ('3','Substituição das velas de incandescência');
INSERT INTO Competencia_Mecanico (idMecanico, competencia)
VALUES ('3','Regeneração ou substituição do filtro de partículas');

INSERT INTO Competencia_Mecanico (idMecanico, competencia)
VALUES ('4','Avaliação do desempenho da bateria');
INSERT INTO Competencia_Mecanico (idMecanico, competencia)
VALUES ('4','Substituição da bateria');


