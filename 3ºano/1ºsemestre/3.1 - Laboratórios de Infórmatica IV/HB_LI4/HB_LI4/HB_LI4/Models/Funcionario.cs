using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.AspNetCore.Identity;

namespace HB_LI4.Models
{
    public class Funcionario 
    {
        [Key]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public new string Id { get; set; }
        [Display(Name = "Nome")]
        public string? Nome { get; set; }
        [Display(Name = "Palavra Passe")]
        public string? PalavraPasse { get; set; }
        [Display(Name = "Email")]
        public string? Email { get; set; }
        [Display(Name = "Número de telemóvel")]
        public int telemovel { get; set; }
        
        public virtual ICollection<Leilao> Leiloes { get; set; }
    }
}