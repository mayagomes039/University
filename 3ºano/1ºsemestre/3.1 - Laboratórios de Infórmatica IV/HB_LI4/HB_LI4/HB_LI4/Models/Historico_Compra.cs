using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace HB_LI4.Models
{
    public class HistoricoCompra
    {
        public int IdCliente { get; set; }
        
        public int Leilao { get; set; }
       
    }
}