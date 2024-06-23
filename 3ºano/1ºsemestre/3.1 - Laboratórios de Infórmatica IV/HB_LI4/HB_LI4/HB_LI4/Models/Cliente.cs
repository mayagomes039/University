using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using HB_LI4.Data;
using Microsoft.AspNetCore.Identity;
using Microsoft.Data.SqlClient;
using Microsoft.EntityFrameworkCore;

namespace HB_LI4.Models

{
    public class Cliente
    {
        [Key]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public new string Id { get; set; }

        public string? Email { get; set; }
        [Display(Name = "Nome")] public string? Name { get; set; }
        [Display(Name = "Palavra Passe")] public string? PalavraPasse { get; set; }

        [Display(Name = "Número de telemóvel")]
        public int Telemovel { get; set; }

        public virtual ICollection<Lance> Lances { get; set; }
        public virtual ICollection<Leilao> Leiloes { get; set; }
        public virtual ICollection<Mensagem> Mensagens { get; set; } = new List<Mensagem>();
        
    }
}