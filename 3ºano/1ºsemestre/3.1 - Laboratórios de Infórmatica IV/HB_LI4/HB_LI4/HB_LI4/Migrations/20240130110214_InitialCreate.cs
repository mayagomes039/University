using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace HB_LI4.Migrations
{
    public partial class InitialCreate : Migration
    {
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateTable(
                name: "Categorias",
                columns: table => new
                {
                    ID = table.Column<int>(type: "INTEGER", nullable: false)
                        .Annotation("Sqlite:Autoincrement", true),
                    Nome = table.Column<string>(type: "TEXT", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_Categorias", x => x.ID);
                });

            migrationBuilder.CreateTable(
                name: "Clientes",
                columns: table => new
                {
                    Id = table.Column<string>(type: "TEXT", nullable: false),
                    Email = table.Column<string>(type: "TEXT", nullable: true),
                    Name = table.Column<string>(type: "TEXT", nullable: true),
                    PalavraPasse = table.Column<string>(type: "TEXT", nullable: true),
                    Telemovel = table.Column<int>(type: "INTEGER", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_Clientes", x => x.Id);
                });

            migrationBuilder.CreateTable(
                name: "Funcionarios",
                columns: table => new
                {
                    Id = table.Column<string>(type: "TEXT", nullable: false),
                    Nome = table.Column<string>(type: "TEXT", nullable: true),
                    PalavraPasse = table.Column<string>(type: "TEXT", nullable: true),
                    Email = table.Column<string>(type: "TEXT", nullable: true),
                    telemovel = table.Column<int>(type: "INTEGER", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_Funcionarios", x => x.Id);
                });

            migrationBuilder.CreateTable(
                name: "Leiloes",
                columns: table => new
                {
                    ID = table.Column<int>(type: "INTEGER", nullable: false)
                        .Annotation("Sqlite:Autoincrement", true),
                    PrecoInicial = table.Column<decimal>(type: "decimal(18, 2)", nullable: false),
                    PrecoFinal = table.Column<decimal>(type: "decimal(18, 2)", nullable: false),
                    Nome = table.Column<string>(type: "TEXT", nullable: true),
                    DataInicio = table.Column<DateTime>(type: "TEXT", nullable: false),
                    DataFim = table.Column<DateTime>(type: "TEXT", nullable: false),
                    CategoriaID = table.Column<int>(type: "INTEGER", nullable: true),
                    Imagem = table.Column<byte[]>(type: "BLOB", nullable: true),
                    ClienteID = table.Column<string>(type: "TEXT", nullable: true),
                    FuncionarioId = table.Column<string>(type: "TEXT", nullable: false),
                    leilaoPago = table.Column<bool>(type: "INTEGER", nullable: false),
                    FuncionarioId1 = table.Column<string>(type: "TEXT", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_Leiloes", x => x.ID);
                    table.ForeignKey(
                        name: "FK_Leiloes_Categorias_CategoriaID",
                        column: x => x.CategoriaID,
                        principalTable: "Categorias",
                        principalColumn: "ID");
                    table.ForeignKey(
                        name: "FK_Leiloes_Funcionarios_FuncionarioId1",
                        column: x => x.FuncionarioId1,
                        principalTable: "Funcionarios",
                        principalColumn: "Id");
                    table.ForeignKey(
                        name: "fk1_leilao",
                        column: x => x.ClienteID,
                        principalTable: "Clientes",
                        principalColumn: "Id");
                    table.ForeignKey(
                        name: "fk2_leilao",
                        column: x => x.FuncionarioId,
                        principalTable: "Funcionarios",
                        principalColumn: "Id");
                });

            migrationBuilder.CreateTable(
                name: "Lances",
                columns: table => new
                {
                    ID = table.Column<int>(type: "INTEGER", nullable: false)
                        .Annotation("Sqlite:Autoincrement", true),
                    Valor = table.Column<decimal>(type: "decimal(18, 2)", nullable: false),
                    FormaPagamento = table.Column<string>(type: "TEXT", nullable: false),
                    LeilaoID = table.Column<int>(type: "INTEGER", nullable: false),
                    ClienteID = table.Column<string>(type: "TEXT", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_Lances", x => x.ID);
                    table.ForeignKey(
                        name: "fk1_lance",
                        column: x => x.ClienteID,
                        principalTable: "Clientes",
                        principalColumn: "Id");
                    table.ForeignKey(
                        name: "fk2_lance",
                        column: x => x.LeilaoID,
                        principalTable: "Leiloes",
                        principalColumn: "ID");
                });

            migrationBuilder.CreateTable(
                name: "Mensagens",
                columns: table => new
                {
                    ID = table.Column<int>(type: "INTEGER", nullable: false)
                        .Annotation("Sqlite:Autoincrement", true),
                    Conteudo = table.Column<string>(type: "TEXT", nullable: false),
                    ClienteID = table.Column<string>(type: "TEXT", nullable: true),
                    LeilaoID = table.Column<int>(type: "INTEGER", nullable: false),
                    mensEnviada = table.Column<bool>(type: "INTEGER", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_Mensagens", x => x.ID);
                    table.ForeignKey(
                        name: "FK_Mensagens_Clientes_ClienteID",
                        column: x => x.ClienteID,
                        principalTable: "Clientes",
                        principalColumn: "Id");
                    table.ForeignKey(
                        name: "FK_Mensagens_Leiloes_LeilaoID",
                        column: x => x.LeilaoID,
                        principalTable: "Leiloes",
                        principalColumn: "ID",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateIndex(
                name: "fk1_lance_idx",
                table: "Lances",
                column: "ClienteID");

            migrationBuilder.CreateIndex(
                name: "fk2_lance_idx",
                table: "Lances",
                column: "LeilaoID");

            migrationBuilder.CreateIndex(
                name: "fk1_leilao_idx",
                table: "Leiloes",
                column: "ClienteID");

            migrationBuilder.CreateIndex(
                name: "fk2_leilao_idx",
                table: "Leiloes",
                column: "FuncionarioId");

            migrationBuilder.CreateIndex(
                name: "IX_Leiloes_CategoriaID",
                table: "Leiloes",
                column: "CategoriaID");

            migrationBuilder.CreateIndex(
                name: "IX_Leiloes_FuncionarioId1",
                table: "Leiloes",
                column: "FuncionarioId1");

            migrationBuilder.CreateIndex(
                name: "IX_Mensagens_ClienteID",
                table: "Mensagens",
                column: "ClienteID");

            migrationBuilder.CreateIndex(
                name: "IX_Mensagens_LeilaoID",
                table: "Mensagens",
                column: "LeilaoID");
        }

        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "Lances");

            migrationBuilder.DropTable(
                name: "Mensagens");

            migrationBuilder.DropTable(
                name: "Leiloes");

            migrationBuilder.DropTable(
                name: "Categorias");

            migrationBuilder.DropTable(
                name: "Funcionarios");

            migrationBuilder.DropTable(
                name: "Clientes");
        }
    }
}
