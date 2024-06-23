using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using HB_LI4.Models;
using Microsoft.AspNetCore.Mvc.Rendering;

namespace HB_LI4.Models
{
    public class Categoria
    {
        [Key]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int ID { get; set; }
        
        [Display(Name = "Nome da Categoria")]
        public string? Nome { get; set; }
        
        
    }
}
