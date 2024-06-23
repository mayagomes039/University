using Microsoft.EntityFrameworkCore;
using HB_LI4.Models;
using Microsoft.AspNetCore.Identity.EntityFrameworkCore;

namespace HB_LI4.Data
{
    public class HB_LI4DbContext : DbContext
    {
        public HB_LI4DbContext(DbContextOptions<HB_LI4DbContext> options)
            : base(options)
        {
        }

        public DbSet<Cliente> Clientes { get; set; } = default!;
        public DbSet<Funcionario> Funcionarios { get; set; } = default!;
        public DbSet<Categoria> Categorias { get; set; } = default!;
        public DbSet<Leilao> Leiloes { get; set; } = default!;
        public DbSet<Lance> Lances { get; set; } = default!;
        public DbSet<Mensagem> Mensagens { get; set; } = default!;

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            modelBuilder.Entity<Lance>(entity =>
            {
                entity.HasIndex(e => e.ClienteID)
                    .HasName("fk1_lance_idx");

                entity.HasIndex(e => e.LeilaoID)
                    .HasName("fk2_lance_idx");

                entity.HasOne(d => d.ClienteIDNavigation)
                    .WithMany(p => p.Lances)
                    .HasForeignKey(d => d.ClienteID)
                    .OnDelete(DeleteBehavior.ClientSetNull)
                    .HasConstraintName("fk1_lance")
                    .IsRequired();

                entity.HasOne(d => d.LeilaoIDNavigation)
                    .WithMany(p => p.Lances)
                    .HasForeignKey(d => d.LeilaoID)
                    .OnDelete(DeleteBehavior.ClientSetNull)
                    .HasConstraintName("fk2_lance");
            });


            modelBuilder.Entity<Leilao>(entity =>
            {
                entity.HasIndex(e => e.ClienteID)
                    .HasName("fk1_leilao_idx");

                entity.HasIndex(e => e.FuncionarioID)
                    .HasName("fk2_leilao_idx");

                entity.HasOne(d => d.ClienteIDNavigation)
                    .WithMany(p => p.Leiloes)
                    .HasForeignKey(d => d.ClienteID)
                    .OnDelete(DeleteBehavior.ClientSetNull)
                    .HasConstraintName("fk1_leilao");

                entity.HasOne(d => d.FuncionarioIDNavigation)
                    .WithMany()
                    .HasForeignKey(d => d.FuncionarioID)
                    .OnDelete(DeleteBehavior.ClientSetNull)
                    .HasConstraintName("fk2_leilao")
                    .IsRequired();

                
                entity.Property(e => e.FuncionarioID).HasColumnName("FuncionarioId");
            });
            
            // Configuração para a entidade Mensagem
            modelBuilder.Entity<Mensagem>()
                .HasKey(m => m.ID);

            // Relacionamento entre Cliente e Mensagem
            modelBuilder.Entity<Mensagem>()
                .HasOne(m => m.Cliente)
                .WithMany(c => c.Mensagens)
                .HasForeignKey(m => m.ClienteID);
            
            // Relacionamento entre LEILAO e Mensagem
            modelBuilder.Entity<Mensagem>()
                .HasOne(m => m.Leilao)
                .WithMany(c => c.Mensagens)
                .HasForeignKey(m => m.LeilaoID);
        }
    }
}
