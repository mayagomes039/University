using Microsoft.AspNetCore.Mvc.Rendering;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using HB_LI4.Models;

namespace HB_LI4.Controllers
{
    public class LeilaoCategoriaViewModel
    {
        public List<Leilao>? Leiloes { get; set; }
        public SelectList? Categorias { get; set; }
        
        [Display(Name = "Preço Máximo")]
        public decimal? PrecoMaximo { get; set; }
        public string? Categoria { get; set; }
        public string? SearchString { get; set; }
        
    }
}
