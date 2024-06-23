using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace HB_LI4.Models
{
    public class Mensagem
    {
        [Key]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int ID { get; set; }

        [Required]
        public string Conteudo { get; set; }

        // Relacionamento com o cliente
        public string ClienteID { get; set; }
        [ForeignKey("ClienteID")]
        public virtual Cliente Cliente { get; set; }
        
        public int LeilaoID { get; set; }
        [ForeignKey("ClienteID")]
        public virtual Leilao Leilao { get; set; }
        
        public bool mensEnviada { get; set; }
    }
}
