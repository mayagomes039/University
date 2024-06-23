using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace HB_LI4.Models
{
    public class Lance
    {
        [Key]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int ID { get; set; }
        [Display(Name = "Valor do lance")]
        [Column(TypeName = "decimal(18, 2)")]
        public decimal Valor { get; set; }
        [Display(Name = "Metodo de pagamento")]
        
        [Required(ErrorMessage = "Por favor, selecione uma forma de pagamento.")]
        public string? FormaPagamento { get; set; }
        
        [Display(Name = "Leilao associado")]
        public int LeilaoID { get; set; }
        [Display(Name = "Cliente associado")]
        public string ClienteID { get; set; }
        
        [ForeignKey("LeilaoID")]
        public virtual Leilao LeilaoIDNavigation { get; set; }
        [ForeignKey("ClienteID")]
        public virtual Cliente ClienteIDNavigation { get; set; }
    }
}