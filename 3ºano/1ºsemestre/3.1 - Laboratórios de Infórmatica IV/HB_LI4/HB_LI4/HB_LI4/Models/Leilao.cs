using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace HB_LI4.Models
{
    public class Leilao
    {
        [Key]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int ID { get; set; }
        [Display(Name = "Preço inicial")]
        [Column(TypeName = "decimal(18, 2)")]
        public decimal PrecoInicial { get; set; }
        [Display(Name = "Preço atual/final")]
        [Column(TypeName = "decimal(18, 2)")]
        public decimal PrecoFinal { get; set; }
        [Display(Name = "Nome")]
        public string? Nome { get; set; }
        [Display(Name = "Data inicial")]
        
        [DataType(DataType.Date)]
        public DateTime DataInicio { get; set; } 
        [Display(Name = "Data final")]
        [DataType(DataType.Date)]
        public DateTime DataFim { get; set; }
        
        public int? CategoriaID { get; set; }
        public Categoria Categoria { get; set; }
        
        [Display(Name = "Imagem")]
        public byte[] Imagem { get; set; }

        [Display(Name = "Cliente associado")]
        public string ClienteID { get; set; }
        [Display(Name = "Funcionario associado")]
        public string FuncionarioID { get; set; }
        
        [ForeignKey("ClienteID")]
        public virtual Cliente ClienteIDNavigation { get; set; }
        [ForeignKey("FuncionarioID")]
        public virtual Funcionario FuncionarioIDNavigation { get; set; }
        
        public virtual ICollection<Lance> Lances { get; set; }
        
        public bool leilaoPago { get; set; }
        
        
        public virtual ICollection<Mensagem> Mensagens { get; set; } = new List<Mensagem>();
    }
}